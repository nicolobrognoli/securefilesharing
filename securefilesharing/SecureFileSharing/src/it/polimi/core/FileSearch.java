package it.polimi.core;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class FileSearch {
	private List<FileOnline> results;
	private boolean searchCompleted;
	
	
	public FileSearch() {
		super();
		this.results = new Vector<FileOnline>();
		this.searchCompleted = false;
	}
	public List<FileOnline> getResults() {
		return results;
	}
	public void setResults(List<FileOnline> results) {
		this.results = results;
	}
	public boolean isSearchCompleted() {
		return searchCompleted;
	}
	public void setSearchCompleted(boolean searchCompleted) {
		this.searchCompleted = searchCompleted;
	}
	public synchronized String search(String filename){
		String result="";
		//send to node the list of file published on the other supernodes
		Iterator<FileOnline> iter = this.results.iterator();
		while(iter.hasNext()){			
			FileOnline temp = iter.next();
			if(temp.getFilename().contains(filename))
			{
				result += temp.getFilename() + "IP" + temp.getIpClient() + "$" ;
			}			
		}
		return result;
	}
	
}
