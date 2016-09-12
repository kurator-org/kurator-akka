package org.kurator;

import akka.actor.FSM;
import akka.actor.UntypedActor;
import org.kurator.akka.FSMActor;

/**
 * FSM Workflow supervisor actor watches for state transitions of its actors
 */
public class FSMActorWorkflow extends UntypedActor {
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof FSM.CurrentState) {
            System.out.println(((FSM.CurrentState) message).state());
        } else if (message instanceof FSM.Transition) {
            FSM.Transition transition = ((FSM.Transition) message);

            if (transition.to().equals(FSMActor.State.STARTED)) {
                System.out.println("Started actor: " + transition.fsmRef());
            }
            System.out.println("from: " + ((FSM.Transition) message).from() + " to: " + ((FSM.Transition) message).to());
        }
    }
}
