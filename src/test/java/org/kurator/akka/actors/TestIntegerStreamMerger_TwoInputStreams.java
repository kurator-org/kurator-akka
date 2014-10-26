package org.kurator.akka.actors;

import java.io.OutputStream;
import java.io.PrintStream;

import junit.framework.TestCase;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class TestIntegerStreamMerger_TwoInputStreams extends TestCase {

    private OutputStream outputStream;
    private PrintStream printStream;
    private ActorSystem actorSystem;

    ActorRef repeaterA;
    ActorRef repeaterB;
    ActorRef merge;
    ActorRef printer;
    //
    // @SuppressWarnings("serial")
    // @Override
    // public void setUp() {
    //
    // outputStream = new ByteArrayOutputStream();
    // printStream = new PrintStream(outputStream);
    // actorSystem = ActorSystem.create("TestActorSystem");
    //
    // repeaterA = actorSystem.actorOf(new Props(new UntypedActorFactory() {
    // public UntypedActor create() {
    // BroadcastActor a = new Repeater();
    // return a;
    // }
    // }), "repeaterA");
    //
    // repeaterB = actorSystem.actorOf(new Props(new UntypedActorFactory() {
    // public UntypedActor create() {
    // return new Repeater();
    // }
    // }), "repeaterB");
    //
    // merge = actorSystem.actorOf(new Props(new UntypedActorFactory() {
    // public UntypedActor create() {
    // return new IntegerStreamMerger(2);
    // }
    // }), "merge");
    //
    // printer = actorSystem.actorOf(new Props(new UntypedActorFactory() {
    // public UntypedActor create() {
    // return new PrintStreamWriter(printStream, ", ");
    // }
    // }), "printer");
    //
    // final ActorRef director = actorSystem.actorOf(new Props(
    // new UntypedActorFactory() {
    // public UntypedActor create() {
    // Workflow a = new Workflow(actorSystem);
    // a.actor(repeaterA);
    // a.actor(repeaterB);
    // a.actor(merge);
    // a.actor(printer);
    // a.connection(repeaterA, merge);
    // a.connection(repeaterB, merge);
    // a.connection(merge, printer);
    // return a;
    // }
    // }), "monitor");
    //
    // final FiniteDuration timeoutDuration = Duration.create(500,
    // TimeUnit.SECONDS);
    // final Timeout timeout = new Timeout(timeoutDuration);
    // Future<Object> future = ask(director, new Initialize(), timeout);
    // try {
    // future.ready(timeoutDuration, null);
    // } catch (TimeoutException e) {
    // e.printStackTrace();
    // } catch (InterruptedException e) {
    // e.printStackTrace();
    // }
    // }
    //
     public void testIntegerStreamMerger_TwoStreams_NoValues() {
//     repeaterA.tell(new EndOfStream(), actorSystem.lookupRoot());
//     repeaterB.tell(new EndOfStream(), actorSystem.lookupRoot());
//     actorSystem.awaitTermination();
//     assertEquals("", outputStream.toString());
     }
    
//     public void testIntegerStreamMerger_TwoStreams_OneEmpty_DistinctValues()
//     {
//     repeaterA.tell(new Integer(1), actorSystem.lookupRoot());
//     repeaterA.tell(new Integer(2), actorSystem.lookupRoot());
//     repeaterA.tell(new Integer(3), actorSystem.lookupRoot());
//     repeaterA.tell(new Integer(4), actorSystem.lookupRoot());
//     repeaterA.tell(new EndOfStream(), actorSystem.lookupRoot());
//     repeaterB.tell(new EndOfStream(), actorSystem.lookupRoot());
//     actorSystem.awaitTermination();
//     assertEquals("1, 2, 3, 4", outputStream.toString());
//     }
//    
//     public void testIntegerStreamMerger_TwoStreams_OneEmpty_IdenticalValues()
//     {
//     repeaterA.tell(new Integer(7), actorSystem.lookupRoot());
//     repeaterA.tell(new Integer(7), actorSystem.lookupRoot());
//     repeaterA.tell(new Integer(7), actorSystem.lookupRoot());
//     repeaterA.tell(new Integer(7), actorSystem.lookupRoot());
//     repeaterA.tell(new EndOfStream(), actorSystem.lookupRoot());
//     repeaterB.tell(new EndOfStream(), actorSystem.lookupRoot());
//     actorSystem.awaitTermination();
//     assertEquals("7", outputStream.toString());
//     }
//    
//     public void
//     testIntegerStreamMerger_TwoStreams_OneEmpty_ValuesWithDuplicates() {
//     repeaterA.tell(new Integer(1), actorSystem.lookupRoot());
//     repeaterA.tell(new Integer(2), actorSystem.lookupRoot());
//     repeaterA.tell(new Integer(2), actorSystem.lookupRoot());
//     repeaterA.tell(new Integer(3), actorSystem.lookupRoot());
//     repeaterA.tell(new Integer(3), actorSystem.lookupRoot());
//     repeaterA.tell(new Integer(4), actorSystem.lookupRoot());
//     repeaterA.tell(new Integer(5), actorSystem.lookupRoot());
//     repeaterA.tell(new Integer(5), actorSystem.lookupRoot());
//     repeaterA.tell(new EndOfStream(), actorSystem.lookupRoot());
//     repeaterB.tell(new EndOfStream(), actorSystem.lookupRoot());
//     actorSystem.awaitTermination();
//     assertEquals("1, 2, 3, 4, 5", outputStream.toString());
//     }
//    
//     public void
//     testIntegerStreamMerger_TwoStreams_DistinctValues_RoundRobin() {
//     repeaterA.tell(new Integer(1), actorSystem.lookupRoot());
//     repeaterB.tell(new Integer(2), actorSystem.lookupRoot());
//     repeaterA.tell(new Integer(3), actorSystem.lookupRoot());
//     repeaterB.tell(new Integer(4), actorSystem.lookupRoot());
//     repeaterA.tell(new EndOfStream(), actorSystem.lookupRoot());
//     repeaterB.tell(new EndOfStream(), actorSystem.lookupRoot());
//     actorSystem.awaitTermination();
//     assertEquals("1, 2, 3, 4", outputStream.toString());
//     }
//    
//     public void
//     testIntegerStreamMerger_TwoStreams_DistinctValues_OneStreamFirst() {
//     repeaterA.tell(new Integer(1), actorSystem.lookupRoot());
//     repeaterA.tell(new Integer(3), actorSystem.lookupRoot());
//     repeaterA.tell(new Integer(5), actorSystem.lookupRoot());
//     repeaterB.tell(new Integer(2), actorSystem.lookupRoot());
//     repeaterB.tell(new Integer(4), actorSystem.lookupRoot());
//     repeaterB.tell(new Integer(6), actorSystem.lookupRoot());
//     repeaterA.tell(new EndOfStream(), actorSystem.lookupRoot());
//     repeaterB.tell(new EndOfStream(), actorSystem.lookupRoot());
//     actorSystem.awaitTermination();
//     assertEquals("1, 2, 3, 4, 5, 6", outputStream.toString());
//     }
//    
//     public void
//     testIntegerStreamMerger_TwoStreams_IdenticalValues_RoundRobin() {
//     repeaterA.tell(new Integer(7), actorSystem.lookupRoot());
//     repeaterB.tell(new Integer(7), actorSystem.lookupRoot());
//     repeaterA.tell(new Integer(7), actorSystem.lookupRoot());
//     repeaterB.tell(new Integer(7), actorSystem.lookupRoot());
//     repeaterA.tell(new EndOfStream(), actorSystem.lookupRoot());
//     repeaterB.tell(new EndOfStream(), actorSystem.lookupRoot());
//     actorSystem.awaitTermination();
//     assertEquals("7", outputStream.toString());
//     }
//    
//     public void
//     testIntegerStreamMerger_TwoStreams_IdenticalValues_OneStreamFirst() {
//     repeaterA.tell(new Integer(7), actorSystem.lookupRoot());
//     repeaterA.tell(new Integer(7), actorSystem.lookupRoot());
//     repeaterA.tell(new Integer(7), actorSystem.lookupRoot());
//     repeaterB.tell(new Integer(7), actorSystem.lookupRoot());
//     repeaterB.tell(new Integer(7), actorSystem.lookupRoot());
//     repeaterB.tell(new Integer(7), actorSystem.lookupRoot());
//     repeaterA.tell(new EndOfStream(), actorSystem.lookupRoot());
//     repeaterB.tell(new EndOfStream(), actorSystem.lookupRoot());
//     actorSystem.awaitTermination();
//     assertEquals("7", outputStream.toString());
//     }
//    
//     public void
//     testIntegerStreamMerger_TwoStreams_ValuesWithDuplicates_RoundRobin() {
//     repeaterA.tell(new Integer(1), actorSystem.lookupRoot());
//     repeaterB.tell(new Integer(2), actorSystem.lookupRoot());
//     repeaterA.tell(new Integer(2), actorSystem.lookupRoot());
//     repeaterB.tell(new Integer(3), actorSystem.lookupRoot());
//     repeaterA.tell(new Integer(3), actorSystem.lookupRoot());
//     repeaterB.tell(new Integer(4), actorSystem.lookupRoot());
//     repeaterA.tell(new Integer(5), actorSystem.lookupRoot());
//     repeaterB.tell(new Integer(5), actorSystem.lookupRoot());
//     repeaterA.tell(new EndOfStream(), actorSystem.lookupRoot());
//     repeaterB.tell(new EndOfStream(), actorSystem.lookupRoot());
//     actorSystem.awaitTermination();
//     assertEquals("1, 2, 3, 4, 5", outputStream.toString());
//     }
//    
//     public void
//     testIntegerStreamMerger_TwoStreams_ValuesWithDuplicates_OneStreamFirst()
//     {
//     repeaterA.tell(new Integer(1), actorSystem.lookupRoot());
//     repeaterA.tell(new Integer(2), actorSystem.lookupRoot());
//     repeaterA.tell(new Integer(2), actorSystem.lookupRoot());
//     repeaterA.tell(new Integer(3), actorSystem.lookupRoot());
//     repeaterB.tell(new Integer(3), actorSystem.lookupRoot());
//     repeaterB.tell(new Integer(4), actorSystem.lookupRoot());
//     repeaterB.tell(new Integer(5), actorSystem.lookupRoot());
//     repeaterB.tell(new Integer(5), actorSystem.lookupRoot());
//     repeaterA.tell(new EndOfStream(), actorSystem.lookupRoot());
//     repeaterB.tell(new EndOfStream(), actorSystem.lookupRoot());
//     actorSystem.awaitTermination();
//     assertEquals("1, 2, 3, 4, 5", outputStream.toString());
//     }

}
