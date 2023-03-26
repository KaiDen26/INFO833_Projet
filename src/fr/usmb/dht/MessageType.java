package fr.usmb.dht;

public enum MessageType {
	
	HELLOWORLD(0), JOIN(1), LEAVE(2);
	
	private int id;
	
	MessageType(int id){
		this.id = id;
	}
	
	public Integer getId() {
		return this.id;
	}

}
