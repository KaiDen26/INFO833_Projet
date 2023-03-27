package fr.usmb.dht;

import java.util.ArrayList;
import java.util.List;

public class Message {

    public MessageType type;
    private String content;
    
    private DhtNode nodeToPlace;

    private DhtNode[] neighbors;
    
    private ArrayList<DhtNode> senders = new ArrayList<>();
    
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
    
    public void addSender(DhtNode node) {
    	this.senders.add(node);
    }
    
    public List<DhtNode> getSenders(){
	   return this.senders;
    }
    
    public DhtNode getLastSender() {
    	return this.senders.get(senders.size() - 1);
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