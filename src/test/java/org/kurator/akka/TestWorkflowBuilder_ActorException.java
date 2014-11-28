package org.kurator.akka;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeoutException;

import org.kurator.akka.ActorBuilder;
import org.kurator.akka.WorkflowBuilder;
import org.kurator.akka.actors.Transformer;
import org.kurator.akka.actors.PrintStreamWriter;
import org.kurator.akka.actors.Repeater;
import org.kurator.akka.messages.EndOfStream;

import junit.framework.TestCase;

public class TestWorkflowBuilder_ActorException extends TestCase {

    private WorkflowBuilder wfb;
    private OutputStream outputBuffer;

     @Override
     public void setUp() {
    
         outputBuffer = new ByteArrayOutputStream();
         PrintStream printStream = new PrintStream(outputBuffer);
        
         wfb = new WorkflowBuilder()
             .outputStream(printStream);
    
         ActorBuilder repeater = wfb.createActorBuilder()
                 .actorClass(Repeater.class);
    
         ActorBuilder testActor = wfb.createActorBuilder()
                 .actorClass(TestActor.class)
                 .listensTo(repeater);
        
         @SuppressWarnings("unused")
         ActorBuilder printer = wfb.createActorBuilder()
                 .actorClass(PrintStreamWriter.class)
                 .parameter("separator", ", ")
                 .listensTo(testActor);
        
         wfb.inputActor(repeater);
         
         wfb.build();
     }
     
     public void testWorkflowBuilder_NoActorException_DistinctValues() throws TimeoutException, InterruptedException {
         wfb.startWorkflow();
         wfb.tellWorkflow(1);
         wfb.tellWorkflow(2);
         wfb.tellWorkflow(3);
         wfb.tellWorkflow(4);
         wfb.tellWorkflow(5);
         wfb.tellWorkflow(new EndOfStream());
         wfb.awaitWorkflow();
         assertEquals("1, 2, 3, 4, 5", outputBuffer.toString());
     }
     
     public void testWorkflowBuilder_ActorException_DistinctValues() throws TimeoutException, InterruptedException {
         wfb.startWorkflow();
         wfb.tellWorkflow(1);
         wfb.tellWorkflow(2);
         wfb.tellWorkflow(3);
         wfb.tellWorkflow(TestActor.exceptionTriggerValue);
         wfb.tellWorkflow(4);
         wfb.tellWorkflow(5);
         wfb.tellWorkflow(new EndOfStream());
         wfb.awaitWorkflow();
         assertEquals("1, 2, 3", outputBuffer.toString());
     }
     
     public static class TestActor extends Transformer {

         static public final Integer exceptionTriggerValue = Integer.MIN_VALUE;
         
         @Override
         public void handleDataMessage(Object message) throws Exception {

             if (message instanceof Integer) {
                 if (((Integer)message) == exceptionTriggerValue) {
                     throw new Exception("Exception trigger value was sent to actor");
                 }
                 broadcast(message);
             }
         }
     }
}
