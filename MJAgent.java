 package WolfHunt;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ProfileImpl;
import jade.core.Profile;

import jade.wrapper.PlatformController;
import jade.wrapper.AgentController;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;

import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;

import javax.swing.*;
import java.util.*;
import java.text.NumberFormat;

/** Note: to start the host agent, it must be named 'MJ'.  Thus:
 * <code><pre>
 *     java jade.Boot MJ:WolfHunt.MJAgent()
 * </pre></code>
 * </p>
 **/

 public class MJAgent
    extends Agent
{
	protected ArrayList players = new ArrayList();    // invitees
    protected int playersNumber = 0;              // arrivals
    protected int acksNumber = 0;              // arrivals
	protected boolean isGameOver = true ;
	protected long m_startTime = 0;
	protected long startVote = 0;
	protected int voteReceived = 0;
	protected boolean saidStfu=false;
	protected boolean listenningToVote = false;
	protected long durationVote = 100;
	protected AID deadGuy=null;
    protected boolean gameStarted = false;

	public static int nWerewolves = 3;
	public static int nVillagers = 6;
	public static int nLittleGirl = 1;
    public static final String HELLOWEREWOLF = "MEBEWEREWOLF";
    public static final String HELLOVILLAGER = "MEBEPOORVILLAGER";
	public static final String HELLOLITTLEGIRL = "MEBEPOORLITTLEGIRL";
    public static final String ACK = "ACK";
    public static final String STFU = "STFU";
    public static final String GOODBYE = "Bye";
	public static final String NIGHTTIME= "Night";
	public static final String DAYTIME= "Day";
	public static final String VOTETIME= "vote";
	public static final String DEATHTIME= "Death";
	public static final String WAKEWEREWOLVES= "WakeWerewolves";
	public static final String PICKINGGIRL = "Uawake?";
	public static final String AWAKE = "FULLYAWAKEBB";
	public HashMap<State, String> messageToSend;
	public HashMap<AID, Integer> voteReceivedMap;
	public State currentState = State.NIGHTTIME;
	
	public MJAgent()
	{
		messageToSend=new HashMap<State,String>();
		messageToSend.put(State.NIGHTTIME,NIGHTTIME);
		messageToSend.put(State.WEREWOLF,WAKEWEREWOLVES);
		messageToSend.put(State.DAYTIME,DAYTIME);
		messageToSend.put(State.VOTETIME,VOTETIME);
		messageToSend.put(State.DEATHTIME, DEATHTIME);
		voteReceivedMap = new HashMap<AID, Integer> ();
		}
	public void sendState()
	{
		
		for (Iterator i = players.iterator();  i.hasNext();  ) 
		{
			ACLMessage msgSent = new ACLMessage( ACLMessage.INFORM );
			msgSent.setContent( messageToSend.get(currentState) );

			msgSent.addReceiver( (AID) i.next() );
			send(msgSent);
		}
	}
	  /**
     * Setup the agent.  Registers with the DF, and adds a behaviour to
     * process incoming messages.
     */
    protected void setup() {
        try {
            System.out.println( getLocalName() + " setting up");


            // create the agent descrption of itself
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName( getAID() );
            DFService.register( this, dfd );

            // add a Behaviour to handle messages from guests
            addBehaviour( new CyclicBehaviour( this ) {
                            public void action() {
								
																		
								if(currentState==State.WEREWOLF && System.currentTimeMillis()>startVote+durationVote && !saidStfu)
								{
									saidStfu=true;
									System.out.println("STFFU FFS");
									for (Iterator i = players.iterator();  i.hasNext();  ) 
										{
											ACLMessage msgSent = new ACLMessage( ACLMessage.INFORM );
											msgSent.setContent( STFU );
											msgSent.addReceiver( (AID) i.next() );	
											send(msgSent);
											listenningToVote = true;
											voteReceived=0;
										}
										/*try {
											// thread to sleep for 1000 milliseconds
											Thread.sleep(100);
										 } catch (Exception e) {
											System.out.println(e);
										 }*/
								}
								
								if(currentState==State.VOTETIME && System.currentTimeMillis()>startVote+durationVote && !saidStfu)
								{
									saidStfu=true;
									System.out.println("STFFU FFS");
									for (Iterator i = players.iterator();  i.hasNext();  ) 
										{
											ACLMessage msgSent = new ACLMessage( ACLMessage.INFORM );
											msgSent.setContent( STFU );
											msgSent.addReceiver( (AID) i.next() );	
											send(msgSent);
											listenningToVote = true;
											voteReceived=0;
										}
										try {
											// thread to sleep for 1000 milliseconds
											Thread.sleep(1000);
										 } catch (Exception e) {
											System.out.println(e);
										 }
								}
                                ACLMessage msg = receive();

                                if (msg != null) {
								//	AID victim = People.stringToAID(msg.getContent());
									
									if(listenningToVote && voteReceived<playersNumber &&  players.contains(People.stringToAID(msg.getContent())))//TODO: modify whith just alive ones
									{
										System.out.println(msg.getSender().getLocalName() + "a voté " + People.stringToAID(msg.getContent()).getLocalName() );
										if(voteReceivedMap.get(People.stringToAID(msg.getContent()))==null)
										{
										voteReceivedMap.put(People.stringToAID(msg.getContent()),1);	
										}
										else
										{
											
										voteReceivedMap.put(People.stringToAID(msg.getContent()),voteReceivedMap.get(People.stringToAID(msg.getContent()))+1);//increment vote count
										}
										voteReceived++;
										System.out.println(" votesRecieved :" + voteReceived + " players :" + playersNumber );
										
									}
									
									// Night werwolves votes
									if(listenningToVote && currentState==State.WEREWOLF && voteReceived>=nWerewolves)//TO MODIFY
									{
										listenningToVote=false;
										voteReceived=0;
										int max = -1;
										System.out.println("Size vote"+voteReceivedMap.size());
										//Kill the person
										for(Map.Entry<AID, Integer> entry : voteReceivedMap.entrySet()) 
										{
											AID key = entry.getKey();
											int value = entry.getValue();
											if(value > max)
											{
												deadGuy = key;
												max=value;
											}
										}
										System.out.println("Initial map elements: " + voteReceivedMap);
										voteReceivedMap.clear();
										System.out.println(deadGuy.getLocalName() + "VA MOURIR! MWAHAHHAHA");
										
										currentState = currentState.next();
										saidStfu = false;
										playersNumber--; //this guy died
										
										System.out.println("Since everybody acked let's do " + currentState);
										sendState();
										//=== dying part
										System.out.println("Notifying everyone player died");
										for (Iterator i = players.iterator();  i.hasNext();  ) 
										{
											ACLMessage msgSent = new ACLMessage( ACLMessage.INFORM );
											msgSent.setContent( deadGuy.toString() );
											msgSent.addReceiver( (AID) i.next() );	
											send(msgSent);
										}
										//Retire player de la liste
										players.remove(deadGuy);
										
										//verified count
										ServiceDescription sd = new ServiceDescription();
										sd.setType( "WerewolfPlayer" );
										sd.setName( "Werewolf" );
										DFAgentDescription dfd = new DFAgentDescription();
										dfd.addServices( sd );
										try
										{
											DFAgentDescription[] result = DFService.search(myAgent, dfd);
											nWerewolves=result.length;
											
											 for (int i = 0; i < result.length; ++i) 
											{
												if (result[i].getName().equals(deadGuy))
												{    
													System.out.println("Werewolf late to die");
													nWerewolves--;  
												}
											}
											
 //TODO check victory condition
										}
										catch (Exception e)
										{
											System.out.println("An error occured while counting werewolves");
										}
										//Checking little grill
										sd = new ServiceDescription();
										sd.setType( "WerewolfPlayer" );
										sd.setName( "LittleGirl" );
										dfd = new DFAgentDescription();
										dfd.addServices( sd );
										try
										{
											DFAgentDescription[] result = DFService.search(myAgent, dfd);
											nLittleGirl=result.length;
											
											 for (int i = 0; i < result.length; ++i) 
											{
												if (result[i].getName().equals(deadGuy))
												{    
													System.out.println("LittleGirl late to die");
													nLittleGirl--;  
												}
											}
											
 //TODO check victory condition
										}
										catch (Exception e)
										{
											System.out.println("An error occured while counting little grill");
										}
										//Villagerleft
										nVillagers=players.size() - nLittleGirl - nWerewolves;
										
										System.out.println("Il reste " + nWerewolves + "loup(s) garou(s), " + nVillagers + " villageois et "+ nLittleGirl + "petite(s) fille(s)");
										if(nVillagers+nLittleGirl==0)
										{
											System.out.println("Victoire des loups garous!");
										}
										if(nWerewolves==0)
										{
											System.out.println("Victoire des Villageois!");
										}
									}

									// idem for dayvote
									if(listenningToVote && currentState==State.VOTETIME && voteReceived>=playersNumber)//TO MODIFY
									{
										listenningToVote=false;
										voteReceived=0;
										int max = -1;
										System.out.println("Size vote"+voteReceivedMap.size());
										//Kill the person
										for(Map.Entry<AID, Integer> entry : voteReceivedMap.entrySet()) 
										{
											AID key = entry.getKey();
											int value = entry.getValue();
											if(value > max)
											{
												deadGuy = key;
												max=value;
											}
										}
										voteReceivedMap.clear();
										System.out.println(deadGuy.getLocalName() + "VA MOURIR! MWAHAHHAHA");
										
										saidStfu = false;
										playersNumber--; //this guy died
										
										currentState = currentState.next();
										System.out.println("Since everybody acked let's do " + currentState);
										sendState();
										//=== dying part
										System.out.println("Notifying everyone player died");
										for (Iterator i = players.iterator();  i.hasNext();  ) 
										{
											ACLMessage msgSent = new ACLMessage( ACLMessage.INFORM );
											msgSent.setContent( deadGuy.toString() );
											msgSent.addReceiver( (AID) i.next() );	
											send(msgSent);
										}
										//Retire player de la liste
										players.remove(deadGuy);
										
										//verified count
										ServiceDescription sd = new ServiceDescription();
										sd.setType( "WerewolfPlayer" );
										sd.setName( "Werewolf" );
										DFAgentDescription dfd = new DFAgentDescription();
										dfd.addServices( sd );
										try
										{
											DFAgentDescription[] result = DFService.search(myAgent, dfd);
											nWerewolves=result.length;
											
											 for (int i = 0; i < result.length; ++i) 
											{
												if (result[i].getName().equals(deadGuy))
												{    
													System.out.println("Werewolf late to die");
													nWerewolves--;  
												}
											}
											
 //TODO check victory condition
										}
										catch (Exception e)
										{
											System.out.println("An error occured while counting werewolves");
										}
										//Checking little grill
										sd = new ServiceDescription();
										sd.setType( "WerewolfPlayer" );
										sd.setName( "LittleGirl" );
										dfd = new DFAgentDescription();
										dfd.addServices( sd );
										try
										{
											DFAgentDescription[] result = DFService.search(myAgent, dfd);
											nLittleGirl=result.length;
											
											 for (int i = 0; i < result.length; ++i) 
											{
												if (result[i].getName().equals(deadGuy))
												{    
													System.out.println("LittleGirl late to die");
													nLittleGirl--;  
												}
											}
											
 //TODO check victory condition
										}
										catch (Exception e)
										{
											System.out.println("An error occured while counting little grill");
										}
										//Villagerleft
										nVillagers=players.size() - nLittleGirl - nWerewolves;
										
										System.out.println("Il reste " + nWerewolves + "loup(s) garou(s), " + nVillagers + " villageois et "+ nLittleGirl + "petite(s) fille(s)");
										if(nVillagers+nLittleGirl==0)
										{
											System.out.println("Victoire des loups garous!");
										}
										if(nWerewolves==0)
										{
											System.out.println("Victoire des Villageois!");
										}
										
									
									}

									
									
                                    if (HELLOWEREWOLF.equals(msg.getContent())) 
									{
                                        // a guest has arrived
                                        playersNumber++;
										System.out.println("the werewolf" + getLocalName() + "arrived. Have a nice hunt! ");
									}
									else if(HELLOVILLAGER.equals(msg.getContent())) 
									{
                                        // a guest has arrived
                                        playersNumber++;
										System.out.println("the villager" +  getLocalName() + "arrived. Have a nice taste! ");
									}
									else if(HELLOLITTLEGIRL.equals(msg.getContent())) 
									{
                                        // a guest has arrived
                                        playersNumber++;
										System.out.println("the little girl" +  getLocalName() + "arrived. Have a nice sneak! ");
									}
									else if(ACK.equals(msg.getContent())) 
									{
                                        // a ack was received
                                        acksNumber++;										
									}
                                    if ( playersNumber == players.size() && !gameStarted) 
									{
                                        System.out.println( "The night has set down, everybody close their eyes in JIN(X)City" );
                                        // all guests have arrived
										gameStarted = true;
										currentState=State.NIGHTTIME;
										sendState();
										
                                    }
									if(acksNumber == players.size())	
									{

										acksNumber=0;
										currentState = currentState.next();
										System.out.println("Since everybody acked let's do " + currentState);
										sendState();
										if(currentState == State.WEREWOLF)
										{
											System.out.println("vous avez 100 ms pour discuter. LOL");
											startVote = System.currentTimeMillis();
											System.out.println("il est"+ startVote);

										}
										if(currentState == State.VOTETIME)
										{
											System.out.println("vous avez 100 ms pour tous discuter. LOL");
											startVote = System.currentTimeMillis();
											System.out.println("il est"+ startVote);

										}
										
									}
                                }
                                else {
                                    // if no message is arrived, block the behaviour
                                  //  block();
                                }
								
								
								
                            }
                        } );
			startGame();
        }
        catch (Exception e) {
            System.out.println( "Saw exception in HostAgent: " + e );
            e.printStackTrace();
        }

    }
	 /**
     * Invite a number of guests, as determined by the given parameter.  Clears old
     * state variables, then creates N guest agents.  A list of the agents is maintained,
     * so that the host can tell them all to leave at the end of the party.
     *
     * @param nGuests The number of guest agents to invite.
     */
    protected void startGame()
	{
		
        // remove any old state
        players.clear();
        playersNumber = 0;
        isGameOver = false;
        
        // notice the start time
        m_startTime = System.currentTimeMillis();

        //setPartyState( "Inviting guests" );

		PlatformController container = getContainerController(); // get a container controller for creating new agents
        // create N guest agents
        try {
            for (int i = 0;  i < nWerewolves;  i++) {
                // create a new agent
				String localName = "playerWerewolf"+i;
				System.out.println("werewolf created");
				AgentController guest = container.createNewAgent(localName, "WolfHunt.Werewolf", null);
				guest.start();

				// keep the guest's ID on a local list
				players.add( new AID(localName, AID.ISLOCALNAME) );
            }
			for (int i = 0;  i < nVillagers;  i++) {
                // create a new agent
				String localName = "playerVillager"+i;
				System.out.println("villager created");
				AgentController guest = container.createNewAgent(localName, "WolfHunt.Villager", null);
				guest.start();

                // keep the guest's ID on a local list
                players.add( new AID(localName, AID.ISLOCALNAME) );
            }
			for (int i = 0; i < nLittleGirl; i++) {
				//create new agent
				String localName = "playerLittleGirl"+i;
				System.out.println("little girl created");
				AgentController guest = container.createNewAgent(localName, "WolfHunt.LittleGirl", null);
				guest.start();

                // keep the guest's ID on a local list
                players.add( new AID(localName, AID.ISLOCALNAME) );
        }
		}
        catch (Exception e) {
            System.err.println( "Exception while adding guests: " + e );
            e.printStackTrace();
        }
    }


    /**
     * End the game: set the state variables, and tell all the guests to leave.
     */
    protected void endGame() {
        //setPartyState( "Party over" );
        isGameOver = true;

        // log the duration of the run
        System.out.println( "Simulation run complete. NGuests = " + playersNumber + ", time taken = ");
        // send a message to all guests to tell them to leave
        for (Iterator i = players.iterator();  i.hasNext();  ) {
            ACLMessage msg = new ACLMessage( ACLMessage.INFORM );
            msg.setContent( GOODBYE );

            msg.addReceiver( (AID) i.next() );

            send(msg);
        }

        players.clear();
    }


}