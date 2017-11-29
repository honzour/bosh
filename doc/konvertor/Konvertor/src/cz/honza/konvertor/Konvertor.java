package cz.honza.konvertor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

public class Konvertor {
	
	
	private static String line2line1(String line, int index) {
		String[] pole = line.split("\\$");
		if (pole.length != 13)
			throw new RuntimeException("Délka pole není 13 (řádek " + index + ")");

		StringBuffer sb = new StringBuffer();
		switch (pole[1]) {
		case "TT":
			sb.append("1$Jan Čarný$");
			break;
		default:
			throw new RuntimeException("Neznámý šéf " + pole[1] + " (řádek " + index + ")");
		}
		
		switch (pole[2]) {
		case "KAM TT CZ3 HO":
			sb.append("2$Jan Holouš$");
			break;
		case "KAM TT CZ1 LU":
			sb.append("5$Martin Lukeš$");
			break;
		case "KAM TT CZ2 SL":
			sb.append("3$Jan Sladký$");
			break;			
		default:
			throw new RuntimeException("Neznámý klikač " + pole[2] + " (řádek " + index + ")");
		}
		
		sb.append(pole[5]);
		sb.append("$" + index);
		sb.append("$");
		sb.append(pole[6]);
		sb.append("$");
		sb.append(pole[7]);
		sb.append("$");
		sb.append(pole[12].replace(",", "$"));
		sb.append("$");
		sb.append(pole[4]);
		
		return sb.toString();
	}
	
	private static String lonlat2lonlat(String s) {
		String[] lonlat = s.replace("'", " ").replace("°", " ").replace("N", "").replace("E", "").replace("\"", "").split(",");
		
		for (int i = 0; i < 2; i++) {
			lonlat[i] = lonlat[i].trim();
			String[] hms = lonlat[i].split(" +");
			lonlat[i] = String.valueOf(Double.valueOf(hms[0]) + Double.valueOf(hms[1]) / 60.0 + Double.valueOf(hms[1]) / 3600.0);
		}
		return lonlat[0] + '$' + lonlat[1]; 
	}
	
	private static String line2line2(String line, int index) {
		String[] pole = line.split("\\$");
		if (pole.length != 13)
			throw new RuntimeException("2 Délka pole není 13 (řádek " + index + ")");

		StringBuffer sb = new StringBuffer();
		switch (pole[1]) {
		case "MP":
			sb.append("9$Vladimír Bílek$");
			break;
		default:
			throw new RuntimeException("2 Neznámý šéf " + pole[1] + " (řádek " + index + ")");
		}
		
		switch (pole[2]) {
		case "Bosch Zdařil Jakub":
			sb.append("6$Jakub Zdařil$");
			break;
		case "Bosch Stehlík Tomáš":
			sb.append("7$Stehlík Tomáš$");
			break;
		case "Bosch Poliak Michal":
			sb.append("8$Michal Poliak$");
			break;
		case "Bosch Sladký Jan":
			sb.append("3$Jan Sladký$");
			break;			
		default:
			throw new RuntimeException("2 Neznámý klikač " + pole[2] + " (řádek " + index + ")");
		}
		
		sb.append(pole[5]);
		sb.append("$" + index);
		sb.append("$");
		sb.append(pole[6]);
		sb.append("$");
		sb.append(pole[7]);
		sb.append("$");
		try {
			sb.append(lonlat2lonlat(pole[12]));
		} catch (Exception e) {
			throw new RuntimeException("2 chyba konverze " + pole[12] + " (řádek " + index + ")");
		}
		sb.append("$");
		sb.append(pole[4]);
		
		return sb.toString();
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			File fileDirIn = new File("/home/honza/github/bosh/doc/GPS.csv");
			File fileDirOut = new File("/home/honza/github/bosh/doc/GPS2.csv");

			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileDirIn), "UTF8"));
			
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(fileDirOut), "UTF8"));

			String line;

			int index = -1;
			while ((line = in.readLine()) != null) {
				index++;
				if (index == 0)
					continue;
				
				
				line = line2line1(line, index);
				out.write(line);
				out.write("\n");
				
			}

			in.close();
			
			int last = index;
			
			fileDirIn = new File("/home/honza/github/bosh/doc/dat.csv");
			in = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileDirIn), "UTF8"));
			
			index = -1;
			while ((line = in.readLine()) != null) {
				index++;
				if (index == 0)
					continue;
				
				
				line = line2line2(line, index + last);
				out.write(line);
				out.write("\n");
				
			}

			in.close();
		
			
			
			out.close();
		} catch (UnsupportedEncodingException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}
