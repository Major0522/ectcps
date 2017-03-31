package com.seagen.ecc.ectcps.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyUtils {
	private static final Logger log = LoggerFactory.getLogger(NettyUtils.class);
	public static final Charset UTF8 = Charset.forName("UTF-8");
	public static final Charset GBK = Charset.forName("GBK");
	public static final Charset DEFAULT_CHARSET = Charset.defaultCharset();

	/**
	 * 清除channel中的所有handler
	 * 
	 * @param channel
	 */
	public static void clearHandlers(Channel channel) {
		ChannelPipeline pipeline = channel.pipeline();
		ChannelHandler handler;
		while ((handler = pipeline.last()) != null) {
			log.debug("remove handler:" + handler.getClass().getName());
			pipeline.remove(handler);
		}
	}

	public static void writeString(ByteBuf buf, Charset charset, String... strs) {
		if (charset == null) {
			charset = DEFAULT_CHARSET;
		}
		for (int i = 0; i < strs.length; i++) {
			if (strs[i] == null) {
				buf.writeInt(-1);
				continue;
			}
			byte[] bytes = strs[i].getBytes(charset);
			buf.writeInt(bytes.length);
			buf.writeBytes(bytes);
		}
	}

	public static void writeString(ByteBuf buf, String... strs) {
		writeString(buf, null, strs);
	}

	public static String readByteString(ByteBuf buffer, Charset charset) {
		if (charset == null) {
			charset = DEFAULT_CHARSET;
		}
		int len = buffer.readByte();
		if (len == -1) {
			return null;
		}
		byte[] buf = new byte[len];
		buffer.readBytes(buf);
		return new String(buf, charset);
	}

	public static String readByteString(ByteBuf buffer) {
		return readByteString(buffer, null);
	}

	public static void writeByteString(ByteBuf buf, Charset charset,
			String... strs) {
		if (charset == null) {
			charset = DEFAULT_CHARSET;
		}
		for (int i = 0; i < strs.length; i++) {
			if (strs[i] == null) {
				buf.writeByte(-1);
				continue;
			}
			byte[] bytes = strs[i].getBytes(charset);
			buf.writeByte(bytes.length);
			buf.writeBytes(bytes);
		}
	}

	public static void writeByteString(ByteBuf buf, String... strs) {
		writeString(buf, null, strs);
	}

	public static String readString(ByteBuf buffer, Charset charset) {
		if (charset == null) {
			charset = DEFAULT_CHARSET;
		}
		int len = buffer.readInt();
		if (len == -1) {
			return null;
		}
		byte[] buf = new byte[len];
		buffer.readBytes(buf);
		return new String(buf, charset);
	}

	public static String readString(ByteBuf buffer) {
		return readString(buffer, null);
	}
}
