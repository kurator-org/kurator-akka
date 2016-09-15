package org.kurator.akka.messages;

import org.kurator.FSMActorStrategy;

/**
 * Created by lowery on 9/15/16.
 */
public class SetStrategy {
    public final FSMActorStrategy strategy;

    public SetStrategy(FSMActorStrategy strategy) {
        this.strategy = strategy;
    }
}
