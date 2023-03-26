package fr.usmb.dht;

import peersim.edsim.*;
import peersim.core.*;

import java.util.ArrayList;
import java.util.List;

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
    	this.transport.send(getMyNode(), dest, msg, this.mypid);
    }

    // affichage a la reception
    private void receive(Message msg) {
    	
    	
    	switch (msg.type) {
		case HELLOWORLD: {
			System.out.println(this + ": Received " + msg.getContent());
			break;
		}
		case JOIN: {
			
			System.out.println("je suis le noeud " + this.id);
			System.out.println("[" + msg.getType().getDescription() + "] provenant de " + msg.getNodeToPlace().getUid() + " sur " + this.uid);
			
			
			if(this.leftNeighbor == this && this.rightNeighbor == this) {
				
				Message changeBothMsg = new Message(MessageType.PLACE_BOTH, "Change your neighbors !", new DhtNode[] {this, this});
				this.send(changeBothMsg, Network.get(msg.getNodeToPlace().getId()));
				
				this.rightNeighbor = msg.getNodeToPlace();
				this.leftNeighbor = msg.getNodeToPlace();
				
				System.out.println("Je me place des deux cotes");
				
				break;
			}
			
			if(msg.getNodeToPlace().getUid() > this.uid) {
				
				if(msg.getNodeToPlace().getUid() < this.rightNeighbor.getUid() || this.uid > this.rightNeighbor.getUid()) {
					
					Message changeBothMsg = new Message(MessageType.PLACE_BOTH, "Change your neighbors !", new DhtNode[] {this, this.rightNeighbor});
					this.send(changeBothMsg, Network.get(msg.getNodeToPlace().getId()));
					
					Message changeLeftMsg = new Message(MessageType.PLACE_LEFT, "Change your left neighbor !", msg.getNodeToPlace());
					
					this.rightNeighbor.send(changeLeftMsg, Network.get(msg.getNodeToPlace().getId()));
					this.rightNeighbor = msg.getNodeToPlace();
					
					System.out.println("Je me place a droite");
					
				} else {
					
					
					this.send(msg, Network.get(this.rightNeighbor.getId()));
					System.out.println("Je vais à droite");
				}
				
			} else {
				
				if(msg.getNodeToPlace().getUid() > this.leftNeighbor.getUid()  || this.uid < this.leftNeighbor.getUid()) {
					
					
					Message changeBothMsg = new Message(MessageType.PLACE_BOTH, "Change your neighbors !", new DhtNode[] {this.leftNeighbor, this});
					this.send(changeBothMsg, Network.get(msg.getNodeToPlace().getId()));
					
					Message changeRightMsg = new Message(MessageType.PLACE_RIGHT, "Change your right neighbor !", msg.getNodeToPlace());
					
					this.leftNeighbor.send(changeRightMsg, Network.get(msg.getNodeToPlace().getId()));
					this.leftNeighbor = msg.getNodeToPlace();
					
					System.out.println("Je me place a gauche");
					
				} else {
					
					this.send(msg, Network.get(this.leftNeighbor.getId()));
					System.out.println("Je vais à gauche");
				}
				
			}
			
			break;
		}
		case LEAVE: {
			
			break;
		}
		case PLACE_BOTH: {
			
			this.leftNeighbor = msg.getNeighbors()[0];
			this.rightNeighbor = msg.getNeighbors()[1];
			
			System.out.println(" both " + this.leftNeighbor +"|" + this.rightNeighbor);
			
			break;
		}
		case PLACE_LEFT: {
			
			this.leftNeighbor = msg.getNodeToPlace();
			
			System.out.println(" gauche " + this.leftNeighbor +"|" + this.rightNeighbor);
			
			break;
		}
		case PLACE_RIGHT: {
			
			this.rightNeighbor = msg.getNodeToPlace();
			
			System.out.println(" droite " + this.leftNeighbor +"|" + this.rightNeighbor);
			
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value");
		}
    	
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