package playgame;

import java.awt.List;
import java.util.ArrayList;

public class Player {
	public int id;
	public String name;
	//���巺��Ϊpoker��list poker,����ҵ���������
	//PlayerList.get(j).pokers,��ʾ��j����ҵ�����
	public List <poker> pokers;
	//�����вε�gouzaofangfa��ʵ����ʱ�������
	public Player(int id,String name){
		this.id=id;
		this.name=name;
		//List��һ���ӿڣ�����ֱ��ʵ����
		 this.pokers=new ArrayList<poker>();
		 
		
		
	}

}
