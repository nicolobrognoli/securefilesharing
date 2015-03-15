package it.polimi.core;

import it.polimi.core.Node.FetchThread;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Utility {
	public final static String directory="FileSharingDownload";
	
	public static List<FileOnline> parseSearchResult(String result) throws UnknownHostException{
		//create the list of file found on connected supernode
		List<FileOnline> fileFound = new ArrayList<FileOnline>();
		int begin = 0;
		String record;
		String nome;
		String Ip;
		FileOnline file;
		for(int i=0; i<result.length();i++)
		{
			if(result.charAt(i) == '$'){
				record = result.substring(begin, i);
				nome = record.substring(0, record.indexOf("IP"));
				Ip = record.substring(record.indexOf("IP") + 3, record.length());
				file = new FileOnline(InetAddress.getByName(Ip), nome);
				fileFound.add(file);
				begin = i+1;
			}					
		}
		return fileFound;
	}
	
	public static boolean printResult(List<FileOnline> fileFound){
		System.out.println("File found:");
		Iterator<FileOnline> iter = fileFound.iterator();
		FileOnline temp;
		List<String> printed = new ArrayList<String>();
		while(iter.hasNext()){
			temp = iter.next();
			if(!printed.contains(temp.getFilename()))
			{
				System.out.println(temp.getFilename() + "\n");
				printed.add(temp.getFilename());
			}
		}
		return printed.isEmpty();
	}
	
	//returns the number of chunks.
	public static int createChunksMD5(String path){
		InputStream fis;
		File file;
		int numRead, i = 0;
		String filename =Utility.returnFilename(path); 
		file=new File(Utility.getDirectory()+filename+"_md5");
		if(!file.exists()){
			try {
	
				
				fis = new FileInputStream(path);
				OutputStream fos = new FileOutputStream(Utility.getDirectory()+filename+"_md5");
				PrintStream p = new PrintStream(fos);
			      
		       MessageDigest md = MessageDigest.getInstance("MD5");			      
	
		       do {
		    	   byte[] buffer = new byte[262144]; //256KB
		           numRead = fis.read(buffer);
		           byte[] digest;
		           if (numRead > 0) {
		        	   if(numRead < 262144)
		        	   {
		        		   
		        		   byte[] temp2 = new byte[numRead];
		        		   for(int j=0; j<numRead; j++)
		        			   temp2[j] = buffer[j];
		        		   md.reset();
			               digest = md.digest(temp2);	
		        	   }
		        	   else
		        	   {
		        		   md.reset();
			               digest = md.digest(buffer);	
		        	   }			   
		               //convert the byte to hex format 
		               StringBuffer sb = new StringBuffer();
		               for (int j = 0; j < digest.length; j++) {
		                 sb.append(Integer.toString((digest[j] & 0xff) + 0x100, 16).substring(1));
		               }
		               p.append(i+ "," + sb + "\n");
		           }
		           i++;
		       } while (numRead != -1);	       
		       
		       fis.close();
		       p.close();
		       fos.close();			       
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			file=new File(path);
			int temp=(int)(file.length()/262144);
			return temp+1;
		}
		return i-1;
	}	
	  //choice = 0 is the exit action
	  public static int getAction(String msg){
		  int choice = 0;
		  if(msg.contains("unpublish"))
  	    	choice = 2;
  	    else if(msg.contains("publish"))
  	    	choice = 1;
  	    else if(msg.contains("search"))
  	    	choice = 3;
  	    else if(msg.contains("fetch"))
  	    	choice = 4;
		  return choice;
	  }
	  
	  public static boolean checkThreadAlive(List<FetchThread> tList){
		  boolean alive = false;
		  for(int i=0; i<tList.size() && !alive; i++)
			  if(tList.get(i).isAlive())
				  alive = true;		  
		 return alive;
	  }

	public static int getChoice(String str) {
		if(str.equals("1"))
			return 1;
		if(str.equals("2"))
			return 2;
		if(str.equals("3"))
			return 3;
		if(str.equals("0"))
			return 0;
		return -1;
	}

	public static boolean checkFileOnDisk(String fileDownload) {
		File file = new File(Utility.getDirectory()+fileDownload);
		if(file.exists())
			return true;
		return false;
	}
	
	public static String returnFilename(String path){
		String name;
		if(System.getProperty("os.name").contains("Windows"))
			name = path.substring(path.lastIndexOf("\\")+1, path.length());
		else
			name = path.substring(path.lastIndexOf("/")+1, path.length());
		return name;
	}
	public static String getDirectory(){
		String name;
		if(System.getProperty("os.name").contains("Windows"))
			name = directory+"\\";
		else
			name = directory+"/";
		return name;
	}
}
