package com.demo.publishCenter.services;

import com.demo.publishCenter.util.Util;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2017/9/22.
 */
@Service
public class Sender {
	//数据块的大小,默认50kb。
	private int blockSize = 50 * 1024;

	private Util util = new Util();

	/**
	 * 中心发送config.json文件
	 *
	 * @param path 文件路径
	 */
	public void sendFile(String path, String ip, int port) {
		InputStream is = null;
		OutputStream os = null;
		try {
			Socket socket = new Socket(ip, port);
			is = socket.getInputStream();
			os = socket.getOutputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		sendFrames(combineFrame(packetConfig(escape(readConfigFile(path)))), os, is);
	}

	/**
	 * 中心发送config.json字符串
	 *
	 * @param json 文件路径
	 */
	public void sendJson(String json, String ip, int port) {
		InputStream is = null;
		OutputStream os = null;
		try {
			Socket socket = new Socket(ip, port);
			is = socket.getInputStream();
			os = socket.getOutputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		sendFrames(combineFrame(packetConfig(escape(json.getBytes()))), os, is);
	}

	/**
	 * 中转平台分发config.json文件
	 *
	 * @param config_json json字符串
	 */
	public void distributeConfigJson(String config_json) {
		List<String> configs = splitConfigs(config_json);
		for(int i=0; i<configs.size(); i++){
			Thread sender = new Thread(new SendThread(configs.get(i)));
			sender.start();
		}
	}

	/**
	 * 将一个json文件拆分为多个工控机对应的json字符串
	 * @param config_json 中心发出的config_json
	 * @return 多个工控机对应的json
	 */
	private List<String> splitConfigs(String config_json){
		List<String> configs = new ArrayList();
		JSONObject configObj = JSONObject.fromObject(config_json);
		JSONArray commands = (JSONArray)configObj.get("commands");
		JSONArray resources = (JSONArray)configObj.get("resources");
		for(int i=0; i<commands.size(); i++){
			JSONObject ipcJson = (JSONObject)commands.get(i);
			String config = "{commands:["+ipcJson.toString()+"], resources:"+resources.toString()+"}";
			configs.add(config);
		}
		return configs;
	}

	/**
	 * 读取config.json文件的字节流
	 *
	 *
	 * @param path 文件路径
	 * @return 文件字节流
	 */
	public byte[] readConfigFile(String path) {
		List<byte[]> list = new ArrayList();
		try {
			FileInputStream fis = new FileInputStream(path);
			BufferedInputStream bis = new BufferedInputStream(fis);
			byte[] data = new byte[1024];
			int len = 0;
			while ((len = bis.read(data)) != -1) {
				data = Arrays.copyOf(data, len);
				list.add(data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		byte[] output = new byte[0];
		int start = 0;
		for (int i = 0; i < list.size(); i++) {
			start = output.length;
			output = Arrays.copyOf(output, start + list.get(i).length);
			System.arraycopy(list.get(i), 0, output, start, list.get(i).length);
		}
		return output;
	}

	/**
	 * 对config.json的字节流进行转义
	 *
	 * @param input config.json文件的字节流
	 */
	public byte[] escape(byte[] input) {
		int extra = 0;
		for (int i = 0; i < input.length; i++) {
			if (input[i] == (byte) 0xaa || input[i] == (byte) 0xcc || input[i] == 0xee) {
				extra++;
			}
		}
		byte[] output = input;
		if (extra != 0) {
			int start = 0;
			output = new byte[input.length + extra];
			for (int i = 0; i < input.length; i++) {
				if (input[i] == (byte) 0xaa || input[i] == (byte) 0xcc || input[i] == 0xee) {
					output[start++] = (byte) 0xee;
					output[start++] = input[i];
				}
			}
		}
		return output;
	}

	/**
	 * 对config.json按文件块大小文件进行分组
	 *
	 * @param input 转义后的json字节流
	 * @return 存放分组的list
	 */
	public List<byte[]> packetConfig(byte[] input) {
		int blockSize = getBlockSize();
		int pack = input.length / blockSize + 1;
		List<byte[]> list = new ArrayList();
		for (int i = 0; i < pack; i++) {
			int len = 0;
			if (i == pack - 1) {
				len = input.length % blockSize;
			} else {
				len = blockSize;
			}
			byte[] data = new byte[len];
			System.arraycopy(input, blockSize * i, data, 0, len);
			list.add(data);
		}
		return list;
	}

	/**
	 * 将每个分组封装为一个帧
	 *
	 * @param list 分组集合
	 * @return 帧集合
	 */
	public List<byte[]> combineFrame(List<byte[]> list) {
		List<byte[]> frames = new ArrayList();
		for (int i = 1; i <= list.size(); i++) {
			byte[] block = list.get(i-1);
			byte[] frame = new byte[7 + block.length];
			byte[] crc_frame = new byte[9 + block.length];
			byte[] start = new byte[]{(byte) 0xaa};
			byte[] cmd = new byte[]{(byte) 0x51};
			byte[] end = new byte[]{(byte) 0xcc};
			byte[] block_size_byte = util.convertInt2Byte(getBlockSize());
			byte[] block_num_byte = util.convertInt2Byte(i);
			byte[] crc;
			System.arraycopy(start, 0, frame, 0, 1);
			System.arraycopy(cmd, 0, frame, 1, 1);
			System.arraycopy(block_size_byte, 0, frame, 2, 2);
			System.arraycopy(block_num_byte, 0, frame, 4, 2);
			System.arraycopy(block, 0, frame, 6, block.length);
			System.arraycopy(end, 0, frame, 6 + block.length, 1);
			crc = util.Pub_CalcCRC(frame);
			System.arraycopy(frame, 0, crc_frame, 0, frame.length);
			System.arraycopy(crc, 0, crc_frame, frame.length, 2);
			frames.add(crc_frame);
		}
		return frames;
	}

	/**
	 * 发送帧
	 *
	 * @param frames 帧集合
	 * @param os     输出流
	 */
	void sendFrames(List<byte[]> frames, OutputStream os, InputStream is) {
		try {
			for (int i = 0; i < frames.size(); i++) {
				os.write(frames.get(i));
				os.flush();
				byte[] result = receive(is);
				if (result[0] == (byte) 0xaa && result[1] == (byte) 0x52) {
					if (result[4] == 1) {
						System.out.println("编号" + util.conventByte2Int(result[2], result[3]) + "的数据块发送成功");
					} else {
						int try_times = 0;
						while (result[4] == 0 && try_times <= 50) {
							os.write(frames.get(i));
							os.flush();
							result = receive(is);
							try_times++;
						}
						if (try_times >= 50) {
							System.out.println("编号" + util.conventByte2Int(result[2], result[3]) + "的数据块多次发送失败，" +
									"请检查网络是否正常！");
							return;
						} else {
							System.out.println("编号" + util.conventByte2Int(result[2], result[3]) + "的数据块发送成功");
						}
					}
				} else {
					System.out.println("返回的数据帧格式有误！");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 读一帧数据
	 *
	 * @param is 输入流
	 * @return 帧
	 */
	private byte[] receive(InputStream is) {
		byte[] frame = {};
		try {
			byte[] first_byte = {(byte) is.read()};
			int available = is.available();
			byte[] data_suffix = new byte[available];
			frame = new byte[available + 1];
			is.read(data_suffix);
			System.arraycopy(first_byte, 0, frame, 0, 1);
			System.arraycopy(data_suffix, 0, frame, 1, available);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return frame;
	}

	public int getBlockSize() {
		return blockSize;
	}

	public void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}
}
