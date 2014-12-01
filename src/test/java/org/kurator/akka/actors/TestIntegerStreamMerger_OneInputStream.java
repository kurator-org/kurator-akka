package org.kurator.akka.actors;

import java.util.concurrent.TimeoutException;

import org.kurator.akka.ActorBuilder;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.WorkflowBuilder;
import org.kurator.akka.messages.EndOfStream;

public class TestIntegerStreamMerger_OneInputStream extends KuratorAkkaTestCase {

    private WorkflowBuilder wfb;

     @Override
     public void setUp() {

         super.setUp();
         
         wfb = new WorkflowBuilder()
             .outputStream(stdoutStream)
             .errorStream(stderrStream);
    
         ActorBuilder repeater = wfb.createActorBuilder()
                 .actorClass(Repeater.class);
    
         ActorBuilder merge = wfb.createActorBuilder()
                 .actorClass(IntegerStreamMerger.class)
                 .parameter("streamCount", 1)
                 .listensTo(repeater);
        
         @SuppressWarnings("unused")
         ActorBuilder printer = wfb.createActorBuilder()
                 .actorClass(PrintStreamWriter.class)
                 .parameter("separator", ", ")
                 .listensTo(merge);
        
         wfb.inputActor(repeater);
         
         wfb.build();
     }
     
    public void testIntegerStreamMerger_NoValues() throws TimeoutException, InterruptedException {
        wfb.startWorkflow();
        wfb.tellWorkflow(new EndOfStream());
        wfb.awaitWorkflow();
        assertEquals("", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }
     
     public void testIntegerStreamMerger_DistinctValues() throws TimeoutException, InterruptedException {
         wfb.startWorkflow();
         wfb.tellWorkflow(1);
         wfb.tellWorkflow(2);
         wfb.tellWorkflow(3);
         wfb.tellWorkflow(4);
         wfb.tellWorkflow(new EndOfStream());
         wfb.awaitWorkflow();
         assertEquals("1, 2, 3, 4", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
    
     public void testIntegerStreamMerger_IdenticalValues() throws TimeoutException, InterruptedException {
         wfb.startWorkflow();
         wfb.tellWorkflow(7);
         wfb.tellWorkflow(7);
         wfb.tellWorkflow(7);
         wfb.tellWorkflow(7);
         wfb.tellWorkflow(new EndOfStream());
         wfb.awaitWorkflow();
         assertEquals("7", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
    
     public void testIntegerStreamMerger_ValuesWithDuplicates() throws TimeoutException, InterruptedException {
         wfb.startWorkflow();
         wfb.tellWorkflow(1);
         wfb.tellWorkflow(2);
         wfb.tellWorkflow(2);
         wfb.tellWorkflow(3);
         wfb.tellWorkflow(3);
         wfb.tellWorkflow(4);
         wfb.tellWorkflow(5);
         wfb.tellWorkflow(5);
         wfb.tellWorkflow(new EndOfStream());
         wfb.awaitWorkflow();
         assertEquals("1, 2, 3, 4, 5", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
}
