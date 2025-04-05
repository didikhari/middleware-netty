package com.codeinvocation.middleware.handler;

import com.codeinvocation.middleware.dto.TransactionContext;

public interface IMessageHandler {

	public void handle(TransactionContext ctx) throws Exception;
}
