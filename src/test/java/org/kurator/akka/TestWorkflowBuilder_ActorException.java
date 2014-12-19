package org.kurator.akka;

import org.kurator.akka.ActorBuilder;
import org.kurator.akka.WorkflowBuilder;
import org.kurator.akka.actors.Transformer;
import org.kurator.akka.actors.PrintStreamWriter;
import org.kurator.akka.actors.Repeater;
import org.kurator.akka.messages.EndOfStream;

public class TestWorkflowBuilder_ActorException extends KuratorAkkaTestCase {

    private WorkflowBuilder wfb;

     @Override
     public void setUp() throws Exception {
    
         super.setUp();
         
         wfb = new WorkflowBuilder()
             .outputStream(stdoutStream)
             .errorStream(stderrStream);

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
     
     public void testWorkflowBuilder_NoActorException() throws Exception {
         wfb.startWorkflow();
         wfb.tellWorkflow(1);
         wfb.tellWorkflow(2);
         wfb.tellWorkflow(3);
         wfb.tellWorkflow(4);
         wfb.tellWorkflow(5);
         wfb.tellWorkflow(new EndOfStream());
         wfb.awaitWorkflow();

         assertEquals("1, 2, 3, 4, 5", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
     
     public void testWorkflowBuilder_ActorException() throws Exception {
         wfb.startWorkflow();
         wfb.tellWorkflow(1);
         wfb.tellWorkflow(2);
         wfb.tellWorkflow(3);
         wfb.tellWorkflow(TestActor.exceptionTriggerValue);
         wfb.tellWorkflow(4);
         wfb.tellWorkflow(5);
         wfb.tellWorkflow(new EndOfStream());
         
         Exception exception = null;
         try {
             wfb.awaitWorkflow();
         } catch(Exception e) {
             exception = e;
         }
         assertNotNull(exception);
         assertTrue(exception.getMessage().contains("Exception trigger value was sent to actor"));
         assertTrue(stderrBuffer.toString().contains("Exception trigger value was sent to actor"));
         assertEquals("1, 2, 3", stdoutBuffer.toString());
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
