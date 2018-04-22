package playgame;

import java.awt.List;
import java.util.ArrayList;

public class Player {
	public int id;
	public String name;
	//定义泛型为poker的list poker,即玩家的手牌属性
	//PlayerList.get(j).pokers,表示第j个玩家的手牌
	public List <poker> pokers;
	//定义有参的gouzaofangfa，实例化时传入参数
	public Player(int id,String name){
		this.id=id;
		this.name=name;
		//List是一个接口，不能直接实例化
		 this.pokers=new ArrayList<poker>();
		 
		
		
	}

}
