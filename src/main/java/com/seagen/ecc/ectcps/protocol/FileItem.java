package com.seagen.ecc.ectcps.protocol;

import com.seagen.ecc.utils.DateUtils;

public class FileItem {
	/**
	 * 当前请求/响应的序列号
	 */
	private long serialNo;
	/**
	 * 上传/下载的文件全路径,如 "d:/test.zip"
	 */
	private String fullPath;
	/**
	 * 上传/下载请求的地址
	 */
	private String url;
	/**
	 * 上传响应
	 */
	private String text;

	public FileItem() {
		this.serialNo = DateUtils.getSerialNo();
	}

	public FileItem(String fullPath) {
		this();
		this.fullPath = fullPath;
	}

	public FileItem(String fullPath, String url) {
		this();
		this.fullPath = fullPath;
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(long serialNo) {
		this.serialNo = serialNo;
	}

	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
