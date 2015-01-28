package org.kurator.akka.actors;

import org.kurator.akka.ActorBuilder;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.messages.EndOfStream;

import akka.actor.ActorRef;

public class TestIntegerStreamMerger_TwoInputStreams extends KuratorAkkaTestCase {

    private WorkflowRunner wfb;
    private ActorRef repeaterA;
    private ActorRef repeaterB;

     @Override
     public void setUp() throws Exception {

         super.setUp();
         
         wfb = new WorkflowRunner()
             .outputStream(stdoutStream)
             .errorStream(stderrStream);
    
         ActorBuilder repeaterABuilder = wfb.createActorBuilder()
                 .actorClass(Repeater.class);
    
         ActorBuilder repeaterBBuilder = wfb.createActorBuilder()
                 .actorClass(Repeater.class);

         ActorBuilder merge = wfb.createActorBuilder()
                 .actorClass(IntegerStreamMerger.class)
                 .parameter("streamCount", 2)
                 .listensTo(repeaterABuilder)
                 .listensTo(repeaterBBuilder);
        
         @SuppressWarnings("unused")
         ActorBuilder printer = wfb.createActorBuilder()
                 .actorClass(PrintStreamWriter.class)
                 .parameter("separator", ", ")
                 .listensTo(merge);
        
         wfb.build();
         
         repeaterA = wfb.getActorForConfig(repeaterABuilder);
         repeaterB = wfb.getActorForConfig(repeaterBBuilder);
     }

