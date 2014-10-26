package org.kurator.akka.actors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.kurator.akka.messages.EndOfStream;

import akka.actor.ActorRef;

public class IntegerStreamMerger extends BroadcastActor {

    public int streamCount = 1;
    
    private Map<ActorRef, Queue<Object>> inputQueues = new HashMap<ActorRef, Queue<Object>>();;
    private Integer lastSent = Integer.MIN_VALUE;

    @Override
    public void onReceive(Object message) {

        super.onReceive(message);

        ActorRef sender = this.getSender();

        if (message instanceof Integer) {

            Integer inputValue = (Integer) message;

            // discard input value if not greater than the last value broadcast
            if (inputValue <= lastSent) {
                return;
            }

            // store received value in message queue corresponding to sender
            addToInputQueue(sender, message);

        } else if (message instanceof EndOfStream) {

            addToInputQueue(sender, message);
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

    private void fire() {

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
                    broadcast(endOfStreamMessage);
                    getContext().stop(getSelf());
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
