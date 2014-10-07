package org.kurator.akka;

import java.util.*;

/* 
 * NOTE: This code was derived from akka.StreamSorterAndLimiter in the FilteredPush repository at
 * svn://svn.code.sf.net/p/filteredpush/svn/trunk/FP-Akka as of 07Oct2014. 
 */

public class StreamSorterAndLimiter extends WfActor {

    private int numStreams;
    private Map<String,Long> map = new HashMap<String, Long>();
    private Queue<Long> q = new PriorityQueue<Long>();


    public StreamSorterAndLimiter(int numStreams) {
        super();
        this.numStreams = numStreams;
    }

    @Override
    public void fire(Object message) {
        if (message instanceof Long) {
            Long x = (Long) message;
            String p = getPort();
            map.put(p,x);
            q.offer(x);
            if (map.keySet().size() >= numStreams)
                checkAndSend();
        }
    }

    private void checkAndSend() {
        Long min = Long.MAX_VALUE;
        for (Long i : map.values()) {
            if (i < min) min = i;
        }
        while (q.size() > 0 && q.peek() <= min) {
            broadcast(q.poll());
        }
    }
}
