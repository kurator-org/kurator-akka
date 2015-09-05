package org.kurator.akka.pojos;

import org.kurator.akka.PojoActor.Port;

public class Ramp {

    public int start = 1;
    public int end = 1;
    public int step = 1;
    public Port port;
    
    public void onStart() throws Exception {

        for (int value = start; value <= end; value += step) {
            port.write(value);
        }
    }
}