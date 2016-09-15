package org.kurator.akka;

import akka.actor.*;
import org.kurator.FSMActorStrategy;
import org.kurator.FSMActorWorkflow;
import org.kurator.akka.messages.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Akka finite state machine actor based on KuratorActor. Uses an instance of FSMActorStrategy to define the
 * business logic operations executed during state transitions.
 */
public class FSMActor extends AbstractLoggingFSM<FSMActor.State, FSMActor.Data> {

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create();

        ActorRef actor = system.actorOf(Props.create(FSMActor.class));
        ActorRef workflow = system.actorOf(Props.create(FSMActorWorkflow.class));

        actor.tell(new SubscribeTransitionCallBack(workflow), ActorRef.noSender());

        actor.tell(new InitializeFSM(new FSMActorStrategyImpl(), new ArrayList<ActorRef>()), ActorRef.noSender());
        actor.tell(new Start(), ActorRef.noSender());

        Map data = new LinkedHashMap<>();
        actor.tell(data, ActorRef.noSender());

        actor.tell(new EndOfStream(), ActorRef.noSender());
    }

    {
        // Begin actor in the constructed state
        startWith(State.CONSTRUCTED, new Data());

        // When an Initialize message is received in state CONSTRUCTED, call onInitialize() and go to state INITIALIZED
        when(State.CONSTRUCTED,
                matchEvent(InitializeFSM.class, Data.class, (initialize, data) -> {
                    data.listeners = initialize.listeners;
                    data.strategy = initialize.strategy;

                    data.strategy.onInitialize();

                    return goTo(State.INITIALIZED).replying(new Success());
                })
        );

        // When a Start message is received in state INITIALIZED, call onStart() and go to state STARTED
        when(State.INITIALIZED,
                matchEvent(Start.class, Data.class, (start, data) -> {
                    data.strategy.onStart();
                    return goTo(State.STARTED);
                })
        );

        // When the EndOfStream message message is received in state STARTED, call onEnd() and go to state ENDED
        when(State.STARTED,
                matchEvent(EndOfStream.class, Data.class, (end, data) -> {
                    data.strategy.onEnd();
                    return goTo(State.ENDED);
                })
        );

        // When a SetStrategy message is received in state STARTED, set the strategy and stay in state STARTED
        when(State.CONSTRUCTED,
                matchEvent(SetStrategy.class, Data.class, (setStrategy, data) -> {
                    data.strategy = setStrategy.strategy;
                    return stay();
                })
        );

        // When a RegisterListener message is received in state STARTED, add listener and stay in state STARTED
        when(State.CONSTRUCTED,
                matchEvent(RegisterListener.class, Data.class, (register, data) -> {
                    data.listeners.add(register.listener);
                    return stay();
                })
        );

        // When any other message is received in state STARTED, call onData() and stay in the current state
        when(State.STARTED,
                matchAnyEvent((message, data) -> {
                    data.strategy.onData(message);
                    return stay();
                })
        );

        // Messages received in the ENDED state are unhandled
        when(State.ENDED, AbstractFSM.NullFunction());

        whenUnhandled(matchAnyEvent((any, data) -> {
            log().info("Received unhandled event: " + any);
            return stay();
        }));

    }


    // states
    public enum State {
        CONSTRUCTED,
        BUILT,
        INITIALIZED,
        STARTED,
        ENDED
    }

    // state data
    public class Data {
        public FSMActorStrategy strategy;
        public List<ActorRef> listeners = new ArrayList<ActorRef>();
    }

}

/**
 * strategy class implementation for testing
 */
class FSMActorStrategyImpl extends FSMActorStrategy {
    @Override
    public void onInitialize() {
        System.out.println("onInitialize()");
    }

    @Override
    public void onStart() {
        System.out.println("onStart()");
    }

    @Override
    public void onData(Object value) {
        System.out.println("onData()");
    }

    @Override
    public void onEnd() {
        System.out.println("onEnd()");
    }
}