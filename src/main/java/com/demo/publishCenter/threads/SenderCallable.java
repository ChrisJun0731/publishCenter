package com.demo.publishCenter.threads;

import com.demo.publishCenter.util.Receiver;
import com.demo.publishCenter.util.Sender;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
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
		Socket socket = null;
		try {
			socket = new Socket();
			SocketAddress endPoint = new InetSocketAddress(ip, defaultPort);
			//设置连接超时时间为10秒
			socket.connect(endPoint, 10*1000);
			//设置读写超时时间10秒
			socket.setSoTimeout(10000);
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			sender = new Sender(os);
			receiver = new Receiver(is);
		} catch (Exception e) {
			logger.error("与IPC[" + ip + "]连接失败：" + e.getMessage());
			((JSONObject)((JSONArray)configObj.get("commands")).get(0)).replace("data", "与IPC[" + ip + "]连接失败！");
			json = configObj.toString();
		}

		if (sender != null && receiver != null) {
			try {
				int sendLimit = 0;
				byte[] frameBody = new byte[]{1};
				byte[] secondFrame;
				do {

					if (sendLimit > 3) {
						String data = "向IPC[" + ip + "]发送的消息失败次数超过3次，停止发送此消息！";
						logger.error(data);
						((JSONObject)((JSONArray)configObj.get("commands")).get(0)).replace("data", data);
						json = configObj.toString();
						break;
					}
					sendLimit++;
					sender.send(sender.combineSendFrame(config.getBytes()));
					logger.info("向IPC[" + ip + "]信息发送完毕！");
					logger.info("等待IPC[" + ip + "]响应消息否发送成功...");
					byte[] frame = receiver.receiveFrame();

					int frameNum = receiver.getFrameNum(frame);
					logger.info("IPC[" + ip + "]返回的帧数：" + frameNum + "帧长度:" + frame.length);
					if (frameNum == 1) {
						frameBody = receiver.parseAndUnescapeFrameBody(frame);
						if (frameBody[0] == 1) {
							logger.info("IPC[" + ip + "]响应消息发送成功！等待IPC发送返回的消息...");
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
							String data = "IPC[" + ip + "]发送无效消息超过3次，中转平台将停止接收此无效消息！";
							((JSONObject)((JSONArray)configObj.get("commands")).get(0)).replace("data", data);
							json = configObj.toString();
							logger.warn(data);
							break;
						}
						receiveLimit++;
						logger.info("向IPC[" + ip + "]响应消息发送失败！");
						sender.send(sender.combineResponseFrame(new byte[]{(byte) 0x00}));
						logger.info("等待接受IPC[" + ip + "]重新发送消息...");
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
				String data = "IPC[" + ip + "]返回消息超时：" + e.getMessage();
				logger.error(data);
				((JSONObject)((JSONArray)configObj.get("commands")).get(0)).replace("data", data);
				json = configObj.toString();
			} finally {
				json = json.replace("\n", "")
						.replace("\r", "")
						.replace("\t", "")
						.replace(" ", "");
				logger.info("从IPC[" + ip + "]接收到消息:" + json);

			}
		}
		return json;
	}
}
