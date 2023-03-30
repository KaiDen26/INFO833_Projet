package fr.usmb.dht;

import peersim.edsim.*;
import peersim.core.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import peersim.config.*;

public class DhtNode implements EDProtocol {
    
    // identifiant de la couche transport
    private int transportPid;

    // objet couche transport
    private HWTransport transport;

    // identifiant de la couche courante (la couche applicative)
    private int mypid;

    // le numero de noeud dans l'ordre de création
    private int id;
    
    // l'identifiant du noeud
    private int uid;

	// prefixe de la couche (nom de la variable de protocole du fichier de config)
    private String prefix;
    
    private DhtNode rightNeighbor;
    private DhtNode leftNeighbor;
    
    private HashMap<DhtNode, Map<Integer, Message>> dataInfos = new HashMap<>();
    
    private HashMap<DhtNode, Integer> rootingTable = new HashMap<>();

    public DhtNode(String prefix) {
    	
		this.prefix = prefix;
		// initialisation des identifiants a partir du fichier de configuration
		this.transportPid = Configuration.getPid(prefix + ".transport");
		this.mypid = Configuration.getPid(prefix + ".myself");
		this.transport = null;
		
    }
    


    // methode appelee lorsqu'un message est recu par le protocole HelloWorld du noeud
    public void processEvent( Node node, int pid, Object event ) {
    	
    	this.receive((Message)event);
    	
    }
    
    // methode necessaire pour la creation du reseau (qui se fait par clonage d'un prototype)
    public Object clone() {

		DhtNode dolly = new DhtNode(this.prefix);
	
		return dolly;
    }

    // liaison entre un objet de la couche applicative et un 
    // objet de la couche transport situes sur le meme noeud
    
    public void setTransportLayer(int nodeId) {
    	this.id = nodeId;
    	this.transport = (HWTransport) Network.get(this.id).getProtocol(this.transportPid);
    }

    // envoi d'un message (l'envoi se fait via la couche transport)
    public void send(Message msg, Node dest) {
    	msg.addSender(this);
    	this.transport.send(getMyNode(), dest, msg, this.mypid);
    }

