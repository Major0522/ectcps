package com.seagen.ecc.ectcps;

import com.seagen.ecc.ectcps.protocol.Protocol;
import com.seagen.ecc.ectcps.protocol.Protocol.ProtocolType;

/**
 * 通信客户端配置
 * 
 * @author kuangjianbo
 * 
 */
public class ClientConfig {
	private String clientName = "test";// 客户端名字
	private String serverIp = "127.0.0.1";// 服务器地址
	private int serverPort = 18090;// 服务器端口
	private int timeout = 60;// 心跳时间
	private int protocolType = ProtocolType.COMMAND;// 协议类型
	private int charsetType = Protocol.CharSetType.UTF8;// 字符编码
	private long userId = 99;// 通信用户ID
	private String passwd = "passwd";// 密码
	private String identity = "";// 认证字符串
	private boolean needLogin = true;// 是否需要登录
	private int roleType = 1;// 角色类型(1终端,2运营服务器,3运维服务器,4会员网站,5售货柜运营服务器)
	private int clearTimeOut = 3 * 60 * 1000;// 消息清理超时时间
	private String uri;// 连接地址,Websocket或者mq地址
	private String sendQueue = "terminal";// 发送消息队列
	private String receiveQueue = "backbend";// 接收消息队列

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getCharsetType() {
		return charsetType;
	}

	public void setCharsetType(int charsetType) {
		this.charsetType = charsetType;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public int getProtocolType() {
		return protocolType;
	}

	public void setProtocolType(int protocolType) {
		this.protocolType = protocolType;
	}

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public boolean isNeedLogin() {
		return needLogin;
	}

	public void setNeedLogin(boolean needLogin) {
		this.needLogin = needLogin;
	}

	public int getRoleType() {
		return roleType;
	}

	public void setRoleType(int roleType) {
		this.roleType = roleType;
	}

	public int getClearTimeOut() {
		return clearTimeOut;
	}

	public void setClearTimeOut(int clearTimeOut) {
		this.clearTimeOut = clearTimeOut;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getSendQueue() {
		return sendQueue;
	}

	public void setSendQueue(String sendQueue) {
		this.sendQueue = sendQueue;
	}

	public String getReceiveQueue() {
		return receiveQueue;
	}

	public void setReceiveQueue(String receiveQueue) {
		this.receiveQueue = receiveQueue;
	}

	@Override
	public String toString() {
		return "ClientConfig [clientName=" + clientName + ", serverIp="
				+ serverIp + ", serverPort=" + serverPort + ", timeout="
				+ timeout + ", protocolType=" + protocolType + ", charsetType="
				+ charsetType + ", userId=" + userId + ", passwd=" + passwd
				+ ", identity=" + identity + ", needLogin=" + needLogin
				+ ", roleType=" + roleType + ", clearTimeOut=" + clearTimeOut
				+ ", uri=" + uri + ", sendQueue=" + sendQueue
				+ ", receiveQueue=" + receiveQueue + "]";
	}
}
