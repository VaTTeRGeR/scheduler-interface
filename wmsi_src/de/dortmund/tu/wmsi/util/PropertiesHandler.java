package de.dortmund.tu.wmsi.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Properties;

public class PropertiesHandler {
	
	Properties properties = null;
	
	public PropertiesHandler(String configPath) {
		if(configPath == null) {
			throw new IllegalStateException("the path cannot be null.");
		}
		properties = new Properties();
		BufferedInputStream stream;
		try {
			stream = new BufferedInputStream(new FileInputStream(configPath));
			properties.load(stream);
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getString(String name, String defaultValue){
		if(properties.containsKey(name))
			return properties.getProperty(name);
		else
			return defaultValue;
	}
	
	public long getLong(String name, long defaultValue){
		try {
			if(properties.containsKey(name))
				return Long.valueOf(properties.getProperty(name));
			else
				return defaultValue;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new IllegalStateException("Field "+name+" does not contain a long value");
		}
	}
	
	public int getInt(String name, int defaultValue){
		try {
			if(properties.containsKey(name))
				return Integer.valueOf(properties.getProperty(name));
			else
				return defaultValue;
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Field "+name+" does not contain an integer value");
		}
	}

	public boolean getBoolean(String name, boolean defaultValue){
		try {
			if(properties.containsKey(name))
				return Boolean.valueOf(properties.getProperty(name));
			else
				return defaultValue;
		} catch (Exception e) {
			throw new IllegalStateException("Field "+name+" does not contain an boolean value");
		}
	}

	public boolean has(String string) {
		return properties.containsKey(string);
	}
}
