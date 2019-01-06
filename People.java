package WolfHunt;
import jade.core.Agent;
import jade.core.AID;

import jade.domain.FIPAException;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.StringACLCodec;
import java.io.StringReader;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;

import javax.swing.*;
import java.util.*;
import java.text.NumberFormat;

import java.lang.Math;

import java.util.Random;


public class People
    extends Agent
{
	protected ArrayList<AID> players ; 
	protected boolean awake;
	protected BehaviourType.behaviours behaviour;
	public BehaviourType behaviourType;
	public AID MJ;
	public State currentState;
	protected AID suspect;                       //Name of the current highest suspect
	protected ArrayList<AID> otherLivingPlayers;
	protected ArrayList<AID> trustyLivingPlayers;
	public static Random rand;
	
/**
 * Returns a pseudo-random number between min and max, inclusive.
 * The difference between min and max can be at most
 * <code>Integer.MAX_VALUE - 1</code>.
 *
 * @param min Minimum value
 * @param max Maximum value.  Must be greater than min.
 * @return Integer between min and max, inclusive.
 * @see java.util.Random#nextInt(int)
 */
public static int randInt(int min, int max) {
    if(rand==null)
	{
		rand = new Random();
	}
    int randomNum = rand.nextInt((max - min) + 1) + min;

    return randomNum;
}
	
	
    /**
     * Set up the agent. Register with the DF, and add a behaviour to process
     * incoming messages.  Also sends a message to the host to say that this
     * guest has arrived.
     */

    protected void setup() {
        try {
			players = new ArrayList();
			otherLivingPlayers = new ArrayList();
			trustyLivingPlayers = new ArrayList();
			if(randInt(0,1)==0)
			{
				System.out.println(getLocalName() +" est un meneur");
				behaviour = BehaviourType.behaviours.meneur;
			}
			else
			{
				System.out.println(getLocalName() +" est un suiveur");
				behaviour = BehaviourType.behaviours.suiveur;
			}
            // add a Behaviour to process incoming messages
            addBehaviour( new CyclicBehaviour( this ) 
			{
				public void action() {
					// listen if a greetings message arrives
					ACLMessage msg = receive( MessageTemplate.MatchPerformative( ACLMessage.INFORM ) );

					if (msg != null) 
					{
						if (MJAgent.GOODBYE.equals( msg.getContent() )) 
						{
							// time to go
							leaveParty();
						}
						else if (msg.getContent().startsWith( MJAgent.NIGHTTIME )) 
						{
							currentState = State.NIGHTTIME;
							System.out.println("Me "+ getLocalName() + " be sleepy");
							awake=false;
							if(players.size() == 0)//La première fois que tout le monde arrive on récupère les autres joueurs
							{
								try	
								{
									ServiceDescription sd = new ServiceDescription();
									sd.setType( "WerewolfPlayer" );
									DFAgentDescription dfd = new DFAgentDescription();
									dfd.addServices( sd );
									DFAgentDescription[] result = DFService.search(myAgent, dfd);
									for (int i = 0; i < result.length; ++i) 
									{
										players.add(result[i].getName());
										if (!result[i].getName().equals( getAID())){		//CHECK THIS
											otherLivingPlayers.add(result[i].getName());   
										}
									}
								}
								catch(FIPAException fe) {
									fe.printStackTrace();
								}
							}
							ack(msg);
						}
						else if (msg.getContent().startsWith(MJAgent.WAKEWEREWOLVES)) //On réveille les loups, et tout le monde ack
						{
							currentState = State.WEREWOLF;
							WerewolfTimeAction(msg,true); //avec jumpStart
							
						}
						else if (currentState == State.WEREWOLF && players.contains(stringToAID(msg.getContent())) && awake )
						{
								WerewolfTimeAction(msg,false);
									
						}
						else if(msg.getContent().startsWith(MJAgent.STFU)) //On arrête de discuter sur ordre du grand et beau MJ
						{
							awake=false;//On s'endort et on caste les votes
							CastVote();
						}
								/*	else if (currentState == State.VOTETIME && players.contains(stringToAID(msg.getContent()))){
										
										VoteTimeAction(msg);
									}*/

									//System.out.println(players.size());
									//System.out.println(otherLivingPlayers.size());
							
							//ack(msg); REMETTRE AU BON ENDROIT
						
						else if (msg.getContent().startsWith(MJAgent.VOTETIME))
						{
							currentState = State.VOTETIME;
							if(behaviour == BehaviourType.behaviours.meneur)
							{
								//pick player au hasard et start spread rumeur
								if (suspect == null || !otherLivingPlayers.contains(suspect))
								{
									suspect = otherLivingPlayers.get(randInt(0, 9));
								}
								SendAccusation(suspect, otherLivingPlayers);
							}
						}
						else if (msg.getContent().startsWith(MJAgent.PICKINGGIRL))
						{		//Little girl is asking if you're awake
							
							if (awake == true){		//only werewolfs
								
								if (randInt(0, 9) < 5){			//You didn't lie, for some reason
									ACLMessage answer = new ACLMessage( ACLMessage.INFORM );
									answer.addReceiver( msg.getSender() );
									answer.setContent(MJAgent.AWAKE);
									send(answer);
									System.out.println(getLocalName() + " : Nah, just killing some dudz");
								}
								else		//You lied, that's dirty!
								{
									System.out.println(getLocalName() + " : ......yes?..... /n Little girl : Oh! Sorry. Sleep tight!");
								}
								if (randInt(0, 11) < 4){		//You took note of the little girl asking
									System.out.println(getLocalName() + " : ....ravioli ravioli....");
									Detect(msg.getSender());
								}
							}
						}
						else if (msg.getContent().startsWith(MJAgent.AWAKE)){
							Detect(msg.getSender());
						}
						else if (currentState == State.VOTETIME && players.contains(msg.getContent())){							
							VoteTimeAction(msg);
						}
					}
					else {
						// if no message is arrived, block the behaviour
						block();
					}
				}
			} );
        }
        catch (Exception e) {
            System.out.println( "Saw exception in GuestAgent: " + e );
            e.printStackTrace();
        }

    }
	
	protected void Detect(AID primeSuspect)
	{
		
	}
	
	//Method sending a message to a random trusted player
	
	protected void SendAccusation(AID suspect, ArrayList<AID> possibleReceivers)   //CHECK THIS method especially the types of the objects
	{
		AID receiver = possibleReceivers.get(randInt(0, possibleReceivers.size() - 1));
		System.out.println( getLocalName() + " accused " + suspect.getName() + " in front of " + receiver.getName());
		ACLMessage accusation = new ACLMessage( ACLMessage.INFORM );
		accusation.setContent( suspect.toString() );
		accusation.addReceiver( receiver );
		send(accusation);
	}
	
	//sends vote to MJ using MJagent as receiver
	
	protected void CastVote()
	{
		if(suspect!=null)
		{
		System.out.println( getLocalName() + " voted for " + suspect);
		ACLMessage vote = new ACLMessage( ACLMessage.INFORM );
		vote.setContent( suspect.toString() );
		vote.addReceiver( MJ );
		send(vote);
		}
	}
	
	protected void ack(ACLMessage msg)
	{
		System.out.println( getLocalName() + "said ACK ");
		ACLMessage msgSent = new ACLMessage( ACLMessage.INFORM );
		msgSent.setContent( MJAgent.ACK );
		msgSent.addReceiver( msg.getSender() );
		send(msgSent);
	}
	public static AID stringToAID(String str)
{
		StringACLCodec codec = new StringACLCodec(new StringReader(str), null);
		try 
		{
			return codec.decodeAID();
		}
		catch(Exception code)
		{
			return null;
		}
	}
	
	protected void VoteTimeAction(ACLMessage msg){
		
	}
	
	protected void WerewolfTimeAction(ACLMessage msg,boolean jumpStart){
	
	}

    // Internal implementation methods
    //////////////////////////////////

    /**
     * To leave the party, we deregister with the DF and delete the agent from
     * the platform.
     */
    protected void leaveParty() {
        try {
            DFService.deregister( this );
            doDelete();
        }
        catch (FIPAException e) {
            System.err.println( "Saw FIPAException while leaving party: " + e );
            e.printStackTrace();
        }
    }



    //==============================================================================
    // Inner class definitions
    //==============================================================================

}

