package de.dortmund.tu.wmsi.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class FileUtil {
	/**
	 * Load lines from an {@linkplain File} into an {@linkplain ArrayList}. Every Line is represented by a {@linkplain String}
	 * @param file The {@linkplain File} to load.
	 * @return An {@linkplain ArrayList} containing the individual lines of the File.
	 */
	public static String[] loadLines(File file) {
		ArrayList<String> lines = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file), (int)Math.pow(2, 10));
			String line;
			while (reader.ready()) {
				line = reader.readLine();
				if(!line.contains(";"))
					lines.add(line);
			}
			reader.close();
		} catch (Exception e) {
			lines.clear();
			e.printStackTrace();
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		String[] lineArray = new String[lines.size()];
		return lines.toArray(lineArray);
	}
	
	public static String[] splitLine(String line, String delimiter) {
		return line.split(delimiter);
	}
	
	public static long getLong(String s){
		try {
			return Long.valueOf(s);
		} catch (Exception e) {
			throw new IllegalStateException("String "+s+" cannot be interpreted as a long value");
		}
	}
}
