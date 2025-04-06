package com.codeinvocation.middleware.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

public class IsoServer {

	private int port;
	
	private final EventLoopGroup bossLoopGroup;
	
	private final EventLoopGroup workerLoopGroup;
	
	private final ChannelGroup channelGroup;
	
	private static final Logger log = LoggerFactory.getLogger(IsoServer.class);
	
	private final ChannelHandler childHandler;
	
	public IsoServer(int port, ChannelHandler childHandler) {
		this.port = port;
		this.childHandler = childHandler;
		this.channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
		this.bossLoopGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
		this.workerLoopGroup = new MultiThreadIoEventLoopGroup(20, NioIoHandler.newFactory());
	}
	
	/**
	 * Starting Server on background thread
	 */
	public void startup() {
		Runnable runnable = () -> {
			try {
				start();
			} catch (Exception e) {
				log.error("Server Error", e);
			}
		};
		new Thread(runnable).start();
	}
	
	public void start() throws Exception {
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossLoopGroup, workerLoopGroup)
				.channel(NioServerSocketChannel.class)	
	            .handler(new LoggingHandler(LogLevel.DEBUG))
	            .childHandler(childHandler)
	            ;
			ChannelFuture ch = b.bind(port).sync();
			channelGroup.add(ch.channel());
			
		} catch (Exception e) {
			shutdown();
			throw e;
		}
	}
	
	public final void shutdown() throws Exception {
        channelGroup.close();
        bossLoopGroup.shutdownGracefully();
        workerLoopGroup.shutdownGracefully();
    }
}
