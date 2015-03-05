package org.kurator.akka;

import org.kurator.akka.ActorConfig;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.actors.PrintStreamWriter;
import org.kurator.akka.actors.Repeater;
import org.kurator.akka.messages.EndOfStream;

public class TestWorkflowBuilder_ActorException extends KuratorAkkaTestCase {

    private WorkflowRunner wr;

     @Override
     public void setUp() throws Exception {
    
         super.setUp();
         
         wr = new WorkflowRunner()
             .outputStream(stdoutStream)
             .errorStream(stderrStream);

         ActorConfig repeater = wr.actor(Repeater.class);
    
         ActorConfig testActor = wr.actor(TestActor.class)
             .listensTo(repeater);
        
         @SuppressWarnings("unused")
         ActorConfig printer = wr.actor(PrintStreamWriter.class)
             .parameter("separator", ", ")
             .listensTo(testActor);
        
         wr.inputActor(repeater);
         
         wr.build();
     }
     
     public void testWorkflowBuilder_NoActorException() throws Exception {
         wr.start();
         wr.tellWorkflow(1);
         wr.tellWorkflow(2);
         wr.tellWorkflow(3);
         wr.tellWorkflow(4);
         wr.tellWorkflow(5);
         wr.tellWorkflow(new EndOfStream());
         wr.await();

         assertEquals("1, 2, 3, 4, 5", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
     
     public void testWorkflowBuilder_ActorException() throws Exception {
         wr.start();
         wr.tellWorkflow(1);
         wr.tellWorkflow(2);
         wr.tellWorkflow(3);
         wr.tellWorkflow(TestActor.exceptionTriggerValue);
         wr.tellWorkflow(4);
         wr.tellWorkflow(5);
         wr.tellWorkflow(new EndOfStream());
         
         Exception exception = null;
         try {
             wr.await();
         } catch(Exception e) {
             exception = e;
         }
         assertNotNull(exception);
         assertTrue(exception.getMessage().contains("Exception trigger value was sent to actor"));
         assertTrue(stderrBuffer.toString().contains("Exception trigger value was sent to actor"));
         assertEquals("1, 2, 3", stdoutBuffer.toString());
     }
     
     public static class TestActor extends AkkaActor {

         static public final Integer exceptionTriggerValue = Integer.MIN_VALUE;
         
         @Override
         public void handleData(Object message) throws Exception {

             if (message instanceof Integer) {
                 if (((Integer)message) == exceptionTriggerValue) {
                     throw new Exception("Exception trigger value was sent to actor");
                 }
                 broadcast(message);
             }
         }
     }
}
