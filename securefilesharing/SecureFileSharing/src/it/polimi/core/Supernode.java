package it.polimi.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

class Supernode extends Node  implements Runnable {
	protected List<FileOnline> onlineFileList;
	private FileSearch fileResult;
	private ServerSocket server = null;
	private String nearSuperNodeIP;
	private PrintWriter outSupernode;
	private BufferedReader inSupernode;
	private Socket socketSupernode;
	private Socket client;	
	private List<Credential> userToCheck;
	private String superNodeIP;
	private Security securitySupernode;
	
	public Supernode(){
		
	}
	  
    public Supernode(Socket client,Vector<FileOnline> list, FileSearch search, String superNodeIP, String nearSuperNodeIP,Vector<Credential> userToCheck) {
    	this.nearSuperNodeIP = nearSuperNodeIP;
		this.superNodeIP = superNodeIP;
        this.client = client;
		this.rsa=new RSA();
		this.security=new Security();
		this.securitySupernode = new Security();
		this.onlineFileList = list;
		this.userToCheck=userToCheck;
		this.fileResult = search;
    }
	
	
	public boolean addFileOnline(FileOnline file){
		FileOnline copy = file.copy(file);	
		if(!this.onlineFileList.contains(file)){
			synchronized(this.onlineFileList){
				this.onlineFileList.add(copy);	
			}
			return true;
		}
		return false;
	}	
	
	/*remove a specific file of a node*/
	public  void removeFileOnline(InetAddress ip, String filename){
		for(int i=0; i < this.onlineFileList.size(); i++)
		{
			if(this.onlineFileList.get(i).checkFile(ip, filename))
			{		
				synchronized(this.onlineFileList){
					this.onlineFileList.remove(i);	
				}				
			}
		}	
	}
	
	/*remove all the file for a given ip */
	public void removeAll(InetAddress ip){
		for(int i=0; i < this.onlineFileList.size();)
		{
			if(this.onlineFileList.get(i).getIpClient().equals(ip))
			{
				synchronized(this.onlineFileList){
					this.onlineFileList.remove(this.onlineFileList.get(i));
				}
				
			}
			else
				i++;
		}
		this.sendMessage("removed_all",this.security,this.out);
	}	
	
	public void printFileOnline(){
		
		System.out.println("**Published file list**");
		for(int i=0; i < this.onlineFileList.size(); i++)
		{
			synchronized(this.onlineFileList){
				System.out.println("File: " + this.onlineFileList.get(i).getFilename() + ", node: " + this.onlineFileList.get(i).getIpClient());
			}
		}
		System.out.println("**-------------------**");
	}
		  
