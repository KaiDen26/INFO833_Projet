package fr.usmb.dht;

public enum MessageType {
	
	HELLOWORLD("HelloWorld"), JOIN("Join"), LEAVE("Leave"), PLACE_RIGHT("Place right"), PLACE_LEFT("Place left"), PLACE_BOTH("Place both"), SHOW_TREE("Show Tree"), ADD_DATA("Add Data"), ADD_ALL_DATA("Add All Data"), REMOVE_DATA("Remove Data"), DELIVER("Deliver Message"), CREATE_LINK("Create Link"), REMOVE_LINK("Remove Link");
	
	private String description;
	
	MessageType(String description){
		this.description = description;
	}
	
	public String getDescription() {
		return this.description;
	}

}
