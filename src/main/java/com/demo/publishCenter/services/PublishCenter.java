package com.demo.publishCenter.services;


import com.demo.publishCenter.util.Receiver;
import com.demo.publishCenter.util.Sender;
import org.springframework.stereotype.Service;

import java.net.Socket;

@Service
public class PublishCenter {

	private Sender sender;
	private Receiver receiver;
	private String serverIp;
	private int serverPort;

	public void sendAndGetReply(String json) {
		try {
			Socket socket = new Socket(serverIp, serverPort);
			sender = new Sender(socket.getOutputStream());
			receiver = new Receiver(socket.getInputStream());

			sender.sendJson(json);
			byte[] frame1 = receiver.receiveFrame();
			byte[] frame2 = receiver.receiveFrame();
			byte[] frameBody = receiver.parseAndUnescapeFrameBody(frame2);

			System.out.println("收到的返回信息："+ new String(frameBody));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}
}
