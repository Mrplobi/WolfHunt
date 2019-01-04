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
   

    public static final String HELLOWEREWOLF = "MEBEWEREWOLF";
    public static final String HELLOVILLAGER = "MEBEPOORVILLAGER";
	public static final String HELLOLITTLEGIRL = "MEBEPOORLITTLEGIRL";
    public static final String ACK = "ACK";
    public static final String GOODBYE = "Bye";
	public static final String NIGHTTIME= "Night";
	public static final String DAYTIME= "Day";
	public static final String VOTETIME= "vote";
	public static final String WAKEWEREWOLVES= "WakeWerewolves";
	public HashMap<State, String> messageToSend;
	public State currentState = State.NIGHTTIME;
	
	public MJAgent()
	{
		messageToSend=new HashMap<State,String>();
		messageToSend.put(State.NIGHTTIME,NIGHTTIME);
		messageToSend.put(State.WEREWOLF,WAKEWEREWOLVES);
		messageToSend.put(State.DAYTIME,DAYTIME);
		messageToSend.put(State.VOTETIME,VOTETIME);
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
                                ACLMessage msg = receive();

                                if (msg != null) {
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
                                    if ( playersNumber == players.size()) 
									{
                                        System.out.println( "The night has set down, everybody close their eyes in JIN(X)City" );
                                        // all guests have arrived
										currentState=State.NIGHTTIME;
										sendState();
                                    }
									if(acksNumber == players.size())	
									{
										acksNumber=0;
										currentState = currentState.next();
										sendState();
									}
                                }
                                else {
                                    // if no message is arrived, block the behaviour
                                    block();
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
		int nWerewolves = 3;
		int nVillagers = 6;
		int nLittlegirl = 1;
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
			for (int i = 0; i < nLittlegirl; i++) {
				//create new agent
				String localName = "playerLittleGirl"+i;
				System.out.println("little girl created");
				AgentController guest = container.createNewAgent(localName, "WolfHunt.LittleGirl", null);
				guest.start();

                // keep the guest's ID on a local list
                players.add( new AID(localName, AID.ISLOCALNAME) );
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