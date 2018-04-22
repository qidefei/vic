package project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class request {
	public static void main(String[] args) {
		String b=null;

		try {
		BufferedReader	in=new BufferedReader( new FileReader(" D:\zhonggoure")); 

			BufferedWriter ou=new BufferedWriter(new FileWriter(" D:\zhonggoure"));
		while((b=in.readLine())!=null){
			ou.write(b);
			System.out.print("复制成功");

		}

		in.close();
		ou.close();			

		} catch (IOException e) {
			System.out.print("找不到指定文件");
			System.exit(0);

		}

		
	}

}
