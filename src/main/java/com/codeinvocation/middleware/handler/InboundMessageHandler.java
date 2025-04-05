package com.codeinvocation.middleware.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codeinvocation.middleware.constant.InternalRC;
import com.codeinvocation.middleware.constant.MTI;
import com.codeinvocation.middleware.constant.NetworkManagementCode;
import com.codeinvocation.middleware.constant.ProcessingCode;
import com.codeinvocation.middleware.dto.CustomIsoMessage;
import com.codeinvocation.middleware.dto.TransactionContext;
import com.codeinvocation.middleware.util.MessageFactoryUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class InboundMessageHandler extends SimpleChannelInboundHandler<CustomIsoMessage> {
	
	private static final Logger log = LoggerFactory.getLogger(InboundMessageHandler.class);
	private static final MessageFactoryUtil messageFactory = MessageFactoryUtil.getInstance();
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, CustomIsoMessage requestMsg) throws Exception {
		
		TransactionContext context = new TransactionContext();
		context.setConnectionId(ctx.channel().id().asLongText());
		context.setRequestTimestamp(System.currentTimeMillis());
		context.setReqMsg(requestMsg);
		context.setRespMsg(messageFactory.createResponse(requestMsg));
		context.setClientIpAddress(ctx.channel().remoteAddress().toString());

		IMessageHandler messageHandler = getMessageHandler(requestMsg); 
		
		if (messageHandler != null) {
			try {
				messageHandler.handle(context);
				context.setResponseTimestamp(System.currentTimeMillis());
				log.info(context.getRespMsg().dumpField(false, context.getConnectionId(), 
						context.getClientIpAddress(), context.getResponseTimestamp()));
				
			} catch (Exception e) {
				log.error("Message Handler Error", e);
				context.setResponseCode(InternalRC.GENERAL_ERROR);
			}			
		} 
		else {
			log.warn("Unknown Message Handler");
			context.setResponseCode(InternalRC.INVALID_MESSAGE);
		}
		
		// Sending Response
		ctx.writeAndFlush(context.getRespMsg());
	}

	private IMessageHandler getMessageHandler(CustomIsoMessage requestMsg) {
		
		IMessageHandler messageHandler = null;
		
		int type = requestMsg.getType();
		String processingCode = requestMsg.getString(3);
		String networkManagementCode = requestMsg.getString(70);
		log.info("Getting Message Handler for MTI [{}] DE#3 [{}] DE#70 [{}]", 
				type, processingCode, networkManagementCode);
		
		if (MTI.NETWORK_MANAGEMENT.val == type) {
			if (NetworkManagementCode.ECHO.val.equals(networkManagementCode)) {
				messageHandler = EchoMessageHandler.getInstance();
			}
			else if (NetworkManagementCode.SIGN_ON.val.equals(networkManagementCode)) {
				// TODO Sign On
			}
			else if (NetworkManagementCode.SIGN_OFF.val.equals(networkManagementCode)) {
				// TODO Sign Off
			}			
		}
		else if (MTI.TRANSACTIONAL.val == type) {
			if (ProcessingCode.INQUIRY.val.equals(processingCode)) {
				// TODO Inquiry
			}
			if (ProcessingCode.PAYMENT.val.equals(processingCode)) {
				// TODO Payment
			}
		}
		else if (MTI.ADVICE.val == type) {
			// TODO Payment Advice
		}
		else if (MTI.ADVICE_REPEAT.val == type) {
			// TODO Payment Advice Repeat
		}
		else if (MTI.REVERSE.val == type) {
			// TODO Payment Reversal
		}
		return messageHandler;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("Inbound Message Handler Error", cause);
        ctx.close();
	}
}
