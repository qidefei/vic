package com.imooc;

import java.util.HashSet;
import java.util.Set;

public class Studnet {
	public String id;
	public String name;
	public Set<Course> courses;
	public Studnet(String id,String name){
		this.id=id;
		this.name=name;
		this.courses=new HashSet<Course>();
		
	}

}
