package org.kurator.akka;

import akka.actor.*;
/* 
 * NOTE: This code was derived from akka.Hamming in the FilteredPush repository at
 * svn://svn.code.sf.net/p/filteredpush/svn/trunk/FP-Akka as of 07Oct2014. 
 */
public class Hamming {

    public static void main(String[] args) {
        Hamming wf = new Hamming();
        wf.run();
    }

    private String enc = "UTF-8";

    public void run() {
        long starttime = System.currentTimeMillis();

        // Create an Akka system
        ActorSystem system = ActorSystem.create("HammingWf");

        final ActorRef txt1 = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new TextDisplay();
            }
        }), "txt1");

        final ActorRef ssal2 = system.actorOf(new Props(new UntypedActorFactory() {
                    public UntypedActor create() {
                StreamSorterAndLimiter s = new StreamSorterAndLimiter(2);
                s.addListener("txt1");
                s.addListener("mul5");
                return s;
            }
        }), "ssal2");


        final ActorRef ssal1 = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                StreamSorterAndLimiter s = new StreamSorterAndLimiter(2);
                s.addListener("ssal2");
                s.addListener("mul3");
                return s;
            }
        }), "ssal1");

        final ActorRef mul2 = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                TimesActor ta2 = new TimesActor(2);
                ta2.addListener("mul2");
                ta2.addListener("ssal1");
                return ta2;
            }
        }), "mul2");

        final ActorRef mul3 = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                TimesActor ta3 = new TimesActor(3);
                ta3.addListener("ssal1");
                return ta3;
            }
        }), "mul3");

        final ActorRef mul5 = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                TimesActor ta5 = new TimesActor(5);
                ta5.addListener("ssal2");
                return ta5;
            }
        }), "mul5");

        final ActorRef const2 = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new ConstActor(1,mul2);
            }
        }), "const2");

        final ActorRef const3 = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new ConstActor(1,mul3);
            }
        }), "const3");

        final ActorRef const5 = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new ConstActor(1,mul5);
            }
        }), "const5");

        // start the calculation
        const2.tell(new Trigger(),system.lookupRoot());
        const3.tell(new Trigger(),system.lookupRoot());
        const5.tell(new Trigger(),system.lookupRoot());
        system.awaitTermination();
        long stoptime = System.currentTimeMillis();
        System.err.printf("Runtime: %d ms",stoptime-starttime);
    }

    static class Curate {
    }
}
