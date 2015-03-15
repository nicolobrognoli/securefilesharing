package it.polimi.core;

import java.net.InetAddress;


public class FileOnline {
	private InetAddress clientIP;
	private String filename;
	
	public FileOnline(InetAddress ipClient, String filename) {
		super();
		this.clientIP = ipClient;
		this.filename = filename;
	}

	public InetAddress getIpClient() {
		return clientIP;
	}

	public void setIpClient(InetAddress ipClient) {
		this.clientIP = ipClient;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public FileOnline copy(FileOnline file){
		return new FileOnline(file.getIpClient(), file.filename);
	}
	
	public boolean checkFile(InetAddress ip, String filename){
		if(this.clientIP.equals(ip) && this.filename.equals(filename))
			return true;
		else 
			return false;
	}
	
	@Override
	public boolean equals(Object o){
		FileOnline file=(FileOnline) o;
		String thisIp,fileIp;
		thisIp=this.clientIP.toString();
		fileIp=file.getIpClient().toString();
		if(this.filename.equals(file.getFilename())&&(thisIp.equals(fileIp)))
			return true;
		return false;
	}

}
