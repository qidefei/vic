// Copyright 2017 VMware, Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package handlers

import (
	"bytes"
	"context"
	"fmt"
	"net/http"
	"net/url"
	"sort"
	"strings"

	"github.com/go-openapi/runtime/middleware"

	"github.com/vmware/vic/lib/apiservers/service/models"
	"github.com/vmware/vic/lib/apiservers/service/restapi/handlers/util"
	"github.com/vmware/vic/lib/apiservers/service/restapi/operations"
	"github.com/vmware/vic/lib/install/data"
	"github.com/vmware/vic/lib/install/management"
	"github.com/vmware/vic/lib/install/validate"
	"github.com/vmware/vic/pkg/trace"
	"github.com/vmware/vic/pkg/vsphere/datastore"
)

const (
	logFilePrefix = "vic-machine" // logFilePrefix is the prefix for file names of all vic-machine log files
	logFileSuffix = ".log"        // logFileSuffix is the suffix for file names of all vic-machine log files
)

// VCHLogGet is the handler for getting the log messages for a VCH
type VCHLogGet struct {
}

// VCHDatacenterLogGet is the handler for getting the log messages for a VCH within a Datacenter
type VCHDatacenterLogGet struct {
}

func (h *VCHLogGet) Handle(params operations.GetTargetTargetVchVchIDLogParams, principal interface{}) middleware.Responder {
	d, err := buildData(params.HTTPRequest.Context(),
		url.URL{Host: params.Target},
		principal.(Credentials).user,
		principal.(Credentials).pass,
		params.Thumbprint,
		nil,
		nil)
	if err != nil {
		return operations.NewGetTargetTargetVchVchIDLogDefault(util.StatusCode(err)).WithPayload(&models.Error{Message: err.Error()})
	}

	d.ID = params.VchID
	op := trace.NewOperation(params.HTTPRequest.Context(), "vch: %s", params.VchID)

	helper, err := getDatastoreHelper(op.Context, d)
	if err != nil {
		return operations.NewGetTargetTargetVchVchIDLogDefault(util.StatusCode(err)).WithPayload(&models.Error{Message: err.Error()})
	}

	logFilePaths, err := getAllLogFilePaths(op.Context, helper)
	if err != nil {
		return operations.NewGetTargetTargetVchVchIDLogDefault(util.StatusCode(err)).WithPayload(&models.Error{Message: err.Error()})
	}

	output, err := getContentFromLogFiles(op.Context, helper, logFilePaths)
	if err != nil {
		return operations.NewGetTargetTargetVchVchIDLogDefault(util.StatusCode(err)).WithPayload(&models.Error{Message: err.Error()})
	}

	return operations.NewGetTargetTargetVchVchIDLogOK().WithPayload(output)
}

func (h *VCHDatacenterLogGet) Handle(params operations.GetTargetTargetDatacenterDatacenterVchVchIDLogParams, principal interface{}) middleware.Responder {
	d, err := buildData(params.HTTPRequest.Context(),
		url.URL{Host: params.Target},
		principal.(Credentials).user,
		principal.(Credentials).pass,
		params.Thumbprint,
		&params.Datacenter,
		nil)
	if err != nil {
		return operations.NewGetTargetTargetDatacenterDatacenterVchVchIDLogDefault(util.StatusCode(err)).WithPayload(&models.Error{Message: err.Error()})
	}

	d.ID = params.VchID
	op := trace.NewOperation(params.HTTPRequest.Context(), "vch: %s", params.VchID)

	helper, err := getDatastoreHelper(op.Context, d)
	if err != nil {
		return operations.NewGetTargetTargetDatacenterDatacenterVchVchIDLogDefault(util.StatusCode(err)).WithPayload(&models.Error{Message: err.Error()})
	}

	logFilePaths, err := getAllLogFilePaths(op.Context, helper)
	if err != nil {
		return operations.NewGetTargetTargetDatacenterDatacenterVchVchIDLogDefault(util.StatusCode(err)).WithPayload(&models.Error{Message: err.Error()})
	}

	output, err := getContentFromLogFiles(op.Context, helper, logFilePaths)
	if err != nil {
		return operations.NewGetTargetTargetDatacenterDatacenterVchVchIDLogDefault(util.StatusCode(err)).WithPayload(&models.Error{Message: err.Error()})
	}

	return operations.NewGetTargetTargetDatacenterDatacenterVchVchIDLogOK().WithPayload(output)
}

// getDatastoreHelper validates the VCH and returns the datastore helper for the VCH. It errors when validation fails or when datastore is not ready
func getDatastoreHelper(ctx context.Context, d *data.Data) (*datastore.Helper, error) {
	// TODO (angiew): abstract some of the boilerplate into helpers in common.go
	validator, err := validateTarget(ctx, d)
	if err != nil {
		return nil, util.WrapError(http.StatusBadRequest, err)
	}

	executor := management.NewDispatcher(validator.Context, validator.Session, nil, false)
	vch, err := executor.NewVCHFromID(d.ID)
	if err != nil {
		return nil, util.NewError(http.StatusNotFound, fmt.Sprintf("Unable to find VCH %s: %s", d.ID, err))
	}

	if err := validate.SetDataFromVM(validator.Context, validator.Session.Finder, vch, d); err != nil {
		return nil, util.NewError(http.StatusInternalServerError, fmt.Sprintf("Failed to load VCH data: %s", err))
	}

	// Get VCH configuration
	vchConfig, err := executor.GetNoSecretVCHConfig(vch)
	if err != nil {
		return nil, fmt.Errorf("Unable to retrieve VCH information: %s", err)
	}

	// Relative path of datastore folder
	vmPath := vchConfig.ImageStores[0].Path

	// Get VCH datastore object
	ds, err := validator.Session.Finder.Datastore(validator.Context, vchConfig.ImageStores[0].Host)
	if err != nil {
		return nil, util.NewError(http.StatusNotFound, fmt.Sprintf("Datastore folder not found for VCH %s: %s", d.ID, err))
	}

	// Create a new datastore helper for file finding
	helper, err := datastore.NewHelper(ctx, validator.Session, ds, vmPath)
	if err != nil {
		return nil, fmt.Errorf("Unable to get datastore helper: %s", err)
	}

	return helper, nil
}

// getAllLogFilePaths returns a list of all log file paths under datastore folder, errors out when no log file found
func getAllLogFilePaths(ctx context.Context, helper *datastore.Helper) ([]string, error) {
	res, err := helper.Ls(ctx, "")
	if err != nil {
		return nil, fmt.Errorf("Unable to list all files under datastore: %s", err)
	}

	var paths []string
	for _, f := range res.File {
		path := f.GetFileInfo().Path
		if strings.HasPrefix(path, logFilePrefix) && strings.HasSuffix(path, logFileSuffix) {
			paths = append(paths, path)
		}
	}

	if len(paths) == 0 {
		return nil, util.NewError(http.StatusNotFound, "No log file available in datastore folder")
	}

	return paths, nil
}

// getContentFromLogFile downloads all log files in the list, concatenates the content of each log file and outputs a string of contents
func getContentFromLogFiles(ctx context.Context, helper *datastore.Helper, paths []string) (string, error) {
	var buffer bytes.Buffer

	// sort log files based on timestamp
	sort.Strings(paths)

	for _, p := range paths {
		reader, err := helper.Download(ctx, p)
		if err != nil {
			return "", fmt.Errorf("Unable to download log file %s: %s", p, err)
		}

		if _, err := buffer.ReadFrom(reader); err != nil {
			return "", fmt.Errorf("Error reading from log file %s: %s", p, err)
		}
	}

	return string(buffer.Bytes()), nil
}