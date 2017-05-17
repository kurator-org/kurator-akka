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

    public void testRScriptActor_PrintHelloWorld_OnInit() throws Exception {

        wr.actor(RScriptActor.class)
          .config("onInit", "cat('Initializing: Hello R-Actor!\n')");

        wr.run();

        assertEquals("", stderrBuffer.toString());
        assertEquals("Initializing: Hello R-Actor!" + EOL, stdoutBuffer.toString());
    }

    public void testRScriptActor_PrintHelloWorld_OnStart() throws Exception {

        wr.actor(RScriptActor.class)
          .config("onStart", "cat('Starting: Hello R-Actor!\n')");

        wr.run();

        assertEquals("", stderrBuffer.toString());
        assertEquals("Starting: Hello R-Actor!" + EOL, stdoutBuffer.toString());
    }
    
    public void testRScriptActor_PrintHelloWorld_OnEnd() throws Exception {

        wr.actor(RScriptActor.class)
          .config("onEnd", "cat('Ending: Hello R-Actor!\n')");

        wr.run();

        assertEquals("", stderrBuffer.toString());
        assertEquals("Ending: Hello R-Actor!" + EOL, stdoutBuffer.toString());
    }
    
    public void testRScriptActor_PrintHelloWorld_OnInitStartEnd() throws Exception {

        wr.actor(RScriptActor.class)
          .config("onInit",  "cat('Initializing: Hello R-Actor!\n')")
          .config("onStart", "cat('Starting: Hello R-Actor!\n')")
          .config("onEnd",   "cat('Ending: Hello R-Actor!\n')");

        wr.run();

        assertEquals("", stderrBuffer.toString());
        assertEquals("Initializing: Hello R-Actor!" + EOL +
                     "Starting: Hello R-Actor!"     + EOL +
                     "Ending: Hello R-Actor!"       + EOL,
                     stdoutBuffer.toString());
    }

}
