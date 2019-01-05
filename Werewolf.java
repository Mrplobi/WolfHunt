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


public class Werewolf extends People
{
	private ArrayList<AID> werewolfs;
	private ArrayList<AID> nonWolf;
	
	protected void setup()
	{
		
		MJ=new AID( "MJ", AID.ISLOCALNAME );

		// create the agent descrption of itself
		try
		{			
		ServiceDescription sd = new ServiceDescription();
		sd.setType( "WerewolfPlayer" );
		sd.setName( "Werewolf" );
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName( getAID() );
		dfd.addServices( sd );

		// register the description with the DF
		DFService.register( this, dfd );
		
		// notify the host that we have arrived
		ACLMessage hello = new ACLMessage( ACLMessage.INFORM );
		hello.setContent( MJAgent.HELLOWEREWOLF );
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
	protected void WerewolfTimeAction(ACLMessage msg)
	{
		if(werewolfs==null)//On remplit les deux listes wold et not wolf
		{
		 werewolfs=new ArrayList<AID>();
		 nonWolf=new ArrayList<AID>();
		ServiceDescription sd = new ServiceDescription();
		sd.setType( "WerewolfPlayer" );
		sd.setName( "Werewolf" );
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.addServices( sd );
		try
		{
		DFAgentDescription[] result = DFService.search(this, dfd);
		System.out.println("COMPARE "+ getAID()); 
		for (int i = 0; i < result.length; ++i) 
		{
			//players.add(result[i].getName());
			
			if (result[i].getName() != getAID()){	
			System.out.println("TO "+result[i].getName());
			werewolfs.add(result[i].getName());   
			}
		}
		for (int i = 0; i < players.size(); ++i) 
		{
			if(!werewolfs.contains(players.get(i)))
			{
				nonWolf.add(players.get(i));
			}
		}
		
		System.out.println("We detected"+werewolfs.size() + " wolves and " + nonWolf.size() + " nonWolf");
		
		}
		catch(Exception e)
		{ 
		System.out.println("an error occured while finding other wolves");
		}
		}
		if (suspect == null){
			
			//System.out.println(ind);
			suspect = nonWolf.get(randInt(0,nonWolf.size() - 1));
			System.out.println("Me be " + behaviour + " " + getLocalName() + ". Me want to eat" + suspect);
		}
		if(nonWolf.contains(stringToAID(msg.getContent()))){
			//System.out.println(msg.getSender() + " LAAAAAAAAAAAAAAAAAAAAAAAAAAA " + msg.getContent()  ); 
			if (behaviour == BehaviourType.behaviours.suiveur)																//Le suiveur se fait convaincre à chaque fois et transmet l'info (une vrai girouette ce suiveur)
			{
				suspect = stringToAID(msg.getContent());
				SendAccusation(suspect, werewolfs);
			}
			else if (behaviour == BehaviourType.behaviours.meneur)
			{
				if(randInt(0, 9) < 2)																//Le meneur est convaincu, change de cible et transmet
				{
					trustyLivingPlayers.remove(msg.getContent());
					suspect = stringToAID(msg.getContent());
					SendAccusation(suspect, werewolfs);
				}
			}
		}
		else{																				//Le meneur n'est pas convaincu, il répend donc sa théorie et pas celle qui lui arrive
			SendAccusation(suspect, werewolfs);
			}
							
		
	}
	
	@Override
	protected void VoteTimeAction(ACLMessage msg){		
		if (msg.getContent() == getLocalName() || werewolfs.contains(msg.getContent()))				//On m'accuse moi ou mes potos, j'accuse en retour
		{
			trustyLivingPlayers.remove(msg.getSender().getName());
			SendAccusation(msg.getSender(), trustyLivingPlayers);
		}
		else if (behaviour == BehaviourType.behaviours.suiveur)																//Le suiveur se fait convaincre à chaque fois et transmet l'info (une vrai girouette ce suiveur)
		{
			suspect = stringToAID(msg.getContent());
			SendAccusation(suspect, trustyLivingPlayers);
		}
		else if (behaviour == BehaviourType.behaviours.meneur)
		{
			if ( !trustyLivingPlayers.contains(msg.getContent())){									//Le meneur était déjà sur cette piste un peu, change donc de suspect et répend la cible
				suspect = stringToAID(msg.getContent());
				SendAccusation(suspect, trustyLivingPlayers);
			}
			else if(randInt(0, 9) < 2)																//Le meneur est convaincu, change de cible et transmet
			{
				trustyLivingPlayers.remove(msg.getContent());
				suspect = stringToAID(msg.getContent());
				SendAccusation(suspect, trustyLivingPlayers);
			}
			else{																					//Le meneur n'est pas convaincu, il répend donc sa théorie et pas celle qui lui arrive
				SendAccusation(suspect, trustyLivingPlayers);
			}
		}
	}
}