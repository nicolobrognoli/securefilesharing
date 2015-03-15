package it.polimi.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

public class Node {
	protected RSA rsa;
	protected Security security;
	private List<String> publishedFilesList;
	protected Socket socket;
	protected PrintWriter out;
	protected BufferedReader in;
	private ListenThread lt;
	private DownloadList downloadList;
	private List<String> uploadList;
	
	public Node(){
		//create rsa key pairs
		this.rsa = new RSA();
		this.security = new Security();
		this.publishedFilesList = new ArrayList<String>();
		this.downloadList = new DownloadList();
		this.uploadList = new ArrayList<String>();
	}
	
	public void publish(String filename){	
		//check if the path exists
		File file = new File(filename);
		if (!file.exists() || file.isDirectory())
		{
			if(file.isDirectory())
				System.out.println("[ERR] "+ filename + " is a directory.");
			else
				System.out.println("[ERR] File not exists.");
		}
		else
		{
			
			filename = new File(filename).getAbsolutePath();
			String name=Utility.returnFilename(filename);
			if(!this.checkFilePublished(name)){
				//update the published file list
				this.publishedFilesList.add(filename);
				//add the published file to the permanent file list
				boolean write = true;
				try {
					//check if the path is not already in the file
					if(new File(Utility.getDirectory()+"published.txt" ).exists()){
						FileReader fr = new FileReader(Utility.getDirectory()+"published.txt");
						BufferedReader br = new BufferedReader(fr);
					    String stringRead = br.readLine();				    
					    while(stringRead != null)
					    {		    
					    	if(stringRead.equals(filename))
					    		write = false;
					    	stringRead = br.readLine();	
					    }
					    fr.close();
					    br.close();
					}				
				    if(write){
				    	System.out.println("[INFO] Writing on \"published.txt\"");
				    	OutputStream fos = new FileOutputStream(Utility.getDirectory()+"published.txt", true);
						PrintStream p = new PrintStream(fos);	
						p.append(filename+"\n");
						fos.close();
						p.close();
				    }				
				} catch (FileNotFoundException e) {			
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}			
				
				//filename = filename.substring(filename.lastIndexOf('/'), filename.length()); tolta, era inutile fare un'altra operazione se possiamo aggiungerci noi la sbarretta
				//notify to the supernode the new published file
				this.sendMessage("publish " +"/"+name,this.security,this.out);
				//receive the response from supernode
				String result = this.readMessage(this.security,this.in);
				if(result.equals("published"))
					System.out.println("[INFO] File correctly published");
				else
				{
					if(result.equals("already published"))
						System.out.println("[INFO] File already published");
					else
						this.publishedFilesList.remove(filename);
				}					
			}
			else
				System.out.println("[INFO] File already published.");
		}
	}
	
