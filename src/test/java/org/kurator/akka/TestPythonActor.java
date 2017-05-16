package org.kurator.akka;

public class TestPythonActor extends KuratorAkkaTestCase {

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

        wr.begin()
          .end();

        assertEquals("", stderrBuffer.toString());
        assertEquals("Hello Python-Actor!" + EOL, stdoutBuffer.toString());
    }
}
