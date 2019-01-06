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
	protected ArrayList<AID> filthyWolfs;	            //The wolfs she's seen
	private boolean picked = false;
	
	protected void setup()
	{
		filthyWolfs = new ArrayList<AID>();
		
		MJ = new AID( "MJ", AID.ISLOCALNAME );

		// create the agent descrption of itself	
		try
		{			
			ServiceDescription sd = new ServiceDescription();
			sd.setType( "WerewolfPlayer" );
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
		}		
		catch(Exception e)
		{
		e.printStackTrace();
		}
		super.setup();
	}
	
	@Override

	protected void Detect(AID primeSuspect)
	{
		if (!filthyWolfs.contains(primeSuspect))
		{
			filthyWolfs.add(primeSuspect);
			if (trustyLivingPlayers.contains(primeSuspect))
			{
				trustyLivingPlayers.remove(primeSuspect);
			}
		}
	}
	
	@Override
	protected void WerewolfTimeAction(ACLMessage msg,boolean jumpStart){
		if (picked == false && filthyWolfs.size() < MJAgent.nWerewolves){
			for (Iterator i = otherLivingPlayers.iterator();  i.hasNext();  ) 
			{
				ACLMessage picking = new ACLMessage( ACLMessage.INFORM );
				picking.setContent( MJAgent.PICKINGGIRL );
				picking.addReceiver( (AID) i.next() );
				send(picking);
				System.out.println("Little girls : Hey! " + i.next() + " U asleep?");
			}
		}
	}
  
	@Override
	protected void VoteTimeAction(ACLMessage msg){
		
		if (filthyWolfs.size() != 0){
			SendAccusation(filthyWolfs.get(0), trustyLivingPlayers);
		}
		else if (msg.getContent() == getLocalName())									//On m'accuse, j'accuse en retour
		{
			trustyLivingPlayers.remove(msg.getSender());
			SendAccusation(msg.getSender(), trustyLivingPlayers);
		}
		else if (behaviour == BehaviourType.behaviours.suiveur)							//Le suiveur se fait convaincre à chaque fois et transmet l'info (une vrai girouette ce suiveur)
		{
			suspect = People.stringToAID(msg.getContent());
			SendAccusation(suspect, trustyLivingPlayers);
		}
		else if (behaviour == BehaviourType.behaviours.meneur)
		{
			if ( !trustyLivingPlayers.contains(msg.getContent())){						//Le meneur était déjà sur cette piste un peu, change donc de suspect et répend la cible
				suspect = People.stringToAID(msg.getContent());
				SendAccusation(suspect, trustyLivingPlayers);
			}
			else if(randInt(0, 9) < 2)													//Le meneur est convaincu, change de cible et transmet
			{
				trustyLivingPlayers.remove(msg.getContent());
				suspect = People.stringToAID(msg.getContent());
				SendAccusation(suspect, trustyLivingPlayers);
			}
			else{																		//Le meneur n'est pas convaincu, il répend donc sa théorie et pas celle qui lui arrive
				SendAccusation(suspect, trustyLivingPlayers);
			}
		}
	}
}