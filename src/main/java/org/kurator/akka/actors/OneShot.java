package org.kurator.akka.actors;

import org.kurator.akka.AkkaActor;

public abstract class OneShot extends AkkaActor {

    public abstract void fireOnce() throws Exception;
    
    @Override
    public void onStart() throws Exception {
        fireOnce();
        endStreamAndStop();
    }
}
