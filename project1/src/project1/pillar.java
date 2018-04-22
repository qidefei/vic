package project1;

public class pillar {
	public Shape shape;
	public   double height;
	public double getVolumn(){
		return this.shape.getarea()*this.height;}
	public static void main(String[] args) {
		pillar p=new pillar();
		p.shape=new Trangle();
		p.shape.dim1=2;
		p.shape.dim2=3;
	    p.height=3;
	    System.out.println("三菱柱的体积"+p.getVolumn());
	     
	    p.shape=new Rectangle();
	    p.shape.dim1=3;
	    p.shape.dim2=4;
	    p.height=3;
	    System.out.println("四凌柱体积"+p.getVolumn());
	}
		// TODO Auto-generated method stub

	

}
