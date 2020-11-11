package com.bit.flipitFinal;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

// 사용자 2
public class ClientF2 {

	public static void main(String[] args) {
		String address = "127.0.0.1";
		int port = 6030;
		Socket socket = null;
		
		try {
			socket = new Socket(address, port);
			if(socket.isConnected()) {
				new ClientApp(socket);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
