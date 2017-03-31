package com.seagen.ecc.ectcps.device;

/**
 * 客户端消息处理接口,收到的消息,已经发送的消息,发送失败的消息将会分别使用该接口定义的话方法进行处理
 */
public interface ICommandHandler {
	/**
	 * 当接收到异步消息时候,调用该方法
	 * 
	 * @param remoteAddress
	 *            客户端连接的远程地址
	 * @param msg
	 *            消息内容
	 */
	public void messageReceived(String remoteAddress, Object msg);

	/**
	 * 当消息发送完成时,调用该方法
	 * 
	 * @param descAddress
	 *            客户端连接的远程地址
	 * @param msg
	 *            消息内容
	 */
	public void messageSent(String descAddress, Object msg);

	/**
	 * 
	 * @param descAddress
	 *            客户端连接的远程地址
	 * @param msg
	 *            消息内容
	 * @param error
	 *            错误信息
	 */

	public void messageSendFail(String descAddress, Object msg, String error);

}
