package com.demo.publishCenter.services;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Administrator on 2017/9/22.
 */
public class SendThread implements Runnable {

	private final int defaultPort = 5005;
	private String config;
	private Sender sender = new Sender();

	public SendThread(String config) {
		this.config = config;
	}

	public void run() {
		JSONObject configObj = JSONObject.fromObject(this.config);
		JSONArray commands = (JSONArray)configObj.get("commands");
		JSONObject ipc = (JSONObject)commands.get(0);
		String ip = (String) ipc.get("ip");
		try {
			Socket socket = new Socket(ip, defaultPort);
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			sender.sendFrames(sender.combineFrame(sender.packetConfig(sender.escape(this.config.getBytes()))), os, is);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public int getDefaultPort() {
		return defaultPort;
	}
}