    // affichage lors de la reception
    @SuppressWarnings("unchecked")
	private void receive(Message msg) {
    	
    	DhtNode sender = msg.getLastSender();
    	
    	String prefixMsg = "[" + msg.getType().getDescription() + "] from " + msg.getLastSender().getUid() + " to " + this.uid + " -> ";
    	
    	switch (msg.type) {
		case JOIN: { 
			
			if(sender.equals(msg.getTarget())) {
				System.out.println("[" + msg.getType().getDescription() + "] from " + msg.getLastSender().getUid() + " -> I join the network");
			}
			
			if(this.leftNeighbor == this && this.rightNeighbor == this) {
				
				Message changeBothMsg = new Message(MessageType.PLACE_BOTH, "Change your neighbors !", new DhtNode[] {this, this});
				this.send(changeBothMsg, Network.get(msg.getTarget().getId()));
				
				this.rightNeighbor = msg.getTarget();
				this.leftNeighbor = msg.getTarget();
				System.out.println(prefixMsg + "Change Left Node to " + this.leftNeighbor.getUid() + " | Change Right Node to " + this.rightNeighbor.getUid());
				
				
				break;
			}
			
			if(msg.getTarget().getUid() > this.uid) {
				
				if(msg.getTarget().getUid() < this.rightNeighbor.getUid() || (this.uid > this.rightNeighbor.getUid())) {
					
					Message changeBothMsg = new Message(MessageType.PLACE_BOTH, "Change your neighbors !", new DhtNode[] {this, this.rightNeighbor});
					this.send(changeBothMsg, Network.get(msg.getTarget().getId()));
					
					Message changeLeftMsg = new Message(MessageType.PLACE_LEFT, "Change your left neighbor !", msg.getTarget());
					
					this.send(changeLeftMsg, Network.get(this.rightNeighbor.getId()));

					this.rightNeighbor = msg.getTarget();
					System.out.println(prefixMsg + "Change Right Node to " + this.rightNeighbor.getUid());
					
				} else {
					
					this.send(msg, Network.get(this.rightNeighbor.getId()));
					
					if(sender.equals(msg.getTarget())) {
						System.out.println(prefixMsg + "Try to place me");
					} else {
						System.out.println(prefixMsg + "Try to place " + msg.getTarget().getUid());
					}
				}
				
			} else {

				if(msg.getTarget().getUid() > this.leftNeighbor.getUid()  || (this.uid < this.leftNeighbor.getUid())) {
					
					
					Message changeBothMsg = new Message(MessageType.PLACE_BOTH, "Change your neighbors !", new DhtNode[] {this.leftNeighbor, this});
					this.send(changeBothMsg, Network.get(msg.getTarget().getId()));
					
					Message changeRightMsg = new Message(MessageType.PLACE_RIGHT, "Change your right neighbor !", msg.getTarget());
					
					this.send(changeRightMsg, Network.get(this.leftNeighbor.getId()));
					this.leftNeighbor = msg.getTarget();
					System.out.println(prefixMsg + "Change Left Node to " + this.leftNeighbor.getUid());
					
					
				} else {
					
					this.send(msg, Network.get(this.leftNeighbor.getId()));
					
					if(sender.equals(msg.getTarget())) {
						System.out.println(prefixMsg + "Try to place me");
					} else {
						System.out.println(prefixMsg + "Try to place " + msg.getTarget().getUid());
					}
					
				}
				
			}
			
			break;
		}
		case LEAVE: {
			
			Message changeLeftMsg = new Message(MessageType.PLACE_LEFT, "Change your left neighbor !", this.leftNeighbor);
			Message changeRightMsg = new Message(MessageType.PLACE_RIGHT, "Change your right neighbor !", this.rightNeighbor);
			
			this.send(changeLeftMsg, Network.get(this.rightNeighbor.getId()));
			this.send(changeRightMsg, Network.get(this.leftNeighbor.getId()));
			
			System.out.println("[" + msg.getType().getDescription() + "] from " + this.uid + " -> I leave the network");
			
			
			for(DhtNode node : this.rootingTable.keySet()) {
				
				Message removeLink = new Message(MessageType.REMOVE_LINK, "Remove your link with me !", this);
				this.send(removeLink, Network.get(node.getId()));
				this.rootingTable.remove(node);
				
			}
			
			Message removeDataMsg = new Message(MessageType.REMOVE_DATA,"Remove your Data",this);
			removeDataMsg.setRemaining(3);
			this.send(removeDataMsg, getMyNode());
			
			
			
			break;
		}
		case PLACE_BOTH: {
			
			this.leftNeighbor = msg.getNeighbors()[0];
			this.rightNeighbor = msg.getNeighbors()[1];
			
			System.out.println(prefixMsg + "Change Left Node to " + this.leftNeighbor.getUid() + " | Change Right Node to " + this.rightNeighbor.getUid());
			
			Message collectRightMsg = new Message(MessageType.COLLECT_DATA, "Send me your data !", this);
			collectRightMsg.setRemaining(3);
			this.send(collectRightMsg, Network.get(this.rightNeighbor.getId()));
			Message collectLeftMsg = new Message(MessageType.COLLECT_DATA, "Send me your data !", this);
			collectLeftMsg.setRemaining(3);
			this.send(collectLeftMsg, Network.get(this.leftNeighbor.getId()));
			
			break;
		}
		case PLACE_LEFT: {
			
			this.leftNeighbor = msg.getTarget();
			
			System.out.println(prefixMsg + "Change Left Node to " + this.leftNeighbor.getUid());
			
			break;
		}
		case PLACE_RIGHT: {
			
			this.rightNeighbor = msg.getTarget();
			
			System.out.println(prefixMsg + "Change Right Node to " + this.rightNeighbor.getUid());
			
			
			break;
		}
		case DELIVER: {
			
			if(msg.getSenders().size() > Initializer.getNodeNb()) {
				System.out.println(prefixMsg + " Timed out");
				break;
			} 
			
			if((msg.getId() > this.uid && msg.getId() < this.rightNeighbor.getUid()) || (this.rightNeighbor.getUid() < this.uid && msg.getId() > this.uid)) {
				
				int dist = Math.abs(msg.getId() - this.getUid());
				int dist2 = Math.abs(msg.getId() - this.rightNeighbor.getUid());
				
				DhtNode deliverDataTo = null;
				int deliverToId;
				
				if(dist < dist2) {
					deliverDataTo = this;
					deliverToId = this.id;
				}
				else { 
					deliverDataTo = this.rightNeighbor;
					deliverToId = this.rightNeighbor.getId();
				}
				
				System.out.println(prefixMsg + "Message's content : " + msg.getContent() + " {id = " + msg.getId() + "}");
				
				Message addData = new Message(MessageType.ADD_DATA, "Update your data !", msg);
	    		addData.setTarget(deliverDataTo);
				addData.setRemaining(3);
	    		addData.setId(msg.getId());
	        	this.send(addData, Network.get(deliverToId));
	        	
	        	if(msg.getSenders().size() > 4) {
	        		
	        		Message createLink = new Message(MessageType.CREATE_LINK, "Create a link with me", deliverDataTo);
	        		this.send(createLink, Network.get(msg.getFirstSender().getId()));
	        		this.rootingTable.put(msg.getFirstSender(), msg.getFirstSender().getId());
	        	}
	        	
				break;
				
			}
			if((msg.getId() < this.uid && msg.getId() > this.leftNeighbor.getUid()) || (this.leftNeighbor.getUid() > this.uid && msg.getId() < this.uid)) {
				
				int dist = Math.abs(msg.getId() - this.getUid());
				int dist2 = Math.abs(msg.getId() - this.leftNeighbor.getUid());
				
				DhtNode deliverDataTo = null;
				int deliverToId = 0;
				
				if(dist < dist2) {
					deliverDataTo = this;
					deliverToId = this.id;
				}
				else { 
					deliverDataTo = this.leftNeighbor;
					deliverToId = this.leftNeighbor.getId();
				}
				
				
				System.out.println(prefixMsg + "Message's content : " + msg.getContent() + " {id = " + msg.getId() + "}");
				
				Message addData = new Message(MessageType.ADD_DATA, "Update your data !", msg);
	    		addData.setTarget(deliverDataTo);
				addData.setRemaining(3);
	    		addData.setId(msg.getId());
	        	this.send(addData, Network.get(deliverToId));
	        	
	        	if(msg.getSenders().size() > 4) {
	        		
	        		Message createLink = new Message(MessageType.CREATE_LINK, "Create a link with me", deliverDataTo);
	        		this.send(createLink, Network.get(msg.getFirstSender().getId()));
	        		this.rootingTable.put(msg.getFirstSender(), msg.getFirstSender().getId());
	        	}
	        	
				break;
				
			}
			
			if(msg.getId() == this.uid) {
				
				System.out.println(prefixMsg + "Message's content : " + msg.getContent() + " {id = " + msg.getId() + "}");
				
				Message addData = new Message(MessageType.ADD_DATA, "Update your data !", msg);
	    		addData.setTarget(this);
				addData.setRemaining(3);
	    		addData.setId(msg.getId());
	        	this.send(addData, getMyNode());
	        	
	        	if(msg.getSenders().size() > 3) {
	        		
	        		Message createLink = new Message(MessageType.CREATE_LINK, "Create a link with me", this);
	        		this.send(createLink, Network.get(msg.getFirstSender().getId()));
	        		this.rootingTable.put(msg.getFirstSender(), msg.getFirstSender().getId());
	        	}
				
			} else {
				
				DhtNode deliverTo = null;
				int deliverToId = 0;
				
				if(this.rightNeighbor.getUid() == msg.getId()) {
					deliverTo = this.rightNeighbor;
					deliverToId = this.rightNeighbor.getId();
				}
				else if(this.leftNeighbor.getUid() == msg.getId()) {
					deliverTo = this.leftNeighbor;
					deliverToId = this.leftNeighbor.getId();
				}
				else {
					
					for(DhtNode node: this.rootingTable.keySet()) {
						if(node.getUid() == msg.getId()) {
							deliverTo = node;
							deliverToId = this.rootingTable.get(node);
							break;
						}
					}
					
					double diff = Double.POSITIVE_INFINITY;
					
					for(DhtNode node : this.rootingTable.keySet()) {
						
						if(Math.abs(msg.getId() - node.getUid()) < diff) {
							diff = Math.abs(msg.getId() - node.getUid());
							deliverTo = node;
							deliverToId = this.rootingTable.get(node);
						}
						
					}
					
					if(Math.abs(msg.getId() - this.leftNeighbor.getUid()) < diff) {
						deliverTo = this.leftNeighbor;
						deliverToId = this.leftNeighbor.getId();
						diff = Math.abs(msg.getId() - this.leftNeighbor.getUid());
					}
					if(Math.abs(msg.getId() - this.rightNeighbor.getUid()) < diff) {
						deliverTo = this.rightNeighbor;
						deliverToId = this.rightNeighbor.getId();
						diff = Math.abs(msg.getId() - this.rightNeighbor.getUid());
					}
					
					
				}
				
				
				this.send(msg, Network.get(deliverToId));
				System.out.println(prefixMsg + "Delivering message to " + deliverTo.getUid() + " {id = " + msg.getId() + "}");	
				
			}
			
			break;
		}
		case CREATE_LINK: {
			
			this.rootingTable.put(msg.getTarget(), msg.getTarget().getId());
			System.out.println(prefixMsg + "Creating link between " + msg.getTarget().getUid() + " and " + this.uid);	
			break;
			
		}
		case REMOVE_LINK: {
			
			this.rootingTable.remove(msg.getTarget());
			System.out.println(prefixMsg + "Removing link between " + msg.getTarget().getUid() + " and " + this.uid);	
			break;
			
		}
		case ADD_DATA: {
			
			checkRemaining(msg);
			
			if(this.dataInfos.keySet().contains(msg.getTarget())) {
	    		Map<Integer, Message> currentInfos = this.dataInfos.get(msg.getTarget());
	    		currentInfos.put(msg.getId(), msg.data2Save());
	    		this.dataInfos.put(msg.getTarget(), currentInfos);
	    		
	    	} else {
	    		Map<Integer, Message> newInfos = new HashMap<>();
	    		newInfos.put(msg.getId(), msg.data2Save());
	    		this.dataInfos.put(msg.getTarget(), newInfos);
	    	}
			
			break;
			
		}
		case COLLECT_DATA: {
			
			checkRemaining(msg);
			
			if(this.dataInfos.get(this) != null) {
				Map<Integer, Message> data = this.dataInfos.get(this);
				Message addAllDataMsg = new Message(MessageType.ADD_ALL_DATA, data);
				addAllDataMsg.setTarget(this);
				addAllDataMsg.setRemaining(3);
				this.send(addAllDataMsg, Network.get(msg.getTarget().getId()));
				
				System.out.println("[Collect Data] from " + msg.getFirstSender().getUid() + " to " + this.uid  + " -> Send me your data");	
			} 
			
			break;
			
		}
		case REMOVE_DATA: {
			
			checkRemaining(msg);
			
	    	this.dataInfos.remove(msg.getTarget());
	    	
	    	if(this != msg.getTarget()) {
	    		
		    	Map<Integer, Message> data = this.dataInfos.get(this);
		    	
		    	if(data != null) {
		    		Message addAllDataMsg = new Message(MessageType.ADD_ALL_DATA, data);
			    	addAllDataMsg.setTarget(this);
			    	this.send(addAllDataMsg, getMyNode());
		    	}

	    	}
	    	
			break;
			
		}
		case ADD_ALL_DATA: {
			
			Map<Integer, Message> data = (Map<Integer, Message>) msg.getContent();
			
			checkRemaining(msg);
			
			if(this.dataInfos.keySet().contains(msg.getTarget())) {
				
				for(int i : data.keySet()) {
					
					if(!this.dataInfos.get(msg.getTarget()).keySet().contains(i)) {
						Map<Integer, Message> currentInfos = this.dataInfos.get(msg.getTarget());
						currentInfos.put(i, data.get(i));
						this.dataInfos.put(msg.getTarget(), currentInfos);
					}	
				}
	    	} else {
	    		
	    		this.dataInfos.put(msg.getTarget(), data);
	    		
	    	}
			
			break;
			
		}
		case SHOW_TREE: {
			
			System.out.println(getTree("Tree :\n", this, this));
			System.out.println(getTreeNode("", this, this));
			break;
			
		}
		
		default:
			throw new IllegalArgumentException("Unexpected value");
		}
    	
    	/*if(!msg.isType(MessageType.ADD_DATA)) {
    		Message addData = new Message(MessageType.ADD_DATA, "Update your data !", msg);
    		addData.setRemaining(3);
    		addData.setId(Controller.generateNewDataId());
        	this.send(addData, getMyNode());
    	}*/
    	
    }
    
