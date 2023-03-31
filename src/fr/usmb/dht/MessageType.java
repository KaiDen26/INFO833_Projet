package fr.usmb.dht;

public enum MessageType {
	
	HELLOWORLD("HelloWorld"), JOIN("Join"), LEAVE("Leave"), PLACE_RIGHT("Place right"), PLACE_LEFT("Place left"), PLACE_BOTH("Place both"), SHOW_TREE("Show Tree"), ADD_DATA("Add Data"), ADD_ALL_DATA("Add All Data"), REMOVE_DATA("Remove Data"), DELIVER("Deliver Message"), CREATE_LINK("Create Link"), REMOVE_LINK("Remove Link"), COLLECT_DATA("Collect Data"), GET("Get Data");
	
	 /*
     * Description d'un type d'opération
     */
	private String description;
	
	 /*
     * Constructeur
     */
	MessageType(String description){
		this.description = description;
	}
	
	 /*
     * Retourne la description
     */
	public String getDescription() {
		return this.description;
	}

}
