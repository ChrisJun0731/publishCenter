package com.demo.publishCenter.models;

/**
 * Created by Administrator on 2017/9/5.
 */
public class Resource {
	private String filename;
	private String filepath;
	private int filetype;

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public int getFiletype() {
		return filetype;
	}

	public void setFiletype(int filetype) {
		this.filetype = filetype;
	}
}
