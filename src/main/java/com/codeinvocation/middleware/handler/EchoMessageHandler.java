package com.codeinvocation.middleware.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codeinvocation.middleware.constant.InternalRC;
import com.codeinvocation.middleware.dto.TransactionContext;

public class EchoMessageHandler implements IMessageHandler {

	private EchoMessageHandler() {}
	private static final EchoMessageHandler INSTANCE = new EchoMessageHandler();
	public static EchoMessageHandler getInstance() {
		return INSTANCE;
	}
	
	private static final Logger log = LoggerFactory.getLogger(EchoMessageHandler.class);
	
	@Override
	public void handle(TransactionContext ctx) throws Exception {
		try {
			log.info("Echo Handler Start");
			ctx.setResponseCode(InternalRC.SUCCESS);
			
		} catch (Exception e) {
			log.error("Echo Handler Error", e);
			
		} finally {
			log.info("Echo Handler Done");
		}
	}

}
