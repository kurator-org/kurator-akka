package org.kurator.akka.actors;

import org.kurator.akka.ActorConfig;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.messages.EndOfStream;

import akka.actor.ActorRef;

public class TestIntegerStreamMerger_TwoInputStreams extends KuratorAkkaTestCase {

    private WorkflowRunner wr;
    private ActorRef repeaterA;
    private ActorRef repeaterB;

     @Override
     public void setUp() throws Exception {

         super.setUp();
         
         wr = new WorkflowRunner()
             .outputStream(stdoutStream)
             .errorStream(stderrStream);
    
         ActorConfig repeaterABuilder = wr.actor(Repeater.class);
    
         ActorConfig repeaterBBuilder = wr.actor(Repeater.class);

         ActorConfig merge = wr.actor(IntegerStreamMerger.class)
                 .param("streamCount", 2)
                 .listensTo(repeaterABuilder)
                 .listensTo(repeaterBBuilder);
        
         @SuppressWarnings("unused")
         ActorConfig printer = wr.actor(PrintStreamWriter.class)
                 .param("separator", ", ")
                 .listensTo(merge);
        
         wr.build();
         
         repeaterA = wr.getActorForConfig(repeaterABuilder);
         repeaterB = wr.getActorForConfig(repeaterBBuilder);
         
         wr.init();
         wr.start();
     }

     public void testIntegerStreamMerger_TwoStreams_NoValues() throws Exception {
         repeaterA.tell(new EndOfStream(), wr.root());
         repeaterB.tell(new EndOfStream(), wr.root());
         wr.end();
         assertEquals("", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
    
     public void testIntegerStreamMerger_TwoStreams_OneEmpty_DistinctValues() throws Exception
     {
         repeaterA.tell(new Integer(1), wr.root());
         repeaterA.tell(new Integer(2), wr.root());
         repeaterA.tell(new Integer(3), wr.root());
         repeaterA.tell(new Integer(4), wr.root());
         repeaterA.tell(new EndOfStream(), wr.root());
         repeaterB.tell(new EndOfStream(), wr.root());
         wr.end();
         assertEquals("1, 2, 3, 4", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
    
     public void testIntegerStreamMerger_TwoStreams_OneEmpty_IdenticalValues() throws Exception
     {
         repeaterA.tell(7, wr.root());
         repeaterA.tell(7, wr.root());
         repeaterA.tell(7, wr.root());
         repeaterA.tell(7, wr.root());
         repeaterA.tell(new EndOfStream(), wr.root());
         repeaterB.tell(new EndOfStream(), wr.root());
         wr.end();
         assertEquals("7", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
    
     public void testIntegerStreamMerger_TwoStreams_OneEmpty_ValuesWithDuplicates() throws Exception {
         repeaterA.tell(new Integer(1), wr.root());
         repeaterA.tell(new Integer(2), wr.root());
         repeaterA.tell(new Integer(2), wr.root());
         repeaterA.tell(new Integer(3), wr.root());
         repeaterA.tell(new Integer(3), wr.root());
         repeaterA.tell(new Integer(4), wr.root());
         repeaterA.tell(new Integer(5), wr.root());
         repeaterA.tell(new Integer(5), wr.root());
         repeaterA.tell(new EndOfStream(), wr.root());
         repeaterB.tell(new EndOfStream(), wr.root());
         wr.end();
         assertEquals("1, 2, 3, 4, 5", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
    
     public void testIntegerStreamMerger_TwoStreams_DistinctValues_RoundRobin() throws Exception {
         repeaterA.tell(new Integer(1), wr.root());
         repeaterB.tell(new Integer(2), wr.root());
         repeaterA.tell(new Integer(3), wr.root());
         repeaterB.tell(new Integer(4), wr.root());
         repeaterA.tell(new EndOfStream(), wr.root());
         repeaterB.tell(new EndOfStream(), wr.root());
         wr.end();
         assertEquals("1, 2, 3, 4", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
    
     public void testIntegerStreamMerger_TwoStreams_DistinctValues_OneStreamFirst() throws Exception {
         repeaterA.tell(new Integer(1), wr.root());
         repeaterA.tell(new Integer(3), wr.root());
         repeaterA.tell(new Integer(5), wr.root());
         repeaterB.tell(new Integer(2), wr.root());
         repeaterB.tell(new Integer(4), wr.root());
         repeaterB.tell(new Integer(6), wr.root());
         repeaterA.tell(new EndOfStream(),  wr.root());
         repeaterB.tell(new EndOfStream(),  wr.root());
         wr.end();
         assertEquals("1, 2, 3, 4, 5, 6", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
    
     public void testIntegerStreamMerger_TwoStreams_IdenticalValues_RoundRobin() throws Exception {
         repeaterA.tell(new Integer(7), wr.root());
         repeaterB.tell(new Integer(7), wr.root());
         repeaterA.tell(new Integer(7), wr.root());
         repeaterB.tell(new Integer(7), wr.root());
         repeaterA.tell(new EndOfStream(), wr.root());
         repeaterB.tell(new EndOfStream(), wr.root());
         wr.end();
         assertEquals("7", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
    
     public void testIntegerStreamMerger_TwoStreams_IdenticalValues_OneStreamFirst() throws Exception {
         repeaterA.tell(new Integer(7), wr.root());
         repeaterA.tell(new Integer(7), wr.root());
         repeaterA.tell(new Integer(7), wr.root());
         repeaterB.tell(new Integer(7), wr.root());
         repeaterB.tell(new Integer(7), wr.root());
         repeaterB.tell(new Integer(7), wr.root());
         repeaterA.tell(new EndOfStream(), wr.root());
         repeaterB.tell(new EndOfStream(), wr.root());
         wr.end();
         assertEquals("7", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
    
     public void testIntegerStreamMerger_TwoStreams_ValuesWithDuplicates_RoundRobin() throws Exception {
         repeaterA.tell(new Integer(1), wr.root());
         repeaterB.tell(new Integer(2), wr.root());
         repeaterA.tell(new Integer(2), wr.root());
         repeaterB.tell(new Integer(3), wr.root());
         repeaterA.tell(new Integer(3), wr.root());
         repeaterB.tell(new Integer(4), wr.root());
         repeaterA.tell(new Integer(5), wr.root());
         repeaterB.tell(new Integer(5), wr.root());
         repeaterA.tell(new EndOfStream(), wr.root());
         repeaterB.tell(new EndOfStream(), wr.root());
         wr.end();
         assertEquals("1, 2, 3, 4, 5", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
    
     public void testIntegerStreamMerger_TwoStreams_ValuesWithDuplicates_OneStreamFirst() throws Exception {
         repeaterA.tell(new Integer(1), wr.root());
         repeaterA.tell(new Integer(2), wr.root());
         repeaterA.tell(new Integer(2), wr.root());
         repeaterA.tell(new Integer(3), wr.root());
         repeaterB.tell(new Integer(3), wr.root());
         repeaterB.tell(new Integer(4), wr.root());
         repeaterB.tell(new Integer(5), wr.root());
         repeaterB.tell(new Integer(5), wr.root());
         repeaterA.tell(new EndOfStream(), wr.root());
         repeaterB.tell(new EndOfStream(), wr.root());
         wr.end();
         assertEquals("1, 2, 3, 4, 5", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
}
