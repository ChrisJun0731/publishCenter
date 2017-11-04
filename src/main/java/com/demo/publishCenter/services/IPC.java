package com.demo.publishCenter.services;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.demo.publishCenter.util.Receiver;
import com.demo.publishCenter.util.Sender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.net.ServerSocket;
import java.net.Socket;

public class IPC {

	private Logger logger = LogManager.getLogger(IPC.class);

	public void work() {
		Sender sender = null;
		Receiver receiver = null;
		Socket socket = null;
		try {
			ServerSocket server = new ServerSocket(5005);
			while (true) {
				socket = server.accept();
				sender = new Sender(socket.getOutputStream());
				receiver = new Receiver(socket.getInputStream());

				System.out.println("*********************************************");
				System.out.println("***************工控机开始工作******************");
				System.out.println("*********************************************");
				logger.info("工控机准备接受来自中转平台的消息！");

				byte[] frame;
				int receiveLimit = 0;
				do {
					if(receiveLimit > 3){
						logger.info("中转平台发送无效帧的次数超过3次，停止接收");
						break;
					}
					receiveLimit++;

					frame = receiver.receiveFrame();
					if (receiver.isValid(frame)) {
						sender.send(sender.combineResponseFrame(new byte[]{(byte) 0x01}));
						String response = "{\"commands\":[{\"cmd\": \"0x52\", \"data\":{\"success\": 1}}], \"resources\":[]}";
						byte[] frameBody;
						int sendLimit = 0;
						do {
							if(sendLimit > 3){
								logger.info("工控机发送无效帧的次数超过三次，将停止发送!");
								break;
							}
							sendLimit++;
							sender.send(sender.combineSendFrame(response.getBytes("GBK")));
							byte[] reply = receiver.receiveFrame();
							frameBody = receiver.parseAndUnescapeFrameBody(reply);
						} while (frameBody[0] == 0);
					}else{
						sender.send(sender.combineResponseFrame(new byte[]{(byte)0x00}));
					}
				} while (!receiver.isValid(frame));


//				byte[] frame = receiver.receiveFrame();
//				logger.info("消息接受完毕");
//				int receiveLimit = 0;
//				while (!receiver.isValid(frame)) {
//					if (receiveLimit > 3) {
//						logger.info("中转平台连续发送无效帧超过3次，停止接收");
//						break;
//
//					}
//					receiveLimit++;
//					logger.info("工控机回复发布中心消息发送失败！");
//					sender.send(sender.combineResponseFrame(new byte[]{0x00}));
//					logger.info("工控机重新等待接受中转平台的消息...");
//					frame = receiver.receiveFrame();
//					logger.info("消息接受完毕");
//				}
//				logger.info("工控机回复中转平台消息发送成功！");
//				sender.send(sender.combineResponseFrame(new byte[]{0x01}));
//
//				String request = receiver.conventFrameBodyToString(receiver.parseAndUnescapeFrameBody(frame));
//				System.out.println("从中转平台接受到消息:" + request);
//				String response = "{\"commands\":[{\"cmd\": \"0x52\", \"data\":{\"success\": 1}}], \"resources\":[]}";
//
//				byte[] frameBody;
//				do {
//					logger.info("工控机向中转平台回复消息");
//					sender.send(sender.combineSendFrame(response.getBytes()));
//					logger.info("工控机等待中转平台回复消息是否发送成功...");
//					byte[] reply = receiver.receiveFrame();
//					logger.info("工控机收到中转平台的回复！");
//					frameBody = receiver.parseAndUnescapeFrameBody(reply);
//				} while (frameBody[0] == 0x00);


				System.out.println("*********************************************");
				System.out.println("***************工控机结束工作******************");
				System.out.println("*********************************************");

				//关闭socket连接
//				socket.shutdownOutput();
//				socket.shutdownInput();
//				socket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
