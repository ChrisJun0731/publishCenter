package com.demo.publishCenter.services;

import com.demo.publishCenter.threads.SenderCallable;
import com.demo.publishCenter.util.Receiver;
import com.demo.publishCenter.util.Sender;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MiddlePlatform {

	private final Logger logger = LogManager.getLogger(MiddlePlatform.class);
	/**
	 * 启动中转平台
	 */
	public void work() {

		logger.info("中转平台已启动，等待来自发布中心的消息。。。");
		Sender sender = null;
		Receiver receiver = null;
		try {
			ServerSocket server = new ServerSocket(5000);
			while (true) {

				System.out.println("**********************************************************************");
				System.out.println("************************中转平台开启一个线程*********************************");
				System.out.println("**********************************************************************");

				Socket socket = server.accept();
				receiver = new Receiver(socket.getInputStream());
				sender = new Sender(socket.getOutputStream());

				List<String> messageFromIPC = new ArrayList();
				String messageFromCenter = receiveMessage(receiver, sender, socket.getOutputStream());
				logger.info("打印从发布中心接收到的消息：" + messageFromCenter);
				if(messageFromCenter.equals("")){
					logger.info("发布中心发送的消息为空或无效，等待下一次的消息。");
					continue;
				}

				boolean isValidJSON = JSONUtils.mayBeJSON(messageFromCenter);
				if(!isValidJSON){
					logger.error("这并不是一个合法的JSON，该消息将不会被发送到工控机！");
				}else{
					List<String> messages = splitMessage(messageFromCenter);
					ExecutorService pool = Executors.newCachedThreadPool();
					for (int i = 0; i < messages.size(); i++) {
						Future future = pool.submit(new SenderCallable(messages.get(i)));
						try {
							String json = (String) future.get();
							json = json.replace("\n", "")
									.replace("\r", "")
									.replace("\t", "")
									.replace(" ", "");
							logger.info("打印从IPC返回的消息:" + json);
							boolean isValidJson = JSONUtils.mayBeJSON(json);
							if (!isValidJson) {
								logger.info("此返回消息是非法JSON，不会被添加到向发布中心返回消息中！");
								continue;
							}
							messageFromIPC.add(json);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					String reply = "";
					if (messageFromIPC.size() != 0) {
						reply = combineMessage(messageFromIPC);
					} else {
						reply = "工控机端没有合法消息返回或工控机无响应!";
					}
					logger.info("打印向发布中心回复的消息：" + reply);
					sender.send(sender.combineSendFrame(reply.getBytes("GBK")));
					sender.flush();
				}

				System.out.println("**********************************************************************");
				System.out.println("************************中转平台结束一个线程*****************************");
				System.out.println("**********************************************************************");
				//关闭socket连接
				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 接受字符串消息
	 *
	 * @return 消息字符串
	 */
	public String receiveMessage(Receiver receiver, Sender sender, OutputStream os) {
		String json = "";
		byte[] frame = receiver.receiveFrame();
		int validTimes = 0;
		while (!receiver.isValid(frame)) {
			if(validTimes > 3){
				logger.error("发布中心发送无效帧的次数超过了3次，将停止接收该无效帧！");
				break;
			}
			validTimes++;
			responseFrameInvalid(sender, os);
			frame = receiver.receiveFrame();
		}
		responseFrameValid(sender, os);
		byte[] unescapeFrameBody = receiver.parseAndUnescapeFrameBody(frame);
		json = receiver.conventFrameBodyToString(unescapeFrameBody);
		return json;
	}

	/**
	 * 拆分消息
	 *
	 * @param config_json 接受到的消息
	 * @return 拆分后的消息
	 */
	private List<String> splitMessage(String config_json) {
		List<String> configs = new ArrayList();
		JSONObject configObj = JSONObject.fromObject(config_json);
		JSONArray commands = (JSONArray) configObj.get("commands");
		JSONArray resources = (JSONArray) configObj.get("resources");
		if (resources == null) {
			resources = new JSONArray();
		}
		for (int i = 0; i < commands.size(); i++) {
			JSONObject ipcJson = (JSONObject) commands.get(i);
			String config = "{\"commands\":[" + ipcJson.toString() + "], \"resources\":" + resources.toString() + "}";
			configs.add(config);
		}
		return configs;
	}

	/**
	 * 响应接受到的数据帧无效
	 */
	public void responseFrameInvalid(Sender sender, OutputStream os) {
		try {
			os.write(sender.combineResponseFrame(new byte[]{0x00}));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 响应数据帧有效
	 */
	public void responseFrameValid(Sender sender, OutputStream os) {
		try {
			os.write(sender.combineResponseFrame(new byte[]{0x01}));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 将多个message组合为一个message
	 *
	 * @param messages
	 * @return
	 */
	public String combineMessage(List<String> messages) {
		String output = "{\"commands\":[";
		String resources = "";
		for (int i = 0; i < messages.size(); i++) {
			JSONObject config = JSONObject.fromObject(messages.get(i));
			String command = ((JSONObject) ((JSONArray) config.get("commands")).get(0)).toString();
			if (config.get("resources") == null) {
				resources = "[]";
			} else {
				resources = ((JSONArray) config.get("resources")).toString();
			}
			output += command + ",";
		}
		output = output.substring(0, output.length()-1);
		output += "],\"resources\":" + resources + "}";
		return output;
	}

}
