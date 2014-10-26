package org.kurator.akka.actors;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.kurator.akka.ActorConfiguration;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.messages.Initialize;
import org.kurator.akka.messages.Response;

import akka.actor.ActorRef;

public abstract class BroadcastActor extends AkkaActor {

    private final Set<ActorRef> listeners = new HashSet<ActorRef>();
    private List<ActorConfiguration> listenerConfigurations = new LinkedList<ActorConfiguration>();
    private WorkflowRunner runner;

    public void addListeners(Set<ActorRef> listeners) {
        this.listeners.addAll(listeners);
    }
    
    public void setListenerConfigs(List<ActorConfiguration> listenerConfigurations) {
        if (listenerConfigurations != null) {
            this.listenerConfigurations = listenerConfigurations;
        }
    }
    
    public void setWorkflowRunner(WorkflowRunner runner) {
        this.runner = runner;
    }

    
    @Override
    public void onReceive(Object message) {

        if (message instanceof Initialize) {
            
            for (ActorConfiguration listenerConfig : listenerConfigurations) {
                ActorRef listener = runner.getActorForConfig(listenerConfig);
                listeners.add(listener);
            }
                        
            getSender().tell(new Response(), getSelf());
        }
    }

    protected void broadcast(Object message) {
        for (ActorRef listener : listeners) {
            listener.tell(message, this.getSelf());
        }
    }
}