package it.polimi.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {


	public static void main(String[] args) throws IOException{
		int choice = -1;
		int result = 0;
		if((args.length != 0) && (args[0].equals("node")))
		{
			Node node = new Node();				
			
			String str="";
			File directory=new File(Utility.directory);
			directory.mkdir();
			InputStreamReader reader = new InputStreamReader (System.in);
			BufferedReader systemInput = new BufferedReader (reader);		
			//join the known supernode
			//Supernode IP, username, password
			result = node.join(args[1], args[2], args[3]);	
			
			
			if(result == 0)
			{
				System.out.println("[INFO] Access denied, exiting...");
			}
			else
			{	
				System.out.println("\n[INFO] Access granted");
				node.publishOnStart();				
				System.out.println("\n** SecureFileSharing: application started in node mode **\n");				
				do{			
					System.out.println("Choose what you want to do:\n");
					System.out.println("\n 1 - Publish a file");
					System.out.println("\n 2 - Unpublish");
					System.out.println("\n 3 - Search");	
					System.out.println("\n 0 - Exit ");
					
					str = systemInput.readLine();
					
					choice = Utility.getChoice(str);;
					switch(choice){
					case 1:
					{
						node.publish();
						break;
					}
					case 2:
					{
						node.unpublish();
						break;
					}
					case 3:
					{
						node.search();
						break;
					}
					case 0:
					{
						System.out.println("\n[INFO] Exiting... ");
						node.sendMessage("exit",node.security,node.out);
						//the supernode automatically executes the leave
						String res = node.readMessage(node.security,node.in);
						if(res.equals("removed_all"))
						{
							System.out.println("[INFO] Your files have been unpublished.");
							node.exit();
						}
						break;
					}
					default:
					{
						System.out.println("[INFO] Typing error. Retry..");
						break;
						}
					}				
				}while(choice != 0);
			}
			node.closeConnection();
			
			System.exit(1); 
			
		}
		else
		{
			System.out.println("\n** SecureFileSharing: application started in supernode mode **\n");
			Supernode sn = new Supernode();			
			sn.listenSocket(args[0], args[1]);
		}		
	}

}
