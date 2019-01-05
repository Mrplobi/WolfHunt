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


public class LittleGirl extends People
{
  protected void setup()
  {
	MJ = new AID( "MJ", AID.ISLOCALNAME );

	// create the agent descrption of itself
	try
	{			
		ServiceDescription sd = new ServiceDescription();
		sd.setType( "LittleGirlPlayer" );
		sd.setName( "LittleGirl" );
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName( getAID() );
		dfd.addServices( sd );

		// register the description with the DF
		DFService.register( this, dfd );

		// notify the host that we have arrived
		ACLMessage hello = new ACLMessage( ACLMessage.INFORM );
		hello.setContent( MJAgent.HELLOLITTLEGIRL );
		hello.addReceiver( MJ );
		send( hello );
		
		
		addBehaviour( new CyclicBehaviour( this ) 
		{
			public void action() {
				if (currentState = WEREWOLF)
				{
				// listen if a message arrives
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
	catch(Exception e)
	{
		e.printStackTrace();
	}
	super.setup();
  }
}