    public void showInfos() {
    	
    	System.err.println("\n\n >>>>> Data Informations for " + this + " {uid : " + this.uid +  "} <<<<<");
    	
    	for(DhtNode currentNode : this.dataInfos.keySet()) {
    		
    		System.out.println("-------- " + currentNode + " {" + currentNode.getUid() + "} --------");
    		
    		for(int id : this.dataInfos.get(currentNode).keySet()) {
    			
    			Message message = this.dataInfos.get(currentNode).get(id);
    			
    			System.out.println("{" + id + "} - " + message.getContent());
    			
    		}
    		
    		
    	}
    	
    	System.out.println("\n");
    	
    }
    
    private void checkRemaining(Message msg) {
    	
    	if(msg.getRemaining() > 0) {
			
			msg.decreaseRemaining();
			
			if(!msg.getSenders().contains(this.leftNeighbor) && !msg.getSenders().contains(this.rightNeighbor)) {
				this.send(msg, Network.get(this.rightNeighbor.getId()));
				this.send(msg, Network.get(this.leftNeighbor.getId()));
			}
			else if(msg.getSenders().contains(this.leftNeighbor)) {
				
				this.send(msg, Network.get(this.rightNeighbor.getId()));
				
			} else {
				
				this.send(msg, Network.get(this.leftNeighbor.getId()));
				
			}
		}
    	
    }
    
