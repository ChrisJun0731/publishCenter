package com.demo.publishCenter.services;


import com.demo.publishCenter.util.Receiver;
import com.demo.publishCenter.util.Sender;

import java.net.ServerSocket;
import java.net.Socket;

public class IPC {

    private Sender sender;
    private Receiver receiver;

    public void work(){
        try{
            ServerSocket server = new ServerSocket(5005);
            while(true){
                Socket socket = server.accept();
                sender = new Sender(socket.getOutputStream());
                receiver = new Receiver(socket.getInputStream());
                byte[] frame = receiver.receiveFrame();
                while(!receiver.isValid(frame)){
                    sender.send(sender.combine(new byte[]{0x00}));
                    frame = receiver.receiveFrame();
                }
                sender.send(sender.combine(new byte[]{0x01}));
                String request = receiver.conventFrameBodyToString(receiver.parseAndUnescapeFrameBody(frame));
                System.out.println(request);
                String response = "{\"commands:\":[{\"cmd\": \"0x52\", \"data\":{\"success\": 1}}], \"resources\":[]}";
                sender.send(sender.combine(response.getBytes()));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void sendJson(){

    }

    public String receiveJson(){
        return null;
    }
}
