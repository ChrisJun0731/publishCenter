package com.demo.publishCenter;


import com.demo.publishCenter.services.Receiver;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/9/28.
 */
public class MiddlePlatform {
	public static void main(String[] args){
		try{
			ServerSocket server = new ServerSocket(5000);
			while(true){
				Socket socket = server.accept();
				InputStream is = socket.getInputStream();
				OutputStream os = socket.getOutputStream();
				Receiver receiver = new Receiver(is, os);
				receiver.receiveFramesAndSendJson();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
