package com.seagen.ecc.ectcps;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.seagen.ecc.common.Options;
import com.seagen.ecc.ectcps.device.DefaultConnection;
import com.seagen.ecc.ectcps.device.webSocket.WebSocketConnection;
import com.seagen.ecc.ectcps.handlers.ClientHandler;
import com.seagen.ecc.ectcps.handlers.WebSocketDecoder;
import com.seagen.ecc.ectcps.handlers.WebSocketEncoder;
import com.seagen.ecc.utils.StringUtils;

/**
 * netty客户端,用于获取连接
 * 
 * @author kuangjianbo
 * 
 */
public class NettyClient {
	final Logger log = LoggerFactory.getLogger(NettyClient.class);
	private static EventLoopGroup group = null;
	private static NettyClient INSTANCE = null;

	public static synchronized NettyClient getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new NettyClient();
		}
		return INSTANCE;
	}

	private NettyClient() {
		group = new NioEventLoopGroup();
		log.info("new Instance of NettyClient");
	}

	public static void destory() {
		INSTANCE = null;
		if (group != null) {
			group.shutdownGracefully();
		}

	}

	/**
	 * 解析通信服务器地址<br>
	 * 当域名解析失败时使用默认IP
	 * 
	 * @param serverHost
	 * @param serverPort
	 * @return
	 */
	private SocketAddress createSocketAddress(String serverHost, int serverPort) {
		InetSocketAddress ret = null;
		try {
			InetAddress addr = InetAddress.getByName(serverHost);
			ret = new InetSocketAddress(addr, serverPort);
		} catch (UnknownHostException e) {
			log.error("域名解析失败");
			String defaultCsIp = Options.get("ectcps.defaultCsIp");
			if (!StringUtils.isEmpty(defaultCsIp)) {
				log.info("尝试默认IP地址:" + defaultCsIp);
				serverHost = defaultCsIp;
			}
			ret = new InetSocketAddress(serverHost, serverPort);
		}
		return ret;
	}

	/**
	 * 根据特定的配置获取一个TCP连接
	 */
	public void createChannel(final DefaultConnection connection) {
		Channel channel = null;
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group).channel(NioSocketChannel.class)
					.option(ChannelOption.TCP_NODELAY, true)
					.option(ChannelOption.SO_KEEPALIVE, true);
			connection.setLogined(false);
			ClientConfig clientConfig = connection.getClientConfig();
			SocketAddress socketAddress = createSocketAddress(
					clientConfig.getServerIp(), clientConfig.getServerPort());
			log.info(clientConfig.getUserId() + " connect to " + socketAddress);
			bootstrap.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new ClientHandler(connection));
				}
			});
			channel = bootstrap.connect(socketAddress).syncUninterruptibly()
					.channel();
			connection.setChannel(channel);
		} catch (Exception e) {
			log.error("连接失败," + e.getClass().getSimpleName() + ",message="
					+ e.getMessage());
			if (channel != null) {
				try {
					channel.close();
				} catch (Exception e1) {
				}
			}
		}
	}

	/**
	 * 根据特定的配置获取一个TCP连接
	 */
	public void createWebsocketChannel(final WebSocketConnection connection) {
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group).channel(NioSocketChannel.class)
					.option(ChannelOption.TCP_NODELAY, true)
					.option(ChannelOption.SO_KEEPALIVE, true);
			connection.setLogined(false);
			final ClientConfig clientConfig = connection.getClientConfig();
			URI uri = new URI(clientConfig.getUri());
			SocketAddress socketAddress = createSocketAddress(uri.getHost(),
					uri.getPort());
			log.info(clientConfig.getUserId() + " connect to " + socketAddress);
			bootstrap.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast("http-codec", new HttpClientCodec());
					ch.pipeline().addLast("aggregator",
							new HttpObjectAggregator(8192));
					ch.pipeline().addLast("webSocketEncoder",
							new WebSocketEncoder());
					ch.pipeline().addLast("webSocketDecoder",
							new WebSocketDecoder(connection));
				}
			});
			Channel channel = bootstrap.connect(socketAddress)
					.syncUninterruptibly().channel();
			connection.setChannel(channel);
		} catch (Exception e) {
			log.error("连接失败," + e.getClass().getSimpleName() + ",message="
					+ e.getMessage());
		}
	}
}
