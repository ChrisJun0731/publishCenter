package com.demo.publishCenter.controllers;

import com.demo.publishCenter.services.PublishCenter;
import com.demo.publishCenter.util.Sender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Administrator on 2017/9/29.
 */
@RestController
public class BasicCommandController {

	@Autowired
	private PublishCenter center;

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

	public void send(String config){
		center.setServerIp("192.168.30.224");
		center.setServerPort(5000);
		center.sendAndGetReply(config);
	}
}
