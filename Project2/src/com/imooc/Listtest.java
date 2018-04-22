package com.imooc;

import java.awt.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

  


public class Listtest {
	public ArrayList coursesToSelect;
	public Listtest(){
		
	this.coursesToSelect=new ArrayList();
	 
	}
	public void testAdd(){
		Course cr1=new Course("1", "数据结构");
		coursesToSelect.add(cr1);
		Course temp=(Course) coursesToSelect.get(0);
		System.out.println("第一种添加了课程"+temp.id+temp.name);
		
		
		
		Course cr2=new Course("2","c语言");
				coursesToSelect.add(1,cr2);
				Course temp1=(Course) coursesToSelect.get(1);
				System.out.println("第二种添加了课程"+temp1.id+temp1.name);
		Course[] course={new Course("3","离散数学"),new Course	("4","高等数学")};
		coursesToSelect.addAll(Arrays.asList(course));
		Course temp3=(Course) coursesToSelect.get(2);
		Course temp4=(Course) coursesToSelect.get(3);
		System.out.println("第三种添加了课程"+temp3.id+temp3.name+"::::"+temp4.id+temp4.name);
		Course[] coure2={new Course("5","高等数学"),new Course("6","java语言")};
		coursesToSelect.addAll(2,Arrays.asList(coure2));
		Course temp5=(Course)coursesToSelect.get(4);
		Course temp6=(Course) coursesToSelect.get(5);
		System.out.println("第四种添加了课程"+temp5.id+temp5.name+"::::"+temp6.id+temp6.name);
	}
	public void testGet(){
		int size=coursesToSelect.size();
		for(int i=0;i<size;i++){
			Course cr=(Course) coursesToSelect.get(i);
			System.out.println("第一种遍历"+cr.id+cr.name);
			
		}
	}
		public void testIterator(){
			Iterator it=coursesToSelect.iterator();
			while(it.hasNext()){
				Course cr=(Course) it.next();
				System.out.println("第二种遍历"+cr.id+cr.name);

			}
		}
		public void testForEach(){
			for(Object obj:coursesToSelect){
				Course cr=(Course)obj;
				System.out.println("第三种遍历"+cr.id+cr.name);

			}
			
		}
		public void testModify(){
			coursesToSelect.set(1, new Course("2","毛概"));
		}
		public void testRomove(){
			  Course[] courses={(Course) coursesToSelect.get(4),(Course) coursesToSelect.get(5)};
				coursesToSelect.removeAll(Arrays.asList(courses));
			  testForEach();
			  
			  }
			
		public static void main(String[] args) {
		Listtest lt=new Listtest();
		lt.testAdd();
		lt.testGet();
		lt.testIterator();
		lt.testForEach();
		lt.testModify();
		lt.testRomove();
		
		
		
			
		
		
		
	}
}