		  @Override
		public void run(){	
			  	int i;
			  	String result,filename,sourceIp;
			    try{
			        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			        out = new PrintWriter(client.getOutputStream(), true);
			    	System.out.println("[INFO] Node " + client.getInetAddress() + " connected...");
			    	//receiving public key of the node connected
			    	int length=Integer.parseInt(in.readLine());

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
			    	//send Supernode PublicKey
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
					    	System.out.println("[INFO] Challenge ok.");
					    }
					    else{
					    	System.out.println("[ERR] Challenge failed!");
					    	this.closeConnection();
					    }
			    	//send synchronous key encrypted
			    	 bytes=this.security.getKey().getEncoded();
			    	 bytes=this.rsa.encrypt(bytes, publicKey);

			    	 length = bytes.length;
				     out.println(length);
				     for(i=0;i<length;i++){
				    	 out.println(bytes[i]);
				     }
				     String typeOfClient=readMessage(this.security,this.in);
				    
				     if(typeOfClient.equals("node")){
				    	 String userReceived=readMessage(this.security,this.in),
				    	 pwdReceived=readMessage(this.security,this.in); 
				    	 if (!new File("credentials.csv" ).exists())
						 {
				    		 sendMessage("access denied",this.security,this.out);
						 }
				    	 else
				    	 {
				    		 FileReader fr = new FileReader("credentials.csv" );//da mettere nella stessa path del jar.					     
						     BufferedReader br = new BufferedReader( fr );
						     String stringRead = br.readLine( );
						     boolean granted=false,userInList=false;
						     String user="";
						     String pwd="";
						     while( stringRead != null )
						     {
						       StringTokenizer st = new StringTokenizer( stringRead, "," );
						       user = st.nextToken( );
						       pwd = st.nextToken( );
						       stringRead = br.readLine( );
						       if(user.equals(userReceived)){
						    	   userInList=true;
						    	   if(pwd.equals(pwdReceived)){
						    		   granted=true;
						    	   }
						       }
						     }
		
						     if(userInList){
						    	 System.out.println("[INFO] User checked on this supernode.");
						    	 if(granted){
						    	 sendMessage("access granted",this.security,this.out);
						    	 }
						    	 else{
							    	 sendMessage("access denied",this.security,this.out);
							     }
						     }else{//forward credentials to near supernode
						    	 System.out.println("[INFO] User credentials for "+userReceived+" sent to supernode "+this.nearSuperNodeIP);
						    	 Credential c=new Credential(userReceived);
						    	 synchronized(this.userToCheck){
						    		 this.userToCheck.add(c);
						    	 }						    	 
						    	 forwardCredential(userReceived, pwdReceived, this.superNodeIP);
						    	 do{
						    		synchronized(this.userToCheck){ 
						    			this.userToCheck.wait();
						    		}
						    	 }while(!c.isDenied()&&!c.isGranted());
						    	 
						    	 synchronized(this.userToCheck){ 
						    		 this.userToCheck.remove(c);
						    	 }
						    	 
						    	 if(c.isDenied()){
							    	 sendMessage("access denied",this.security,this.out);
						    	 }else{
						    		 sendMessage("access granted",this.security,this.out);
						    	 }
						     }
				    	 }
				    	 
				    	 String decrMsg; 
					     String substring;
					     boolean added;
					     InetAddress clientIP = client.getInetAddress();
				    	do{	  

				    		//riceiving bytes encypted with synchronous key.
				    	    decrMsg = readMessage(this.security,this.in);	
				    	    System.out.println("[INFO] Message received: "+decrMsg);
				    	    switch( Utility.getAction(decrMsg)){
				    	    case 1: //publish
				    	    {
				    	    	substring = decrMsg.substring(decrMsg.indexOf('/') + 1, decrMsg.length());			    	    	
				    	    	added = addFileOnline(new FileOnline(clientIP, substring));
				    	    	if(added)
				    	    	{
				    	    		System.out.println("File " + substring + " from node " + clientIP + " added to the list.");
					    	    	printFileOnline();
					    	    	this.sendMessage("published",this.security,this.out);					   
				    	    	}		
				    	    	else				    
				    	    		this.sendMessage("already published",this.security,this.out);
				    	    	break;			    	    	
				    	    }
				    	    case 2: //unpublish
				    	    {
				    	    	substring = decrMsg.substring(decrMsg.indexOf('/') + 1, decrMsg.length());				    	    	
				    	    	removeFileOnline(clientIP, substring);			    	    		    	    	
				    	    	System.out.println("File " + substring + " of node " + clientIP + " removed from the list.");
				    	    	this.sendMessage("unpublished",this.security,this.out);
				    	    	printFileOnline();
				    	    	break;
				    	    }
				    	    case 3: //search
				    	    {
				    	    	substring = decrMsg.substring(decrMsg.indexOf('/') + 1, decrMsg.length());
				    	    	this.search(substring);
				    	    	break;
				    	    }
				    	    case 4: //fetch
				    	    {
				    	    	break;
				    	    }
				    	    }	    	
				       
						}while(!decrMsg.equals("exit"));
				    	
				    	//leave the supernode
				    	this.leave();
				    	printFileOnline();
					     
				     }else{//request connection from supernode
				    	 String received=readMessage(security, in);
				    	 //check the credentials 
				    	 if(received.contains("credentials")){
						     FileReader fr = new FileReader("credentials.csv" );//da mettere nella stessa path del jar.
						     BufferedReader br = new BufferedReader( fr );
						     String stringRead = br.readLine( );
						     boolean granted=false,userInList=false;
						     String user="";
						     String pwd="";
						     String userReceived=readMessage(security, in);
						     String pwdReceived=readMessage(security, in);
						     String IpSuperNodeRequest=readMessage(security, in);
					
				    		 if(!IpSuperNodeRequest.equals(this.superNodeIP)){
				    			 System.out.println("[INFO] Checking credentials for user " + userReceived
				    					 +" on this supernode.");
							     while(stringRead != null)
							     {
							       StringTokenizer st = new StringTokenizer(stringRead, ",");
							       user = st.nextToken();
							       pwd = st.nextToken();
							       stringRead = br.readLine();
							       if(user.equals(userReceived)){
							    	   userInList=true;
							    	   if(pwd.equals(pwdReceived)){
							    		   granted=true;
							    	   }
							       }
							     }
			
							     if(userInList){
							    	 if(granted){
							    		 sendResponse(userReceived,IpSuperNodeRequest,"access granted");
							    		 System.out.println("[INFO] Access granted.");
							    	 }
							    	 else{
							    		 sendResponse(userReceived,IpSuperNodeRequest,"access denied");
							    		 System.out.println("[INFO] Access denied.");
								     }
							     }else{//forward credentials to near supernode
							    	 System.out.println("Credentials for user "+userReceived+" sent to supernode "+this.nearSuperNodeIP);
							    	 forwardCredential(userReceived, pwdReceived, IpSuperNodeRequest);
							     }
					    	 }else{
					    		 Credential credential;
					    		 synchronized(this.userToCheck){
					    			 for(i=0;i<this.userToCheck.size();i++){
					    				 credential=this.userToCheck.get(i);
					    				 if(credential.getUser().equals(userReceived)){
					    					 credential.setDenied(true);
					    						 System.out.println("[INFO] User not in list.");
					    						 this.userToCheck.notifyAll();		    					 
					    				 }
					    			 }
					    		 }
					    	 }
				    	 }else if(received.contains("response")){
				    		 //receive the result of the credential forwarding
				    		 String user,response;
				    		 Credential credential;
				    		 user=readMessage(security, in);
				    		 response=readMessage(security, in);
				    		 synchronized(this.userToCheck){
				    			 for(i=0;i<this.userToCheck.size();i++){
				    				 credential=this.userToCheck.get(i);
				    				 if(credential.getUser().equals(user)){
								    	 if(response.contains("access granted")){
								    		 credential.setGranted(true);
								    	 }
								    	 else{
								    		 credential.setDenied(true);
									     }								    	 
							    		 this.userToCheck.notifyAll();					    	 
				    				 }
				    			 }
				    		 }

				    	 }else{
				    		 // manage search forward
				             filename=this.readMessage( this.security, in);
				             result=this.readMessage(this.security, in);
				             sourceIp=this.readMessage(this.security, in);
				             if(sourceIp.equals(this.superNodeIP)){
				            	 synchronized(this.fileResult){
				            		 this.fileResult.setSearchCompleted(true);
				            		 this.fileResult.setResults( Utility.parseSearchResult(result));
				            		 this.fileResult.notifyAll();
				            	 }
				             }else{
				        		synchronized(this.onlineFileList){
				        			Iterator<FileOnline> iter = this.onlineFileList.iterator();
				        			while(iter.hasNext()){			
				        				FileOnline temp = iter.next();
				        				if(temp.getFilename().contains(filename))
				        				{
				        					result += temp.getFilename() + "IP" + temp.getIpClient() + "$" ;
				        				}			
				        			}
				        		}
				        		forwardSearch(filename,result,sourceIp);
				             }
				    	 }
				    	 
				     }
				     
			      } catch (IOException e) {
			        System.out.println("[ERR] Read failed"+e.toString());
			        System.exit(-1);
			      } catch (InvalidKeySpecException e) {
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		  }
	
