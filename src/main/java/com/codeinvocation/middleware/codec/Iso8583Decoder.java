package com.codeinvocation.middleware.codec;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codeinvocation.middleware.dto.CustomIsoMessage;
import com.codeinvocation.middleware.util.MessageFactoryUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class Iso8583Decoder extends ByteToMessageDecoder {
	
	private final MessageFactoryUtil messageFactory = MessageFactoryUtil.getInstance();
	private static final Logger log = LoggerFactory.getLogger(Iso8583Decoder.class);
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
		log.debug("Iso8583Decoder START");
		if (!byteBuf.isReadable()) {
			return;
		}
		byte[] bytes = new byte[byteBuf.readableBytes()];
		byteBuf.readBytes(bytes);

		log.debug("Parsing Incoming Message {}", new String(bytes));
		final CustomIsoMessage isoMessage = messageFactory.parseMessage(bytes, 0);
		
		if (isoMessage != null) {
			log.info(isoMessage.dumpField(true, ctx.channel().id().asLongText(), 
					ctx.channel().remoteAddress().toString(), 
					System.currentTimeMillis()));
			
			out.add(isoMessage);
			
		} 
		else {
			throw new RuntimeException("Can't parse ISO8583 message");
			
		}
		log.debug("Iso8583Decoder DONE");
	}

}
