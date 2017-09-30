package com.demo.publishCenter;

import com.demo.publishCenter.services.Receiver;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

/**
 * Created by Administrator on 2017/9/30.
 */
public class IPCMachine {
	public static void main(String[] args) {
		try {
			ServerSocket server = new ServerSocket(5005);
			while (true) {
				Socket socket = server.accept();
				InputStream is = socket.getInputStream();
				OutputStream os = socket.getOutputStream();
				Receiver receiver = new Receiver(is, os);
				String json = receiver.receiveFramesAndPrintJson();

//				JSONObject jsonObject = JSONObject.fromObject(json);
//				JSONArray array = (JSONArray) jsonObject.get("resources");
////				if (array.size() != 0 ) {
//					String ip = (String) jsonObject.get("ip");
//					String urlStr = "http://192.168.30.36:3300/api/test?fileName=test2.wmv";
////							"http://" + ip + ":3300" + "?fileName=test.mp4";
////							((JSONObject) array.get(0)).get("fileName");
//					URL url = new URL(urlStr);
//					URLConnection connection = url.openConnection();
//					connection.connect();
////				}
			}

		} catch (Exception e) {

		}
	}
}
