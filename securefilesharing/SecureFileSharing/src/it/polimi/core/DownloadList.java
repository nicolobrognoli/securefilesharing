package it.polimi.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadList {
	private List<ActiveDownload> download;
	private ActiveDownload activeTemp;
	private boolean exit;
	private int nThread;
	
	public DownloadList(){
		this.download = new ArrayList<ActiveDownload>();
		this.exit = false;
	}

	private synchronized ActiveDownload search(String filename){
		for(int i=0; i<this.download.size();i++)
		{
			if(this.download.get(i).getFilename().equals(filename))
				return this.download.get(i);
		}
		return null;
	}
	
	public synchronized void addActiveDownload(String filename, int nChunk){		
		activeTemp = new ActiveDownload(filename);
		activeTemp.initializeChunkMap(nChunk);
		this.download.add(activeTemp);
	}

	public synchronized void md5put(String filename, int num, String digest) {
		activeTemp = this.search(filename);
		activeTemp.getMd5Map().put(num, digest);		
	}
	
	public synchronized int chunkMapSize(String filename){
		activeTemp = this.search(filename);	
		return activeTemp.chunkMapSize();
	}
	
	public synchronized int md5MapSize(String filename){
		activeTemp = this.search(filename);	
		return activeTemp.md5MapSize();
	}

	public synchronized int getChunkNotDownloaded(String filename) {
		activeTemp = this.search(filename);
		return activeTemp.getChunkNotDownloaded();
	}

	public synchronized void resetChunk(String filename, int chunk) {
		activeTemp = this.search(filename);
		activeTemp.getChunkMap().put(chunk, -1);
	}

	public synchronized boolean checkMd5(String filename, int chunk, String md5) {
		activeTemp = this.search(filename);
		return activeTemp.getMd5Map().get(chunk).equals(md5);
	}

	public synchronized void chunkComplete(String filename, int chunk) {
		boolean completed = true;
		activeTemp = this.search(filename);
		activeTemp.getChunkMap().put(chunk, 1);
		for(int i=0; i<activeTemp.chunkMapSize() && completed; i++){
			if(activeTemp.getChunkMap().get(i) != 1)
				completed = false;	
		}		
		if(completed)
		{
			activeTemp.setComplete(true);
			this.notifyAll();
		}			
	}

	public synchronized boolean isCompleted(String filename) {
		activeTemp = this.search(filename);
		if(activeTemp!=null)
			return activeTemp.isComplete();
		return true;
	}

	public synchronized boolean isEmpty() {
		return this.download.isEmpty();
	}

	public synchronized void remove(String filename) {
		activeTemp = this.search(filename);		
		//delete the chunks
		for(int i=0; i<activeTemp.chunkMapSize(); i++)
		{
			File chunk = new File(Utility.getDirectory()+filename+"_part"+i);
			if(chunk.exists())
				chunk.delete();
		}	
		while(activeTemp.getnThread()!=0){
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.download.remove(activeTemp);
	}

	public synchronized void removeAll() {
		this.download = new ArrayList<ActiveDownload>();
	}

	public synchronized boolean isExit() {
		return exit;
	}

	public synchronized void setExit(boolean exit) {
		this.exit = exit;
	}
	
	public synchronized void removeDownloadThread(String filename){
		activeTemp = this.search(filename);
		activeTemp.decreasenThread();
		this.notifyAll();
	}
	
	public synchronized boolean isExitAllDownloads(){
		boolean ok = true;
		for(int i=0; i<this.download.size() && ok; i++){
			if(!this.download.get(i).isExitDownload())
			{
				ok = false;
			}
				
		}
		if(ok){
			for(int i=0; i<this.download.size() && ok;){
				this.remove(this.download.get(i).getFilename());
			}			
		}
		return ok;
	}

	public synchronized int getnThread() {
		return nThread;
	}

	public synchronized void setnThread(String filename, int nThread) {
		activeTemp = this.search(filename);
		activeTemp.setnThread(nThread);
	}

	public synchronized void setExited(String filename) {
		activeTemp = this.search(filename);
		activeTemp.setExited(true);
		this.notifyAll();//dovrebbe notificare quando tutto è uscito
	}	
}
