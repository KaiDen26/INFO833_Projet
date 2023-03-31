package fr.usmb.dht;

import peersim.edsim.*;
import peersim.core.*;

import java.util.HashMap;
import java.util.Map;

import peersim.config.*;

public class DhtNode implements EDProtocol {
    
	/*
	 * Identifiant de la couche de transport
	 */
    private int transportPid;

    /*
     * Objet de la couche transport
     */
    private HWTransport transport;

    /*
     * Identifiant de la couche courante (applicative)
     */
    private int mypid;

    /*
     * Le numéro du noeud dans l'ordre de création
     */
    private int id;
    
    /*
     * L'identifiant unique du noeud
     */
    private int uid;

    /*
     * Prefixe de la couche (nom de la variable de protocole du fichier de config)
     */
    private String prefix;
    
    /*
     * Noeud se situant à sa droite dans l'anneau
     */
    private DhtNode rightNeighbor;
    
    /*
     * Noeud se situant à sa gauche dans l'anneau
     */
    private DhtNode leftNeighbor;
    
    /*
     * Variable contenant les données à sauvegarder
     * C'est un dictionnaire avec comme clé un noeud et comme valeur une Map
     * Cette map a pour clé l'identifiant de la donnée et comme valeur la donnée elle-même (objet message)
     */
    private HashMap<DhtNode, Map<Integer, Message>> dataInfos = new HashMap<>();
    
    /*
     * Variable représentant la table de routage
     * Dictionnaire avec comme clé un noeud et comme valeur son numéro de noeud
     */
    private HashMap<DhtNode, Integer> routingTable = new HashMap<>();

    /*
     * Constructeur de la classe
     */
    public DhtNode(String prefix) {
    	
		this.prefix = prefix;
		
		// Initialisation des identifiants a partir du fichier de configuration
		this.transportPid = Configuration.getPid(prefix + ".transport");
		this.mypid = Configuration.getPid(prefix + ".myself");
		this.transport = null;
		
    }
    

    /*
     * Méthode appelée lorsqu'un message est recu par le protocole HelloWorld du noeud
     */
    public void processEvent( Node node, int pid, Object event ) {
    	
    	this.receive((Message)event);
    	
    }
    
    /*
     * Méthode necessaire pour la creation du réseau (qui se fait par clonage d'un prototype)
     */
    public Object clone() {
		DhtNode dolly = new DhtNode(this.prefix);
		return dolly;
    }

    /*
     * Liaison entre un objet de la couche applicative et un 
     * Objet de la couche transport situes sur le meme noeud
     */
    public void setTransportLayer(int nodeId) {
    	this.id = nodeId;
    	this.transport = (HWTransport) Network.get(this.id).getProtocol(this.transportPid);
    }

    /*
     * Envoi d'un message (l'envoi se fait via la couche transport)
     */
    public void send(Message msg, Node dest) {
    	// Ajout du noeud à la liste des relayeurs
    	msg.addSender(this);
    	this.transport.send(getMyNode(), dest, msg, this.mypid);
    }

