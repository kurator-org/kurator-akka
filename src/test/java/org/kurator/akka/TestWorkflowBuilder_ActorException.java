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
             .param("separator", ", ")
             .listensTo(testActor);
        
         wr.inputActor(repeater);
     }
     
     public void testWorkflowBuilder_NoActorException() throws Exception {

         wr.begin();
         wr.tellWorkflow(1, 2, 3, 4, 5, new EndOfStream());
         wr.end();

         assertEquals("1, 2, 3, 4, 5", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
     
     public void testWorkflowBuilder_ActorException() throws Exception {
         
         wr.begin();
         wr.tellWorkflow(1, 2, 3, TestActor.exceptionTriggerValue, 4, 5, new EndOfStream());
         
         Exception exception = null;
         try {
             wr.end();
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
         public void onData(Object message) throws Exception {

             if (message instanceof Integer) {
                 if (((Integer)message) == exceptionTriggerValue) {
                     throw new Exception("Exception trigger value was sent to actor");
                 }
                 broadcast(message);
             }
         }
     }
}
