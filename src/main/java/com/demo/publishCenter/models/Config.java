package com.demo.publishCenter.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/9/5.
 */
public class Config {
	private List<IPC> commands;
	private List<Resource> resources;

	public Config(){
		this.commands = new ArrayList();
		this.resources = new ArrayList<Resource>();
	}

	public void addIPC(IPC ipc){
		commands.add(ipc);
	}

	public void removeIPC(String ip){
		for(IPC ipc: commands){
			if (ip.equals(ipc.getIp())) {
				commands.remove(ipc);
				return;
			}
		}
	}

	public void addResource(Resource resource){
		resources.add(resource);
	}

	public void removeResource(String filename){
		for(Resource resource: resources){
			if (filename.equals(resource.getFilename())) {
				resources.remove(resource);
				return;
			}
		}
	}

	public List<IPC> getCommands() {
		return commands;
	}

	public void setCommands(List<IPC> commands) {
		this.commands = commands;
	}

	public List<Resource> getResources() {
		return resources;
	}

	public void setResources(List<Resource> resources) {
		this.resources = resources;
	}
}