	public void listenSocket(String superNodeIP,String nearSuperNodeIP){
		
	    try{
	      server = new ServerSocket(4444); 
	    } catch (IOException e) {
	      System.out.println("[ERR] Could not listen on port 4444");
	      System.exit(-1);
	    }
	    Vector<FileOnline> list= new Vector<FileOnline>();
	    Vector<Credential> userToCheck= new Vector<Credential>();
	    FileSearch search = new FileSearch(); 
	    while(true){
	    	Supernode myt;
	        try {
				myt = new Supernode(server.accept(), list, search, superNodeIP, nearSuperNodeIP, userToCheck);
				Thread t = new Thread(myt);
		        t.start();
			} catch (IOException e) {
				System.out.println("[ERR] Accept failed: 4444");
			    System.exit(-1);
			}
	        
	      }
	    
	  }
	    
	public void forwardCredential(String username,String pwd,String sourceIp){
		try{
		    this.connectionToSupernode(nearSuperNodeIP);     
             //send credentials to supernode
             this.sendMessage("supernode", this.securitySupernode, outSupernode);              
             this.sendMessage("credentials", this.securitySupernode, outSupernode);//operation, puo' essere credential, search
             //send username
             this.sendMessage(username, this.securitySupernode, outSupernode);
             this.sendMessage(pwd, this.securitySupernode, outSupernode);
             this.sendMessage(sourceIp, this.securitySupernode, outSupernode);
             
             outSupernode.close();
             inSupernode.close();
             socketSupernode.close();
		   } catch (UnknownHostException e) {
		     System.out.println("[ERR] Unknown host:"+this.nearSuperNodeIP);
		     System.exit(1);
		   } catch  (IOException e) {
		     System.out.println("[ERR] No I/O"); 
		     System.exit(1);
		   }	
	}
	public void forwardSearch(String filename,String result,String sourceIp){
		try{
		    this.connectionToSupernode(nearSuperNodeIP);     
             //send search query to supernode
             this.sendMessage("supernode", this.securitySupernode, outSupernode);              
             this.sendMessage("search", this.securitySupernode, outSupernode);//operation, puo' essere credential, search
             this.sendMessage(filename, this.securitySupernode, outSupernode);
             this.sendMessage(result, this.securitySupernode, outSupernode);
             this.sendMessage(sourceIp, this.securitySupernode, outSupernode);
             
             outSupernode.close();
             inSupernode.close();
             socketSupernode.close();
		   } catch (UnknownHostException e) {
		     System.out.println("[ERR] Unknown host:"+this.nearSuperNodeIP);
		     System.exit(1);
		   } catch  (IOException e) {
		     System.out.println("[ERR] No I/O"); 
		     System.exit(1);
		   }	
	}
	
	
	public void leave(){
		//remove all the published files of the node
		removeAll(client.getInetAddress());
    	System.out.println("[INFO] Node "+ client.getInetAddress() + " connection closed.");
	}
	
