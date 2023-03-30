package fr.usmb.dht;

import java.util.ArrayList;
import java.util.List;

public class Message {

	/**
	 * Type du message
	 */
    public MessageType type;
    
    /**
     * Contenu du message
     */
    private Object content;
    
    /*
     * Cible d'un message
     */
    private DhtNode target;

    /*
     * tableau de plusieurs cibles
     */
    private DhtNode[] targets;
    
    /*
     * Liste des noeuds ayant relayés le message
     */
    private ArrayList<DhtNode> senders = new ArrayList<>();
    
    /*
     * Compteur d'envoies restants
     */
    private int remaining = 0;
    
    /*
     * Données d'un message (un autre objet message)
     */
    private Message data;
    
    /*
     * Identifiant de l'objet message
     */
    private int id;
    
    /*
     * Constructeur classique
     */
	Message(MessageType type, Object content) {
    	this.type = type;
		this.content = content;
    }
    
	/*
     * Constructeur avec un noeud cible
     */
    Message(MessageType type, Object content, DhtNode nodeToPlace) {
    	this.type = type;
		this.content = content;
		this.target = nodeToPlace;
    }
    
    /*
     * Constructeur avec de la donnée
     */
    Message(MessageType type, Object content, Message data) {
    	this.type = type;
		this.content = content;
		this.data = data;
    }
    
    /*
     * Constructeur avec plusieurs noeuds cibles
     */
    Message(MessageType type, Object content, DhtNode[] targets) {
    	this.type = type;
		this.content = content;
		this.targets = targets;
    }
    
    /*
     * Retourne la donnée
     */
    public Message data2Save() {
    	return this.data;
    }
    
    /*
     * Retourne un boolean en fonction du type demandé
     */
    public boolean isType(MessageType type) {
    	return this.type == type;
    }
    
    /*
     * Ajoute un noeud à la lise des noeuds relayeurs
     */
    public void addSender(DhtNode node) {
    	this.senders.add(node);
    }
    
    /*
     * Retourne la liste des noeuds relayeurs
     */
    public List<DhtNode> getSenders(){
	   return this.senders;
    }
    
    /*
     * Retourne le dernier noeud ayant relayé
     */
    public DhtNode getLastSender() {
    	return this.senders.get(senders.size() - 1);
    }
    
    /*
     * Retourne le premier noeud ayant relayé
     */
    public DhtNode getFirstSender() {
    	return this.senders.get(0);
    }
    
    /*
     * Définir un nombre pour le compteur
     */
    public void setRemaining(int remaining) {
    	this.remaining = remaining;
    }
    
    /*
     * Retourne la valeur du compteur restante
     */
    public Integer getRemaining() {
    	return this.remaining;
    }
    
    /*
     * Décrémente de un la valeur du compteur
     */
    public void decreaseRemaining() {
    	this.remaining--;
    }

    /*
     * Retourne le contenu
     */
    public Object getContent() {
    	return this.content;
    }
    
    /*
     * Retourne la cible
     */
    public DhtNode getTarget() {
    	return this.target;
    }
    
    /*
     * Définir la cible
     */
    public void setTarget(DhtNode target) {
    	this.target = target;
    }
    
    /*
     * Retourne un tableau de cibles
     */
    public DhtNode[] getTargets() {
    	return this.targets;
    }
    
    /*
     * Retourne son type
     */
    public MessageType getType() {
    	return this.type;
    }
    
    /*
     * Retourne son id
     */
    public Integer getId() {
		return this.id;
	}

    /*
     * Définir son id
     */
	public void setId(int id) {
		this.id = id;
	}

	/*
     * Retourne l'objet message sous format String
     */
    public String toString() {
    	
    	return "[" + this.type.getDescription() + "] from " + this.senders.get(0) + " (Uid : " + this.senders.get(0).getUid()+ ")";

    }

    
}