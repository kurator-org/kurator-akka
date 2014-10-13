package org.kurator.akka;

import static akka.pattern.Patterns.ask;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.TestCase;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.util.Timeout;

public class TestIntegerStreamMerger_OneInputStream extends TestCase {

    private OutputStream outputStream;
    private PrintStream printStream;
    private ActorSystem actorSystem;

    ActorRef repeater;
    ActorRef merge;
    ActorRef printer;

    @SuppressWarnings("serial")
    @Override
    public void setUp() {

        outputStream = new ByteArrayOutputStream();
        printStream = new PrintStream(outputStream);
        actorSystem = ActorSystem.create("TestActorSystem");

        repeater = actorSystem.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                BroadcastActor a = new Repeater();
                a.addListener("merge");
                return a;
            }
        }), "repeater");

        merge = actorSystem.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                BroadcastActor a = new IntegerStreamMerger(1);
                a.addListener("printer");
                return a;
            }
        }), "merge");

        printer = actorSystem.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new PrintStreamWriter(printStream, ", ");
            }
        }), "printer");

        final ActorRef director = actorSystem.actorOf(new Props(
                new UntypedActorFactory() {
                    public UntypedActor create() {
                        WorkflowDirector a = new WorkflowDirector(actorSystem);
                        a.monitor("repeater");
                        a.monitor("merge");
                        a.monitor("printer");
                        return a;
                    }
                }), "monitor");

        final FiniteDuration timeoutDuration = Duration.create(500,
                TimeUnit.SECONDS);
        final Timeout timeout = new Timeout(timeoutDuration);
        Future<Object> future = ask(director, new Initialize(), timeout);
        try {
            future.ready(timeoutDuration, null);
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void testIntegerStreamMerger_NoValues() {
        repeater.tell(new EndOfStream(), actorSystem.lookupRoot());
        actorSystem.awaitTermination();
        assertEquals("", outputStream.toString());
    }

    public void testIntegerStreamMerger_DistinctValues() {
        repeater.tell(new Integer(1), actorSystem.lookupRoot());
        repeater.tell(new Integer(2), actorSystem.lookupRoot());
        repeater.tell(new Integer(3), actorSystem.lookupRoot());
        repeater.tell(new Integer(4), actorSystem.lookupRoot());
        repeater.tell(new EndOfStream(), actorSystem.lookupRoot());
        actorSystem.awaitTermination();
        assertEquals("1, 2, 3, 4", outputStream.toString());
    }

    public void testIntegerStreamMerger_IdenticalValues() {
        repeater.tell(new Integer(7), actorSystem.lookupRoot());
        repeater.tell(new Integer(7), actorSystem.lookupRoot());
        repeater.tell(new Integer(7), actorSystem.lookupRoot());
        repeater.tell(new Integer(7), actorSystem.lookupRoot());
        repeater.tell(new EndOfStream(), actorSystem.lookupRoot());
        actorSystem.awaitTermination();
        assertEquals("7", outputStream.toString());
    }

    public void testIntegerStreamMerger_ValuesWithDuplicates() {
        repeater.tell(new Integer(1), actorSystem.lookupRoot());
        repeater.tell(new Integer(2), actorSystem.lookupRoot());
        repeater.tell(new Integer(2), actorSystem.lookupRoot());
        repeater.tell(new Integer(3), actorSystem.lookupRoot());
        repeater.tell(new Integer(3), actorSystem.lookupRoot());
        repeater.tell(new Integer(4), actorSystem.lookupRoot());
        repeater.tell(new Integer(5), actorSystem.lookupRoot());
        repeater.tell(new Integer(5), actorSystem.lookupRoot());
        repeater.tell(new EndOfStream(), actorSystem.lookupRoot());
        actorSystem.awaitTermination();
        assertEquals("1, 2, 3, 4, 5", outputStream.toString());
    }

}
