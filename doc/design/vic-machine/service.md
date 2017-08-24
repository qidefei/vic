1. [Overview](#overview)
2. [Design goals and project scope](#design-goals-and-project-scope)
3. [Implementation Decisions](#implementation-decisions)
     1. [Technology Choices](#technology-choices)
     2. [Delivery](#delivery)
     3. [Service Upgrade](#service-upgrade)
     4. [Compatibility](#compatibility)
     5. [Authentication](#authentication)
     6. [Certificate Management](#certificate-management)
          1. [Host Certificates](#host-certificates)
          2. [Client Certificates](#client-certificates)
     7. [Log Management](#log-management)
     8. [Cross-Origin Requests &amp; Cross-Site Request Forgery](#cross-origin-requests--cross-site-request-forgery)
     9. [ISO Management](#iso-management)
    10. [Communicating modifiability](#communicating-modifiability)
    11. [Use of a query parameter for compute-resource](#use-of-a-query-parameter-for-compute-resource)
4. [The REST API](#the-rest-api)
     1. [API Versioning](#api-versioning)
     2. [Headers](#headers)
     3. [Resources](#resources)
     4. [Query Parameters](#query-parameters)
     5. [Operations](#operations)
          1. [Get version information](#get-version-information)
          2. [List VCHs](#list-vchs)
          3. [Create a VCH](#create-a-vch)
          4. [Inspect a VCH](#inspect-a-vch)
          5. [Access the creation and reconfiguration log for a VCH](#access-the-creation-and-reconfiguration-log-for-a-vch)
          6. [Access the host certificate for a VCH](#access-the-host-certificate-for-a-vch)
          7. [Reconfigure a VCH](#reconfigure-a-vch)
          8. [Delete a VCH](#delete-a-vch)
          9. [Upgrade a VCH](#upgrade-a-vch)
         10. [Debug a VCH](#debug-a-vch)
         11. [View firewall rule settings](#view-firewall-rule-settings)
         12. [Update firewall rules](#update-firewall-rules)
5. [The WebSockets API](#the-websockets-api)
6. [Proposed changes to existing functionality](#proposed-changes-to-existing-functionality)
     1. [Support for authenticating via SAML or with a session key in vic-machine](#support-for-authenticating-via-saml-or-with-a-session-key-in-vic-machine)
     2. [VCH creation spawns a custom task](#vch-creation-spawns-a-custom-task)
     3. [Refactoring of vic-machine](#refactoring-of-vic-machine)
     4. [Deprecation of demo VCH installer](#deprecation-of-demo-vch-installer)
     5. [Client certificate management using Admiral](#client-certificate-management-using-admiral)
     6. [Associating contact information with a VCH](#associating-contact-information-with-a-vch)
7. [Testing](#testing)
8. [See Also](#see-also)


## Overview

The `vic-machine-{darwin,linux,windows}` command line utilities are used to perform VCH lifecycle operations.

It is desirable to expose this same functionality via a network protocol to allow for a wider range of interaction models, such as the integration of VCH management functionality into web interfaces (such as the vSphere H5 client). A REST API (and, more specifically, an OpenAPI) would be the natural way to do so.

## Design goals and project scope

It is expected that this API will become a public, documented API at some point in the future (and, to some extent, all APIs are public), but functionality may be delivered incrementally and the first versions may be versioned or annotated in a way that communicates breaking changes are expected and backwards compatibility may not be maintained.

The initial priority is for this API to implement sufficient functionality to enable development of a vSphere plugin, as described here: https://vmware.invisionapp.com/share/GDC9QEDAZ. This may require functionality which is _not_ currently implemented by the `vic-machine` command line utilities.

The API may not initially _implement_ all functionality currently implemented by the `vic-machine` command line utilities. However, to avoid painting ourselves into a corner, it is desirable to consider all functionality in the design. Further, the _design_ should consider plausible future work to ensure that eventual extension of the API is possible.

Because this API would likely be used in conjunction with, or by developers familiar with, other VMware APIs, it is desirable to provide a similar "look & feel" where possible.

Because this API may be used in conjunction with, or by developers familiar with, other container-related APIs, providing a similar "look & feel" to those APIs is also desirable.

This document will attempt to adhere to VMware standards and guidance around API design.

## Implementation Decisions

### Technology Choices

The API will be defined using OpenAPI (Swagger), today's de facto standard for REST APIs.

The service will be built in Go, using `go-swagger`. This allows any developer on the team to contribute, maintain, and modify the service with a minimal learning curve.

WebSockets will be used to provide streaming information about the status on long-running operations so that sophisticated clients do not need to poll for updates. See below for more information.

### Delivery

The new service will be implemented as an additional flavor of `vic-machine`: `vic-machine-service`. This standalone linux binary (a swagger server) will be invoked with a port and certificate information to serve the REST API on all interfaces.

This service will be packaged as a container and included in the VIC OVA, following existing best practices.

The service will not require a access to a persistent data directory.

A configuration file (stored on the OVA's data directory) will be used to provide the service with high-level configuration information, such as a syslog server (and perhaps the admiral CA certificate ― see below).

### Service Upgrade

The VIC OVA is upgraded in a side-by-side fashion:

1. The new OVA is deployed.
2. The old OVA is shutdown.
3. The data disk is moved from the old to the new.
4. A data migration script is run.
    * In the case of the upgrading to the first version of the OVA that includes the `vic-machine` server, sane default values could be assumed (e.g., by copying syslog settings from another service).
5. The new OVA is started.

This does involve downtime of the service (but not of the VCHs or containers). Clients (including the H5 plugin) should handle this appropriately.

### Compatibility

We expect the compatibility between `vic-machine-service` and VCHs to be similar to `vic-machine-{darwin,linux,windows}`:

 * Creation will only be supported for VCH of the same version
 * Reconfigure will only be supported for VCH of the same version
 * Upgrade will only be supported for VCH of the same or lesser versions
 * Rollback will only be supported to the same VCH version
 * Deletion will be supported for VCH of the same version, and will be best-effort for older versions

We expect the vSphere H5 client plugin to support a single version of `vic-machine-service` and be upgraded in lock-step.

 - [ ] Do we need the plugin to provide at least basic support the "N+1" version of `vic-machine-service` as well so that it remains functional between when the OVA is upgraded and when the plugin is upgraded?

### Authentication

As with the `vic-machine` CLI, vSphere credentials must be supplied each time an operation is invoked using `vic-machine-service`. The service itself will not store or manage credentials. This means that **all** operations must either be explicitly tied to a vSphere operation or unauthenticated. This is similar to the model for the `vic-machine` CLI, where all operations require vSphere credentials except for those to display help and version information.

See below for a discussion of how this decision influences the design of new operations, such as those to access logs or certificates.

### Certificate Management

When invoking the `vic-machine` CLI, access to PKI files is managed out-of-band. PKI files can be placed on the filesystem, and paths passed to the CLI. Generated PKI files are placed on the filesystem for subsequent access, and the OS handles access control for those files.

With the REST API, these workflows need to be handled explicitly: the REST API must allow PKI files to be supplied as a part of requests and must allow for retrieval of generated PKI files, with appropriate access controls.

#### Host Certificates

Host certificates are persisted in the VCH's guest info and are available via the vSphere API today. `vic-machine-service` will allow users to access them via its API.

#### Client Certificates

Due to the complexity of ensuring the secrecy of client certificate private keys, the `vic-machine-service` API will not support generation of client certificates. (If this is a strict requirement, it could protect generated certificates with a user-supplied passphrase and then re-use the pattern for log management, below.)

For an alternative approach that allows for creation of VCHs without requiring users to specify client certificates, see the section on client certificate management using Admiral, below.

### Log Management

When invoking the `vic-machine` CLI, real-time information is provided to stdout and log files can be persisted on the filesystem. The REST API needs to provide equivalent functionality.

Logs will be streamed to the VCH's datastore folder as `vic-machine-service` executes. Access to logs is then restricted to those vSphere users who can read those files from the datastore.

### Cross-Origin Requests & Cross-Site Request Forgery

Because the vSphere H5 client plugin will be served from a different host than `vic-machine-service`, it will be making cross-origin requests. To allow for this, `vic-machine-service` must support responding to all requests with an `Access-Control-Allow-Origin` header with an appropriate value, and respond appropriately to `Options` requests for all resources. It must also include a `Vary` header on all responses with a value of `Origin`.

When the vSphere H5 client plugin is installed, `vic-machine-service` must be informed of the address(es) of the vSphere H5 UI. When `vic-machine-service` receives requests, it must check the `Origin` header and respond with the same value in the `Access-Control-Allow-Origin` header if and only if the value is in the configured list of addresses.

With this approach, the same-origin policy protects against cross-site request forgery attacks.

### ISO Management

Several VCH operations (create, configure, and upgrade) take a pair of ISOs as input. These ISOs are used for booting the VCH appliance and container VMs respectively.

The current implementation involves maintaining the master ISOs on the OVA and duplicating these ISOs for each VCH that is created. We may wish to change this behavior in the future to make it easier for customers to build their own bootstrap ISO, or to transition to something like direct boot.

Even considering only the current model, uploading these ISOs as a part of service API calls would introduce complexity and inefficiency.

For simplicity and flexibility, we will provide an API that lists "flavors" of ISOs which are known to the OVA and which may be used for API operations. By default, a single "flavor" will be included with the OVA: the stock appliance and boostrap ISOs shipped with that release. We will provide instructions for users who wish to add additional "flavors" of the appliance ISO (e.g., in order to use a RHEL kernel or systemd), which might involve putting the ISOs in a particular directory on the OVA or adding paths to the ISOs in some sort of manifest file. For now, custom bootstrap ISOs will not be supported (but it is easy to imagine adding support following this same pattern).

In the future, it may be desirable to improve our handling of ISOs to reduce duplication (e.g., by storing them on the datastore instead of within the OVA). This is, however, orthogonal to the introduction of an API.

 - [ ] Come up with a better term than "flavor".

### Communicating modifiability

VCH properties may or may not be modifiable for a variety of reasons. Some properties, such as the id, may never be modifiable. Other properties may not be modifiable without a power-cycle of the VCH. Yet others may be dependent on the state of other resource, such as whether containers are using an attached network.

In all cases, the server would enforce unmodifiablility when performing an operation, but to provide a good experience for direct and indirect users of the API, it would be helpful to communicate which properties of a given VCH are not currently modifiable and why. (That is, it's better to grey out a field in the interface and provide a help tooltip with an explanation than to allow users to attempt to an operation which will inevitably fail.)

A variety of approaches exist for this:

 * Capturing the general mutability rules in a formal language so that clients can evaluate those rules against the current state of the resource. This powerful pattern would allow interfaces built on the API to clearly communicate why a field cannot be modified, and offer options for remediation, without attempting the modification. However, this would require substantial effort for both the client and server.
 * Evaluating mutability rules on the server to determining point-in-time mutability, and including that information as a part of GET requests. This reduces the burden on the client, while still allowing for a good user experience. However, this significantly bloats the API and leads to GET/PUT asymmetry.
 * Documenting the mutability rules in a human-readable way. This would allow implementors of interfaces built on the API to read the rules and select some or all of them to evaluate in their client code. This can be cumbersome when clients wish to support multiple versions of the API, but seems to be the approach used by most common REST APIs today.

With each of these approaches, it is also possible for the client or sever to express a level of intrusiveness. For example, a server might communicate "the VCH must be restarted to modify this" instead of simply "this cannot be modified in the current state." Similarly, a client might communicate "I want to make this change, even if it requires restarting the VCH" or "I want to make this change, even if it requires powering off all containers."

More complex logic can be introduced in the future, but in the interest of simplicity of the API and ease of implementation, it seems desirable to start simply with documenting the mutability rules in a human-readable way. Time permitting, it may be useful to allow clients to communicate "I want to make this change, even if it requires restarting the VCH."

### Use of a query parameter for compute-resource

A datacenter represents an aggregation of resources within which a VCH may exist (spanning compute, storage, and networking), and is therefore included as a hierarchical path element. However, the compute-resource (i.e., cluster or resource pool) is, conceptually, a one-dimensional filter. Analogous filters for storage-resource or networking-resource could exist to identify VCHs using a particular datastore or network respectively. Expressing such filters as query parameters avoids the incorrect connotation of hierarchy that a path element would imply, and ensures composeability.

(In the future, one could even imagine a more flexible filter mechanism that allowed for filtering on _any_ property of a VCH. Under such a model, compute-resource could be deprecated in favor of a more verbose expression, or viewed as a shorthand.)

## The REST API

### API Versioning

 - [ ] To do

### Headers

The standard Authorization header will be used for authentication.

Two schemes will be supported:

 * "basic", which will allow direct authentication with username and password
 * "Bearer", which will allow authentication via SAML, including from the H5 client plugin. (Alternatively, if supporting this is hard, a scheme that accepts a session key or session cookie would make sense.)

Additional scheme(s) could be adopted as necessary.

### Resources

The base resource for all API operations will be `/container`.

Additional resources will exist to represent:

1. a vSphere target (ESX, vCenter, or Datacenter)
    * The root resource followed by `/{target-network-address}`
       * The target-network-address parameter must be a valid network address (FQDN or IP address) of a vSphere Server (ESX or vCenter)
    * The root resource followed by `/{target-network-address}/datacenter/{datacenter-id}`
       * The target-network-address parameter must be a valid network address (FQDN or IP address) of a vCenter Server
       * The datacenter-id parameter must be an identifier for a resource of type Datacenter located within that vCenter Server
2. the collection of VCHs within (1)
    * Any resource from (1) followed by `/vch`
3. a VCH within (2)
    * Any resource from (2) followed by `/{vch-id}`

Note: Given the use cases for this API, the exclusive use of identifiers (vs. names) seems acceptable. If necessary, lookup-by-name can be implemented using the `filter.names` query pattern from in the vSphere REST API.

### Query Parameters

For all requests to all resources except the root resource: An optional "thumbprint" parameter will be supported to allow an API client to indicate the expected thumbprint of the target vSphere system. This parameter need not be supplied if the target system has a certificate signed by a trusted certificate authority. There will be no equivalent to the "force" command-line argument.

 - [ ] Figure out how certificate authority management will work. (Presumably there's something this can piggyback on.)

For many requests, as detailed below: An optional "compute-resource" parameter will be supported to scope a request to a particular compute resource within a vSphere target. This is equivalent to the "compute-resource" command-line argument except that it takes an identifier instead of a name.

### Operations

#### Get version information
```
GET /container

GET /container/version
```

A `GET` request on the base resource will return a JSON object containing metadata. Initially, the only pieces of metadata included will be the version number and a list of known appliance ISOs.

A `GET` request on the `version` sub-resource will return just the version.

 - [ ] Should this also capture the required vSphere permissions for various operations? (If so, how?)

Corresponding CLI: `vic-machine-{darwin,linux,windows} version`

#### List VCHs
```
GET /container/{target-network-address}/[datacenter/{datacenter-id}]/vch?[thumbprint={thumbprint}]&[compute-resource={compute-resource}]
```

Making a `GET` request on `/vch` under a target and optionally a datacenter will return information about the VCHs on that target, in that datacenter.

 - [ ] Pagination?

Corresponding CLI: `vic-machine-{darwin,linux,windows} ls`

#### Create a VCH
```
POST /container/{target-network-address}/[datacenter/{datacenter-id}]/vch?[thumbprint={thumbprint}]
```

Making a `POST` request on `/vch` under a target and optionally a datacenter will create a VCH on that target, in that datacenter. Information about the VCH will be provided in the body of the request in a format similar to this.

Note that validation of the request would occur synchronously, with any errors being returned using an appropriate response code and status. The rest of creation would proceed asynchronously, with errors being reported via a vSphere task that is returned once the synchronous validation is complete. (See "VCH creation spawns a custom task" below.)

Corresponding CLI: `vic-machine-{darwin,linux,windows} create`

#### Inspect a VCH
```
GET /container/{target-network-address}/[datacenter/{datacenter-id}]/vch/{vch-id}?[thumbprint={thumbprint}]
```

Making a `GET` request on a VCH resource will return information about the VCH. Information about the VCH will be provided in the body of the response in the same format as create.

Corresponding CLI: `vic-machine-{darwin,linux,windows} inspect`

#### Access the creation and reconfiguration log for a VCH
```
GET /container/{target-network-address}/[datacenter/{datacenter-id}]/vch/{vch-id}/log?[thumbprint={thumbprint}]
```

Making a `GET` request on `/log` under a VCH resource will return the contents of that VCH's log. The log is created during VCH creation and appended to during subsequent operations. This request is different than most others in that the return type is `text/plain`.

    To do: dumping all logs to a single endpoint seems sub-optimal, but so does attempting to identify individual log files. Reconcile.

Corresponding CLI: N/A

#### Access the host certificate for a VCH
```
GET /container/{target-network-address}/[datacenter/{datacenter-id}]/vch/{vch-id}/certificate?[thumbprint={thumbprint}]
```

Making a `GET` request on `/certificate` under a VCH resource will return the certificate the VCH uses when acting as a server, which clients may wish to access to download and add to a trust store. This request is different than most others in that the return type is `application/x-pem-file`.

Corresponding CLI: N/A

#### Reconfigure a VCH
```
PUT /container/{target-network-address}/[datacenter/{datacenter-id}]/vch/{vch-id}?[thumbprint={thumbprint}]

PATCH /container/{target-network-address}/[datacenter/{datacenter-id}]/vch/{vch-id}?[thumbprint={thumbprint}]
```

Making a `PUT` request on a VCH resource will update that VCH's configuration. Information about the VCH will be provided in the body of the request in the same format as create.

In trying to strike a balance between the Robustness Principle and the Principle of Least Astonishment, we will allow for fields which cannot be modified to appear in the body of a `PUT` as long as the value of those fields match the current state of the object. This allows us to be relatively liberal in what we accept while avoiding the potential surprise of having edits dropped from the request. When the value of a field which cannot be modified does not match the current state, a 409 Conflict will be returned. To preserve the idempotency requirement for `PUT`, modifications to mutable portions of the body must not cause immutable portions of the body to change as a side-effect.

Making a `PATCH` request on a VCH resource (with a body as described in RFC 7396) will update a subset of that VCH's configuration.

As `PATCH` is an explicit request to update a set of fields, fields which cannot be modified must not appear in the body of the `PATCH` request, even if the modification would be a no-op.

Corresponding CLI: `vic-machine-{darwin,linux,windows} configure`

#### Delete a VCH
```
DELETE /container/{target-network-address}/[datacenter/{datacenter-id}]/vch/{vch-id}?[thumbprint={thumbprint}]
```

Making a `DELETE` request on a VCH resource will delete that VCH.

 - [ ] Determine whether the deletion affects, e.g., volume stores. (Current default CLI behavior is not to delete data.) (Should there also, eventually, be an option to delete a VCH, but preserve containers?)

Corresponding CLI: `vic-machine-{darwin,linux,windows} delete`

#### Upgrade a VCH
```
POST /container/{target-network-address}/[datacenter/{datacenter-id}]/vch/{vch-id}?action=upgrade&[thumbprint={thumbprint}]
```

Making a `POST` request on a VCH resource with an action of upgrade will initiate an upgrade of the VCH. The body of the request will be a JSON object containing the following optional properties: `bootstrap-iso` (a reference to a known bootstrap ISO on the OVA) and `rollback` (a boolean value).

Corresponding CLI: `vic-machine-{darwin,linux,windows} upgrade`

#### Debug a VCH
```
POST /container/{target-network-address}/[datacenter/{datacenter-id}]/vch/{vch-id}?action=debug&[thumbprint={thumbprint}]
```

Making a `POST` request on a VCH resource with an action of debug will modify the debug settings for the VCH. The body of the request will be a JSON object containing the following optional properties: `enable-ssh` (a boolean value), `authorized-key` (a string representation of a public key), `rootpw` (a string).

Corresponding CLI: `vic-machine-{darwin,linux,windows} debug`

#### View firewall rule settings
```
GET /container/{target-network-address}/[datacenter/{datacenter-id}]?[thumbprint={thumbprint}]&[compute-resource={compute-resource}]
```

Making a `GET` request on a vSphere target (with an optional datacenter and compute resource) will return information about the state of the host firewall on those resources. This allows a user to easily determine whether the hosts are in an appropriate state, and allows interfaces to display alerts when they are not. This also provides some measure of symmetry with the update firewall rules operation below.

Corresponding CLI: N/A

#### Update firewall rules
```
POST /container/{target-network-address}/[datacenter/{datacenter-id}]?action=firewall:[allow|deny]&[thumbprint={thumbprint}]&[compute-resource={compute-resource}]
```

Making a `POST` request on a vSphere target (with an optional datacenter and compute resource) with an action of `firewall:allow` or `firewall:deny` will update the host firewall on those resources.

Corresponding CLI: `vic-machine-{darwin,linux,windows} update`

## The WebSockets API

A WebSockets-based API will be used to provide streaming access to log data.

 - [ ] Specify this.

## Proposed changes to existing functionality

### Support for authenticating via SAML or with a session key in `vic-machine`

**Status:** Prerequisite

As expected, raw user credentials are not available to H5 client plugins. To perform operations on behalf of the authenticated user, `vic-machine` will need to support being invoked with information that is available to the client plugin, such as a SAML token or a session key.

### VCH creation spawns a custom task

**Status:** Required

Currently, VCH creation consists of three main steps:

1. Validation of the request and the state of the system.
2. A series of vSphere operations to create the VCH.
3. Starting the container VM and its services.

A single parent task could be created for the middle portion of this workflow, to be used as a starting point for a user wishing to query for the status of the creation operation.

When initiating VCH creation via the service, the first (validation) step would occur synchronously. Once the second (vSphere) step begins and the custom task is created, the API would return the handle to the vSphere task, which tracks the asynchronous portion of the request.

### Refactoring of `vic-machine`

**Status:** Prerequisite

The CLI and REST API should each be a thin layer around a common idiomatic Go API. That is, the CLI and REST API should be interaction and translation layers which do not include "business logic". Refactoring may be necessary to achieve this.

Alternatively, the CLI could be be re-imagined and implemented as a client of the REST API. The advantages and disadvantages of such a change should be carefully considered.

### Deprecation of demo VCH installer

**Status:** Optional

The demo VCH installer is currently shipped as a part of the OVA as a container with a web application that listens on port 1337. It provides users with a simple web interface to provision a VCH for demo/testing purposes by invoking the `vic-machine` CLI.

Re-implementing this UI using the interface using the new API would be feasible. However, it seems to make sense to consider the use cases for this application in the design of the vSphere H5 client plugin. And as there are no known backwards compatibility guarantees (or important use cases/workflows which would be impacted), it would make sense to deprecate and eventually remove the demo VCH installer once the vSphere H5 client plugin is available.

### Client certificate management using Admiral

**Status:** Required

Currently, `vic-machine` supports using user-specified client certificates or generating certificates for users to use. As an alternate model, it could delegate client certificate management to Admiral.

As an outline of how this might work:

 * When Admiral is installed, it could generate a CA certificate and expose that certificate's public key (or make use of the vSphere CA).
 * When a VCH is created, the Admiral CA certificate's public key could be supplied as the client certificate CA.
 * When a user in created in Admiral, it could generate a client certificate signed by its client CA certificate (or sign a public key supplied by the user).
 * When VCH is authenticating a request, the Admiral CA certificate's public key (from guest info) could be blended with dynamic configuration information from Admiral to limit access to only those users who have been granted access to the VCH's project in Admiral.
 * When a user accesses a VCH, they would use the client certificate assigned by Admiral.

This provides several key benefits:

 * Each user would have a single certificate which authorizes them to use all VCHs they have been granted access to.
 * Operations performed on a VCH can be tied to a user.
 * This would help address the lack of certificate revocation mechanisms and allow Admiral to be used to dynamically manage user authorization.

Most relevantly, and perhaps most importantly: this means that a VCH creation workflow does not require upload or download of client certificates, allowing for a simplified user experience.

The work that would be required for this would include:

1. Discovery/lookup of the Admiral CA certificate by `vic-machine-service`.
2. Certificate blending within the VCH. (Validating that the client certificates supplied by Admiral's dynamic configuration are signed by a client CA certificate configured on the VCH, and then limiting access to only those client certificates.)
3. Per-user client certificate generation (or signing) within Admiral.
4. Support for per-user client certificates in Admiral dynamic config.
5. To allow operations performed on a VCH to be tied to a user:
    1. Expanding operation logging across the personality/portlayer boundary.
    2. Logging the public details of the client certificate used for authorization.

### Associating contact information with a VCH

**Status:** Optional

The original VCH configuration had an "environment contact" and an "administrative contact", which were stored using vSphere notes. This allowed a vSphere administrator to associate their contact information with a VCH, so that other administrators would know who to contact before making changes to the system.

This information could also be included on the vicadmin page, even for unauthenticated users, to enable users of all types to know who to talk to for help. (Perhaps the contact information should be free-form to allow for things like referring to a ticket-tracking system.)

 - [ ] Customers with large environments must have a way to manage ownership information of entities. Can we learn from that?

## Testing

`vic-machine-service` will require two types of tests:

1. Unit tests to verify functionality of handler logic.
2. End-to-end tests to verify the API functionality from a client's point of view (and to serve as the first "client", and as a secondary form of documentation).

Additionally, appropriate testing will be needed for each of the items in the "proposed changes to existing VIC functionality" section.

## See Also

 * The GitHub Epic for this work: https://github.com/vmware/vic/issues/5721
 * The GitHub Epic for the UI work: https://github.com/vmware/vic/issues/5150
