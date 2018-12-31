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


public class People
    extends Agent
{

    /**
     * Set up the agent. Register with the DF, and add a behaviour to process
     * incoming messages.  Also sends a message to the host to say that this
     * guest has arrived.
     */
    protected void setup() {
        try {
            // create the agent descrption of itself
            ServiceDescription sd = new ServiceDescription();
            sd.setType( "WerewolfPlayer" );
            sd.setName( "GuestServiceDescription" );
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName( getAID() );
            dfd.addServices( sd );

            // register the description with the DF
            DFService.register( this, dfd );

            // notify the host that we have arrived
            ACLMessage hello = new ACLMessage( ACLMessage.INFORM );
            hello.setContent( MJAgent.HELLOWEREWOLF );
            hello.addReceiver( new AID( "MJ", AID.ISLOCALNAME ) );
            send( hello );

            // add a Behaviour to process incoming messages
            addBehaviour( new CyclicBehaviour( this ) 
			{
                            public void action() {
                                // listen if a greetings message arrives
                                ACLMessage msg = receive( MessageTemplate.MatchPerformative( ACLMessage.INFORM ) );

                                if (msg != null) {
/*                                    if (HostAgent.GOODBYE.equals( msg.getContent() )) {
                                        // time to go
                                        leaveParty();
                                    }
                                    else if (msg.getContent().startsWith( HostAgent.INTRODUCE )) {
                                        // I am being introduced to another guest
                                        introducing( msg.getContent().substring( msg.getContent().indexOf( " " ) ) );
                                    }
                                    else if (msg.getContent().startsWith( HostAgent.HELLO )) {
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
 */                             System.out.println("Got somethin received in people");
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

