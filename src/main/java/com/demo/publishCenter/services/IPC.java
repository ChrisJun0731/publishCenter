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
		System.out.println("*********************************************");
		System.out.println("***************工控机开始工作******************");
		System.out.println("*********************************************");
		try {
			ServerSocket server = new ServerSocket(5005);
			while (true) {

				socket = server.accept();
				sender = new Sender(socket.getOutputStream());
				receiver = new Receiver(socket.getInputStream());

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
						//工控机发送不符合规范的json测试1(能被JSONUtils工具检测出来)
//						String response = "\"commands\":[{\"cmd\": \"0x52\", \"data\":{\"success\": 1}}], \"resources\":[]}";
						//工控机发送不符合规范的json测试2(不能被JSONUtils工具检测出来)
//						String response = "\"{commands\":[{\"cmd\": \"0x52\", \"data\":{\"success\": 1}}], resources}";
						//工控机发送正确的json测试
						String response = "{\"commands\":[{\"cmd\": \"0x52\", \"data\":{\"success\": 1}}], \"resources\":[]}";

						byte[] frameBody;
						int sendLimit = 0;
						do {
							if(sendLimit > 3){
								logger.info("工控机发送无效帧的次数超过三次，将停止发送!");
								break;
							}
							sendLimit++;
							//工控机返回错误帧格式错误
//							byte[] errorFrame = sender.combineResponseFrame(response.getBytes("GBK"));
//							System.out.println("打印工控机发送的正确的帧格式:");
//							for(int i=0; i<errorFrame.length; i++){
//								System.out.print(errorFrame[i] + " ");
//								if((i+1)%20 == 0){
//									System.out.println();
//								}
//							}
//							errorFrame[0] = (byte)0x00;
//							System.out.println("打印工控机发送的错误的帧格式:");
//							for(int i=0; i<errorFrame.length; i++){
//								System.out.print(errorFrame[i] + " ");
//								if((i+1)%20 == 0){
//									System.out.println();
//								}
//							}

//							sender.send(errorFrame);

							sender.send(sender.combineSendFrame(response.getBytes("GBK")));
							byte[] reply = receiver.receiveFrame();
							frameBody = receiver.parseAndUnescapeFrameBody(reply);
						} while (frameBody[0] == 0);
					}else{
						sender.send(sender.combineResponseFrame(new byte[]{(byte)0x00}));
					}
				} while (!receiver.isValid(frame));

				//关闭socket连接
				socket.shutdownOutput();
				socket.shutdownInput();
				socket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("*********************************************");
			System.out.println("***************工控机结束工作******************");
			System.out.println("*********************************************");
		}
	}
}
