package com.seagen.ecc.ectcps.util;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ConnectTimeoutException;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seagen.ecc.ectcps.handlers.ClientHandler;

public class HandlerUtils {
	private final static Logger log = LoggerFactory
			.getLogger(ClientHandler.class);

	public static void handException(ChannelHandlerContext ctx, Throwable cause) {
		if (cause instanceof ConnectTimeoutException) {
			log.error("连接超时!!!");
		} else if (cause instanceof IOException && cause.getMessage() != null) {
			log.warn(cause.getMessage());
		} else {
			log.error("exceptionCaught", cause);
		}
	}
}
