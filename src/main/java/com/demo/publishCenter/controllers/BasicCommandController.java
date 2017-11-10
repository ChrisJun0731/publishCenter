package com.demo.publishCenter.controllers;

import com.demo.publishCenter.services.PublishCenter;
import com.demo.publishCenter.util.Sender;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by Administrator on 2017/9/29.
 */
@RestController
public class BasicCommandController {

	@Autowired
	private PublishCenter center;
	@Autowired
	private Sender sender;

	@RequestMapping("/turnOn")
	public void turnOn(HttpServletRequest req, HttpServletResponse res){
		String config = req.getParameter("config");
		System.out.println(config);
		send(config);
	}

	@RequestMapping("/turnOff")
	public void turnOff(HttpServletRequest req, HttpServletResponse res){
		String config = req.getParameter("config");
		System.out.println(config);
		send(config);

	}

	@RequestMapping("/time")
	public void getTime(HttpServletRequest req, HttpServletResponse res){
		String config = req.getParameter("config");
		System.out.println(config);
		send(config);
	}

	@RequestMapping("/status")
	public void getStatus(HttpServletRequest req, HttpServletResponse res){
		String config = req.getParameter("config");
		System.out.println(config);
		send(config);
	}

	@RequestMapping("/brightness")
	public void getBrightness(HttpServletRequest req, HttpServletResponse res){
		String config = req.getParameter("config");
		System.out.println(config);
		send(config);
	}

	@RequestMapping("/setBrightness")
	public void setBrightness(HttpServletRequest req, HttpServletResponse res){
		String config = req.getParameter("config");
		System.out.println(config);
		send(config);
	}

	@RequestMapping("/autoBrightness")
	public void autoBrightness(HttpServletRequest req, HttpServletResponse res){
		String config = req.getParameter("config");
		System.out.println(config);
		send(config);
	}

	@RequestMapping("/screenParameter")
	public void getScreenParameter(HttpServletRequest req, HttpServletResponse res){
		String config = req.getParameter("config");
		System.out.println(config);
		send(config);
	}

	@RequestMapping("/screenShot")
	public void screenShot(HttpServletRequest req, HttpServletResponse res){
		String config = req.getParameter("config");
		System.out.println(config);
		send(config);
	}

	@RequestMapping("/serverConfig")
	public String getServerConfig(){
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("resources\\server.json");
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line;
		String output = "";
		try{
			while((line = reader.readLine()) != null){
				line = line.replace("\n", "")
						.replace("\t", "")
						.replace(" ", "");
				output += line;
			}

		}catch(Exception e){
			e.printStackTrace();
		}
		return output;
	}

	@RequestMapping("/sendPlayList")
	public void sendPlayList(HttpServletRequest req, HttpServletResponse resp){
		String config = req.getParameter("config");
		System.out.println(config);
		send(config);
	}

	@RequestMapping("/convertMessage")
	public String convertMessage(HttpServletRequest req, HttpServletResponse res) throws UnsupportedEncodingException {
		String message = req.getParameter("message");
		String str = "";
		if(message!=null){
			byte[] frame = sender.combineSendFrame(message.getBytes("GBK"));
			for (int i = 0; i < frame.length; i++) {
				String temp = Integer.toHexString(frame[i]);
				if(temp.length() == 8){
					temp = temp.substring(6);
				}
				str += temp + " ";
			}
			str = str.substring(0, str.length() - 1);
		}
		return "{\"frame\":\"" + str + "\"}";
	}

	public void send(String config){
		String json = getServerConfig();
		JSONObject obj = JSONObject.fromObject(json);
		String ip = (String)obj.get("ip");
		int port = (Integer)obj.get("port");
		center.setServerIp(ip);
		center.setServerPort(port);
		center.sendAndGetReply(config);
	}
}
