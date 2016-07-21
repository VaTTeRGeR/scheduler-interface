package de.dortmund.tu.wmsi.dynamicUserModel;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.lang.ArrayUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

public class ConfigReader {
	
	//String[] both = (String[])ArrayUtils.addAll(first, second);
	
	public String getConfigFolderPath() {
		return "C:/Users/stephan/Desktop/experiments/16_01_19_dummytest";
	}
	
	public String getTracePath() {
		return this.getProgramPath() + "teikoku-research/SWFTrace/site-OOO.csv";
	}
	
	public String getProgramPath() {
		return "C:/Users/stephan/Documents/Usermodel/teikoku/";
	}
	

	public String[] getCommand(String propertiesPath) {
		String path = this.getProgramPath();
		String PROPERTIES_PATH  = propertiesPath;
		
		String simulationPath = "simulationconfig.xml"; 
		String[] java = this.readJava(simulationPath);
		
		String mavenPath = this.readMavenPath(simulationPath);
		
		String[] command = new String[] {
				"-classpath",
					path + "/teikoku-research/target/classes;"
						+ path + "teikoku-sim/target/classes;"
						+ path + "teikoku-util/target/classes;"
						+ mavenPath + "commons-cli/commons-cli/1.2/commons-cli-1.2.jar;"
						+ mavenPath + "commons-daemon/commons-daemon/1.0.1/commons-daemon-1.0.1.jar;"
						+ mavenPath + "log4j/log4j/1.2.14/log4j-1.2.14.jar;"
						+ mavenPath + "org/springframework/spring-context/3.0.2.RELEASE/spring-context-3.0.2.RELEASE.jar;"
						+ mavenPath + "org/springframework/spring-aop/3.0.2.RELEASE/spring-aop-3.0.2.RELEASE.jar;"
						+ mavenPath + "aopalliance/aopalliance/1.0/aopalliance-1.0.jar;"
						+ mavenPath + "org/springframework/spring-beans/3.0.2.RELEASE/spring-beans-3.0.2.RELEASE.jar;"
						+ mavenPath + "org/springframework/spring-core/3.0.2.RELEASE/spring-core-3.0.2.RELEASE.jar;"
						+ mavenPath + "org/springframework/spring-expression/3.0.2.RELEASE/spring-expression-3.0.2.RELEASE.jar;"
						+ mavenPath + "org/springframework/spring-asm/3.0.2.RELEASE/spring-asm-3.0.2.RELEASE.jar;"
						+ mavenPath + "commons-lang/commons-lang/2.2/commons-lang-2.2.jar;"
						+ mavenPath + "org/apache/commons/commons-math3/3.3/commons-math3-3.3.jar;"
						+ mavenPath + "commons-logging/commons-logging/1.1.1/commons-logging-1.1.1.jar;"	
						+ mavenPath + "org/apache/commons/commons-math3/3.4/commons-math3-3.4.jar;"
						+ mavenPath + "org/apache/commons/",
				"de.irf.it.rmg.core.teikoku.Bootstrap", "-gp", PROPERTIES_PATH };
		
		return command = (String[])ArrayUtils.addAll(java, command);
		
		
	}
	


	private String[] readJava(String path) {
		
		String[] result = new String[0];
		
		try {
			File fXmlFile = new File(path);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("parameter");
			
			result = new String[nList.getLength()];
			
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);		
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					String content = eElement.getTextContent();
					result[temp] = content;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	

	
	
	private String readMavenPath(String path) {
		String result = "";
		
		try {
			File fXmlFile = new File(path);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("mavenrepository");
			
			result = nList.item(0).getTextContent();		
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
}
