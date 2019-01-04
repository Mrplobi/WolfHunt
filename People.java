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


public class People
    extends Agent
{
	protected ArrayList players ; 
	protected boolean awake;
	private behaviours behaviour;
	public BehaviourType behaviourType;
	public AID MJ;
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
												}
												System.out.println(players.size());
											}
											catch(FIPAException fe) {
												fe.printStackTrace();
											}
// Perform the request
										}
										
									}
									else if (msg.getcontent().startsWith(MJAgent.VOTETIME))
									{
										if(this.behaviour = behaviours.meneur)
										{
											//pick player au hasard et start spread rumeur
										}
										if(this.behaviour = behaviours.suiveur)
										{
											//
										}
									}
//TODO GERER MIEUX LE ACK
									//									ack(msg);
/*                                    else if (msg.getContent().startsWith( HostAgent.HELLO )) {
                                        // someone saying hello
                                        passRumour( msg.getSender() );
                                    }
                                    else if (msg.getContent().startsWith( HostAgent.RUMOUR )) {
                                        // someone passing a rumour to me
                                        hearRumour();
                                    }
                                    else {
                                        System.out.println( "Guest received unexpected message: " + msg );
                                    }
 */          //                   System.out.println("Got somethin received in people");
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

