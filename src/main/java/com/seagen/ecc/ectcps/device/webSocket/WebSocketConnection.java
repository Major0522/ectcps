package com.seagen.ecc.ectcps.device.webSocket;

import com.seagen.ecc.ectcps.ClientConfig;
import com.seagen.ecc.ectcps.NettyClient;
import com.seagen.ecc.ectcps.device.DefaultConnection;

/**
 * 当前连接/会话
 * 
 * @author kuangjianbo
 * 
 */
public class WebSocketConnection extends DefaultConnection {

	public WebSocketConnection(ClientConfig clientConfig) {
		super(clientConfig);
	}

	@Override
	public void setLogined(boolean isLogined) {
		this.isLogined = true;
	}

	@Override
	public synchronized void connect() {
		if (!isConnected()) {
			try {
				int wait = connectCount.getAndAdd(1);
				if (wait > 0) {
					log.info(clientConfig.getClientName() + ":连接频繁,等待" + wait
							+ "秒");
					Thread.sleep(wait % waitSeconds * 1000);
				}
			} catch (InterruptedException e) {
			}
			NettyClient.getInstance().createWebsocketChannel(this);
		} else {
			log.info(clientConfig.getClientName() + ":isConnected!!!");
		}
	}
}
