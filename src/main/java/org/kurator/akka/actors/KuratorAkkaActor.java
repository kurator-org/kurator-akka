package org.kurator.akka.actors;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.kurator.akka.ActorBuilder;
import org.kurator.akka.WorkflowBuilder;
import org.kurator.akka.messages.ControlMessage;
import org.kurator.akka.messages.EndOfStream;
import org.kurator.akka.messages.ExceptionMessage;
import org.kurator.akka.messages.Initialize;
import org.kurator.akka.messages.Response;
import org.kurator.akka.messages.StartMessage;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public abstract class KuratorAkkaActor extends UntypedActor{

    public boolean endOnEos = true;
    
    protected final Set<ActorRef> listeners = new HashSet<ActorRef>();
    protected List<ActorBuilder> listenerConfigurations = new LinkedList<ActorBuilder>();
    protected WorkflowBuilder runner;

    public void handleInitialize() throws Exception {}
    public void handleStart() throws Exception {}
    public void handleControlMessage(ControlMessage message) {}
    public void handleEnd() throws Exception {}

    
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
    
    @Override
    public void onReceive(Object message) throws Exception {

        try {
            
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
                endStreamAndStop((EndOfStream)message);
            }
    
        } catch (Exception e) {

            endStreamAndStop();
        }
    }   
    
    
    protected void endStreamAndStop(EndOfStream message) { 
        broadcast(message);
        stop();
    }
    
    protected void endStreamAndStop() { 
        endStreamAndStop(new EndOfStream());
    }
    
    protected void broadcast(Object message) {
        for (ActorRef listener : listeners) {
            listener.tell(message, this.getSelf());
        }
    }
    
    public void stop() {
        getContext().stop(getSelf());
    }
    
    protected void reportException(Exception e) {
        ActorRef workflowRef = runner.getWorkflowRef();
        ExceptionMessage em = new ExceptionMessage(e);
        workflowRef.tell(em, this.getSelf());
    }
}
