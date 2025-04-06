package com.codeinvocation.middleware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codeinvocation.middleware.codec.AsciiFrameDecoder;
import com.codeinvocation.middleware.codec.AsciiFrameEncoder;
import com.codeinvocation.middleware.codec.Iso8583Decoder;
import com.codeinvocation.middleware.codec.Iso8583Encoder;
import com.codeinvocation.middleware.config.IsoServer;
import com.codeinvocation.middleware.dto.ConfigurationKey;
import com.codeinvocation.middleware.handler.InboundMessageHandler;
import com.codeinvocation.middleware.util.ConfigurationUtil;

import ch.qos.logback.classic.ClassicConstants;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class MiddlewareApplication {

	private static ConfigurationUtil configurationUtil = ConfigurationUtil.getInstance();
	private static final Logger log = LoggerFactory.getLogger(MiddlewareApplication.class);
	
	public static void main(String[] args) throws InterruptedException {
		System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, "config/logback.xml");
		log.info("Power UP");
		String serverPort = configurationUtil.getConfig(ConfigurationKey.ASCII_SERVER_PORT, "8081");
		
		/*
		 * ISO Server
		 */
		
		IsoServer isoServer = new IsoServer(Integer.valueOf(serverPort), new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline p = ch.pipeline();
				p.addLast("frameDecoder", new AsciiFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
				p.addLast("decoder", new Iso8583Decoder());
				p.addLast("frameEncoder", new AsciiFrameEncoder(4, false));
				p.addLast("encoder", new Iso8583Encoder());
				p.addLast(new InboundMessageHandler());
			}
		});
		isoServer.startup();
		
	}
}
