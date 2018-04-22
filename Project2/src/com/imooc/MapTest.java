package com.imooc;

 
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

public class MapTest {
	public Map<String,Studnet> student;
	public void testPut(){
		Scanner console =new Scanner(System.in);
		int i=0;
		while(i<3){
			System.out.println("请输入学生ID：：");
			String ID=console.next();
			Studnet st=student.get(ID);
			if(st==null){
				System.out.println("请输入学生姓名：：");
				String name=console.next();
				Studnet newStudnet=new Studnet(ID,name);
				student.put(ID, newStudnet);
				System.out.println("成功添加学生"+student.get(ID).name);
				i++;
				
			}else{
				System.out.println("学生ID被占用：：");
				continue;
			} 
		}
	}
	public void testKeySet(){
		Set<String> keySet=student.keySet();
		for(String stuID:keySet){
			Studnet st=student.get(stuID);
			if(st!=null)
				System.out.println("学生：："+st.name);
		}
		
	}
		public void testRemove(){
			Scanner console =new Scanner(System.in);
		while(true){
			
			System.out.println("请输入删除学生ID：：");
			 
			 
				String ID=console.next();
				Studnet st=student.get(ID);
				if(st==null){
					System.out.println("学生ID不存在：：");
					continue;
				}
			student.remove(ID);
			System.out.println("成功删除学生"+st.name);
			break;
		}
		}
		public void testEntrySet(){
			 Set<Entry<String,Studnet>>entrySet=student.entrySet();
			 Object entry;
			System.out.println("取得建"  +entry.getkey());
			System.out.println("对应的值"+entry。getValue().name);
		}
		public void testModify(){
			Scanner console =new Scanner(System.in);
			while(true){
				
				System.out.println("请输入修改学生ID：：");
				 
				 
					String stuID=console.next();
					Studnet st=student.get(stuID);
					if(st==null){
						System.out.println("学生ID不存在：：");
						continue;
					}
				 System.out.println("请输入当前学生ID所对应的学生为"+student.name);
				 System.out.println("请输入新的学生姓名");
				Studnet newStudnet=new  Studnet(stuID,name);
				 student.put(stuID,newStudnet);
				 System.out.println("修改成功");
				 break;
			
		}
	
	
		}
	
	
	
		public static void main(String[] args) {
		// TODO Auto-generated method stub
			MapTest mt=new MapTest();
			mt.testKeySet();
			mt.testPut();
			mt.testRemove();
			mt.testEntrySet();
			mt.testModify();
			mt.testPut();
			

	}

}
