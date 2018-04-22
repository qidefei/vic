package playgame;

import java.nio.charset.MalformedInputException;
import java.util.Random;

import javax.swing.plaf.basic.BasicBorders.MarginBorder;

public class creatPokerAndShuffle {
	System.out.println("----创建成功---");
	System。out.println("----扑克牌---");
	for(Poker poker:pokerList){
		System.out.print(poker.color+poker.point+",");
		
	}
	System.out.pritnln("\n----开始洗牌---");
	Random random=new Random();
	poker poker=new poker();
	for(int i=0;i<52;i++){
		do{
			int position=random.nextInt(pokerList.size());
			poker=pokerList.get(position);
			
		}while(pokerListAfterShuffle.contains(poker));
		pokerListAfterShuffle.add(poker);
		
	}
	System.out.priln("洗牌结束---");
	
    }
public void creatPlayer(){
	System.out.println("---创建玩家----);
	for(int i=1;i<3;i++)
		System.out.println("请输入第"+i+"玩家的信息");
	while（true){
		try{
			System.out.println("请输入 玩家ID");
			ID=cosole.nestInt();
			System.out.println("请输入玩家的姓名");
			name=colsole.next();
			Player newPlayer=newPlayer(ID,Name);
			playerList.add(newplayer);
		}catch(InputMismatchException e){
			String token=conslole.next();
			System.out.println("请输入整数烈性的ID");
			continue;
		}
		break;
	}
	for(Plaryer player:playerList){
		System.out.println("欢迎玩家"+player.name);

     }
      }
       public void sendPokerAndPaly(){
    	   System.out.println("----开始发牌------");
    	   for(int i=0;i<2;i++){
    		   for(int j=0;j<2;j++){
    			   System.out.println("额俺家：："+palyerList.grt(j).name);
    			   getpoker=pokerListAfterShuffle.grt(i*2+j);
    			   playerList.get(j).pokers.add(gretPoker);
    			   
    		   }
    		   System.out.println("--发牌结束---");
    		   System.out.println("开始游戏");
    		   System.out.println("玩家手里的牌位：：")；
    		   for(int n=0;n<2;n++){
    			   System.out.println(playerList.grt(n).name+"::");
    			   for(int m=0;m<2;m++){
        			   System.out.println(playerList.grt(n).pokers.get(m).color+playerList.get(n).pokers.get(m).point+",""::"); 
        			   //比较大小，通过利用扑克pokerlist中的位置，利用list的indexof，
    		   }
    			   if(pokerList.indexOf(playerList.get(0).pookers.get(1))>pokerList.indexOf(playerList.gey(1).pokers.get(1))){
    			   System.out.println("玩家：：："+playerList.get(0).name+"获胜");
    			   }else{
    				  System.out.println("玩家：：："+playerList.grt(1).name+"获胜"); 
    			   }
    	   }
    		  public Static void main(String[] args){
    			  TestPoker tp=new TestPoker();
    			  tp.creatPokerAndShuffle();
    			  tp.creatPlayer();
    			  tp.sendPokerAndpaly();
    		  }

}
