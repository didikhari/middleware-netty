package com.codeinvocation.middleware.util;

import java.io.File;
import java.io.IOException;

import com.codeinvocation.middleware.dto.CustomIsoMessage;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.parse.ConfigParser;

public class MessageFactoryUtil extends MessageFactory<CustomIsoMessage> {

	private MessageFactoryUtil(){
		try {
			log.debug("Configuring Packager");
			setAssignDate(true);
			ConfigParser.configureFromUrl(this, new File("config/j8583.xml").toURI().toURL());
		} catch (IOException e) {
			log.error("Failed To Create Decoder", e);
		}
	}
	
	private static final MessageFactoryUtil INSTANCE = new MessageFactoryUtil();
	public static MessageFactoryUtil getInstance() {
		return INSTANCE;
	}
	
	@Override
	protected CustomIsoMessage createIsoMessage(String header) {
		return new CustomIsoMessage(header);
	}
}
