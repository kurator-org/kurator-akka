package org.kurator.akka.actors;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeoutException;

import org.kurator.akka.ActorBuilder;
import org.kurator.akka.WorkflowBuilder;
import org.kurator.akka.messages.EndOfStream;

import junit.framework.TestCase;

public class TestIntegerStreamMerger_OneInputStream extends TestCase {

    private WorkflowBuilder wfb;
    private OutputStream outputBuffer;

     @Override
     public void setUp() {
    
         outputBuffer = new ByteArrayOutputStream();
         PrintStream printStream = new PrintStream(outputBuffer);
        
         wfb = new WorkflowBuilder();
    
         ActorBuilder repeater = wfb.createActorBuilder()
                 .actorClass(Repeater.class);
    
         ActorBuilder merge = wfb.createActorBuilder()
                 .actorClass(IntegerStreamMerger.class)
                 .parameter("streamCount", 1)
                 .listensTo(repeater);
        
         @SuppressWarnings("unused")
         ActorBuilder printer = wfb.createActorBuilder()
                 .actorClass(PrintStreamWriter.class)
                 .parameter("stream", printStream)
                 .parameter("separator", ", ")
                 .listensTo(merge);
        
         wfb.inputActor(repeater);
         
         wfb.build();
     }
     
    public void testIntegerStreamMerger_NoValues() throws TimeoutException, InterruptedException {
        wfb.startWorkflow();
        wfb.tellWorkflow(new EndOfStream());
        wfb.awaitWorkflow();
        assertEquals("", outputBuffer.toString());
    }
     
     public void testIntegerStreamMerger_DistinctValues() throws TimeoutException, InterruptedException {
         wfb.startWorkflow();
         wfb.tellWorkflow(1);
         wfb.tellWorkflow(2);
         wfb.tellWorkflow(3);
         wfb.tellWorkflow(4);
         wfb.tellWorkflow(new EndOfStream());
         wfb.awaitWorkflow();
         assertEquals("1, 2, 3, 4", outputBuffer.toString());
     }
    
     public void testIntegerStreamMerger_IdenticalValues() throws TimeoutException, InterruptedException {
         wfb.startWorkflow();
         wfb.tellWorkflow(7);
         wfb.tellWorkflow(7);
         wfb.tellWorkflow(7);
         wfb.tellWorkflow(7);
         wfb.tellWorkflow(new EndOfStream());
         wfb.awaitWorkflow();
         assertEquals("7", outputBuffer.toString());
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
         assertEquals("1, 2, 3, 4, 5", outputBuffer.toString());
     }

}
