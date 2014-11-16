package org.kurator.akka.actors;

public abstract class OneShot extends Transformer {

    public boolean sendEos = true;
    
    public abstract void fireOnce() throws Exception;
    
    @Override
    public void handleStart() throws Exception {

        fireOnce();
        
        if (sendEos) {
            endStreamAndStop();
        } else {
            stop();
        }
    }
    
    @Override
    public final void onReceive(Object message) throws Exception {

        super.onReceive(message);
    }
}
