package com.demo.publishCenter.threads;

import com.demo.publishCenter.util.Receiver;
import com.demo.publishCenter.util.Sender;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

/**
 * Created by Administrator on 2017/10/16.
 */
public class SenderCallable implements Callable {

	private final Logger logger = LogManager.getLogger(SenderCallable.class);

	private final int defaultPort = 5005;
	private String config;

	public SenderCallable(String config) {
		this.config = config;
	}

	public String call() {
		String json = "";
		JSONObject configObj = JSONObject.fromObject(this.config);
		JSONArray commands = (JSONArray) configObj.get("commands");
		JSONObject ipc = (JSONObject) commands.get(0);
		String ip = (String) ipc.get("ip");
		Sender sender = null;
		Receiver receiver = null;
		try {
			Socket socket = new Socket(ip, defaultPort);
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			sender = new Sender(os);
			receiver = new Receiver(is);

			int sendLimit = 0;
			byte[] frameBody = new byte[]{1};
			byte[] secondFrame;
			do {

				if (sendLimit > 3) {
					logger.error("向工控机发送的消息失败次数超过3次，停止发送此消息！");
					break;
				}
				sendLimit++;

				logger.info("开始向工控机发送信息。。。");
				logger.info("打印向工控机发送的字节:");
				byte[] sendFrame = sender.combineSendFrame(config.getBytes());
				logger.info("发送的帧的长度为:" + sendFrame.length);
				for(int i=0; i<sendFrame.length; i++){
					System.out.print(sendFrame[i] + " ");
					if((i+1) % 20==0){
						System.out.println();
					}
				}
				sender.send(sender.combineSendFrame(config.getBytes()));
				//发送一个非法的帧测试
//				sender.send(new byte[]{0x11});
				logger.info("信息发送完毕！");
				logger.info("等待工控机反馈该信息是否发送成功");
				byte[] frame = receiver.receiveFrame();
				logger.info("收到工控机的反馈信息！");
				System.out.println();
				System.out.println("收到的反馈信息的长度:"+frame.length);
				System.out.println("打印反馈信息的内容：");
				for(int i=0; i<frame.length; i++){
					System.out.print(frame[i] + " ");
					if((i+1)%10==0){
						System.out.println();
					}
				}
				System.out.println();

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

				//如果工控机发送的json帧不合法，则回复工控机发送失败，并重新接收
				int receiveLimit = 0;
				while(!receiver.isValid(secondFrame)){
					if(receiveLimit > 3){
						logger.info("工控机发送无效的帧的次数超过3次，停止接收");
						break;
					}
					receiveLimit++;
					logger.info("向工控机回复发送的消息无效:");
					sender.send(sender.combineResponseFrame(new byte[]{(byte)0x00}));
					logger.info("等待接受工控机重新发送json");
					secondFrame = receiver.receiveFrame();
				}

				//回复工控机发送成功
				logger.info("回复工控机消息发送成功！");
				sender.send(sender.combineResponseFrame(new byte[]{(byte)0x01}));
				json = receiver.conventFrameBodyToString(receiver.parseAndUnescapeFrameBody(secondFrame));
			} while (frameBody[0] == 0);

			//关闭socket
			socket.shutdownOutput();
			socket.shutdownInput();
			socket.close();

		} catch (Exception e) {
			logger.error("与工控机建立连接失败：" + e.getMessage());
		}

		return json;
	}
}
