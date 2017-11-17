package com.demo.publishCenter.services;


import com.demo.publishCenter.util.Receiver;
import com.demo.publishCenter.util.Sender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.net.Socket;

@Service
public class PublishCenter {

	private final Logger logger = LogManager.getLogger(PublishCenter.class);

	private Sender sender;
	private Receiver receiver;
	private String serverIp;
	private int serverPort;

	public void sendAndGetReply(String json) {
		try {
			Socket socket = new Socket(serverIp, serverPort);
			sender = new Sender(socket.getOutputStream());
			receiver = new Receiver(socket.getInputStream());

			byte[] secondFrame = null;
			byte[] frameBody = new byte[]{(byte) 0x00};

			int sendLimit = 0;
			do {
				if (sendLimit > 3) {
					logger.error("发布中心发送无效帧的次数超过三次，停止发送");
					break;
				}
				sendLimit++;
				sender.sendJson(json);
				byte[] frame = receiver.receiveFrame();
				int frameNum = receiver.getFrameNum(frame);
				if (frameNum == 1) {
					frameBody = receiver.parseAndUnescapeFrameBody(frame);
					if (frameBody[0] == 1) {
						secondFrame = receiver.receiveFrame();
					} else {

						continue;
					}
				} else {
					secondFrame = receiver.getSecondFrame(frame);
				}

				int receiveLimit = 0;
				while (!receiver.isValid(secondFrame)) {
					if (receiveLimit > 3) {
						logger.info("中转平台发送无效的帧的次数超过3次，停止接收");
						break;
					}
					receiveLimit++;
					logger.info("向中转平台回复发送的消息无效:");
					sender.send(sender.combineResponseFrame(new byte[]{(byte) 0x00}));
					logger.info("等待接中转平台重新发送json");
					secondFrame = receiver.receiveFrame();
				}
			} while (frameBody[0] == 0);

			if (null == secondFrame) {
				logger.info("中转平台无响应或者发送的json消息无效");
			} else {
				System.out.println("打印secondframe的值：");
				for(int i=0; i<secondFrame.length; i++){
					System.out.print(secondFrame[i] + " ");
					if((i+1) % 20 == 0){
						System.out.println();
					}
				}
				logger.info("收到的返回信息：" + new String(receiver.parseAndUnescapeFrameBody(secondFrame), "GBK"));
			}


			//关闭socket连接
			socket.shutdownInput();
			socket.shutdownOutput();

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
