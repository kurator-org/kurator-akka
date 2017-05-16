package org.kurator.akka;

public class TestPythonScriptActor extends KuratorAkkaTestCase {

    private WorkflowRunner wr;
    
    @Override
    public void setUp() throws Exception {
         super.setUp();
         wr = new WorkflowRunner()
        		  .outputStream(stdoutStream)
        		  .errorStream(stderrStream);

    }

    public void testPythonScriptActor_PrintHelloWorld() throws Exception {

        ActorConfig actor = wr.actor(PythonScriptActor.class)
                              .config("onStart", "print('Hello Python-Actor!')");

        wr.run();

        assertEquals("", stderrBuffer.toString());
        assertEquals("Hello Python-Actor!" + EOL, stdoutBuffer.toString());
    }
}
