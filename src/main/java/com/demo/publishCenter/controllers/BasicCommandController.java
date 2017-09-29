package com.demo.publishCenter.controllers;

import com.demo.publishCenter.services.Sender;
import net.sf.json.JSONObject;
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
	private Sender sender;

	@RequestMapping("/turnOn")
	public void turnOn(HttpServletRequest req, HttpServletResponse res){
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("resources\\config.json");
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
		JSONObject configObj = JSONObject.fromObject(output);
		sender.sendJson(output, "192.168.30.224", 5000);
	}

	@RequestMapping("/turnOff")
	public void turnOff(){

	}

	@RequestMapping("/time")
	public void getTime(){

	}

	@RequestMapping("/brightness")
	public void getBrightness(){

	}

	@RequestMapping("/setBrightness")
	public void setBrightness(){

	}

	@RequestMapping("autoBrightness")
	public void autoBrightness(){

	}

	@RequestMapping("/screenShot")
	public void screenShot(){

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
}
