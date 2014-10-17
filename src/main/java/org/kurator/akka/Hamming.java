package org.kurator.akka;

import static akka.pattern.Patterns.ask;

import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.util.Timeout;

public class Hamming {

    private int maxHammingNumber;
    private PrintStream outputStream;
    private String separator;

    public Hamming(int maxHammingNumber, PrintStream outputStream,
            String separator) {
        this.maxHammingNumber = maxHammingNumber;
        this.outputStream = outputStream;
        this.separator = separator;
    }

    public Hamming(int maxHammingNumber) {
        this(maxHammingNumber, System.out, System.lineSeparator());
    }

    @SuppressWarnings({ "serial", "unused" })
    public void run() throws TimeoutException, InterruptedException {

        final ActorSystem system = ActorSystem.create("HammingWorkflow");

        final ActorRef oneShot = system.actorOf(new Props(
                new UntypedActorFactory() {
                    public UntypedActor create() {
                        return new Oneshot();
                    }
                }), "oneshot");

        final ActorRef filter = system.actorOf(new Props(
                new UntypedActorFactory() {
                    public UntypedActor create() {
                        return new Filter(maxHammingNumber);
                    }
                }), "filter");

        final ActorRef multiplyByTwo = system.actorOf(new Props(
                new UntypedActorFactory() {
                    public UntypedActor create() {
                        return new Multiplier(2);
                    }
                }), "multiplyByTwo");

        final ActorRef multiplyByThree = system.actorOf(new Props(
                new UntypedActorFactory() {
                    public UntypedActor create() {
                        return new Multiplier(3);
                    }
                }), "multiplyByThree");

        final ActorRef multiplyByFive = system.actorOf(new Props(
                new UntypedActorFactory() {
                    public UntypedActor create() {
                        return new Multiplier(5);
                    }
                }), "multiplyByFive");

        final ActorRef mergeTwoThree = system.actorOf(new Props(
                new UntypedActorFactory() {
                    public UntypedActor create() {
                        return new IntegerStreamMerger(2);
                    }
                }), "mergeTwoThree");

        final ActorRef mergeTwoThreeFive = system.actorOf(new Props(
                new UntypedActorFactory() {
                    public UntypedActor create() {
                        return new IntegerStreamMerger(2);
                    }
                }), "mergeTwoThreeFive");

        final ActorRef printStreamWriter = system.actorOf(new Props(
                new UntypedActorFactory() {
                    public UntypedActor create() {
                        return new PrintStreamWriter(outputStream, separator);
                    }
                }), "printStreamWriter");

        final ActorRef workflow = system.actorOf(new Props(
                new UntypedActorFactory() {
                    public UntypedActor create() {

                        Workflow a = new Workflow(system);

                        a.actor("oneshot");
                        a.actor("filter");
                        a.actor("printStreamWriter");
                        a.actor("multiplyByTwo");
                        a.actor("multiplyByThree");
                        a.actor("multiplyByFive");
                        a.actor("mergeTwoThree");
                        a.actor("mergeTwoThreeFive");

                        a.connection("oneshot", "filter");
                        a.connection("filter", "printStreamWriter");
                        a.connection("filter", "multiplyByTwo");
                        a.connection("filter", "multiplyByThree");
                        a.connection("filter", "multiplyByFive");
                        a.connection("multiplyByTwo", "mergeTwoThree");
                        a.connection("multiplyByThree", "mergeTwoThree");
                        a.connection("multiplyByFive", "mergeTwoThreeFive");
                        a.connection("mergeTwoThree", "mergeTwoThreeFive");
                        a.connection("mergeTwoThreeFive", "filter");

                        return a;
                    }
                }), "workflow");

        final FiniteDuration timeoutDuration = Duration.create(
                Properties.TIMEOUT_SECONDS, TimeUnit.SECONDS);
        final Timeout timeout = new Timeout(timeoutDuration);
        Future<Object> future = ask(workflow, new Initialize(), timeout);
        future.ready(timeoutDuration, null);

        oneShot.tell(new Integer(1), system.lookupRoot());

        system.awaitTermination();
    }
}
