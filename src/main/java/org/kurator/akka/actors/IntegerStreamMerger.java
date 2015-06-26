package org.kurator.akka.actors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.kurator.akka.AkkaActor;
import org.kurator.akka.messages.EndOfStream;

import akka.actor.ActorRef;

public class IntegerStreamMerger extends AkkaActor {

    public int streamCount = 1;
    
    private Map<ActorRef, Queue<Object>> inputQueues = new HashMap<ActorRef, Queue<Object>>();;
    private Integer lastSent = Integer.MIN_VALUE;
    
    public IntegerStreamMerger() {
        super();
        endOnEos = false;
    }

    @Override
    public void onEndOfStream(EndOfStream message) throws Exception {
        addToInputQueue(this.getSender(), message);
        fire();
    }
    
    @Override
    public void onData(Object value) throws Exception {

        ActorRef sender = this.getSender();

        if (value instanceof Integer) {

            Integer intValue = (Integer) value;

            // discard input value if not greater than the last value broadcast
            if (intValue <= lastSent) {
                return;
            }

            // store received value in message queue corresponding to sender
            addToInputQueue(sender, value);
        }

        // fire if actor is ready
        fire();
    }

    private void addToInputQueue(ActorRef sender, Object message) {
        Queue<Object> q = inputQueues.get(sender);
        if (q == null) {
            q = new LinkedList<Object>();
            inputQueues.put(sender, q);
        }
        q.add(message);
    }

    private boolean isReadyToFire() {

        if (inputQueues.size() < streamCount) {
            return false;
        }

        for (Queue<Object> q : inputQueues.values()) {
            if (q.size() < 1)
                return false;
        }

        return true;
    }

    private void fire() throws Exception {

        while (isReadyToFire()) {

            Set<Entry<ActorRef, Queue<Object>>> closingInputStreams = new HashSet<Entry<ActorRef, Queue<Object>>>();

            for (Entry<ActorRef, Queue<Object>> entry : inputQueues.entrySet()) {
                Queue<Object> queue = entry.getValue();
                Object message = queue.peek();
                if (message instanceof EndOfStream) {
                    closingInputStreams.add(entry);
                }
            }

            for (Entry<ActorRef, Queue<Object>> entry : closingInputStreams) {
                ActorRef sender = entry.getKey();
                inputQueues.remove(sender);
                if (--streamCount == 0) {
                    Queue<Object> queue = entry.getValue();
                    EndOfStream endOfStreamMessage = (EndOfStream) queue.peek();
                    endStreamAndStop(endOfStreamMessage);
                    return;
                }
            }

            Integer minHead = Integer.MAX_VALUE;

            for (Queue<Object> q : inputQueues.values()) {
                Integer head = (Integer) q.peek();
                if (head < minHead) {
                    minHead = head;
                }
            }

            for (Queue<Object> q : inputQueues.values()) {
                while (q.peek() == minHead)
                    q.remove();
            }

            if (minHead > lastSent) {
                lastSent = minHead;
                broadcast(minHead);
            }
        }
    }
}