    /*
     * Cette méthode gère la reception des messages
     * Elle est conséquente puisqu'elle va gérer tous ce qui touche à la réception
     */
    @SuppressWarnings("unchecked")
	private void receive(Message msg) {
    	
    	// On récupère le dernier relayeur
    	DhtNode sender = msg.getLastSender();
    	
    	// Préfixe pour les messages des logs
    	String prefixMsg = "[" + msg.getType().getDescription() + "] from " + msg.getLastSender().getUid() + " to " + this.uid + " -> ";
    	
    	// Structure conditionnelle switch pour réaliser des opérations selon le type de message
    	switch (msg.type) {
    		// Ici on s'intéresse aux messages de type join, lorsqu'un noeud rejoint l'anneau
			case JOIN: { 
				
				// Si la cible est lui-même, alors il envoit un message de log signifiant qu'il a rejoint le réseau
				if(sender.equals(msg.getTarget())) {
					System.out.println("[" + msg.getType().getDescription() + "] from " + msg.getLastSender().getUid() + " -> I join the network");
				}
				
				// Ici on traite le cas lorsqu'un second noeud rejoint l'anneau
				// En effet, le premier noeud avait lui même comme voisins
				if(this.leftNeighbor == this && this.rightNeighbor == this) {
					
					Message changeBothMsg = new Message(MessageType.PLACE_BOTH, "Change your neighbors !", new DhtNode[] {this, this});
					this.send(changeBothMsg, Network.get(msg.getTarget().getId()));
					
					this.rightNeighbor = msg.getTarget();
					this.leftNeighbor = msg.getTarget();
					System.out.println(prefixMsg + "Change Left Node to " + this.leftNeighbor.getUid() + " | Change Right Node to " + this.rightNeighbor.getUid());
					
					break;
				}
				
				// Ici on veut savoir si l'uid de la cible est strictement supérieure à l'uid du noeud actuel 
				if(msg.getTarget().getUid() > this.uid) {
					
					// Ici on veut savoir si l'uid de la cible est strictement inférieure à l'uid du noeud de droite (donc le nouveau noeud doit se placer entre le noeud courant et celui de droite)
					// Ou alors que l'uid du noeud courant est strictement supérieure à celui de droite ce qui veut dire qu'on est arrivé en fin d'anneau (numériquement parlant), donc le nouveau noeud possède l'uid la plus élevée
					if(msg.getTarget().getUid() < this.rightNeighbor.getUid() || (this.uid > this.rightNeighbor.getUid())) {
						
						Message changeBothMsg = new Message(MessageType.PLACE_BOTH, "Change your neighbors !", new DhtNode[] {this, this.rightNeighbor});
						this.send(changeBothMsg, Network.get(msg.getTarget().getId()));
						
						Message changeLeftMsg = new Message(MessageType.PLACE_LEFT, "Change your left neighbor !", msg.getTarget());
						
						this.send(changeLeftMsg, Network.get(this.rightNeighbor.getId()));
	
						this.rightNeighbor = msg.getTarget();
						System.out.println(prefixMsg + "Change Right Node to " + this.rightNeighbor.getUid());
						
					} else {
						
						// Sinon on fait passer le message de type join au noeud de droite
						
						this.send(msg, Network.get(this.rightNeighbor.getId()));
						
						if(sender.equals(msg.getTarget())) {
							System.out.println(prefixMsg + "Try to place me");
						} else {
							System.out.println(prefixMsg + "Try to place " + msg.getTarget().getUid());
						}
					}
					
				// Sinon c'est que l'uid de la cible est inférieure ou égale à l'uid du noeud courant (en fait ça ne peut être que strictement inférieure comme on check la liste des uids avant)
				} else {
	
					// Ici on veut savoir si l'uid de la cible est strictement supérieure à l'uid du noeud de gauche (donc le nouveau noeud doit se placer entre le noeud courant et celui de gauche)
					// Ou alors que l'uid du noeud courant est strictement inférieure à celui de gauche ce qui veut dire qu'on est arrivé en fin d'anneau (numériquement parlant), donc le nouveau noeud possède l'uid la plus faible
					if(msg.getTarget().getUid() > this.leftNeighbor.getUid()  || (this.uid < this.leftNeighbor.getUid())) {
						
						
						Message changeBothMsg = new Message(MessageType.PLACE_BOTH, "Change your neighbors !", new DhtNode[] {this.leftNeighbor, this});
						this.send(changeBothMsg, Network.get(msg.getTarget().getId()));
						
						Message changeRightMsg = new Message(MessageType.PLACE_RIGHT, "Change your right neighbor !", msg.getTarget());
						
						this.send(changeRightMsg, Network.get(this.leftNeighbor.getId()));
						this.leftNeighbor = msg.getTarget();
						System.out.println(prefixMsg + "Change Left Node to " + this.leftNeighbor.getUid());
						
					} else {
						
						// Sinon on fait passer le message de type join au noeud de gauche
						
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
			// Ici on s'intéresse aux messages de type leave, lorsqu'un noeud quitte l'anneau
			case LEAVE: {
				
				Message changeLeftMsg = new Message(MessageType.PLACE_LEFT, "Change your left neighbor !", this.leftNeighbor);
				Message changeRightMsg = new Message(MessageType.PLACE_RIGHT, "Change your right neighbor !", this.rightNeighbor);
				
				this.send(changeLeftMsg, Network.get(this.rightNeighbor.getId()));
				this.send(changeRightMsg, Network.get(this.leftNeighbor.getId()));
				
				System.out.println("[" + msg.getType().getDescription() + "] from " + this.uid + " -> I leave the network");
				
				// On parcourt la table de routage du noeud courant pour ensuite envoyer un message aux noeuds ayant une jointure avec celui-ci
				for(DhtNode node : this.routingTable.keySet()) {
					
					Message removeLink = new Message(MessageType.REMOVE_LINK, "Remove your link with me !", this);
					this.send(removeLink, Network.get(node.getId()));
					this.routingTable.remove(node);
					
				}
				
				Message removeDataMsg = new Message(MessageType.REMOVE_DATA,"Remove your Data",this);
				removeDataMsg.setRemaining(3);
				this.send(removeDataMsg, getMyNode());
				
				break;
			}
			// Ici on s'intéresse aux messages de type place both, c'est à dire quand un noeud doit changer ses voisins de droite et de gauche
			case PLACE_BOTH: {
				
				this.leftNeighbor = msg.getTargets()[0];
				this.rightNeighbor = msg.getTargets()[1];
				
				System.out.println(prefixMsg + "Change Left Node to " + this.leftNeighbor.getUid() + " | Change Right Node to " + this.rightNeighbor.getUid());
				
				Message collectRightMsg = new Message(MessageType.COLLECT_DATA, "Send me your data !", this);
				collectRightMsg.setRemaining(3);
				this.send(collectRightMsg, Network.get(this.rightNeighbor.getId()));
				Message collectLeftMsg = new Message(MessageType.COLLECT_DATA, "Send me your data !", this);
				collectLeftMsg.setRemaining(3);
				this.send(collectLeftMsg, Network.get(this.leftNeighbor.getId()));
				
				break;
			}
			// Ici on s'intéresse aux messages de type place left, lorsqu'un noeud doit changer son voisin de gauche
			case PLACE_LEFT: {
				
				this.leftNeighbor = msg.getTarget();
				
				System.out.println(prefixMsg + "Change Left Node to " + this.leftNeighbor.getUid());
				
				break;
			}
			// Ici on s'intéresse aux messages de type place right, lorsqu'un noeud doit changer son voisin de droite
			case PLACE_RIGHT: {
				
				this.rightNeighbor = msg.getTarget();
				
				System.out.println(prefixMsg + "Change Right Node to " + this.rightNeighbor.getUid());
				
				
				break;
			}
			// Ici on s'intéresse aux messages de type deliver, lorsqu'un noeud doit délivrer un message
			case DELIVER: {
				
				// Sécurité lorsqu'un noeud n'est pas trouvé
				// Ne devrait jamais être appelé mais nous gardons cela comme sécurité
				if(msg.getSenders().size() > Initializer.getNodeNb()) {
					System.out.println(prefixMsg + " Timed out");
					break;
				} 
				
				// Les 2 blocs de conditions suivants sont réalisés lorsque le message est arrivé à sa destination
				
				
				// On regarde si l'id de la donnée est strictement supérieur à l'uid du noeud courant ET que l'id de la donnée est strictement inférieur à l'uid du noeud de droite -> Donc la donnée se stocke soit sur le noeud courant soit sur le noeud de droite
				// OU ALORS
				// On regarde si l'uid du voisin de droite est strictement inférieur à l'uid du noeud courant ET que le l'id de la donnée est strictement supérieur à l'uid du noeud courant (Cette condition sert à savoir si le noeud courant est en fin d'anneau numériquement parlant)
				if((msg.getId() > this.uid && msg.getId() < this.rightNeighbor.getUid()) || (this.rightNeighbor.getUid() < this.uid && msg.getId() > this.uid)) {
					
					Object[] values = getBestDiff(msg, this.rightNeighbor);
					
					if(msg.isSavable()) {
						addData(prefixMsg, (DhtNode) values[0], (int) values[1], msg);
					} else {
						System.out.println(prefixMsg + "Message's content : " + msg.getContent() + " {id = " + msg.getId() + "}");
					}
		        	
		        	checkCreateLink(msg);
		        	
					break;
					
				}
				
				// On regarde si l'id de la donnée est strictement inférieur à l'uid du noeud courant ET que l'id de la donnée est strictement supérieur à l'uid du noeud de gauche -> Donc la donnée se stocke soit sur le noeud courant soit sur le noeud de gauche
				// OU ALORS
				// On regarde si l'uid du voisin de gauche est strictement supérieur à l'uid du noeud courant ET que le l'id de la donnée est strictement inférieur à l'uid du noeud courant (Cette condition sert à savoir si le noeud courant est en début d'anneau numériquement parlant)
				if((msg.getId() < this.uid && msg.getId() > this.leftNeighbor.getUid()) || (this.leftNeighbor.getUid() > this.uid && msg.getId() < this.uid)) {
					
					Object[] values = getBestDiff(msg, this.leftNeighbor);
					
					if(msg.isSavable()) {
						addData(prefixMsg, (DhtNode) values[0], (int) values[1], msg);
					} else {
						System.out.println(prefixMsg + "Message's content : " + msg.getContent() + " {id = " + msg.getId() + "}");
					}
		        	
		        	checkCreateLink(msg);
		        	
					break;
					
				}
				
				
				// On regarde si l'id de la donnée est égale à l'uid du noeud courant
				if(msg.getId() == this.uid) {
					
					if(msg.isSavable()) {
						addData(prefixMsg, this, this.id, msg);
					} else {
						System.out.println(prefixMsg + "Message's content : " + msg.getContent() + " {id = " + msg.getId() + "}");
					}
					
		        	checkCreateLink(msg);
					
				} else {
					
					// A partir d'ici on s'intéresse au relay du message avec tout le fonctionnement derrière
					
					DhtNode deliverDataTo = null;
					int deliverToId = -1;
					
					// On initialise la variable différence comme infiny pour que forcément un résultat soit inférieur
					double diff = Double.POSITIVE_INFINITY;
					
					
					// Ici on parcourt notre table de routage
					for(DhtNode node : this.routingTable.keySet()) {
						
						// Si l'uid exacte correspondant à l'id de la donnée a été trouvé
						if(node.getUid() == msg.getId()) {
							deliverDataTo = node;
							deliverToId = this.routingTable.get(node);
							break;
						
						// Sinon on continue de parcourir la table pour essayer de trouver la jointure avec la plus petite différence entre l'id de la donnée et l'uid du noeud
						} else {
							if(Math.abs(msg.getId() - node.getUid()) < diff) {
								diff = Math.abs(msg.getId() - node.getUid());
								deliverDataTo = node;
								deliverToId = this.routingTable.get(node);
							}
						}
						
					}
					
					// A la toute fin, nous vérifions si au final la différence l'id de la donnée et l'uid d'un noeud n'est pas plus faible pour les voisins

					// Ici nous checkons pour le voisin de gauche
					if(Math.abs(msg.getId() - this.leftNeighbor.getUid()) < diff) {
						deliverDataTo = this.leftNeighbor;
						deliverToId = this.leftNeighbor.getId();
						diff = Math.abs(msg.getId() - this.leftNeighbor.getUid());
					}
					// Ici nous checkons pour le voisin de droite
					if(Math.abs(msg.getId() - this.rightNeighbor.getUid()) < diff) {
						deliverDataTo = this.rightNeighbor;
						deliverToId = this.rightNeighbor.getId();
						diff = Math.abs(msg.getId() - this.rightNeighbor.getUid());
					}
						
					
					// On relaye le message vers la cible trouvée
					this.send(msg, Network.get(deliverToId));
					System.out.println(prefixMsg + "Delivering message to " + deliverDataTo.getUid() + " {id = " + msg.getId() + "}");	
					
				}
				
				break;
			}
			// Ici on s'intéresse aux messages de type get, lorsqu'un noeud veut récupérer de la donnée
			case GET: {
				
				// Sécurité lorsqu'un noeud n'est pas trouvé
				// Ne devrait jamais être appelé mais nous gardons cela comme sécurité
				if(msg.getSenders().size() > Initializer.getNodeNb()) {
					System.out.println(prefixMsg + " Timed out");
					break;
				} 
				
				// Les 2 blocs de conditions suivants sont réalisés lorsque le message est arrivé à sa destination
				
				
				// On regarde si l'id de la donnée est strictement supérieur à l'uid du noeud courant ET que l'id de la donnée est strictement inférieur à l'uid du noeud de droite -> Donc la donnée se stocke soit sur le noeud courant soit sur le noeud de droite
				// OU ALORS
				// On regarde si l'uid du voisin de droite est strictement inférieur à l'uid du noeud courant ET que le l'id de la donnée est strictement supérieur à l'uid du noeud courant (Cette condition sert à savoir si le noeud courant est en fin d'anneau numériquement parlant)
				if((msg.getId() > this.uid && msg.getId() < this.rightNeighbor.getUid()) || (this.rightNeighbor.getUid() < this.uid && msg.getId() > this.uid)) {
					
		        	DhtNode node = (DhtNode) getBestDiff(msg, this.rightNeighbor)[0];
		        	
		        	int resultId = -1;
		        	
		        	// On vérifie que dataInfos contient des informations
		        	if(node.dataInfos != null && node.dataInfos.get(node) != null) {
		        		// On parcourt les id compris dans les informations d'un noeud
		        		for(int idTarget : node.dataInfos.get(node).keySet()) {
			        		
			        		// Si la donnée est trouvée on définit l'id cible
			        		if(idTarget == msg.getId()) {
			        			resultId = idTarget;
					        	break;
			        		}
			        		
			        	}
		        	}
		        	Message deliverGetDataMsg;
		        	
		        	// Si la donnée n'est pas trouvée
		        	if(resultId == -1) {
		        		deliverGetDataMsg = new Message(MessageType.DELIVER, "Could not find the data");
		        	} else {
		        		deliverGetDataMsg = new Message(MessageType.DELIVER, node.dataInfos.get(node).get(resultId).getContent());
		        	}
		        	
		        	deliverGetDataMsg.setSavable(false);
		        	deliverGetDataMsg.setId(msg.getFirstSender().getUid());
		        	this.send(deliverGetDataMsg, getMyNode());
					break;
					
				}
				
				// On regarde si l'id de la donnée est strictement inférieur à l'uid du noeud courant ET que l'id de la donnée est strictement supérieur à l'uid du noeud de gauche -> Donc la donnée se stocke soit sur le noeud courant soit sur le noeud de gauche
				// OU ALORS
				// On regarde si l'uid du voisin de gauche est strictement supérieur à l'uid du noeud courant ET que le l'id de la donnée est strictement inférieur à l'uid du noeud courant (Cette condition sert à savoir si le noeud courant est en début d'anneau numériquement parlant)
				if((msg.getId() < this.uid && msg.getId() > this.leftNeighbor.getUid()) || (this.leftNeighbor.getUid() > this.uid && msg.getId() < this.uid)) {
					
		        	DhtNode node = (DhtNode) getBestDiff(msg, this.leftNeighbor)[0];
		        	
		        	int resultId = -1;
		        	
		        	// On vérifie que dataInfos contient des informations
		        	if(node.dataInfos != null && node.dataInfos.get(node) != null) {
		        		// On parcourt les id compris dans les informations d'un noeud
		        		for(int idTarget : node.dataInfos.get(node).keySet()) {
			        		
			        		// Si la donnée est trouvée on définit l'id cible
			        		if(idTarget == msg.getId()) {
			        			resultId = idTarget;
					        	break;
			        		}
			        		
			        	}
		        	}
		        	Message deliverGetDataMsg;
		        	
		        	// Si la donnée n'est pas trouvée
		        	if(resultId == -1) {
		        		deliverGetDataMsg = new Message(MessageType.DELIVER, "Could not find the data");
		        	} else {
		        		deliverGetDataMsg = new Message(MessageType.DELIVER, node.dataInfos.get(node).get(resultId).getContent());
		        	}
		        	
		        	deliverGetDataMsg.setSavable(false);
		        	deliverGetDataMsg.setId(msg.getFirstSender().getUid());
		        	this.send(deliverGetDataMsg, getMyNode());
					break;
					
				}
				
				
				// On regarde si l'id de la donnée est égale à l'uid du noeud courant
				if(msg.getId() == this.uid) {
					
		        	int resultId = -1;
		        	
		        	// On vérifie que dataInfos contient des informations
		        	if(this.dataInfos != null && this.dataInfos.get(this) != null) {
		        		// On parcourt les id compris dans les informations d'un noeud
		        		for(int idTarget : this.dataInfos.get(this).keySet()) {
			        		
			        		// Si la donnée est trouvée on définit l'id cible
			        		if(idTarget == msg.getId()) {
			        			resultId = idTarget;
					        	break;
			        		}
			        		
			        	}
		        	}
		        	
		        	Message deliverGetDataMsg;
		        	
		        	// Si la donnée n'est pas trouvée
		        	if(resultId == -1) {
		        		deliverGetDataMsg = new Message(MessageType.DELIVER, "Could not find the data");
		        	} else {
		        		deliverGetDataMsg = new Message(MessageType.DELIVER, this.dataInfos.get(this).get(resultId).getContent());
		        	}
		        	
		        	deliverGetDataMsg.setSavable(false);
		        	deliverGetDataMsg.setId(msg.getFirstSender().getUid());
		        	this.send(deliverGetDataMsg, getMyNode());
					
				} else {
					
					// A partir d'ici on s'intéresse au relay du message avec tout le fonctionnement derrière
					
					DhtNode deliverDataTo = null;
					int deliverToId = -1;
					
					// On initialise la variable différence comme infiny pour que forcément un résultat soit inférieur
					double diff = Double.POSITIVE_INFINITY;
					
					
					// Ici on parcourt notre table de routage
					for(DhtNode node : this.routingTable.keySet()) {
						
						// Si l'uid exacte correspondant à l'id de la donnée a été trouvé
						if(node.getUid() == msg.getId()) {
							deliverDataTo = node;
							deliverToId = this.routingTable.get(node);
							break;
						
						// Sinon on continue de parcourir la table pour essayer de trouver la jointure avec la plus petite différence entre l'id de la donnée et l'uid du noeud
						} else {
							if(Math.abs(msg.getId() - node.getUid()) < diff) {
								diff = Math.abs(msg.getId() - node.getUid());
								deliverDataTo = node;
								deliverToId = this.routingTable.get(node);
							}
						}
						
					}
					
					// A la toute fin, nous vérifions si au final la différence l'id de la donnée et l'uid d'un noeud n'est pas plus faible pour les voisins

					// Ici nous checkons pour le voisin de gauche
					if(Math.abs(msg.getId() - this.leftNeighbor.getUid()) < diff) {
						deliverDataTo = this.leftNeighbor;
						deliverToId = this.leftNeighbor.getId();
						diff = Math.abs(msg.getId() - this.leftNeighbor.getUid());
					}
					// Ici nous checkons pour le voisin de droite
					if(Math.abs(msg.getId() - this.rightNeighbor.getUid()) < diff) {
						deliverDataTo = this.rightNeighbor;
						deliverToId = this.rightNeighbor.getId();
						diff = Math.abs(msg.getId() - this.rightNeighbor.getUid());
					}
						
					
					// On relaye le message vers la cible trouvée
					this.send(msg, Network.get(deliverToId));
					System.out.println(prefixMsg + "Delivering message to " + deliverDataTo.getUid() + " {id = " + msg.getId() + "}");	
					
				}
				
				break;
			}
			// Ici on s'intéresse aux messages de type place left, lorsqu'un noeud doit changer son voisin de gauche
			case CREATE_LINK: {
				
				this.routingTable.put(msg.getTarget(), msg.getTarget().getId());
				System.out.println(prefixMsg + "Creating link between " + msg.getTarget().getUid() + " and " + this.uid);	
				break;
				
			}
			// Ici on s'intéresse aux messages de type remove link, lorsqu'un noeud doit supprimer une jointure de sa table de routage
			case REMOVE_LINK: {
				
				this.routingTable.remove(msg.getTarget());
				System.out.println(prefixMsg + "Removing link between " + msg.getTarget().getUid() + " and " + this.uid);	
				break;
				
			}
			// Ici on s'intéresse aux messages de type add data, lorsqu'un noeud sauvegarde des données
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
			// Ici on s'intéresse aux messages de type collect data, lorsqu'un noeud demande des données
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
			// Ici on s'intéresse aux messages de type remove data, lorsqu'un noeud supprime les données d'un autre noeud
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
			// Ici on s'intéresse aux messages de type add all data, lorsqu'un noeud ajoute toute les données reçues
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
			// Ici on s'intéresse aux messages de type show tree, ce message affiche le résultat des méthodes d'affichage d'anneau
			// Ce message nous permet de mieux visualiser l'anneau
			case SHOW_TREE: {
				
				System.out.println(getRingUid("Ring :\n", this, this));
				System.out.println(getRingNode("", this, this));
				break;
				
			}
			
			default:
				throw new IllegalArgumentException("Unexpected value");
			}
    }
    
    /*
     * Méthode permettant d'envoyer un message d'ajout de donnée vers un noeud ciblé
     */
    private void addData(String prefixMsg, DhtNode deliverDataTo, int deliverToId, Message msg) {
    	
    	System.out.println(prefixMsg + "Message's content : " + msg.getContent() + " {id = " + msg.getId() + "}");
		
		Message addData = new Message(MessageType.ADD_DATA, "Update your data !", msg);
		addData.setRemaining(3);
		addData.setId(msg.getId());
    	addData.setTarget(deliverDataTo);
    	this.send(addData, Network.get(deliverToId));
    		
    	
    }
    
    /*
     * Méthode permettant de comparer les différences de distance avec l'id de la donnée
     * Cela permet de savoir quel noeud entre le noeud courant et le noeuf en paramètre possède l'uid le plus proche de l'id de la donnée
     */
    private Object[] getBestDiff(Message msg, DhtNode node) {
    	
    	DhtNode deliverDataTo;
    	int deliverToId = -1;
    	
    	int diff = Math.abs(msg.getId() - this.getUid());
		int diff2 = Math.abs(msg.getId() - node.getUid());
		
		if(diff < diff2) {
			deliverDataTo = this;
			deliverToId = this.id;
		}
		else { 
			deliverDataTo = node;
			deliverToId = node.getId();
		}
		
		return new Object[] {deliverDataTo, deliverToId};
    }
    
    /*
     * Fonction permettant d'afficher les informations d'un noeud
     * Ici on affiche les données que le noeud à sauvegarder
     */
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
    
    /*
     * Méthode permettant de check si la taille de la liste des relayeurs est strictement supérieure à la taille de la dht divisée par 3
     * Si c'est le cas, alors on envoie un message de création de lien
     */
    private void checkCreateLink(Message msg) {
    	
    	if(msg.getSenders().size() > (Initializer.getNodeNb() / 3)) {
    		
    		Message createLink = new Message(MessageType.CREATE_LINK, "Create a link with me", this);
    		this.send(createLink, Network.get(msg.getFirstSender().getId()));
    		this.routingTable.put(msg.getFirstSender(), msg.getFirstSender().getId());
    	}
    	
    }
    
    /*
     * Fonction permettant de check si le message doit être relayé encore une fois
     */
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
    
    /*
     * Retourne l'anneau des identifiants uniques des noeuds sous forme de chaîne de caractères
     */
    private String getRingUid(String message, DhtNode startingNode, DhtNode currentNode) {
    	
    	message += " " + currentNode.getUid() + " -> ";
    	
		if(!currentNode.getRightNeighor().equals(startingNode)) {
			
			return getRingUid(message, startingNode, currentNode.getRightNeighor());
			
		} 
		
		return message;
    	
    }
    
    /*
     * Retourne l'anneau des numéros des noeuds sous forme de chaîne de caractères
     */
    private String getRingNode(String message, DhtNode startingNode, DhtNode currentNode) {
    	
    	message += " Node " + currentNode.getId() + " -> ";
    	
		if(!currentNode.getRightNeighor().equals(startingNode)) {
			
			return getRingNode(message, startingNode, currentNode.getRightNeighor());
			
		}
		
		return message;
    	
    }

    /*
     * Retourne le noeud courant
     */
    private Node getMyNode() {
    	return Network.get(this.id);
    }

    /*
     * Retourne le noeud courant sous format String
     */
    public String toString() {
    	return "Node " + this.id;
    }
    
    /*
     * Retourne le nombre correspondant au noeud actuel
     */
    public Integer getId() {
  		return this.id;
  	}
    
    /*
     * Retourne l'identifiant unique
     */
    public Integer getUid() {
  		return this.uid;
  	}
    
    /*
     * Définir l'uid
     */
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
     */
    public void setRightNeighbor(DhtNode node) {
    	
    	this.rightNeighbor = node;
    	
    }
    
    /**
     * Fonction pour ajouter un voisin à gauche
     */
    public void setLeftNeighbor(DhtNode node) {
    	
    	this.leftNeighbor = node;
    	
    }
    
}