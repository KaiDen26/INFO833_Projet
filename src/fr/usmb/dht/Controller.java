package fr.usmb.dht;

import java.util.List;
import peersim.core.*;

import java.util.ArrayList;
import java.util.Random;

public class Controller implements peersim.core.Control{
	
	 /*
	  * Pid de la dht
	  */
	 private int dhtPid;
	
	 /*
	  * Compteur d'execution d'étapes
	  */
	 private int executedStep = 0;
	 
	 /*
	  * Liste d'étapes
	  */
	 private final List<Runnable> steps = new ArrayList<>();
	 
	 /*
	  * Liste des identifiants des messages
	  */
	 private ArrayList<Integer> msgIds = new ArrayList<>();

	 public Controller(String prefix) {
	    	
		// Récuperation du pid de la couche applicative
	    this.dhtPid = Initializer.getDhtPid();
	    // Récupération du nombre de noeud dans la dht
	    int nodeNb = Initializer.getNodeNb();
		
		// Pour chaque noeud, on les ajoute à l'anneau
		for (int i = 1; i < nodeNb; i++) {
			
			// On récupère le noeud actuel
			DhtNode current = (DhtNode) Network.get(i).getProtocol(this.dhtPid);

			// On lui attribut un destinataire aléatoire
			// Celui-ci sera le premier noeud auquel il demandera de se placer
		    int randomNodeId = new Random().nextInt(i);
		    while (randomNodeId == i) {
				randomNodeId = new Random().nextInt(i);
			}
		    
		    int destId = randomNodeId;
			
		    // On envoie un message de type join au noeud selectionné
			Message joinMsg = new Message(MessageType.JOIN, "Joining the network !", current);
			this.steps.add(() -> sendMsg(current, joinMsg, Network.get(destId)));
			
		} 
		
		Message ringMsg = new Message(MessageType.SHOW_TREE, "");
		this.steps.add(() -> sendMsg((DhtNode) Network.get(0).getProtocol(this.dhtPid), ringMsg, Network.get(0)));
		
		Message leaveMsg = new Message(MessageType.LEAVE, "");
		this.steps.add(() -> sendMsg((DhtNode) Network.get(3).getProtocol(this.dhtPid), leaveMsg, Network.get(3)));
		
		this.steps.add(() -> sendMsg((DhtNode) Network.get(0).getProtocol(this.dhtPid), ringMsg, Network.get(0)));
		
		
		Message joinMsg = new Message(MessageType.JOIN, "Joining the network !", (DhtNode) Network.get(3).getProtocol(this.dhtPid));
		this.steps.add(() -> sendMsg((DhtNode) Network.get(3).getProtocol(this.dhtPid), joinMsg, Network.get(0)));
		
		DhtNode node2 = (DhtNode) Network.get(2).getProtocol(this.dhtPid);
		
		Message deliverMsg = new Message(MessageType.DELIVER, "Hello dude, wanna meet ?");
		deliverMsg.setId(generateNewDataId());
		this.steps.add(() -> sendMsg(node2, deliverMsg, Network.get(2)));
		
		Message deliverMsg2 = new Message(MessageType.DELIVER, "Hello dude, wanna meet ?");
		deliverMsg2.setId(generateNewDataId());
		this.steps.add(() -> sendMsg(node2, deliverMsg2, Network.get(2)));
		
		
		DhtNode node3 = (DhtNode) Network.get(3).getProtocol(this.dhtPid);
		Message leaving = new Message(MessageType.LEAVE, "Leaving", (DhtNode) Network.get(3).getProtocol(this.dhtPid));
		this.steps.add(() -> sendMsg(node3, leaving, Network.get(3)));
		
		Message joinMsg2 = new Message(MessageType.JOIN, "Joining the network !", (DhtNode) Network.get(3).getProtocol(this.dhtPid));
		this.steps.add(() -> sendMsg(node3, joinMsg2, Network.get(8)));
		
		Message deliverMsg3 = new Message(MessageType.DELIVER, "Hello dude, wanna meet ?");
		deliverMsg3.setId(generateNewDataId());
		this.steps.add(() -> sendMsg(node2, deliverMsg3, Network.get(2)));
		
		
		Message deliverMsg4 = new Message(MessageType.DELIVER, 8000.54);
		deliverMsg4.setId(generateNewDataId());
		this.steps.add(() -> sendMsg(node2, deliverMsg4, Network.get(2)));
		
		Message leaving2 = new Message(MessageType.LEAVE, "Leaving", (DhtNode) Network.get(3).getProtocol(this.dhtPid));
		this.steps.add(() -> sendMsg(node3, leaving2, Network.get(3)));
		
		this.steps.add(node2::showInfos);
				
		Message getMsg = new Message(MessageType.GET, "Give me the data !");
		getMsg.setId(msgIds.get(new Random().nextInt(msgIds.size())));
		this.steps.add(() -> sendMsg((DhtNode) Network.get(0).getProtocol(this.dhtPid), getMsg, Network.get(0)));
		
	}
	 
	/*
	 * Fonction permettant d'envoyer un message à partir d'un noeud vers un autre noeud
	 */
	public void sendMsg(DhtNode node, Message msg, Node dest) {
		node.send(msg, dest);
	}
	 
	/*
	 * Fonction principale appelée par le simulateur
	 * Celle-ci réalise l'execution de chaque étapes stockées
	 * En réalisant cela, nous n'avons plus de problèmes avec les latences des différents envoies de messages
	 */
	@Override
	public boolean execute() {
		
		if (this.executedStep == this.steps.size()) return false;
		
		this.steps.get(this.executedStep).run();
        this.executedStep++;
		
		return false;
	}

	/*
	 * Fonction permettant de générer un nouvel identifiant unique
	 */
	public int generateNewDataId() {
		
		int msgId = new Random().nextInt(Initializer.getNodeNb() * 10);
		
		while (msgIds.contains(msgId)) {
			msgId = new Random().nextInt(Initializer.getNodeNb() * 10);
		}
		
		msgIds.add(msgId);
		return msgId;
		
	}
}
