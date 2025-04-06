package com.codeinvocation.middleware.config;

import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codeinvocation.middleware.codec.AsciiFrameDecoder;
import com.codeinvocation.middleware.codec.AsciiFrameEncoder;
import com.codeinvocation.middleware.codec.Iso8583Decoder;
import com.codeinvocation.middleware.codec.Iso8583Encoder;
import com.codeinvocation.middleware.dto.CustomIsoMessage;
import com.codeinvocation.middleware.util.CommonUtil;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;


public class IsoClient {

	private final String host;
	
	private final int port;
	
	private final long reconnectDelay;
	
	private final EventLoopGroup workGroup;
	
	private final Timer timer;
	
	private Channel channel;

    private final Bootstrap bootstrap;
    
	private static final Logger log = LoggerFactory.getLogger(IsoClient.class);
    
	private final Map<String, CustomIsoMessage> correlationMap = new ConcurrentHashMap<>();
	
	private boolean isShutdown = false;
	
	public IsoClient(String host, int port, long reconnectDelay) {
		this.host = host;
		this.port = port;
		this.reconnectDelay = reconnectDelay;
		this.timer = new Timer();
		this.bootstrap = new Bootstrap();
		this.workGroup = new MultiThreadIoEventLoopGroup(20, NioIoHandler.newFactory());
		bootstrap.group(workGroup);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline p = ch.pipeline();
				p.addLast("frameDecoder", new AsciiFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
				p.addLast("decoder", new Iso8583Decoder());
				p.addLast("frameEncoder", new AsciiFrameEncoder(4, false));
				p.addLast("encoder", new Iso8583Encoder());
				p.addLast(new SimpleChannelInboundHandler<CustomIsoMessage>() {

					@Override
					protected void channelRead0(ChannelHandlerContext ctx, CustomIsoMessage msg) throws Exception {
						log.debug("Putting {} into correlation map", msg.getMessageKey());
						correlationMap.put(msg.getMessageKey(), msg);
					}
					
				});
			}
			
		});
	}
	
	public void connect() {
		try{
			log.info("Connecting to {}:{}", this.host, this.port);
			ChannelFuture channelFuture = bootstrap.connect(this.host, this.port);
			channelFuture.addListener(new ChannelFutureListener() {
				
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if( !future.isSuccess() ) {
						future.channel().close();
						bootstrap.connect(host, port).addListener(this);
						
					} else {
						channel = future.channel();
						log.info("Connection to {}:{} Established", host, port);
						channel.closeFuture().addListener(new ChannelFutureListener() {							
							@Override
							public void operationComplete(ChannelFuture future) throws Exception {
								log.warn("Connection to {}:{} Lost", host, port);
								if (!isShutdown)
									reconnect( reconnectDelay );
								else
									log.warn("Shutting Channel Down");
							}
						});
					}
				}
				
			});
        } catch (Exception e) {
        	log.error("Connection not Established", e);
        	if (!isShutdown)
				reconnect( reconnectDelay );
		}
	}
	
	private void reconnect( long delay ) {
		log.info("Reconnecting in {} milis", delay);
		timer.schedule( new TimerTask() {
			@Override
			public void run() {
				if ((channel == null || !channel.isActive()) 
						&& !isShutdown)
					connect();
			}
		}, delay );
	}
	
	public CustomIsoMessage sendAndReceive(CustomIsoMessage requestMsg, long timeout) throws IOException {
		sendRequest(requestMsg);
		long start = System.currentTimeMillis();
		while (!correlationMap.containsKey(requestMsg.getMessageKey()) 
				&& (System.currentTimeMillis() - start < timeout) ) {
			log.debug("Getting {} from correlation map", requestMsg.getMessageKey());
			CommonUtil.sleep(100);
		}
		
		return correlationMap.get(requestMsg.getMessageKey());
	}
	
	private synchronized void sendRequest(CustomIsoMessage requestMsg) throws IOException {
		if (channel == null) {
			reconnect(5);
			// wait until 10s
			int counter = 1;
			while ( (channel == null || !channel.isActive()) && counter <= 20) {
				CommonUtil.sleep(500l);
				counter++;
			}
		}
		
		if( channel != null && channel.isActive() ) {
			channel.writeAndFlush( requestMsg );
			
		} else {
			throw new IOException("Can't send message to inactive connection");
		}
	}
	
	/**
     *	Shutdown a client 
     */
    public void shutdown(){
        try {
        	isShutdown = true;
            workGroup.shutdownGracefully().addListener(e -> {log.info("Shutdown Completed");});
			channel.closeFuture().sync();
            timer.cancel();
            timer.purge();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
}