	@Override
	public void publish(){
		
	}
	
	@Override
	public void unpublish(){
		
	}
	
	public void search(String filename){
		String result = "";
		synchronized(this.onlineFileList){
			Iterator<FileOnline> iter = this.onlineFileList.iterator();
			while(iter.hasNext()){			
				FileOnline temp = iter.next();
				if(temp.getFilename().contains(filename))
				{
					result += temp.getFilename() + "IP" + temp.getIpClient() + "$" ;
				}			
			}
		}
		//send to node the list of file published on the local supernode
		this.sendMessage(result, this.security, this.out);
		result=this.readMessage(this.security, this.in);
		//forward the search request to the other supernodes
		if(result.equals("forward search")){
			forwardSearch(filename,"",this.superNodeIP);
			synchronized(this.fileResult){
				do{
					try {
						this.fileResult.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}while(!this.fileResult.isSearchCompleted());
			}
			result=this.fileResult.search(filename);
			this.sendMessage(result, this.security, this.out);
		}
		
	}
	
	@Override
	public void fetch(List<InetAddress> nodeIP, String filename){
		
	}
	
	public void connectionToSupernode(String sourceSuperNodeIp){
		try{
		     socketSupernode = new Socket(InetAddress.getByName(sourceSuperNodeIp), 4444);
		     outSupernode = new PrintWriter(socketSupernode.getOutputStream(), true);
		     inSupernode = new BufferedReader(new InputStreamReader(socketSupernode.getInputStream()));
		     //get the public key bytes
		     byte[] bytes = this.rsa.getPublicKey().getEncoded();
		     int length=this.rsa.getPublicKey().getEncoded().length;
		    
		   //send the public key length
		     outSupernode.println(length);
		     //send the public key bytes
		     for(int i=0;i<length;i++){
		    	 outSupernode.println(bytes[i]);
		     }
		    int i;
		    //read Supernode PublicKey
		    //read the key lenght
		    byte[] b=new byte[256];
		    for(i=0;i<256;i++){
		    	b[i]=(byte) Integer.parseInt(inSupernode.readLine());
		    }
		    b=rsa.decrypt(b);
		    length=(b[0] << 24)
	   + ((b[1] & 0xFF) << 16)
	   + ((b[2] & 0xFF) << 8)
	   + (b[3] & 0xFF);
		    
		    //read the key
		    bytes=new byte[length];
	   	for(i=0;i<length;i++){
	   		bytes[i]=(byte) Integer.parseInt((inSupernode.readLine()));
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
		    	 outSupernode.println(rN[j]);
		     }
	   	
	   	//read my random number modified by supernode
		    b=new byte[256];
		    for(i=0;i<256;i++){
		    	b[i]=(byte) Integer.parseInt(inSupernode.readLine());
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
		    	b[i]=(byte) Integer.parseInt(inSupernode.readLine());
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
		    	 outSupernode.println(rN[j]);
		     }
	    	
		    //read the synchronous key length
		    length = Integer.parseInt(inSupernode.readLine());
	    	bytes = new byte[length];
	    	//build the key array of bytes 
	    	for(i=0;i<length;i++){
	    		bytes[i]=(byte)Integer.parseInt(inSupernode.readLine());;
	    	}
	    	//set the common synchronous key
	    	this.securitySupernode.setSecurity(rsa.decrypt(bytes));
	    	
	   		}
		}catch(UnknownHostException e){
			
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
	public void sendResponse(String user,String sourceSuperNodeIp, String response){
		try{
			this.connectionToSupernode(sourceSuperNodeIp);
           //send the result of the credential checking
            this.sendMessage("supernode", this.securitySupernode, outSupernode);
            this.sendMessage("response", this.securitySupernode, outSupernode);//operation, puo' essere credential, search
            this.sendMessage(user, this.securitySupernode, outSupernode);
            this.sendMessage(response, this.securitySupernode, outSupernode);
        
            outSupernode.close();
            inSupernode.close();
            socketSupernode.close();
         
		   } catch (UnknownHostException e) {
		     System.out.println("[ERR] Unknown host:"+this.nearSuperNodeIP);
		     System.exit(1);
		   } catch  (IOException e) {
		     System.out.println("[ERR] No I/O");
		     System.exit(1);
		   }
	}
}