     public void testIntegerStreamMerger_TwoStreams_NoValues() throws Exception {
         wfb.start();
         repeaterA.tell(new EndOfStream(), wfb.root());
         repeaterB.tell(new EndOfStream(), wfb.root());
         wfb.await();
         assertEquals("", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
    
     public void testIntegerStreamMerger_TwoStreams_OneEmpty_DistinctValues() throws Exception
     {
         wfb.start();
         repeaterA.tell(new Integer(1), wfb.root());
         repeaterA.tell(new Integer(2), wfb.root());
         repeaterA.tell(new Integer(3), wfb.root());
         repeaterA.tell(new Integer(4), wfb.root());
         repeaterA.tell(new EndOfStream(), wfb.root());
         repeaterB.tell(new EndOfStream(), wfb.root());
         wfb.await();
         assertEquals("1, 2, 3, 4", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
    
     public void testIntegerStreamMerger_TwoStreams_OneEmpty_IdenticalValues() throws Exception
     {
         wfb.start();
         repeaterA.tell(7, wfb.root());
         repeaterA.tell(7, wfb.root());
         repeaterA.tell(7, wfb.root());
         repeaterA.tell(7, wfb.root());
         repeaterA.tell(new EndOfStream(), wfb.root());
         repeaterB.tell(new EndOfStream(), wfb.root());
         wfb.await();
         assertEquals("7", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
    
     public void testIntegerStreamMerger_TwoStreams_OneEmpty_ValuesWithDuplicates() throws Exception {
         wfb.start();
         repeaterA.tell(new Integer(1), wfb.root());
         repeaterA.tell(new Integer(2), wfb.root());
         repeaterA.tell(new Integer(2), wfb.root());
         repeaterA.tell(new Integer(3), wfb.root());
         repeaterA.tell(new Integer(3), wfb.root());
         repeaterA.tell(new Integer(4), wfb.root());
         repeaterA.tell(new Integer(5), wfb.root());
         repeaterA.tell(new Integer(5), wfb.root());
         repeaterA.tell(new EndOfStream(), wfb.root());
         repeaterB.tell(new EndOfStream(), wfb.root());
         wfb.await();
         assertEquals("1, 2, 3, 4, 5", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
    
     public void testIntegerStreamMerger_TwoStreams_DistinctValues_RoundRobin() throws Exception {
         wfb.start();
         repeaterA.tell(new Integer(1), wfb.root());
         repeaterB.tell(new Integer(2), wfb.root());
         repeaterA.tell(new Integer(3), wfb.root());
         repeaterB.tell(new Integer(4), wfb.root());
         repeaterA.tell(new EndOfStream(), wfb.root());
         repeaterB.tell(new EndOfStream(), wfb.root());
         wfb.await();
         assertEquals("1, 2, 3, 4", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
    
     public void testIntegerStreamMerger_TwoStreams_DistinctValues_OneStreamFirst() throws Exception {
         wfb.start();
         repeaterA.tell(new Integer(1), wfb.root());
         repeaterA.tell(new Integer(3), wfb.root());
         repeaterA.tell(new Integer(5), wfb.root());
         repeaterB.tell(new Integer(2), wfb.root());
         repeaterB.tell(new Integer(4), wfb.root());
         repeaterB.tell(new Integer(6), wfb.root());
         repeaterA.tell(new EndOfStream(),  wfb.root());
         repeaterB.tell(new EndOfStream(),  wfb.root());
         wfb.await();
         assertEquals("1, 2, 3, 4, 5, 6", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
    
     public void testIntegerStreamMerger_TwoStreams_IdenticalValues_RoundRobin() throws Exception {
         wfb.start();
         repeaterA.tell(new Integer(7), wfb.root());
         repeaterB.tell(new Integer(7), wfb.root());
         repeaterA.tell(new Integer(7), wfb.root());
         repeaterB.tell(new Integer(7), wfb.root());
         repeaterA.tell(new EndOfStream(), wfb.root());
         repeaterB.tell(new EndOfStream(), wfb.root());
         wfb.await();
         assertEquals("7", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
    
     public void testIntegerStreamMerger_TwoStreams_IdenticalValues_OneStreamFirst() throws Exception {
         wfb.start();
         repeaterA.tell(new Integer(7), wfb.root());
         repeaterA.tell(new Integer(7), wfb.root());
         repeaterA.tell(new Integer(7), wfb.root());
         repeaterB.tell(new Integer(7), wfb.root());
         repeaterB.tell(new Integer(7), wfb.root());
         repeaterB.tell(new Integer(7), wfb.root());
         repeaterA.tell(new EndOfStream(), wfb.root());
         repeaterB.tell(new EndOfStream(), wfb.root());
         wfb.await();
         assertEquals("7", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
    
     public void testIntegerStreamMerger_TwoStreams_ValuesWithDuplicates_RoundRobin() throws Exception {
         wfb.start();
         repeaterA.tell(new Integer(1), wfb.root());
         repeaterB.tell(new Integer(2), wfb.root());
         repeaterA.tell(new Integer(2), wfb.root());
         repeaterB.tell(new Integer(3), wfb.root());
         repeaterA.tell(new Integer(3), wfb.root());
         repeaterB.tell(new Integer(4), wfb.root());
         repeaterA.tell(new Integer(5), wfb.root());
         repeaterB.tell(new Integer(5), wfb.root());
         repeaterA.tell(new EndOfStream(), wfb.root());
         repeaterB.tell(new EndOfStream(), wfb.root());
         wfb.await();
         assertEquals("1, 2, 3, 4, 5", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
    
     public void testIntegerStreamMerger_TwoStreams_ValuesWithDuplicates_OneStreamFirst() throws Exception {
         wfb.start();
         repeaterA.tell(new Integer(1), wfb.root());
         repeaterA.tell(new Integer(2), wfb.root());
         repeaterA.tell(new Integer(2), wfb.root());
         repeaterA.tell(new Integer(3), wfb.root());
         repeaterB.tell(new Integer(3), wfb.root());
         repeaterB.tell(new Integer(4), wfb.root());
         repeaterB.tell(new Integer(5), wfb.root());
         repeaterB.tell(new Integer(5), wfb.root());
         repeaterA.tell(new EndOfStream(), wfb.root());
         repeaterB.tell(new EndOfStream(), wfb.root());
         wfb.await();
         assertEquals("1, 2, 3, 4, 5", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }

}
