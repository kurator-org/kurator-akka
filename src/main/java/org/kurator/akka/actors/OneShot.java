package org.kurator.akka.actors;

import org.kurator.akka.KuratorActor;

public abstract class OneShot extends KuratorActor {

    public abstract void fireOnce() throws Exception;
    
    @Override
    public void onStart() throws Exception {
        fireOnce();
        endStreamAndStop();
    }
}
