package fr.usmb.dht;

import java.util.ArrayList;
import java.util.List;

public class Message {

    public MessageType type;
    private Object content;
    
    private DhtNode target;

    private DhtNode[] neighbors;
    
    private ArrayList<DhtNode> senders = new ArrayList<>();
    
    private int remaining = 0;
    
    private Message data;
    
    private int id;
    
	Message(MessageType type, Object content) {
    	this.type = type;
		this.content = content;
    }
    
    Message(MessageType type, Object content, DhtNode nodeToPlace) {
    	this.type = type;
		this.content = content;
		this.target = nodeToPlace;
    }
    
    Message(MessageType type, Object content, Message data) {
    	this.type = type;
		this.content = content;
		this.data = data;
    }
    
    Message(MessageType type, Object content, DhtNode[] neighbors) {
    	this.type = type;
		this.content = content;
		this.neighbors = neighbors;
    }
    
    public Message data2Save() {
    	return this.data;
    }
    
    public boolean isType(MessageType type) {
    	return this.type == type;
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
    
    public DhtNode getFirstSender() {
    	return this.senders.get(0);
    }
    
    public void setRemaining(int remaining) {
    	this.remaining = remaining;
    }
    
    public Integer getRemaining() {
    	return this.remaining;
    }
    
    public void decreaseRemaining() {
    	this.remaining--;
    }

    public Object getContent() {
    	return this.content;
    }
    
    public DhtNode getTarget() {
    	return this.target;
    }
    
    public void setTarget(DhtNode target) {
    	this.target = target;
    }
    
    public DhtNode[] getNeighbors() {
    	return this.neighbors;
    }
    
    public MessageType getType() {
    	return this.type;
    }
    
    public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

    
    public String toString() {
    	
    	return "[" + this.type.getDescription() + "] from " + this.senders.get(0) + " (Uid : " + this.senders.get(0).getUid()+ ")";

    }

    
}