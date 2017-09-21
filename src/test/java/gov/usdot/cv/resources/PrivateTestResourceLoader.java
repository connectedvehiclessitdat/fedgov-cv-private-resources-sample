package gov.usdot.cv.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PrivateTestResourceLoader {

	private static final Logger logger = Logger.getLogger(PrivateTestResourceLoader.class);
	
	private static FilePropertyMapper filePropertyMap = new FilePropertyMapper();
	
	public static String getProperty(String resourceName) {
		String cleanResourceName = stripResourceIndicators(resourceName);
		
		String[] splitResourceName = cleanResourceName.split("/");
		String propertyFileName = "properties/" + splitResourceName[0] + ".properties";
		String propertyName = splitResourceName[1];
		
		if(!filePropertyMap.containsProperties(propertyFileName)) {
			filePropertyMap.loadProperties(propertyFileName);
		}
		
		String property = filePropertyMap.getProperty(propertyFileName, propertyName); 
		
		return property;
	}
	
	public static InputStream getFileAsStream(String fileName)  {
		String cleanFileName = stripResourceIndicators(fileName);
		
		logger.info("Attempting to find file " + cleanFileName + " on classpath.");
		InputStream is = PrivateTestResourceLoader.class.getClassLoader().getResourceAsStream(cleanFileName);
		
		if(is == null) {
			logger.error("File " + cleanFileName + " could not be found");
		}
		
		return is;
	}
	
	public static boolean isPrivateResource(String resourceName) {
		return resourceName.startsWith("@") && resourceName.endsWith("@");
	}
	
	public static String stripResourceIndicators(String resourceName) {
		return (isPrivateResource(resourceName))?
					(resourceName.substring(1, resourceName.length()-1)) :
					(resourceName); 
	}
	
	private static class FilePropertyMapper {
		private Map<String, Properties> fileNameToPropertiesMap = new HashMap<String, Properties>();
		
		public void loadProperties(String fileName) {
			Properties props = new Properties();
			InputStream is = getFileAsStream(fileName);
			
			if(is != null) {
				try {
					props.load(is);
					
					fileNameToPropertiesMap.put(fileName, props);
				} catch (IOException e) {
					logger.error("Failed to load properties file " + fileName + ".", e);
				}
				finally {
					try { is.close(); } catch(Exception ignore) {};
				}
			}
		}
		
		public String getProperty(String fileName, String propertyName) {
			String property = null;
			
			Properties props = fileNameToPropertiesMap.get(fileName);
			if(props != null) {
				property = props.getProperty(propertyName);
			}
			else {
				logger.warn("Property file " + fileName + " is missing properties.");
			}
			
			return property;
		}
		
		public boolean containsProperties(String fileName) {
			return fileNameToPropertiesMap.containsKey(fileName);
		}
	}
}
