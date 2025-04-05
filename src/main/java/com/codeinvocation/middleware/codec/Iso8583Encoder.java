package com.codeinvocation.middleware.codec;

import com.solab.iso8583.IsoMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class Iso8583Encoder extends MessageToByteEncoder<IsoMessage> {

	@Override
	protected void encode(ChannelHandlerContext ctx, IsoMessage isoMessage, ByteBuf out) throws Exception {
		byte[] byteBuffer = isoMessage.writeData();
        out.writeBytes(byteBuffer);
	}

}