    private String getTree(String message, DhtNode startingNode, DhtNode currentNode) {
    	
    	message += " " + currentNode.getUid() + " -> ";
    	
		if(!currentNode.getRightNeighor().equals(startingNode)) {
			
			return getTree(message, startingNode, currentNode.getRightNeighor());
			
		} 
		
		return message;
    	
    }
    
    private String getTreeNode(String message, DhtNode startingNode, DhtNode currentNode) {
    	
    	message += " Node " + currentNode.getId() + " -> ";
    	
		if(!currentNode.getRightNeighor().equals(startingNode)) {
			
			return getTreeNode(message, startingNode, currentNode.getRightNeighor());
			
		} 
		
		return message;
    	
    }

    // retourne le noeud courant
    
    private Node getMyNode() {
    	return Network.get(this.id);
    }

    public String toString() {
    	return "Node "+ this.id;
    }
    
    /**
     * Fonction pour récupérer tous les voisins
     * 
     */
    public List<DhtNode> getAllNeighors(){
    	
    	List<DhtNode> allNeighbors = new ArrayList<>();
    	allNeighbors.add(this.rightNeighbor);
    	allNeighbors.add(this.leftNeighbor);
    	
    	return allNeighbors;
    	
    }
    
    public Integer getId() {
  		return this.id;
  	}
    
