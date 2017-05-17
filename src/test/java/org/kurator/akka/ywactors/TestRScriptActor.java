package org.kurator.akka.ywactors;

import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.ywactors.RScriptActor;

public class TestRScriptActor extends KuratorAkkaTestCase {

    private WorkflowRunner wr;
    
    @Override
    public void setUp() throws Exception {
         super.setUp();
         wr = new WorkflowRunner()
        		  .outputStream(stdoutStream)
        		  .errorStream(stderrStream);

         System.setProperty("yw.actors.r.command", "/usr/local/bin/R");
    }

    public void testRScriptActor_PrintHelloWorld() throws Exception {

        wr.actor(RScriptActor.class)
          .config("onStart", "cat('Hello R-Actor!')");

        wr.run();

        assertEquals("", stderrBuffer.toString());
        assertEquals("Hello R-Actor!", stdoutBuffer.toString());
    }
}
