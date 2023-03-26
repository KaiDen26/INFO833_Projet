package fr.usmb.dht;

public enum MessageType {
	
	HELLOWORLD("HelloWorld"), JOIN("Join"), LEAVE("Leave"), PLACE_RIGHT("Place à droite"), PLACE_LEFT("Place à gauche"), PLACE_BOTH("Place des deux côtés");
	
	private String description;
	
	MessageType(String description){
		this.description = description;
	}
	
	public String getDescription() {
		return this.description;
	}

}
