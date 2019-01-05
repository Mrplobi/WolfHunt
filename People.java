package WolfHunt;
import jade.core.Agent;
import jade.core.AID;

import jade.domain.FIPAException;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.core.behaviours.CyclicBehaviour;

import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;

import javax.swing.*;
import java.util.*;
import java.text.NumberFormat;

import java.lang.Math;

import java.util.Random;

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

    // NOTE: This will (intentionally) not run as written so that folks
    // copy-pasting have to think about how to initialize their
    // Random instance.  Initialization of the Random instance is outside
    // the main scope of the question, but some decent options are to have
    // a field that is initialized once and then re-used as needed or to
    // use ThreadLocalRandom (if using at least Java 1.7).
    // 
    // In particular, do NOT do 'Random rand = new Random()' here or you
    // will get not very good / not very random results.
    Random rand;

    // nextInt is normally exclusive of the top value,
    // so add 1 to make it inclusive
    int randomNum = rand.nextInt((max - min) + 1) + min;

    return randomNum;
}

public class People
    extends Agent
{
	protected ArrayList players ; 
	protected boolean awake;
	protected behaviours behaviour;
	public BehaviourType behaviourType;
	public AID MJ;
	public State currentState;
	protected String suspect;                       //Name of the current highest suspect
	protected ArrayList trustyLivingPlayers;
	protected ArrayList werewolfs;
	
	
	
    /**
     * Set up the agent. Register with the DF, and add a behaviour to process
     * incoming messages.  Also sends a message to the host to say that this
     * guest has arrived.
     */

    protected void setup() {
        try {
			players = new ArrayList();
						//=============== 
            // add a Behaviour to process incoming messages
            addBehaviour( new CyclicBehaviour( this ) 
			{
                            public void action() {
                                // listen if a greetings message arrives
                                ACLMessage msg = receive( MessageTemplate.MatchPerformative( ACLMessage.INFORM ) );

                                if (msg != null) {
                                    if (MJAgent.GOODBYE.equals( msg.getContent() )) 
									{
                                        // time to go
                                        leaveParty();
                                    }
                                    else if (msg.getContent().startsWith( MJAgent.NIGHTTIME )) 
									{
										currentState = NIGHTTIME;
										System.out.println("Me "+ getLocalName() + "be sleepy");
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
													if (result[i].getName() != getLocalName()){		//CHECK THIS
														livingPlayers.add(result[i].getName());   
													}
												}
												System.out.println(players.size());
												System.out.println(livingPlayers.size());
											}
											catch(FIPAException fe) {
												fe.printStackTrace();
											}
// Perform the request
										}
										
									}
									else if (msg.getcontent().startsWith(MJAgent.VOTETIME))
									{
										currentState = VOTETIME;
										if(this.behaviour = behaviours.meneur)
										{
											//pick player au hasard et start spread rumeur
											if (suspect == null)
											{
												suspect = otherLivingPlayers[randInt(0, 9)];
											}
											SendAccusation(suspect, otherLivingPlayers);
										}
									}
									else if (currentState == VOTETIME && players.contains(msg.getcontent()){
										if (msg.getContent() == getLocalName())
										{
											trustyLivingPlayers.remove(msg.getSender().getName());
											SendAccusation(msg.getSender().getName(), trustyLivingPlayers);
										}
										else if (behaviour == suiveur)									//Le suiveur se fait convaincre à chaque fois et transmet l'info (une vrai girouette ce suiveur)
										{
											suspect = msg.getContent();
											SendAccusation(suspect, trustyLivingPlayers);
										}
										else if (behaviour = meneur)
										{
											if ( !trustyLivingPlayers.contains(msg.getContent()){						//Le meneur était déjà sur cette piste un peu, change donc de suspect et répend la cible
												suspect = msg.getContent();
												SendAccusation(suspect, trustyLivingPlayers);
											}
											else if(randInt(0, 9) < 2)				//Le meneur est convaincu, change de cible et transmet
											{
												trustyLivingPlayers.remove(msg.getContent());
												suspect = msg.getContent();
												SendAccusation(suspect, trustyLivingPlayers);
											}
											else{									//Le meneur n'est pas convaincu, il répend donc sa théorie et pas celle qui lui arrive
												SendAccusation(suspect, trustyLivingPlayers);
											}
										}
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
	
	//Method sending a message to a random trusted player
	
	protected void SendAccusation(String suspect, ArrayList possibleReceivers)   //CHECK THIS method especially the types of the objects
	{
		People receiver = possibleReceivers[randInt(0, possibleReceivers.size()];
		System.out.println( getLocalName() + " accused " + suspect.getName() + " in front of " + receiver.getName());
		ACLMessage accusation = new ACLMessage( ACLMessage.INFORM );
		accusation.setContent( suspect );
		accusation.addReceiver( receiver.getAID() );
		send(accusation);
	}
	
	//sends vote to MJ using MJagent as receiver
	
	protected void CastVote(String suspect)
	{
		System.out.println( getLocalName() + " voted for " + suspect.getName());
		ACLMessage vote = new ACLMessage( ACLMessage.INFORM );
		vote.setContent( suspect );
		vote.addReceiver( MJAgent );
		send(vote);
	}
	
	protected void ack(ACLMessage msg)
	{
			System.out.println( getLocalName() + "said ACK ");
			ACLMessage msgSent = new ACLMessage( ACLMessage.INFORM );
			msgSent.setContent( MJAgent.ACK );
			msgSent.addReceiver( msg.getSender() );
			send(msgSent);
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

