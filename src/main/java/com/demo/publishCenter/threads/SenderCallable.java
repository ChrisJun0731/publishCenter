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
		Sender sender;
		Receiver receiver;
		try {
			Socket socket = new Socket(ip, defaultPort);
			//设置超时时间5秒
			socket.setSoTimeout(5000);
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			sender = new Sender(os);
			receiver = new Receiver(is);

			int sendLimit = 0;
			byte[] frameBody = new byte[]{1};
			byte[] secondFrame;
			do {

				if (sendLimit > 3) {
					logger.error("向IPC[" + ip + "]发送的消息失败次数超过3次，停止发送此消息！");
					break;
				}
				sendLimit++;
				sender.send(sender.combineSendFrame(config.getBytes()));
				logger.info("向IPC[" + ip + "]信息发送完毕！");
				logger.info("等待IPC[" + ip + "]响应消息否发送成功。。。");
				byte[] frame = receiver.receiveFrame();

				int frameNum = receiver.getFrameNum(frame);
				logger.info("IPC[" + ip + "]返回的帧数：" + frameNum + "帧长度:" + frame.length);
				if (frameNum == 1) {
					frameBody = receiver.parseAndUnescapeFrameBody(frame);
					if (frameBody[0] == 1) {
						logger.info("IPC[" + ip + "]响应消息发送成功！等待IPC发送返回的消息。。。");
						secondFrame = receiver.receiveFrame();
						logger.info("收到IPC[" + ip + "]返回的消息。");
					} else {
						logger.info("IPC[" + ip + "]响应消息发送失败！中转平台准备重新发送消息");
						continue;
					}
				} else {
					secondFrame = receiver.getSecondFrame(frame);
				}

				//如果工控机发送的json帧不合法，则回复工控机发送失败，并重新接收
				int receiveLimit = 0;

				boolean isValid = receiver.isValid(secondFrame);
				logger.info("检查IPC[" + ip + "]返回的消息是否有效，true有效，false无效：" + isValid);
				while (!isValid) {

					if (receiveLimit > 3) {
						logger.info("IPC[" + ip + "]发送无效消息超过3次，中转平台将停止接收此无效消息！");
						break;
					}
					receiveLimit++;
					logger.info("向IPC[" + ip + "]响应消息发送失败！");
					sender.send(sender.combineResponseFrame(new byte[]{(byte) 0x00}));
					logger.info("等待接受IPC[" + ip + "]重新发送消息。。。");
					secondFrame = receiver.receiveFrame();
					logger.info("收到IPC[" + ip + "]重新发送的消息。");
					isValid = receiver.isValid(secondFrame);
					logger.info("再次检查收到的消息是否有效，true有效，false无效：" + isValid);
				}

				if (isValid) {
					logger.info("向IPC[" + ip + "]响应消息发送成功！");
					sender.send(sender.combineResponseFrame(new byte[]{(byte) 0x01}));
					json = receiver.conventFrameBodyToString(receiver.parseAndUnescapeFrameBody(secondFrame));
				}
			} while (frameBody[0] == 0);

			//关闭socket
			socket.shutdownOutput();
			socket.shutdownInput();
			socket.close();

		} catch (Exception e) {
			logger.error("与IPC[" + ip + "]建立连接失败或读写超时：" + e.getMessage());
		} finally {
			json = json.replace("\n", "")
					.replace("\r", "")
					.replace("\t", "")
					.replace(" ", "");
			logger.info("从IPC[" + ip + "]接收到消息:" + json);
			return json;
		}
	}
}
