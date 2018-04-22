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
			System.out.println("������ѧ��ID����");
			String ID=console.next();
			Studnet st=student.get(ID);
			if(st==null){
				System.out.println("������ѧ����������");
				String name=console.next();
				Studnet newStudnet=new Studnet(ID,name);
				student.put(ID, newStudnet);
				System.out.println("�ɹ����ѧ��"+student.get(ID).name);
				i++;
				
			}else{
				System.out.println("ѧ��ID��ռ�ã���");
				continue;
			} 
		}
	}
	public void testKeySet(){
		Set<String> keySet=student.keySet();
		for(String stuID:keySet){
			Studnet st=student.get(stuID);
			if(st!=null)
				System.out.println("ѧ������"+st.name);
		}
		
	}
		public void testRemove(){
			Scanner console =new Scanner(System.in);
		while(true){
			
			System.out.println("������ɾ��ѧ��ID����");
			 
			 
				String ID=console.next();
				Studnet st=student.get(ID);
				if(st==null){
					System.out.println("ѧ��ID�����ڣ���");
					continue;
				}
			student.remove(ID);
			System.out.println("�ɹ�ɾ��ѧ��"+st.name);
			break;
		}
		}
		public void testEntrySet(){
			 Set<Entry<String,Studnet>>entrySet=student.entrySet();
			 Object entry;
			System.out.println("ȡ�ý�"  +entry.getkey());
			System.out.println("��Ӧ��ֵ"+entry��getValue().name);
		}
		public void testModify(){
			Scanner console =new Scanner(System.in);
			while(true){
				
				System.out.println("�������޸�ѧ��ID����");
				 
				 
					String stuID=console.next();
					Studnet st=student.get(stuID);
					if(st==null){
						System.out.println("ѧ��ID�����ڣ���");
						continue;
					}
				 System.out.println("�����뵱ǰѧ��ID����Ӧ��ѧ��Ϊ"+student.name);
				 System.out.println("�������µ�ѧ������");
				Studnet newStudnet=new  Studnet(stuID,name);
				 student.put(stuID,newStudnet);
				 System.out.println("�޸ĳɹ�");
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
