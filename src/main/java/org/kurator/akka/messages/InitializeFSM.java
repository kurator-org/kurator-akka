package org.kurator.akka.messages;

import akka.actor.ActorRef;
import akka.japi.pf.FSMStateFunctionBuilder;
import org.kurator.FSMActorStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lowery on 9/15/16.
 */
public class InitializeFSM {
    public final FSMActorStrategy strategy;
    public final List<ActorRef> listeners;

    public InitializeFSM(FSMActorStrategy strategy, List<ActorRef> listeners) {
        this.strategy = strategy;
        this.listeners = listeners;
    }
}
