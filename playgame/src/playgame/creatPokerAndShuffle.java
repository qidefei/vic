package playgame;

import java.nio.charset.MalformedInputException;
import java.util.Random;

import javax.swing.plaf.basic.BasicBorders.MarginBorder;

public class creatPokerAndShuffle {
	System.out.println("----�����ɹ�---");
	System��out.println("----�˿���---");
	for(Poker poker:pokerList){
		System.out.print(poker.color+poker.point+",");
		
	}
	System.out.pritnln("\n----��ʼϴ��---");
	Random random=new Random();
	poker poker=new poker();
	for(int i=0;i<52;i++){
		do{
			int position=random.nextInt(pokerList.size());
			poker=pokerList.get(position);
			
		}while(pokerListAfterShuffle.contains(poker));
		pokerListAfterShuffle.add(poker);
		
	}
	System.out.priln("ϴ�ƽ���---");
	
    }
public void creatPlayer(){
	System.out.println("---�������----);
	for(int i=1;i<3;i++)
		System.out.println("�������"+i+"��ҵ���Ϣ");
	while��true){
		try{
			System.out.println("������ ���ID");
			ID=cosole.nestInt();
			System.out.println("��������ҵ�����");
			name=colsole.next();
			Player newPlayer=newPlayer(ID,Name);
			playerList.add(newplayer);
		}catch(InputMismatchException e){
			String token=conslole.next();
			System.out.println("�������������Ե�ID");
			continue;
		}
		break;
	}
	for(Plaryer player:playerList){
		System.out.println("��ӭ���"+player.name);

     }
      }
       public void sendPokerAndPaly(){
    	   System.out.println("----��ʼ����------");
    	   for(int i=0;i<2;i++){
    		   for(int j=0;j<2;j++){
    			   System.out.println("��ң���"+palyerList.grt(j).name);
    			   getpoker=pokerListAfterShuffle.grt(i*2+j);
    			   playerList.get(j).pokers.add(gretPoker);
    			   
    		   }
    		   System.out.println("--���ƽ���---");
    		   System.out.println("��ʼ��Ϸ");
    		   System.out.println("����������λ����")��
    		   for(int n=0;n<2;n++){
    			   System.out.println(playerList.grt(n).name+"::");
    			   for(int m=0;m<2;m++){
        			   System.out.println(playerList.grt(n).pokers.get(m).color+playerList.get(n).pokers.get(m).point+",""::"); 
        			   //�Ƚϴ�С��ͨ�������˿�pokerlist�е�λ�ã�����list��indexof��
    		   }
    			   if(pokerList.indexOf(playerList.get(0).pookers.get(1))>pokerList.indexOf(playerList.gey(1).pokers.get(1))){
    			   System.out.println("��ң�����"+playerList.get(0).name+"��ʤ");
    			   }else{
    				  System.out.println("��ң�����"+playerList.grt(1).name+"��ʤ"); 
    			   }
    	   }
    		  public Static void main(String[] args){
    			  TestPoker tp=new TestPoker();
    			  tp.creatPokerAndShuffle();
    			  tp.creatPlayer();
    			  tp.sendPokerAndpaly();
    		  }

}
