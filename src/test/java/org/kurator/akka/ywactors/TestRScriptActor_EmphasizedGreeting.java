package org.kurator.akka.ywactors;

import org.kurator.akka.ActorConfig;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.ywactors.RScriptActor;

public class TestRScriptActor_EmphasizedGreeting extends KuratorAkkaTestCase {

    private WorkflowRunner wr;
    
    @Override
    public void setUp() throws Exception {
         super.setUp();
         wr = new WorkflowRunner()
              .outputStream(stdoutStream)
        	  .errorStream(stderrStream);
         System.setProperty("yw.actors.r.command", "/usr/local/bin/R");
    }
    
    public void testRScriptActor_SingleGreeting() throws Exception {
        ActorConfig greetingSource = 
            wr.actor(RScriptActor.class)
              .config("onStart", "out <- 'Hello'");
        ActorConfig greetingPrinter = 
            wr.actor(RScriptActor.class)
              .config("onData", "cat(inp,'\n',sep='')")
              .listensTo(greetingSource);
        wr.run();
        assertEquals("", stderrBuffer.toString());
        assertEquals("Hello" + EOL, stdoutBuffer.toString());
    }    
}
