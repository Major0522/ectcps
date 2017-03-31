package com.seagen.ecc.ectcps.device.mcs;

import com.seagen.ecc.ectcps.ClientConfig;
import com.seagen.ecc.ectcps.device.DefaultConnection;

public class McsConnection extends DefaultConnection {
	private McsDevice device;

	public McsConnection(ClientConfig clientConfig) {
		super(clientConfig);
	}

	public McsConnection(ClientConfig clientConfig, McsDevice device) {
		super(clientConfig);
		this.device = device;
	}

	@Override
	public void close() {
		super.close();
		device.clearMessage();
	}
}
