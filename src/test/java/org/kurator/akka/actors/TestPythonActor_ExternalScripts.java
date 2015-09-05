package org.kurator.akka.actors;

import org.junit.Test;
import org.kurator.akka.ActorConfig;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.PythonActor;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.actors.PrintStreamWriter;
import org.kurator.akka.actors.Repeater;
import org.kurator.akka.messages.EndOfStream;

public class TestPythonActor_ExternalScripts extends KuratorAkkaTestCase {

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
         
         ActorConfig repeater = wr.actor(Repeater.class);
         
         jythonActor = wr.actor(PythonActor.class)
                         .config("script", "src/test/resources/org/kurator/akka/python/multiplier.py")
                         .config("onData", "multiply")
                         .param("factor", 2)
                         .listensTo(repeater);
    
        wr.actor(PrintStreamWriter.class)
          .param("separator", ",")
          .listensTo(jythonActor);
        
        wr.inputActor(repeater);
     }
    
    @Test
    public void testPythonActor() throws Exception {
        
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
