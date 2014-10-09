package org.kurator.akka;

import static akka.pattern.Patterns.ask;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.util.Timeout;

/* 
 * NOTE: This code was derived from akka.Hamming in the FilteredPush repository at
 * svn://svn.code.sf.net/p/filteredpush/svn/trunk/FP-Akka as of 07Oct2014. 
 */

public class Hamming {

    public static void main(String[] args) throws TimeoutException, InterruptedException {
        Hamming wf = new Hamming();
        wf.run();
    }

    @SuppressWarnings("serial")
	public void run() throws TimeoutException, InterruptedException {

        final ActorSystem system = ActorSystem.create("HammingWorkflow");
        
        final ActorRef oneShot = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                BroadcastActor a = new Oneshot();
                a.addListener("filter");
                return a;
            }
        }), "oneshot");

        final ActorRef filter = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
            	BroadcastActor a = new Filter(30);
                a.addListener("printStreamWriter");
                a.addListener("multiplyByTwo");
                a.addListener("multiplyByThree");
                a.addListener("multiplyByFive");
                return a;
            }
        }), "filter");

        final ActorRef multiplyByTwo = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
            	BroadcastActor a = new Multiplier(2);
                a.addListener("mergeTwoThree");
                return a;
            }
        }), "multiplyByTwo");

        final ActorRef multiplyByThree = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
            	BroadcastActor a = new Multiplier(3);
                a.addListener("mergeTwoThree");
                return a;
            }
        }), "multiplyByThree");

        final ActorRef multiplyByFive = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
            	BroadcastActor a = new Multiplier(5);
                a.addListener("mergeTwoThreeFive");
                return a;
            }
        }), "multiplyByFive");


        final ActorRef mergeTwoThree = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
            	BroadcastActor a = new IntegerStreamMerger(2);
                a.addListener("mergeTwoThreeFive");
                return a;
            }
        }), "mergeTwoThree");

        final ActorRef mergeTwoThreeFive = system.actorOf(new Props(new UntypedActorFactory() {
        	public UntypedActor create() {
        		BroadcastActor a = new IntegerStreamMerger(2);
                a.addListener("filter");
                return a;
            }
        }), "mergeTwoThreeFive");

        
        final ActorRef printStreamWriter = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new PrintStreamWriter(System.out);
            }
        }), "printStreamWriter");


        final ActorRef director = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                WorkflowDirector a = new WorkflowDirector(system);
                a.monitor("oneshot");
                a.monitor("filter");
                a.monitor("printStreamWriter");
                a.monitor("multiplyByTwo");
                a.monitor("multiplyByThree");
                a.monitor("multiplyByFive");                
                a.monitor("mergeTwoThree");
                a.monitor("mergeTwoThreeFive");
                return a;
            }
        }), "monitor");
        
		final Duration timeoutDuration = Duration.create(500, TimeUnit.SECONDS);
		final Timeout timeout = new Timeout(Duration.create(500, TimeUnit.SECONDS));
        Future<Object> future = ask(director, new Initialize(), timeout);
		future.ready(timeoutDuration, null);
        
        // start the calculation
        oneShot.tell(new Integer(1),system.lookupRoot());
                
       system.awaitTermination();
    }
}
