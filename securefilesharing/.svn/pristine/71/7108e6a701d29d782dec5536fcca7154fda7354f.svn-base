package it.polimi.core;

public class Credential {
	private String user;
	private boolean granted;
	private boolean denied;
	public Credential(String user){
		this.user=user;
		this.granted=false;
		this.denied=false;//TODO:inserito perche' dico che l'accesso e' negato solamente dopo che la richiesta e' 
		//passata per tutti i supernodi.
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public boolean isGranted() {
		return granted;
	}

	public void setGranted(boolean granted) {
		this.granted = granted;
	}

	public boolean isDenied() {
		return denied;
	}

	public void setDenied(boolean denied) {
		this.denied = denied;
	}
	
}
