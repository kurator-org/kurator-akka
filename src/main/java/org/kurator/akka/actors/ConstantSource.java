package org.kurator.akka.actors;

import java.util.Collection;

public class ConstantSource extends OneShot {

    public Object value;
    public Collection<Object> values;

    @Override
    public void fireOnce() throws Exception {
        
        if (value != null) {
            broadcast(value);
        } else if (values != null) {
            for (Object value : values) {
                broadcast(value);                    
            }
        }        
    }
}
