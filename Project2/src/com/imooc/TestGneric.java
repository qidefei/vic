package com.imooc;

import java.util.ArrayList;
import java.util.List;
//����

public class TestGneric {
	public List<Course> course;
	public TestGneric(){
		this.course=new ArrayList<Course>();
	}
	public void testAdd(){
		Course cr1=new Course("1","��ѧ����");
		course.add(cr1);
		Course cr2=new Course("2","java����");
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
			ccr.name="������ʵ��";
				course.add(ccr);}
	  
	  public void testBasicType(){
		  List<Integer> list=new ArrayList<Integer>();
		  list.add(1);
		  System.out.println("�������ͱ���ʹ�ð�װ��"+list.get(0));
	  }
	public static void main(String[] args ){
		TestGneric tg=new TestGneric();
		tg.testAdd();
		tg.testForEach();
		tg.testChild();
		tg.testBasicType();
		
	}

}

 