    public Integer getUid() {
  		return this.uid;
  	}
    
    public void setNodeUid(int nodeUid) {
  		this.uid = nodeUid;
  	}
    
    /**
     * Fonction pour récupérer le voisin de droite
     * 
     */
    public DhtNode getRightNeighor(){
    	
    	return this.rightNeighbor;
    	
    }
    
    /**
     * Fonction pour récupérer le voisins de gauche
     * 
     */
    public DhtNode getLeftNeighor(){
    	
    	return this.leftNeighbor;
    	
    }
    
    /**
     * Fonction pour ajouter les voisins
     * 
     */
    public void setNeighbors(DhtNode leftNode, DhtNode rightNode) {
    	
    	this.leftNeighbor = leftNode;
    	this.rightNeighbor = rightNode;
    	
    }
    
    /**
     * Fonction pour ajouter un voisin à droite
     * 
     */
    public void setRightNeighbor(DhtNode node) {
    	
    	this.rightNeighbor = node;
    	
    }
    
    /**
     * Fonction pour ajouter un voisin à gauche
     * 
     */
    public void setLeftNeighbor(DhtNode node) {
    	
    	this.leftNeighbor = node;
    	
    }
    
    /**
     * Fonction pour savoir si ce noeud a pour voisin un autre noeud
     * 
     */
    public boolean containsNeighbor(DhtNode node) {
    	
    	return this.leftNeighbor == node || this.rightNeighbor == node;
    	
    }

}