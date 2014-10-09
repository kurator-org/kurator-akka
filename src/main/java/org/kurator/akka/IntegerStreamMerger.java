package org.kurator.akka;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import akka.actor.ActorRef;

public class IntegerStreamMerger extends BroadcastActor {

    private int streamCount;
    private Map<ActorRef, Queue<Integer>> inputQueues;
    private Integer lastSent = Integer.MIN_VALUE;

    public IntegerStreamMerger(int streamCount) {
        this.streamCount = streamCount;
        inputQueues = new HashMap<ActorRef, Queue<Integer>>();
    }

    @Override
    public void onReceive(Object message) {
        super.onReceive(message);
        if (message instanceof Integer) {

            Integer inputValue = (Integer) message;

            // discard input value if not greater than the last value broadcast
            if (inputValue <= lastSent)
                return;

            // store received value in message queue corresponding to sender
            ActorRef sender = this.getSender();
            addToQueue(sender, inputValue);

            // fire if actor is ready
            if (isReadyToFire())
                fire();
        } else if (message instanceof EndOfStream) {
            ActorRef sender = this.getSender();
            inputQueues.remove(sender);
            if (--streamCount == 0) {
                broadcast(message);
                getContext().stop(getSelf());
            }
        }
    }

    private void addToQueue(ActorRef sender, Integer value) {
        Queue<Integer> q = inputQueues.get(sender);
        if (q == null) {
            q = new LinkedList<Integer>();
            inputQueues.put(sender, q);
        }
        q.add(value);
    }

    private boolean isReadyToFire() {
        if (inputQueues.size() < streamCount)
            return false;
        for (Queue<Integer> q : inputQueues.values()) {
            if (q.size() < 1)
                return false;
        }
        return true;
    }

    private void fire() {

        Integer minHead = Integer.MAX_VALUE;

        for (Queue<Integer> q : inputQueues.values()) {
            Integer head = q.peek();
            if (head < minHead)
                minHead = head;
        }

        for (Queue<Integer> q : inputQueues.values()) {
            while (q.peek() == minHead)
                q.remove();
        }

        lastSent = minHead;

        broadcast(minHead);
    }
}
