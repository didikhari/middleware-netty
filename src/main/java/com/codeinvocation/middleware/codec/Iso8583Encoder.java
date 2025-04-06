package com.codeinvocation.middleware.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codeinvocation.middleware.dto.CustomIsoMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class Iso8583Encoder extends MessageToByteEncoder<CustomIsoMessage> {

	private static final Logger log = LoggerFactory.getLogger(Iso8583Encoder.class);
	
	@Override
	protected void encode(ChannelHandlerContext ctx, CustomIsoMessage isoMessage, ByteBuf out) throws Exception {
		log.info(isoMessage.dumpField(false, ctx.channel().id().asLongText(), 
				ctx.channel().remoteAddress().toString(), 
				System.currentTimeMillis()));
		
		byte[] byteBuffer = isoMessage.writeData();
        out.writeBytes(byteBuffer);
	}

}
