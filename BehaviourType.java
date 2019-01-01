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


public abstract class BehaviourType
{
	public abstract void morningVote();
	public abstract void werewolfVote();
}