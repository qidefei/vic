package playgame;

public class Poker {
	public String point;
	public String color;
	//�����в����ķ�����ʵ����ʱ�������
	public poker(String point,String color){
		this.point=point;
		this.color=color;
		public Poker(){
			
		}
		//�����޲εĹ��췽����ϴ���ǻ�ʵ�����޲ι��췽��
		public Poker(){
			public boolean equals(Object obj){
				if(this==obj)
					return true;
				if(obj==null)
					return false;
				if(grtClass()!=obj.grtClass())
					return false;
				poker other=(poker) obj;
				if(color==null){
					return false;
					if(other.color!=null)
						return fakse;
				}else if(!color.equals(other.color))
					return false;
				
			}if(point==null){
				if(other.point!=null)
					return false;
			}else if (!point.equals(other.point))
				return false;
			return true;
			
			
 
		
		
	 
	}
	

}
