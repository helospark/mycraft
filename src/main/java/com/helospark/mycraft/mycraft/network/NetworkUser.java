package com.helospark.mycraft.mycraft.network;

import java.net.InetAddress;

public class NetworkUser {
	private InetAddress address;
	private int port;
	private String stringAddress;

	public NetworkUser(InetAddress address, int port) {
		this.address = address;
		this.port = port;
		this.stringAddress = address.getHostAddress().toString();
	}

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean equals(NetworkUser user) {
		return (port == user.port && user.getAddress().getHostAddress()
				.toString().equals(stringAddress));
	}

}
