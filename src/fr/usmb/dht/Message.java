package fr.usmb.dht;

public class Message {

    public MessageType type;
    private String content;
    
    private DhtNode nodeToPlace;

    private DhtNode[] neighbors;
    
    Message(MessageType type, String content) {
    	this.type = type;
		this.content = content;
    }
    
    Message(MessageType type, String content, DhtNode nodeToPlace) {
    	this.type = type;
		this.content = content;
		this.nodeToPlace = nodeToPlace;
    }
    
    Message(MessageType type, String content, DhtNode[] neighbors) {
    	this.type = type;
		this.content = content;
		this.neighbors = neighbors;
    }

    public String getContent() {
    	return this.content;
    }
    
    public DhtNode getNodeToPlace() {
    	return this.nodeToPlace;
    }
    
    public DhtNode[] getNeighbors() {
    	return this.neighbors;
    }
    
    public MessageType getType() {
    	return this.type;
    }

    
}