package com.codeinvocation.middleware.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.codeinvocation.middleware.dto.ConfigurationKey;

public class ConfigurationUtil {
	private static final String PATH = "config/application.properties";
	private Properties props;
	private ConfigurationUtil() {
		props = new Properties();
		try {
			props.load(new FileInputStream(PATH));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	};
	private static final ConfigurationUtil INSTANCE = new ConfigurationUtil();
	public static ConfigurationUtil getInstance() {
		return INSTANCE;
	}
	
	public String getConfig(ConfigurationKey key) {
		return getConfig(key, null);
	}
	
	public String getConfig(ConfigurationKey key, String defaultVal) {
		return props.getProperty(key.getVal(), defaultVal);
	}
}
