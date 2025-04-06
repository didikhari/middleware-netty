package com.codeinvocation.middleware.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codeinvocation.middleware.config.IsoClient;

public class CommonUtil {

	private static final Logger log = LoggerFactory.getLogger(CommonUtil.class);
	
	public static void sleep(long milis) {
		try {
			Thread.sleep(milis);
		} catch (InterruptedException e) {
			log.error("Sleep Interupted", e);
		}
	}
}
