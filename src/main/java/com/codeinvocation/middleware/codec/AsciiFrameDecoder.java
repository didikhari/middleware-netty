package com.codeinvocation.middleware.codec;

import java.nio.ByteOrder;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.CharsetUtil;

public class AsciiFrameDecoder extends LengthFieldBasedFrameDecoder {

	public AsciiFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment,
			int initialBytesToStrip) {
		super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, true);
	}

	@Override
	protected long getUnadjustedFrameLength(ByteBuf buf, int offset, int length, ByteOrder order) {
		buf = buf.order(order);
		byte[] lengthBytes = new byte[length];
		buf.getBytes(offset, lengthBytes);
		String s = new String(lengthBytes, CharsetUtil.US_ASCII);
		return Long.parseLong(s);
	}
}
