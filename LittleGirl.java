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
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		super.setup();
	  }
}