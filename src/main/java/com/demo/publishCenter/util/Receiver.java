package com.demo.publishCenter.util;

import java.io.InputStream;

public class Receiver {

    private InputStream is;

    MyUtil util = new MyUtil();

    public Receiver(InputStream is){
        this.is = is;
    }

    /**
     * 将转义后的数据帧体，转为字符串
     * @param frameBody
     * @return
     */
    public String conventFrameBodyToString(byte[] frameBody){
        return new String(frameBody);
    }

    /**
     * 判断帧是否有效
     * @param frame 帧
     * @return 是否有效
     */
    public boolean isValid(byte[] frame){
        int len = frame.length;
        if (frame[0] == (byte) 0xaa && frame[1] == (byte) 0x51 && frame[len - 3] == (byte) 0xcc) {
            byte[] suffix = new byte[frame.length - 2];
            System.arraycopy(frame, 0, suffix, 0, suffix.length);
            byte[] crc = util.Pub_CalcCRC(suffix);
            if (crc[0] == frame[len - 2] && crc[1] == frame[len - 1]) {
                return true;
            } else {
                System.out.println("数据帧校验码校验错误！");
                return false;
            }
        } else {
            System.out.println("数据帧格式不正确");
            return false;
        }
    }

    /**
     * 获取帧体，并对帧体进行反转义
     * @param frame 数据帧
     */
    public byte[] parseAndUnescapeFrameBody(byte[] frame){
        byte[] frameBody = new byte[frame.length - 5];
        System.arraycopy(frame, 2, frameBody, 0, frame.length - 5);
        return unescape(frameBody);
    }

    /**
     * 接受一帧数据
     * @return 一帧
     */
    public byte[] receiveFrame(){
        byte[] frame = {};
        try {
            byte[] first_byte = new byte[]{(byte) this.is.read()};
            int available = this.is.available();
            byte[] data_suffix = new byte[available];
            frame = new byte[available + 1];
            this.is.read(data_suffix);
            System.arraycopy(first_byte, 0, frame, 0, 1);
            System.arraycopy(data_suffix, 0, frame, 1, available);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return frame;
    }

    /**
     * 对帧体进行反转义
     * @param input 待反转义字节数组
     * @return 反转义后的字节数组
     */
    public byte[] unescape(byte[] input){
        byte[] output;
        int count = 0;
        int start = 0;
        for (int i = 0; i < input.length; i++) {
            if (input[i] == (byte) 0xee) {
                count++;
            }
        }
        output = new byte[input.length - count];
        for (int i = 0; i < input.length; i++) {
            if (input[i] == (byte) 0xee) {
                if (input[i + 1] == (byte) 0x0a) {
                    output[start++] = (byte) 0xaa;
                    i++;
                }
                if (input[i + 1] == (byte) 0x0c) {
                    output[start++] = (byte) 0xcc;
                    i++;
                }
                if (input[i + 1] == (byte) 0x0e) {
                    output[start++] = (byte) 0xee;
                    i++;
                }
            } else {
                output[start++] = input[i];
            }
        }
        return output;
    }

    public byte[] getSecondFrame(byte[] frame){
        byte[] secondFrame = new byte[frame.length - 6];
        System.arraycopy(frame, 6, secondFrame, 0, secondFrame.length);
        return secondFrame;
    }

    /**
     * 计算返回的数据块中包含的帧的个数
     * @param frame
     * @return
     */
    public int computeFrameNum(byte[] frame){
        int frameNum = 0;
        for(int i=0; i<frame.length; i++){
            if(frame[i] == (byte)0xaa){
                frameNum++;
            }
        }
        return frameNum;
    }

}
