package project;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Docment {

	public static void main(String[] args) throws IOException {
		File file = new File("File.txt");
		Scanner num = new Scanner(System.in);
		System.out.println("������Ҫ¼������ݣ�");
		String b = num.nextLine();
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write(b);
		writer.close();
		
		System.out.println();
	}
		
	}


