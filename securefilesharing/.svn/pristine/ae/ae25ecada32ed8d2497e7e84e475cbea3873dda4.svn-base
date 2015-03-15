package it.polimi.core;

import java.util.HashMap;

public class ActiveDownload {
	private HashMap<Integer, Integer> chunkMap;
	private HashMap<Integer, String> md5Map;
	private String filename; 
	private boolean complete,exited;//exited aggiunta per sistemare la questione dell'uscita con il download da tre nodi,va a true quando il downloadtread raggiunge la fine.
	//private boolean exitDownload
	private int nThread;
	
	public ActiveDownload(String filename) {
		super();
		this.chunkMap = new HashMap<Integer, Integer>();
		this.md5Map = new HashMap<Integer, String>();
		this.filename = filename;
		this.complete = false;
		this.nThread = 0;
		this.exited=false;
	}
	

	public HashMap<Integer, Integer> getChunkMap() {
		return chunkMap;
	}

	public void setChunkMap(HashMap<Integer, Integer> chunkMap) {
		this.chunkMap = chunkMap;
	}

	public HashMap<Integer, String> getMd5Map() {
		return md5Map;
	}

	public void setMd5Map(HashMap<Integer, String> md5Map) {
		this.md5Map = md5Map;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public synchronized int chunkMapSize(){
		return this.chunkMap.size();
	}
	
	public synchronized int md5MapSize(){
		return this.md5Map.size();
	}
	
	public synchronized void initializeChunkMap(int nChunk){
		for(int i=0; i<nChunk ; i++){
			this.chunkMap.put(i, -1);
		}	
	}	

	public synchronized boolean isComplete() {
		return complete;
	}


	public synchronized void setComplete(boolean complete) {
		this.complete = complete;
	}	
	
	public boolean isExitDownload() {
		if((this.nThread == 0)&& this.isExited())
			return true;
		else 
			return false;
	}

	public int getChunkNotDownloaded() {
		int i;
		boolean download = false;
		for(i=0; i<chunkMapSize() && !download;i++){
			if(chunkMap.get(i) == -1)
			{
				download = true;
				chunkMap.put(i, 0);
			}
		}
		i--;
		if(download)
			return i;
		return -1;
	}


	public void setnThread(int nThread) {
		this.nThread = nThread;
	}


	public int getnThread() {
		return nThread;
	}	
	
	public void decreasenThread(){
		this.nThread--;
	}


	public boolean isExited() {
		return exited;
	}


	public void setExited(boolean exited) {
		this.exited = exited;
	}
	
}
