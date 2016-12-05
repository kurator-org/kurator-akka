package org.kurator.akka.actors;

import org.kurator.akka.ActorConfig;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.KuratorScriptActor;
import org.kurator.akka.WorkflowRunner;

public class TestRActor extends KuratorAkkaTestCase {

    private WorkflowRunner wr;
    
    @Override
    public void setUp() throws Exception {
         super.setUp();
         wr = new WorkflowRunner()
        		  .outputStream(stdoutStream)
        		  .errorStream(stderrStream);
    }

    public void testRScriptActor_PrintHelloWorld() throws Exception {

        ActorConfig actor = wr.actor(KuratorScriptActor.class)
                              .config("onStart", "cat('Hello R-Actor!')");

        wr.begin()
          .end();

        assertEquals("", stderrBuffer.toString());
        assertEquals("Hello R-Actor!", stdoutBuffer.toString());
    }
}
