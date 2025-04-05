package com.codeinvocation.middleware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codeinvocation.middleware.codec.AsciiFrameDecoder;
import com.codeinvocation.middleware.codec.AsciiFrameEncoder;
import com.codeinvocation.middleware.codec.Iso8583Decoder;
import com.codeinvocation.middleware.codec.Iso8583Encoder;
import com.codeinvocation.middleware.dto.ConfigurationKey;
import com.codeinvocation.middleware.handler.InboundMessageHandler;
import com.codeinvocation.middleware.util.ConfigurationUtil;

import ch.qos.logback.classic.ClassicConstants;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class MiddlewareApplication {

	private static ConfigurationUtil configurationUtil = ConfigurationUtil.getInstance();
	private static final Logger log = LoggerFactory.getLogger(MiddlewareApplication.class);
	
	public static void main(String[] args) throws InterruptedException {
		System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, "config/logback.xml");
		log.info("Power UP");
		final EventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
		final EventLoopGroup workerGroup = new MultiThreadIoEventLoopGroup(20, NioIoHandler.newFactory());
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)	
	            .handler(new LoggingHandler(LogLevel.DEBUG))
//                .option(ChannelOption.SO_REUSEADDR, true)
//                .option(ChannelOption.SO_KEEPALIVE, keepAlive)
//                .option(ChannelOption.SO_BACKLOG, backlog)
				.childHandler(new ChannelInitializer<SocketChannel>() {

					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline p = ch.pipeline();
						p.addLast("frameDecoder", new AsciiFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
						p.addLast("frameEncoder", new AsciiFrameEncoder(4, false));
						p.addLast("decoder", new Iso8583Decoder());
						p.addLast("encoder", new Iso8583Encoder());
						p.addLast(new InboundMessageHandler());
					}
				})
	            ;
			String serverPort = configurationUtil.getConfig(ConfigurationKey.ASCII_SERVER_PORT, "8081");
			Channel ch = b.bind(Integer.valueOf(serverPort)).sync().channel();
			ch.closeFuture().sync();
			
		} finally {
			
			bossGroup.shutdownGracefully();	
			workerGroup.shutdownGracefully();
		}
	}
}
