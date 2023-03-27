package fr.usmb.dht;

public enum MessageType {
	
	HELLOWORLD("HelloWorld"), JOIN("Join"), LEAVE("Leave"), PLACE_RIGHT("Place right"), PLACE_LEFT("Place left"), PLACE_BOTH("Place both"), SHOW_TREE("Show Tree");
	
	private String description;
	
	MessageType(String description){
		this.description = description;
	}
	
	public String getDescription() {
		return this.description;
	}

}
