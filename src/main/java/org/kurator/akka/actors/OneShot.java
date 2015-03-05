package org.kurator.akka.actors;

import org.kurator.akka.AkkaActor;

public abstract class OneShot extends AkkaActor {

    public abstract void fireOnce() throws Exception;
    
    @Override
    public void handleStart() throws Exception {
        fireOnce();
        endStreamAndStop();
    }
}
