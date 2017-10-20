package com.demo.publishCenter.services;

import com.demo.publishCenter.threads.SenderCallable;
import com.demo.publishCenter.util.Receiver;
import com.demo.publishCenter.util.Sender;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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

	/**
	 * 启动中转平台
	 */
	public void work() {
		try {
			ServerSocket server = new ServerSocket(5000);
			while (true) {

				Socket socket = server.accept();
				Receiver receiver = new Receiver(socket.getInputStream());
				Sender sender = new Sender(socket.getOutputStream());

				List<String> messageFromIPC = new ArrayList();
				String messgeFromCenter = receiveMessage(receiver, sender, socket.getOutputStream());
				System.out.println("从发布中心接受到消息："+ messgeFromCenter);
				List<String> messages = splitMessage(messgeFromCenter);
				ExecutorService pool = Executors.newCachedThreadPool();
				for (int i = 0; i < messages.size(); i++) {
					Future future = pool.submit(new SenderCallable(messages.get(i)));
					try {
						String json = (String) future.get();
						messageFromIPC.add(json);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				String reply = combineMessage(messageFromIPC);
				System.out.println("向发布中心回复消息："+ reply);
				sender.send(sender.combine(reply.getBytes()));
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
		while (!receiver.isValid(frame)) {
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
		if(resources == null){
			resources = new JSONArray();
		}
		for (int i = 0; i < commands.size(); i++) {
			JSONObject ipcJson = (JSONObject) commands.get(i);
			String config = "{commands:[" + ipcJson.toString() + "], resources:" + resources.toString() + "}";
			configs.add(config);
		}
		return configs;
	}

	/**
	 * 响应接受到的数据帧无效
	 */
	public void responseFrameInvalid(Sender sender, OutputStream os) {
		try {
			os.write(sender.combine(new byte[]{0x00}));
			os.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 响应数据帧有效
	 */
	public void responseFrameValid(Sender sender, OutputStream os) {
		try {
			os.write(sender.combine(new byte[]{0x01}));
			os.flush();
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
			resources = ((JSONArray) config.get("resources")).toString();
			output += command;
		}
		output += "],resources:" + resources;
		return output;
	}

}
