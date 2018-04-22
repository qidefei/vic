package com.imooc;

import java.util.ArrayList;
import java.util.List;
//泛型

public class TestGneric {
	public List<Course> course;
	public TestGneric(){
		this.course=new ArrayList<Course>();
	}
	public void testAdd(){
		Course cr1=new Course("1","大学语文");
		course.add(cr1);
		Course cr2=new Course("2","java基础");
		course.add(cr2);
	}
	public void testForEach(){
		for(Course cr:course){
			System.out.println(cr.id+cr.name);
			
		}
	}
	
	
	  public void testChild(){
			childCourse ccr=new childCourse();
			ccr.id="3";
			ccr.name="子类型实例";
				course.add(ccr);}
	  
	  public void testBasicType(){
		  List<Integer> list=new ArrayList<Integer>();
		  list.add(1);
		  System.out.println("基本类型必须使用包装类"+list.get(0));
	  }
	public static void main(String[] args ){
		TestGneric tg=new TestGneric();
		tg.testAdd();
		tg.testForEach();
		tg.testChild();
		tg.testBasicType();
		
	}

}

 
