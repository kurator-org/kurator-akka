package org.kurator.akka;

import java.util.List;
import java.util.Random;

/*
 * NOTE: This code was derived from akka.TimesActor in the FilteredPush repository at
 * svn://svn.code.sf.net/p/filteredpush/svn/trunk/FP-Akka as of 07Oct2014. 
 */
public class TimesActor extends WfActor {
    int constant;
    Random random = new Random();
    
    public TimesActor(int i, List<String> listeners) {
        super(listeners);
        this.constant = i;
    }

    public TimesActor(int i) {
        super();
        this.constant = i;
    }

    @Override
    public void fire(Object message) {
        long x = (Long)message * this.constant;
        if (x <= 1000) {
            broadcast(x);
        }
    }
}
