package com.codeinvocation.middleware.codec;

import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

import java.nio.ByteOrder;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldPrepender;

public class AsciiFrameEncoder extends LengthFieldPrepender {

	private final ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
    private final int lengthFieldLength;
    private final boolean lengthIncludesLengthFieldLength;
    
	public AsciiFrameEncoder(int lengthFieldLength, 
			boolean lengthIncludesLengthFieldLength) {
		super(lengthFieldLength, lengthIncludesLengthFieldLength);
		this.lengthFieldLength = lengthFieldLength;
		this.lengthIncludesLengthFieldLength = lengthIncludesLengthFieldLength;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        int length = msg.readableBytes();
        if (lengthIncludesLengthFieldLength) {
            length += lengthFieldLength;
        }

        checkPositiveOrZero(length, "length");
        String lenStr = StringUtils.leftPad(String.valueOf(length), lengthFieldLength, '0');
        out.add(ctx.alloc().buffer(lengthFieldLength).order(byteOrder).writeBytes(lenStr.getBytes()));
        out.add(msg.retain());
    }
}
