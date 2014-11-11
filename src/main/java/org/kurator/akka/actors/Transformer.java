package org.kurator.akka.actors;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.kurator.akka.ActorBuilder;
import org.kurator.akka.WorkflowBuilder;
import org.kurator.akka.messages.ControlMessage;
import org.kurator.akka.messages.EndOfStream;
import org.kurator.akka.messages.Initialize;
import org.kurator.akka.messages.Response;
import org.kurator.akka.messages.StartMessage;

import akka.actor.ActorRef;

public abstract class Transformer extends AkkaActor {

    private final Set<ActorRef> listeners = new HashSet<ActorRef>();
    private List<ActorBuilder> listenerConfigurations = new LinkedList<ActorBuilder>();
    private WorkflowBuilder runner;
    public boolean endOnEos = true;

    public void addListeners(Set<ActorRef> listeners) {
        this.listeners.addAll(listeners);
    }
    
    public void setListenerConfigs(List<ActorBuilder> listenerConfigurations) {
        if (listenerConfigurations != null) {
            this.listenerConfigurations = listenerConfigurations;
        }
    }
    
    public void setWorkflowRunner(WorkflowBuilder runner) {
        this.runner = runner;
    }

    
    public void handleInitialize() throws Exception {}
    public void handleStart() throws Exception {}
    public void handleControlMessage(ControlMessage message) {}
    public void handleDataMessage(Object message) throws Exception {}
    public void handleEnd() throws Exception {}
    
    @Override
    public final void onReceive(Object message) throws Exception {

        if (message instanceof Initialize) {
            
            for (ActorBuilder listenerConfig : listenerConfigurations) {
                ActorRef listener = runner.getActorForConfig(listenerConfig);
                listeners.add(listener);
            }
                        
            getSender().tell(new Response(), getSelf());
            
            handleInitialize();
            
        } else if (message instanceof StartMessage) {
            
            handleStart();
        
        } else if (message instanceof EndOfStream && endOnEos) {

            handleEnd();
            broadcast(message);
            getContext().stop(getSelf());
            
        }

        if (message instanceof ControlMessage) {
            handleControlMessage((ControlMessage)message);
        } else {
            handleDataMessage(message);
        }
    }

    protected void broadcast(Object message) {
        for (ActorRef listener : listeners) {
            listener.tell(message, this.getSelf());
        }
    }
}