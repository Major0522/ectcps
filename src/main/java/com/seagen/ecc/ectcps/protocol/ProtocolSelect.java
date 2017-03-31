package com.seagen.ecc.ectcps.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.nio.charset.Charset;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seagen.ecc.ectcps.ClientConfig;
import com.seagen.ecc.ectcps.handlers.CommandMessageDecoder;
import com.seagen.ecc.ectcps.handlers.CommandMessageEncoder;
import com.seagen.ecc.ectcps.handlers.MscDecoder;
import com.seagen.ecc.ectcps.handlers.MscEncoder;
import com.seagen.ecc.ectcps.util.NettyUtils;

/**
 * 自定义协议
 */

public abstract class ProtocolSelect {

	private static SelfSignedCertificate ssc = null;
	private static SslContext serverContext = null;
	private static SslContext clientContext = null;

	private static Logger log = LoggerFactory.getLogger(ProtocolSelect.class);

	public synchronized static SslContext getServerContext() {
		if (serverContext == null) {
			try {
				ssc = new SelfSignedCertificate("easyget.com");
				serverContext = SslContext.newServerContext(ssc.certificate(),
						ssc.privateKey());
			} catch (CertificateException | SSLException e) {
				log.error("serverContext获取失败", e);
			}
		}
		return serverContext;
	}

	public synchronized static SslContext getClientContext() {
		if (clientContext == null) {
			try {
				clientContext = SslContext
						.newClientContext(InsecureTrustManagerFactory.INSTANCE);
			} catch (SSLException e) {
				log.error("clientContext获取失败", e);
			}
		}
		return clientContext;
	}

	public static Charset getCharSet(int charSetType) {
		switch (charSetType) {
		case Protocol.CharSetType.UTF8:
			return NettyUtils.UTF8;
		case Protocol.CharSetType.GBK:
			return NettyUtils.GBK;
		default:
			return null;
		}
	}

	public static ByteBuf createLoginBuffer(ClientConfig clientConfig) {
		Charset charset = getCharSet(clientConfig.getCharsetType());
		byte[] passwd = clientConfig.getPasswd().getBytes(charset);
		byte[] identity = clientConfig.getIdentity().getBytes(charset);
		int datalength = 1 + 1 + 3 + 8 + passwd.length + identity.length;
		ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(datalength);
		buffer.writeByte(clientConfig.getProtocolType());// 协议
		buffer.writeByte(clientConfig.getCharsetType());// 编码
		buffer.writeLong(clientConfig.getUserId());
		buffer.writeByte(passwd.length);// 密码
		buffer.writeBytes(passwd);
		buffer.writeByte(identity.length);// key
		buffer.writeBytes(identity);
		return buffer;
	}

	public static ClientConfig readClientConfig(ByteBuf buffer)
			throws ProtocolException {
		try {
			ClientConfig config = new ClientConfig();
			config.setProtocolType(buffer.readByte());
			config.setCharsetType(buffer.readByte());
			Charset charset = getCharSet(config.getCharsetType());
			config.setUserId(buffer.readLong());
			config.setPasswd(NettyUtils.readByteString(buffer, charset));
			config.setIdentity(NettyUtils.readByteString(buffer, charset));
			return config;
		} catch (Exception e) {
			throw new ProtocolException(e.getMessage());
		}
	}

	public static boolean applyProtocol(Channel channel, ClientConfig config) {
		return applyProtocol(channel, config, true);
	}

