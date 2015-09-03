package org.kurator.akka.pojos;

public class Filter {

    public int max = 1;
    
    public Integer onData(Integer value) throws Exception {

        if (value <= max) {
            return value;
        } else {
            return null;
        }
    }
}