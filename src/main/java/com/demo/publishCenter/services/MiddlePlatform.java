package com.demo.publishCenter.services;

import com.demo.publishCenter.threads.SenderCallable;
import com.demo.publishCenter.util.Receiver;
import com.demo.publishCenter.util.Sender;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
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
	private ServerSocket server = null;
	/**
	 * 启动中转平台
	 */
	public void work() {

		logger.info("中转平台已启动！");
		try {
			server = new ServerSocket(5000);
		} catch (IOException e) {
			logger.error("请确认5000的端口号是否被占用! "+ e.getMessage());
		}
		createSockets(server);
	}

	public void createSockets(ServerSocket server){
		try{
			while (true) {
				Socket socket = server.accept();
				//设置读写超时时间为10秒
				socket.setSoTimeout(10000);

				System.out.println("**********************************************************************");
				System.out.println("************************中转平台开启一个消息处理线程***********************");
				System.out.println("**********************************************************************");

				Receiver receiver = new Receiver(socket.getInputStream());
				Sender sender = new Sender(socket.getOutputStream());

				List<String> messageFromIPC = new ArrayList();
				logger.info("等待来自发布中心的消息...");
				String messageFromCenter = receiveMessage(receiver, sender, socket.getOutputStream());
				if (messageFromCenter == null) {
					String reply = "发布中心发送的消息无效，此消息不会被平台处理和转发！";
					logger.warn(reply);
					sender.send(sender.combineSendFrame(reply.getBytes("GBK")));
					closeSocket(socket);
					continue;
				} else {
					logger.info("即将处理来自发布中心的消息：" + messageFromCenter);
				}

				boolean isValidJSON = JSONUtils.mayBeJSON(messageFromCenter);
				if (!isValidJSON) {
					String reply = "发布中心的消息不符合json规范，此消息不会被平台处理！";
					logger.warn(reply);
					sender.send(sender.combineSendFrame(reply.getBytes("GBK")));
					continue;
				} else {
					List<String> messages = splitMessage(messageFromCenter);
					if(messages.size() == 0){
						logger.info("此次来自发布中心发布的消息，没有要发往IPC的有效信息!");
					}else{
						List<Future> futureList = new ArrayList<>();
						ExecutorService pool = Executors.newCachedThreadPool();
						for (int i = 0; i < messages.size(); i++) {
							Future future = pool.submit(new SenderCallable(messages.get(i)));
							futureList.add(future);
						}
						for(int i=0; i<futureList.size(); i++){
							try {
								String json = (String) futureList.get(i).get();
								boolean isValidJson = JSONUtils.mayBeJSON(json);
								if (!isValidJson) {
									logger.info("此消息是非法JSON，不会被添加到向发布中心返回消息中！");
									continue;
								}
								messageFromIPC.add(json);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}

					String reply;
					if (messageFromIPC.size() != 0) {
						reply = combineMessage(messageFromIPC);
					} else {
						reply = "IPC没有消息返回，或者IPC无响应!";
					}
					logger.info("向发布中心返回消息：" + reply);
					sender.send(sender.combineSendFrame(reply.getBytes("GBK")));
					sender.flush();
				}

				//关闭socket连接
				closeSocket(socket);

				System.out.println("**********************************************************************");
				System.out.println("************************中转平台结束一个消息处理线程**********************");
				System.out.println("**********************************************************************");
			}
		} catch (Exception e) {
			logger.error("与发布中心进行读写时发生异常: " + e.getMessage());
			logger.info("本次连接异常，将被关闭！请重新发起连接！");
			this.createSockets(server);
		} finally {
			logger.error("中转平台停止运行，需要手动重启！");

		}


	}

	/**
	 * 接受字符串消息
	 *
	 * @return 消息字符串
	 */
	public String receiveMessage(Receiver receiver, Sender sender, OutputStream os) {
		String json = null;
		byte[] frame = null;
		int receiveLimit = 0;
		do {
			if (receiveLimit > 2) {
				logger.error("发布中心发送无效消息的次数超过3次，停止接收此无效消息");
				break;
			}
			receiveLimit++;
			frame = receiver.receiveFrame();
			if (!receiver.isValid(frame)) {
				responseFrameInvalid(sender, os);
			} else {
				responseFrameValid(sender, os);
			}
		} while (!receiver.isValid(frame));

		if (receiver.isValid(frame)) {
			byte[] unescapeFrameBody = receiver.parseAndUnescapeFrameBody(frame);
			json = receiver.conventFrameBodyToString(unescapeFrameBody);
		}

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
		try {
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
		} catch (JSONException e) {
			logger.error("发布中心发送的消息在解析时发生错误！");
		} finally {
			return configs;
		}
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
		output = output.substring(0, output.length() - 1);
		output += "],\"resources\":" + resources + "}";
		return output;
	}

	/**
	 * 关闭socket
	 *
	 * @param socket
	 */
	public void closeSocket(Socket socket) {
		try {
			socket.shutdownInput();
			socket.shutdownOutput();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