	public static boolean applyProtocol(Channel channel, ClientConfig config,
			boolean client) {
		NettyUtils.clearHandlers(channel);// 清除初始handler,选择协议
		int protocolType = config.getProtocolType();
		Charset charset = getCharSet(config.getCharsetType());
		switch (protocolType) {
		case Protocol.ProtocolType.STRING: {// 字符串
			channel.pipeline().addLast("lengthFieldPrepender",
					new LengthFieldPrepender(4));
			channel.pipeline().addLast(
					"lengthFieldBasedFrameDecoder",
					new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4,
							0, 4));
			channel.pipeline().addLast("stringDecoder",
					new StringDecoder(charset));
			channel.pipeline().addLast("stringEncoder",
					new StringEncoder(charset));
			break;
		}
		case Protocol.ProtocolType.STRING_COMPRESS: {// 压缩字符串
			channel.pipeline().addLast("deflater",
					ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP));
			channel.pipeline().addLast("inflater",
					ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP));
			channel.pipeline().addLast("lengthFieldPrepender",
					new LengthFieldPrepender(4));
			channel.pipeline().addLast(
					"lengthFieldBasedFrameDecoder",
					new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4,
							0, 4));
			channel.pipeline().addLast("stringDecoder",
					new StringDecoder(charset));
			channel.pipeline().addLast("stringEncoder",
					new StringEncoder(charset));
			break;
		}
		case Protocol.ProtocolType.STRING_SSL: {// 加密字符串
		}
		case Protocol.ProtocolType.MCS: {// 模块控制通信
			channel.pipeline().addLast("MscDecoder", new MscDecoder(charset));
			channel.pipeline().addLast("MscEncoder", new MscEncoder(charset));
			break;
		}
		case Protocol.ProtocolType.COMMAND: {
			channel.pipeline().addLast("lengthFieldPrepender",
					new LengthFieldPrepender(4));
			channel.pipeline().addLast(
					"lengthFieldBasedFrameDecoder",
					new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4,
							0, 4));
			channel.pipeline().addLast("CommandMessageDecoder",
					new CommandMessageDecoder());
			channel.pipeline().addLast("CommandMessageEncoder",
					new CommandMessageEncoder());
			break;
		}
		case Protocol.ProtocolType.COMMAND_COMPRESS: {
			channel.pipeline().addLast("deflater",
					ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP));
			channel.pipeline().addLast("inflater",
					ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP));
			channel.pipeline().addLast("lengthFieldPrepender",
					new LengthFieldPrepender(4));
			channel.pipeline().addLast(
					"lengthFieldBasedFrameDecoder",
					new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4,
							0, 4));
			channel.pipeline().addLast("CommandMessageDecoder",
					new CommandMessageDecoder());
			channel.pipeline().addLast("CommandMessageEncoder",
					new CommandMessageEncoder());
			break;
		}
		case Protocol.ProtocolType.COMMAND_SSL: {
			if (client) {
				channel.pipeline().addLast("ssl",
						getClientContext().newHandler(channel.alloc()));
			} else {
				channel.pipeline().addLast("ssl",
						getServerContext().newHandler(channel.alloc()));
			}
			channel.pipeline().addLast("deflater",
					ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP));
			channel.pipeline().addLast("inflater",
					ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP));
			channel.pipeline().addLast("lengthFieldPrepender",
					new LengthFieldPrepender(4));
			channel.pipeline().addLast(
					"lengthFieldBasedFrameDecoder",
					new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4,
							0, 4));
			channel.pipeline().addLast("CommandMessageDecoder",
					new CommandMessageDecoder());
			channel.pipeline().addLast("CommandMessageEncoder",
					new CommandMessageEncoder());
			break;
		}
		case Protocol.ProtocolType.COMMAND_SSL_SIMPLE: {
			if (client) {
				channel.pipeline().addLast("ssl",
						getClientContext().newHandler(channel.alloc()));
			} else {
				channel.pipeline().addLast("ssl",
						getServerContext().newHandler(channel.alloc()));
			}
			channel.pipeline().addLast("lengthFieldPrepender",
					new LengthFieldPrepender(4));
			channel.pipeline().addLast(
					"lengthFieldBasedFrameDecoder",
					new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4,
							0, 4));
			channel.pipeline().addLast("CommandMessageDecoder",
					new CommandMessageDecoder());
			channel.pipeline().addLast("CommandMessageEncoder",
					new CommandMessageEncoder());
			break;
		}
		default: {
			log.error("not surported protocolType:" + protocolType);
			return false;
		}
		}
		return true;
	}
}
