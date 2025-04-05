package com.codeinvocation.middleware.dto;

public enum ConfigurationKey {
	ASCII_SERVER_PORT("ascii.server.port")
	;

	private String val;
	ConfigurationKey(String key) {
		this.val = key;
	}
	public String getVal() {
		return this.val;
	}
}