	public void publish(){
		InputStreamReader reader = new InputStreamReader (System.in);
		BufferedReader systemInput = new BufferedReader (reader);	
		String path;
		try {
			System.out.println("File to publish (absolute path): ");
			path = systemInput.readLine();
			
			//publish the file
			this.publish(path);
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public void publishOnStart(){
		FileReader fr;
		try {
			if(new File(Utility.getDirectory()+"published.txt" ).exists()){
				fr = new FileReader(Utility.getDirectory()+"published.txt");
				BufferedReader br = new BufferedReader(fr);
			    String stringRead = br.readLine();
			    while(stringRead != null)
			    {		    
			    	this.publish(stringRead);
			        stringRead = br.readLine();
			    }	
			    System.out.println("[INFO] Published file from published.txt.");	
			}
				     
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}				     
	     
	}
	
	public void unpublish(){
		InputStreamReader reader = new InputStreamReader (System.in);
		BufferedReader systemInput = new BufferedReader (reader);	
		String filename;
		int i=0, fileindex;
		try {		
			
			//update the local published file list
			if(! this.publishedFilesList.isEmpty())
			{
				System.out.println("File published: ");
				
				String temp;
				
				Iterator<String> iter = this.publishedFilesList.iterator();
				do{
					temp = iter.next();
					System.out.println(i + " - " + temp);
					i++;
				}while(iter.hasNext());
				System.out.println("Choose the file to unpublish: ");
				fileindex = Integer.parseInt(systemInput.readLine());
				//if the filename is in the current absolute path
				if((fileindex >= 0) && (fileindex < this.publishedFilesList.size()))
				{
					filename = this.publishedFilesList.get(fileindex);
					//remove the file published from published.txt
					if(new File(Utility.getDirectory()+"published.txt" ).exists()){
						FileReader fr = new FileReader(Utility.getDirectory()+"published.txt");
						BufferedReader br = new BufferedReader(fr);
						String list = "";
					    String stringRead = br.readLine();				    
					    while(stringRead != null)
					    {		    
					    	if(!stringRead.equals(filename))
					    		list += stringRead +"\n";
					    	stringRead = br.readLine();	
					    }
					    fr.close();
					    br.close();
					    //write the correct list on the file
					    OutputStream fos = new FileOutputStream(Utility.getDirectory()+"published.txt");
						PrintStream p = new PrintStream(fos);	
						p.append(list);
						fos.close();
						p.close();
					}	

					
					filename = Utility.returnFilename(filename);
					this.sendMessage("unpublish " + "/"+filename,this.security,this.out);
					String result = this.readMessage(this.security,this.in);
					if(result.equals("unpublished")){
						System.out.println("[INFO] File " + filename + " unpublished.");
						synchronized(this.publishedFilesList){
							this.publishedFilesList.remove(fileindex);	
						}
					}

				}	
				else
				{
					System.out.println("[ERR] Typing error.");
				}
			}		
			else
			{
				System.out.println("[ERR] No file published.");				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	private List<InetAddress> search(String name){
		List<InetAddress> files =null;
		InputStreamReader reader = new InputStreamReader (System.in);
		BufferedReader systemInput = new BufferedReader (reader);
		List<FileOnline> fileFound,fileFoundOther;
		Iterator<FileOnline> iter;
		FileOnline temp;
		try{
		this.sendMessage("search /" + name, this.security, this.out);
		String result = this.readMessage(this.security, this.in);
		
		fileFound =  Utility.parseSearchResult(result);
		

		//forward the search on other supernodes
		sendMessage("forward search",this.security,this.out);
		result=readMessage(this.security,this.in);
		fileFoundOther= Utility.parseSearchResult(result);
		fileFound.addAll(fileFoundOther);
		if(fileFound.size()!=0){
		files = new ArrayList<InetAddress>();
		iter = fileFound.iterator();
			while(iter.hasNext()){
				temp = iter.next();
				if(temp.getFilename().equals(name))
				{
					files.add(temp.getIpClient());
				}
			}
		}
		}catch (IOException e) {
			e.printStackTrace();
		}
		return files;
	}
	public void search(){
		boolean noResult, noResultForward = false;
		InputStreamReader reader = new InputStreamReader (System.in);
		BufferedReader systemInput = new BufferedReader (reader);
		List<FileOnline> fileFound,fileFoundOther;
		Iterator<FileOnline> iter;
		FileOnline temp;
		try {
			String name;
			System.out.println("Search file name: ");
			name = systemInput.readLine();
			this.sendMessage("search /" + name, this.security, this.out);
			String result = this.readMessage(this.security, this.in);
			
			fileFound =  Utility.parseSearchResult(result);
			
			//print the download options
			noResult =  Utility.printResult(fileFound);
			if(noResult)
			{
				//no results on the connected supernode
				sendMessage("forward search",this.security,this.out);
				System.out.println("[INFO] No file found on the connected supernode.\nSearching...");
				result = readMessage(this.security,this.in);
				fileFoundOther = Utility.parseSearchResult(result);
				fileFound.addAll(fileFoundOther);
				noResultForward = Utility.printResult(fileFoundOther);
			}			
			else
			{
				System.out.println("Do you want " +
						"to forward the search? [y/n]");
				String choice = systemInput.readLine();
				if(choice.contains("y")){
					//forward the search on other supernodes
					sendMessage("forward search",this.security,this.out);
					System.out.println("[INFO] Searching...");
					result=readMessage(this.security,this.in);
					fileFoundOther= Utility.parseSearchResult(result);
					fileFound.addAll(fileFoundOther);
					noResultForward = Utility.printResult(fileFoundOther);
				}else{
					//user doesn't want to forward the request to other supernodes
					sendMessage("search done",this.security,this.out);
				}
			}
			if(!noResult || !noResultForward){
				//select the file to dowload
				System.out.println("Specify the file to download: (type 0 to quit)");
				String fileDownload = systemInput.readLine();
				
				if(!fileDownload.equals("0")){
					if(!Utility.checkFileOnDisk(fileDownload) && !this.checkFilePublished(fileDownload)){
						List<InetAddress> files = new ArrayList<InetAddress>();
						iter = fileFound.iterator();
						while(iter.hasNext()){
							temp = iter.next();
							if(temp.getFilename().equals(fileDownload))
							{
								files.add(temp.getIpClient());
							}
						}
						if(!files.isEmpty())
							fetch(files, fileDownload);
						else
							System.out.println("[ERR] Typing error, retry..");
					}
					else
						System.out.println("[ERR] You already have this file.");
				}
				
			}else{
				System.out.println("[ERR] No file \""+name+"\" found on the system.");
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		
		
	}
	
	private synchronized boolean checkFilePublished(String fileDownload) {
		boolean published = false;
		String filename;
		for(int i=0; i<this.publishedFilesList.size() && !published; i++)
		{
			filename=this.publishedFilesList.get(i);
			filename = Utility.returnFilename(filename);
			if(filename.equals(fileDownload))
				published = true;
		}
		return published;
	}

	public void fetch(List<InetAddress> nodeIp, String filename){	
		DownloadManagerThread dwMan = new DownloadManagerThread(nodeIp, filename);
		dwMan.start();
	}
	
	private void joinChunks(String filename){
		//join the chunks
	    int numRead; 
	    OutputStream fos;
		try {
			fos = new FileOutputStream(Utility.getDirectory()+filename);
			PrintStream p = new PrintStream(fos);
			InputStream fis;
			
			for(int i=0; i<this.downloadList.chunkMapSize(filename); i++)
			{
				fis = new FileInputStream(Utility.getDirectory()+filename+"_part"+i);
				byte[] buffer = new byte[262144];
				numRead = fis.read(buffer);
				if (numRead > 0) {
		        	   //if the buffer is not full
		        	   if(numRead < 262144)
		        	   {
		        		   byte[] temp2 = new byte[numRead];
		        		   for(int j=0; j<numRead; j++)
		        			   temp2[j] = buffer[j];
		        		   p.write(temp2);
		        	   }
		        	   else
		        		   p.write(buffer);
		           }
				fis.close();
			}
			p.close();
			fos.close();  
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		  
	}
	
	private void joinNode(BufferedReader in, PrintWriter out, Security securityNode) {
		//create socket connection
				try{			  
				
				     //get the public key bytes
				     byte[] bytes = this.rsa.getPublicKey().getEncoded();
				     
				    
				     int length=this.rsa.getPublicKey().getEncoded().length;
				     //send the public key length
				     out.println(length);
				     //send the public key bytes
				     for(int i=0;i<length;i++){
				    	 out.println(bytes[i]);
				     }
				    int i;
				    //read Node PublicKey
				    //read the key lenght
				    byte[] b=new byte[256];
				    for(i=0;i<256;i++){
				    	b[i]=(byte) Integer.parseInt(in.readLine());
				    }
				    b=rsa.decrypt(b);
				    length=(b[0] << 24)
		            + ((b[1] & 0xFF) << 16)
		            + ((b[2] & 0xFF) << 8)
		            + (b[3] & 0xFF);
				    
				    //read the key
				    bytes=new byte[length];
			    	for(i=0;i<length;i++){
			    		bytes[i]=(byte) Integer.parseInt((in.readLine()));
			    	}
			    	//public key from bytes received
		            KeySpec keyspec = new X509EncodedKeySpec(bytes);
			    	KeyFactory keyFactory = KeyFactory.getInstance("RSA");			   
			    	PublicKey publicKey = keyFactory.generatePublic(keyspec);

			    	//generate and send random number
				    Random random=new Random();
				    Date date= new Date();
				    Timestamp now=new Timestamp(date.getTime());
				    random.setSeed(now.getTime());
				    int randomNumber=random.nextInt();
				    byte[] rN=rsa.encrypt(randomNumber,publicKey);
				     for(int j=0;j<rN.length;j++){
				    	 out.println(rN[j]);
				     }
			    	
			    	//read my random number modified by the other node
				    b=new byte[256];
				    for(i=0;i<256;i++){
				    	b[i]=(byte) Integer.parseInt(in.readLine());
				    }
				    b=rsa.decrypt(b);
				    int randomNumberReceived=(b[0] << 24)
		            + ((b[1] & 0xFF) << 16)
		            + ((b[2] & 0xFF) << 8)
		            + (b[3] & 0xFF);
				    
			    	if (randomNumber-30==(randomNumberReceived)){
			    		System.out.println("[INFO] Challenge successfully completed.");

				    	//read the other node random number
				    	b=new byte[256];
					    for(i=0;i<256;i++){
					    	b[i]=(byte) Integer.parseInt(in.readLine());
					    }
					    b=rsa.decrypt(b);
					    randomNumberReceived=(b[0] << 24)
			            + ((b[1] & 0xFF) << 16)
			            + ((b[2] & 0xFF) << 8)
			            + (b[3] & 0xFF);
					    //modify the node random number
					    randomNumberReceived = randomNumberReceived - 30;
					    //send back the supernode random  number
					    rN=rsa.encrypt(randomNumberReceived,publicKey);
					     for(int j=0;j<rN.length;j++){
					    	 out.println(rN[j]);
					     }
				    	
					    //read the synchronous key length
					    length = Integer.parseInt(in.readLine());
				    	bytes = new byte[length];
				    	//build the key array of bytes 
				    	for(i=0;i<length;i++){
				    		bytes[i]=(byte)Integer.parseInt(in.readLine());;
				    	}
				    	//set the common synchronous key
					     securityNode.setSecurity(rsa.decrypt(bytes));					 
			    	}
			  
			    	
				  
				   } catch (UnknownHostException e) {
				     e.printStackTrace();
				   } catch  (IOException e) {
				     System.out.println("[ERR] No I/O");
				     System.exit(1);
				   } catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (InvalidKeySpecException e) {
					e.printStackTrace();
				}
		
	}

	
	//return 0 if access is not granted, 1 otherwise
	public int join(String supernodeIp, String username, String pwd){		
		this.lt = new ListenThread();
		//start the listen thread (incoming requests)
		this.lt.start();
		
		int result = 0;

		//create socket connection
		try{			  
		     socket = new Socket(InetAddress.getByName(supernodeIp), 4444);
		     out = new PrintWriter(socket.getOutputStream(), true);
		     in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		     //get the public key bytes
		     byte[] bytes = this.rsa.getPublicKey().getEncoded();
		     
		    
		     int length=this.rsa.getPublicKey().getEncoded().length;
		     //send the public key length
		     out.println(length);
		     //send the public key bytes
		     for(int i=0;i<length;i++){
		    	 out.println(bytes[i]);
		     }
		    int i;
		    //read Supernode PublicKey
		    //read the key lenght
		    byte[] b=new byte[256];
		    for(i=0;i<256;i++){
		    	b[i]=(byte) Integer.parseInt(in.readLine());
		    }
		    b=rsa.decrypt(b);
		    length=(b[0] << 24)
            + ((b[1] & 0xFF) << 16)
            + ((b[2] & 0xFF) << 8)
            + (b[3] & 0xFF);
		    
		    //read the key
		    bytes=new byte[length];
	    	for(i=0;i<length;i++){
	    		bytes[i]=(byte) Integer.parseInt((in.readLine()));
	    	}
	    	//public key from bytes received
            KeySpec keyspec = new X509EncodedKeySpec(bytes);
	    	KeyFactory keyFactory = KeyFactory.getInstance("RSA");			   
	    	PublicKey publicKey = keyFactory.generatePublic(keyspec);

	    	//generate and send random number
		    Random random=new Random();
		    Date date= new Date();
		    Timestamp now=new Timestamp(date.getTime());
		    random.setSeed(now.getTime());
		    int randomNumber=random.nextInt();
		    byte[] rN=rsa.encrypt(randomNumber,publicKey);
		     for(int j=0;j<rN.length;j++){
		    	 out.println(rN[j]);
		     }
	    	
	    	//read my random number modified by supernode
		    b=new byte[256];
		    for(i=0;i<256;i++){
		    	b[i]=(byte) Integer.parseInt(in.readLine());
		    }
		    b=rsa.decrypt(b);
		    int randomNumberReceived=(b[0] << 24)
            + ((b[1] & 0xFF) << 16)
            + ((b[2] & 0xFF) << 8)
            + (b[3] & 0xFF);
		    
	    	if (randomNumber-30==(randomNumberReceived)){
	    		System.out.println("[INFO] Challenge successfully completed.");

		    	//read the supernode random number
		    	b=new byte[256];
			    for(i=0;i<256;i++){
			    	b[i]=(byte) Integer.parseInt(in.readLine());
			    }
			    b=rsa.decrypt(b);
			    randomNumberReceived=(b[0] << 24)
	            + ((b[1] & 0xFF) << 16)
	            + ((b[2] & 0xFF) << 8)
	            + (b[3] & 0xFF);
			    //modify the random supernode random number
			    randomNumberReceived = randomNumberReceived - 30;
			    //send back the supernode random  number
			    rN=rsa.encrypt(randomNumberReceived,publicKey);
			     for(int j=0;j<rN.length;j++){
			    	 out.println(rN[j]);
			     }
		    	
			    //read the synchronous key length
			    length = Integer.parseInt(in.readLine());
		    	bytes = new byte[length];
		    	//build the key array of bytes 
		    	for(i=0;i<length;i++){
		    		bytes[i]=(byte)Integer.parseInt(in.readLine());;
		    	}
		    	//set the common synchronous key
			     this.security.setSecurity(rsa.decrypt(bytes));
			     
			     //send credentials to supernode
			     //send username
			     this.sendMessage("node",this.security,this.out);
			     this.sendMessage(username,this.security,this.out);
			     this.sendMessage(pwd,this.security,this.out);
			     
			     String reply = this.readMessage(this.security,this.in);
			     if(reply.contains("denied"))
			    	 result = 0;
			     else
			    	 result = 1;
	    	}
	    	else
	    	{
	    		System.out.println("[ERR] Challenge error.");
	    		this.closeConnection();
	    		result = 0;
	    	}	    	
		  
		   } catch (UnknownHostException e) {
		     System.out.println("[ERR] Unknown host:"+supernodeIp);
		     System.exit(1);
		   } catch  (IOException e) {
		     System.out.println("[ERR] No I/O");
		     System.exit(1);
		   } catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		   return result;
	}
	
	public void closeConnection(){
	try{
		try {
			
			synchronized(this.uploadList){

				while(!this.uploadList.isEmpty()){					
						this.uploadList.wait();
						
					}	
			}
			while(!this.downloadList.isExitAllDownloads()){
				synchronized(this.downloadList){
					this.downloadList.wait();
				}	
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		out.close();
		in.close();
		socket.close();
	 } 
    catch (IOException e) {
       System.out.println(e);
    }
	}
	

	public void sendMessage(Object o, Security security, PrintWriter out){
		byte[] bytes = null;
		//encrypt the message
		if(o instanceof String){
			bytes= security.encrypt((String)o);
		}else if(o instanceof byte[]){
			bytes=security.encrypt((byte[])o);
		}
		if(bytes!=null){
			//send the array length
			out.println(bytes.length);
			//send the array
			for(int i=0;i<bytes.length;i++){
		    	 out.println(bytes[i]);
		     }
		}
	}
	public byte[] readBytes(Security security, BufferedReader in){
		int length;
		byte[] bytes = null;
		String len;
		try {
			do{
				len = in.readLine();
			}while(len == null);
			length = Integer.parseInt(len);
			bytes = new byte[length];
	    	//build the key array of bytes 
	    	for(int i=0;i<length;i++){
	    		bytes[i]=(byte)Integer.parseInt(in.readLine());;
	    	}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//decrypt the message
		return security.decryptByte(bytes);
			
	}
	public String readMessage(Security security, BufferedReader in){ 
		int length;
		byte[] bytes = null;
		String len;
		try {
			do{
				len = in.readLine();
			}while(len == null);
			length = Integer.parseInt(len);
			bytes = new byte[length];
	    	//build the key array of bytes 
	    	for(int i=0;i<length;i++){
	    		bytes[i]=(byte)Integer.parseInt(in.readLine());;
	    	}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//decrypt the message
		String decrMsg = security.decrypt(bytes);
		return decrMsg;    	
	}

	//interaction and key exchange between nodes
	private void connectNode(BufferedReader in, PrintWriter out, Security securityNode){
		int i;
		
		try {
			//receiving public key of the node connected
	    	int length;
			length = Integer.parseInt(in.readLine());
			byte[] bytes=new byte[length];
	    	for(i=0;i<length;i++){
	    		bytes[i]=(byte)Integer.parseInt(in.readLine());
	    	}
	    	//public key from bytes received
	        KeySpec keyspec = new X509EncodedKeySpec(bytes);
	    	KeyFactory keyFactory = KeyFactory.getInstance("RSA");			   
	    	PublicKey publicKey = keyFactory.generatePublic(keyspec);
	    	
		     //send the public key length
		    byte[] bytesLength=rsa.encrypt(length,publicKey);
		     for(i=0;i<bytesLength.length;i++){
		    	 out.println(bytesLength[i]);
		     }
	    	//send my PublicKey
		    length=this.rsa.getPublicKey().getEncoded().length;
		    bytes=this.rsa.getPublicKey().getEncoded();
		     //send the public key bytes
		     for(i=0;i<length;i++){
		    	 out.println(bytes[i]);
		     }   
		     //receiving and decypting random number
		     byte[] b = new byte[256];
			    for(i=0;i<256;i++){
			    	b[i]=(byte) Integer.parseInt(in.readLine());
			    }
			    b=rsa.decrypt(b);
			    int randomNumberReceived=(b[0] << 24)
	            + ((b[1] & 0xFF) << 16)
	            + ((b[2] & 0xFF) << 8)
	            + (b[3] & 0xFF);
			    
			randomNumberReceived-=30;
			//sending random number modified
	    	bytes = rsa.encrypt(randomNumberReceived, publicKey);
		     for(i=0;i<bytes.length;i++){
		    	 out.println(bytes[i]);
		     }
			//sending my random number
		    Random random=new Random();
		    Date date= new Date();
		    Timestamp now=new Timestamp(date.getTime());
		    random.setSeed(now.getTime());
		    int randomNumber=random.nextInt();
	    	bytes = rsa.encrypt(randomNumber, publicKey);
		     for(i=0;i<bytes.length;i++){
		    	 out.println(bytes[i]);
		     }
		     //receiving and decypting my random number
		     b = new byte[256];
			    for(i=0;i<256;i++){
			    	b[i]=(byte) Integer.parseInt(in.readLine());
			    }
			    b=rsa.decrypt(b);
			    randomNumberReceived=(b[0] << 24)
	            + ((b[1] & 0xFF) << 16)
	            + ((b[2] & 0xFF) << 8)
	            + (b[3] & 0xFF);
			 //check number received
			    if((randomNumber-30)==randomNumberReceived){
			    	System.out.println("[INFO] Challenge accepted");
			    }
	    	//send synchronous key encrypted
	    	 bytes=securityNode.getKey().getEncoded();
	    	 bytes=this.rsa.encrypt(bytes, publicKey);

	    	 length = bytes.length;
		     out.println(length);
		     for(i=0;i<length;i++){
		    	 out.println(bytes[i]);
		     }			
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}   	
	}
	
	public void exit() {
		synchronized(this.publishedFilesList){
			this.publishedFilesList = new ArrayList<String>();
		}
		this.downloadList.setExit(true);
	}

	private synchronized boolean checkAlreadyPublished(String filename){
		//check if the file is already published
		  boolean published = false;
		  String file, temp;
		  synchronized(publishedFilesList){
			  Iterator<String> iter = publishedFilesList.iterator();			    			  
	    	  while(iter.hasNext()){
	    		  temp = iter.next();
	    		  file = Utility.returnFilename(temp);
	    		  if(file.equals(filename))
	    			  published = true;
	    	  }	 
		  }	
		  return published;
	}
	
	public class DownloadManagerThread extends Thread{
		private List<InetAddress> nodeIp;
		private String filename;	
		
		public DownloadManagerThread(List<InetAddress> nodeIp, String filename) {
			super();
			this.nodeIp = nodeIp;
			this.filename = filename;
		}

		public void run(){
			try {
				int i=0;
				String response;
				Security securityNode;
				PrintWriter out;
				BufferedReader in;
				do{				
					socket = new Socket(nodeIp.get(i), 5555);
					out = new PrintWriter(socket.getOutputStream(), true);
				    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				    securityNode = new Security();
				    joinNode(in, out, securityNode);	
				    
					System.out.println("[INFO] Node connected...");					
					
				    sendMessage("fetch /"+ filename, securityNode, out);
				    
				    //receive md5 filename
				    response = readMessage(securityNode, in);	
				    if(response.equals("unpublished"))
				    	nodeIp.remove(i);
				    else
				    	i++;
				    
				}while(response.equals("unpublished") && i!=nodeIp.size()+1);

				if(!response.equals("unpublished"))
				{
					String md5filename = response;
				    OutputStream fos = new FileOutputStream(Utility.getDirectory()+md5filename);
					PrintStream p = new PrintStream(fos);
					String temp = readMessage(securityNode, in);
					//receive md5 file
					while(!temp.equals("end")){	
						p.append(temp);
						temp = readMessage(securityNode, in);
					}
					fos.close();
					p.close();

					//receive the number of chunks
					String nChunkString = readMessage(securityNode, in);
					int nChunk = Integer.parseInt(nChunkString);		
					
					//create chunk HashMap for the file

					downloadList.addActiveDownload(this.filename, nChunk);		
					//create md5 HashMap 
					FileReader fr = new FileReader(Utility.getDirectory()+md5filename);			     
				    BufferedReader br = new BufferedReader(fr);
				    String stringRead = br.readLine( );
				    String numString, digest;
				    int num;
				     while( stringRead != null )
				     {
				       StringTokenizer st = new StringTokenizer( stringRead, "," );
				       numString = st.nextToken();
				       num = Integer.parseInt(numString);
				       digest = st.nextToken();
				       downloadList.md5put(filename, num, digest);
				       stringRead = br.readLine( );		       
				     }
				     
				    List<FetchThread> tList = new ArrayList<FetchThread>();
					Iterator<InetAddress> iter = nodeIp.iterator();
					while(iter.hasNext()){
						FetchThread fT = new FetchThread(iter.next(), filename);
						fT.start();
						tList.add(fT);
					}
					//set the number of active threads for the file
					downloadList.setnThread(filename, tList.size());
					
					//check if the download is completed 				
					do{							
						try {
							synchronized(downloadList){
								downloadList.wait(5000);
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if(!Utility.checkThreadAlive(tList)&&!downloadList.isExit()&&!downloadList.isCompleted(filename)){
							nodeIp=search(filename);
						    tList = new ArrayList<FetchThread>();
						    if(nodeIp!=null){
								iter = nodeIp.iterator();
								while(iter.hasNext()){
									FetchThread fT = new FetchThread(iter.next(), filename);
									fT.start();
									tList.add(fT);
								}
								//set the number of active threads for the file
								downloadList.setnThread(filename, tList.size());
						    }
						}
					}while((Utility.checkThreadAlive(tList)||!downloadList.isExit()) && !downloadList.isCompleted(filename));				
					if(downloadList.isCompleted(filename)){
						//download completed, so join the chunks
						joinChunks(filename);
						//download removed from the active dowload list
						downloadList.remove(filename);
						//automatically publish the downloaded file
						publish(Utility.getDirectory()+filename);
					}else{
						if(downloadList.isExit()){
							/*downloadList.remove(filename);*///questa istruzione posso toglierla, se la faccio in isExitAllDownload
							downloadList.setExited(filename);
						}else{
							downloadList.remove(filename);
						}
						
					}
				}
				
					
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	//thread used for listen the incoming requests
	public class ListenThread extends Thread{

		public void run(){
			try {
				Socket client;
				ServerSocket server = new ServerSocket(5555);
				System.out.println("[INFO] ListenThread activated.\n");				
				
				while(true){
					client = server.accept();
					ResponseThread rt = new ResponseThread(client);
					rt.start();
				}
			
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}
	
	
	//thread called when a request reach the node
	public class ResponseThread extends Thread{
		private Socket client; 
		private String command;
		//attribute used for establish a secure channel between the nodes
		private Security securityNode;
		
		public ResponseThread(Socket socket){
			this.client = socket;
			this.securityNode = new Security();
		}
		
		//handle the request
		public void run(){
			String path = null, temp;
			int numRead = 0;
			try{
			      BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			      PrintWriter out = new PrintWriter(client.getOutputStream(), true);
			      connectNode(in, out, this.securityNode);
			      System.out.println("[INFO] Handling request from " + client.getInetAddress());
			      command =  readMessage(this.securityNode, in);			      
			      System.out.println("[INFO] Message received: " + command);	
			      String filename = command.substring(command.indexOf('/') + 1, command.length());
			      if(command.contains("fetch")){			    	 
			    	  //check if the file is already published
			    	  synchronized(Node.this){
				    	  Iterator<String> iter = publishedFilesList.iterator();
							do{
								temp = iter.next();
								if(temp.contains(filename))
									path = temp;
							}while(iter.hasNext());
			    	  }
			    	  if(path != null)
			    	  {
			    		  String md5Filename = Utility.getDirectory()+filename+"_md5";
			    		  int nChunk = Utility.createChunksMD5(path);
			    		  md5Filename = Utility.returnFilename(md5Filename);
			    		  //send the md5 filename
			    		  sendMessage(md5Filename, securityNode, out);
			    		  //send the md5 file
			    		  md5Filename = Utility.getDirectory()+filename+"_md5";
			    		  InputStream fis = new FileInputStream(md5Filename);	
			    		  do {
					    	   byte[] buffer = new byte[262144]; //256KB
					           numRead = fis.read(buffer);
					           
					           if (numRead > 0) {
					        	   //if the buffer is not full
					        	   if(numRead < 262144)
					        	   {
					        		  byte[] temp2 = new byte[numRead];
					        		  for(int j=0; j<numRead; j++)
					        			  temp2[j] = buffer[j];
					        		  sendMessage(temp2, securityNode, out);
					        	   }
					        	   else
					        		   sendMessage(buffer, securityNode, out);
					           }				        	  				       
					       } while (numRead != -1);	     
			    		  
					       sendMessage("end", securityNode, out);
					       //send the number of chunks
					       sendMessage(nChunk+"", securityNode, out);
					       
					       fis.close();		
			    	  }
			    	  else
			    	  {
			    		  System.out.println("[INFO] File not found...");
			    		  sendMessage("unpublished", securityNode, out);
			    	  }
			      }
			      else{
			    	  if(command.contains("chunk") && checkAlreadyPublished(filename)){				    	 
				    	  int chunk, i;
				    	  long nSkipped = 0;
				    	  boolean published = true;	
				    	  String substring;
				    
				    	  //get the real path
				    	  String realPath = "";
				    	  for(i=0; i< publishedFilesList.size() ; i++){
				    		  substring = Utility.returnFilename(publishedFilesList.get(i));
				    		  if(substring.equals(filename))
				    			  realPath = publishedFilesList.get(i);
				    	  }			    	  
					      //a request of download is active
					      synchronized(uploadList){
					    	  uploadList.add(filename);
					      }
			    		//send the chunks				    	  
				    	  command = readMessage(securityNode, in);
				    	  InputStream fis;
				    	  byte[] buffer;
				    	  while(!command.contains("stop") && published){		
				    			    		  
				    		  buffer = new byte[262144];
				    		  fis = new FileInputStream(realPath);
				    		  
				    		  command = command.substring(command.lastIndexOf(':') +1, command.length());
				    		  chunk = Integer.parseInt(command);
				    		  if(chunk!=0)
				    			  nSkipped = (int) fis.skip(chunk*262144);			    		 
				    		  
				    		  if(nSkipped == chunk*262144){
				    			  numRead = fis.read(buffer, 0, 262144);			    			
				    		  }
				    		  else
				    			  System.out.println("[ERR] Skip error.");	
				    		  System.out.println("[INFO] Filename: " + filename + " Chunk #: " + chunk);
					          if (numRead > 0) {
					        	   //if the buffer is not full
					        	   if(numRead < 262144)
					        	   {
					        		   byte[] temp2 = new byte[numRead];
					        		   for(int j=0; j<numRead; j++)
					        			   temp2[j] = buffer[j];
					        		   sendMessage("send", securityNode, out);
					        		   sendMessage(temp2, securityNode, out);
					        	   }
					        	   else
					        	   {
					        		   sendMessage("send", securityNode, out);
					        		   sendMessage(buffer, securityNode, out);
					        	   }				        		   
					           }	
					          command = readMessage(securityNode, in);
					          fis.close();
					          
					        //check if the file is already published
				    		  published = checkAlreadyPublished(filename);			    	  
				    	  }
				    	  if(!published){
				    		  sendMessage("unpublished", securityNode, out);
				    	  }	 
			    		  synchronized(uploadList){
			    		      uploadList.remove(filename);
			    			  uploadList.notifyAll();
			    		  }
				      }
				      else
				      {				    	  
				    	  sendMessage("unpublished", securityNode, out);
				      }	
			      }
			      	      		      
			} catch (IOException e) {
			      System.out.println("[ERR] Accept failed: 4444");
			      e.printStackTrace();
			      System.exit(-1);
			}
			
		}
	}

	public class FetchThread extends Thread{
		private InetAddress ip;
		private String filename;
		
		public FetchThread(InetAddress ip, String filename){
			this.ip = ip;
			this.filename = filename;
		}
		
		public void run(){
			Socket socket;
			boolean published = true;
			int chunk;
			byte[] bytes = new byte[262144];
			try {
				socket = new Socket(this.ip, 5555);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			    Security securityNode = new Security();
			    joinNode(in, out, securityNode);		
			    sendMessage("chunk " + "/" +filename, securityNode, out);		    
		    	do
		    	{
		    		chunk = downloadList.getChunkNotDownloaded(filename);
		    		if(!downloadList.isCompleted(filename)&&chunk!=-1)
		    		{
		    			OutputStream fos = new FileOutputStream(Utility.getDirectory()+filename+"_part"+chunk);
		    			PrintStream p = new PrintStream(fos);
		    			//request the chunk #chunk
		    			sendMessage("chunk:"+chunk, securityNode, out);	
		    			String reply = readMessage(securityNode, in);
		    			if(reply.equals("unpublished")){
		    				published = false;
		    				fos.close();
		    				p.close();
		    				File f = new File(Utility.getDirectory()+filename+"_part"+chunk);
		    				f.delete();
		    				downloadList.resetChunk(filename,chunk);
		    			}
		    			else // receive the chunk requested
		    			{
		    				bytes = readBytes(securityNode, in);
			    			p.write(bytes);
			    			System.out.println("[INFO] Filename: " + filename + " Bytes received size: " + bytes.length);
			    			
			    			try {
								MessageDigest md = MessageDigest.getInstance("MD5");
								md.reset();
								bytes = md.digest(bytes);	
								//convert the byte to hex format 
					            StringBuffer sb = new StringBuffer();
					            for (int j = 0; j < bytes.length; j++) {
					               sb.append(Integer.toString((bytes[j] & 0xff) + 0x100, 16).substring(1));
					            }
					            if(downloadList.checkMd5(filename,chunk,sb.toString())){
					            	System.out.println("[INFO] Filename: " + filename + " Digest chunk correct.");
					    			System.out.println("[INFO] Filename: " + filename  + "  Chunk #"+chunk+" created.");
					    			//set the chunk completed and check if the download is completed
					    			downloadList.chunkComplete(filename, chunk);
					            }	
					            else{
					            	downloadList.resetChunk(filename, chunk);
					            }
					            	
								
							} catch (NoSuchAlgorithmException e) {
								e.printStackTrace();
							}
			    			fos.flush();
			    			fos.close();
			    			p.flush();
			    			p.close();					    			
		    			}	
		    			
		    		}	    	
		    	}while(!downloadList.isCompleted(filename) && published && !downloadList.isExit());
		    	
		    	sendMessage("stop", securityNode, out); 
	    		downloadList.removeDownloadThread(filename);
		    	   
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}	   
		}	
	}
}
