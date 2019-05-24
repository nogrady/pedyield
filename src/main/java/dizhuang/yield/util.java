package dizhuang.yield;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class util {
	public static void main(String[] args) throws IOException {
		reformat("GH010228");
		reformat("GH020228");
		reformat("GH030228");
		reformat("GH040228");
		reformat("GH050228");
		
		reformat("GH060228");
		reformat("GH070228");
		reformat("GH080228");
		reformat("GH090228");
		reformat("GH100228");
	}
	
	public static void reformat(String ntwk) throws IOException {
		PrintWriter pw=new PrintWriter("csvCar_"+ntwk+"_2.csv");
		
		BufferedReader br = new BufferedReader(new FileReader(ntwk+"_2_1920-1080-59_car.csv"));
		String line=""; 
		while ((line=br.readLine()) != null) {
			String[] lines=line.split(",");
			pw.println(lines[0]+","+lines[1]+","+lines[4]+","+lines[5]+","+lines[6]+","+lines[7]);
		}
		br.close();
		pw.close();
		
		
		pw=new PrintWriter("csvPed_"+ntwk+"_2.csv");
		
		br = new BufferedReader(new FileReader(ntwk+"_2_1920-1080-59_ped.csv"));
		while ((line=br.readLine()) != null) {
			String[] lines=line.split(",");
			pw.println(lines[0]+","+lines[1]+","+lines[4]+","+lines[5]+","+lines[6]+","+lines[7]);
		}
		br.close();
		pw.close();
	}
}
