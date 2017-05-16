package org.kurator.akka;

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

        ActorConfig actor = wr.actor(RScriptActor.class)
                              .config("onStart", "cat('Hello R-Actor!')");

        wr.run();

        assertEquals("", stderrBuffer.toString());
        assertEquals("Hello R-Actor!", stdoutBuffer.toString());
    }
}
