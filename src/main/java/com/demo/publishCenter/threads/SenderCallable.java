package com.demo.publishCenter.threads;

import com.demo.publishCenter.util.Receiver;
import com.demo.publishCenter.util.Sender;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

/**
 * Created by Administrator on 2017/10/16.
 */
public class SenderCallable implements Callable {

	private final int defaultPort = 5005;
	private String config;

	public SenderCallable(String config){
		this.config = config;
	}

	public String call(){
		String json = "";
		JSONObject configObj = JSONObject.fromObject(this.config);
		JSONArray commands = (JSONArray)configObj.get("commands");
		JSONObject ipc = (JSONObject)commands.get(0);
		String ip = (String) ipc.get("ip");
		try {
			Socket socket = new Socket(ip, defaultPort);
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			Sender sender = new Sender(os);
			Receiver receiver = new Receiver(is);

			//先判断发送给工控机的json是否发送成功
			int frameNum;
			do{
				sender.send(sender.combine(sender.escape(config.getBytes())));
				byte[] frame = receiver.receiveFrame();
				frameNum = receiver.computeFrameNum(frame);
				if(frameNum != 1){
					byte[] secondFrame = receiver.getSecondFrame(frame);
					if(receiver.isValid(secondFrame)){
						json = receiver.conventFrameBodyToString(receiver.parseAndUnescapeFrameBody(secondFrame));
					}else{
						while(true){
							sender.send(sender.combine(new byte[]{0x00}));
							byte[] jsonFrame = receiver.receiveFrame();
							if(receiver.isValid(jsonFrame)){
								json = receiver.conventFrameBodyToString(receiver.parseAndUnescapeFrameBody(jsonFrame));
								break;
							}
						}
					}
				}
			}while(frameNum == 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return json;
	}
}
