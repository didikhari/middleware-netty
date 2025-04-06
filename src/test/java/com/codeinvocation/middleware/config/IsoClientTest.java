package com.codeinvocation.middleware.config;

import java.util.ArrayList;
import java.util.List;import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import com.codeinvocation.middleware.dto.ConfigurationKey;
import com.codeinvocation.middleware.dto.CustomIsoMessage;
import com.codeinvocation.middleware.util.CommonUtil;
import com.codeinvocation.middleware.util.ConfigurationUtil;
import com.codeinvocation.middleware.util.MessageFactoryUtil;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;

public class IsoClientTest {

	private static ConfigurationUtil configurationUtil = ConfigurationUtil.getInstance();
	private static ExecutorService executorService = Executors.newFixedThreadPool(2);
	
	public static void main(String[] args) throws InterruptedException {
		String serverPort = configurationUtil.getConfig(ConfigurationKey.ASCII_SERVER_PORT, "8081");		
		MessageFactoryUtil messageFactory = MessageFactoryUtil.getInstance();
		
		IsoClient isoClient = new IsoClient("localhost", Integer.valueOf(serverPort), 5000);

		AtomicInteger stan = new AtomicInteger();
		List<Callable<Void>> tasks = new ArrayList<>();
		for (int i = 0; i < 1; i++) {
			tasks.add(() -> {
				sendRequest(messageFactory, isoClient, stan);
				return null;
			});
		}
		List<Future<Void>> invokeAll = executorService.invokeAll(tasks);
		while (invokeAll.stream().filter(v -> !v.isDone()).findAny().isPresent()) {
			CommonUtil.sleep(1000);
		}
		isoClient.shutdown();
	}

	private static void sendRequest(MessageFactoryUtil messageFactory, IsoClient isoClient, AtomicInteger stan) {
		CustomIsoMessage requestMsg = messageFactory.newMessage(0x800);
		requestMsg.setField(11, new IsoValue<Integer>(IsoType.NUMERIC, stan.addAndGet(1), 6));
		requestMsg.setField(41, new IsoValue<String>(IsoType.ALPHA, "12345678", 8));
		requestMsg.setField(70, new IsoValue<String>(IsoType.ALPHA, "301", 3));
		try {
			CustomIsoMessage responseMsg = isoClient.sendAndReceive(requestMsg, 30000);
			if (responseMsg == null)
				throw new TimeoutException("Timeout for "+requestMsg.getMessageKey());
			
			if (!requestMsg.getMessageKey().equals(responseMsg.getMessageKey()))
				throw new Exception("Invalid Correlation");
			
		} catch (TimeoutException e) {
			System.out.println(e.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
