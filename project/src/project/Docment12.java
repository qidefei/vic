package project;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Docment12 {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		 File file =new File("File.txt");
		 Scanner num=new Scanner(System.in);
		 System.out.println("请输入要录入的内容");
		 String b=num.nextLine();
		 BufferedWriter Writer=new BufferedWriter(new FileWriter(file));
		 Writer.write(b);
		 System.out.println();
	}

}
