package org.kurator.akka.actors;

import org.junit.Test;
import org.kurator.akka.ActorConfig;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.actors.JythonFunctionActor;
import org.kurator.akka.actors.PrintStreamWriter;
import org.kurator.akka.actors.Repeater;
import org.kurator.akka.messages.EndOfStream;

public class TestJythonFunctionActor extends KuratorAkkaTestCase {

    private WorkflowRunner wr;
    private ActorConfig jythonActor;
    
    @SuppressWarnings("unused")
    private ActorConfig printerActor;
    
     @Override
     public void setUp() throws Exception {
         
         super.setUp();

         wr = new WorkflowRunner()
             .outputStream(stdoutStream)
             .errorStream(stderrStream);
         
         ActorConfig repeater = wr.configureNewActor()
              .actorClass(Repeater.class);
         
         jythonActor = wr.configureNewActor()
             .actorClass(JythonFunctionActor.class)
             .parameter("path", "src/test/resources/org/kurator/akka/jython/multiplier.py")
             .parameter("function", "multiplier")
             .listensTo(repeater);
    
        wr.configureNewActor()
             .actorClass(PrintStreamWriter.class)
             .parameter("separator", ",")
             .listensTo(jythonActor);
        
        wr.inputActor(repeater);
     }
    
    @Test
    public void testJythonActor() throws Exception {
        
        wr.build();
        wr.start();
        wr.tellWorkflow(1);
        wr.tellWorkflow(2);
        wr.tellWorkflow(3);
        wr.tellWorkflow(4);
        wr.tellWorkflow(5);
        wr.tellWorkflow(new EndOfStream());
        wr.await();
        
        assertEquals("2,4,6,8,10", stdoutBuffer.toString());
    }

}
