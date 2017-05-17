package org.kurator.akka.ywactors;

import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.ywactors.PythonScriptActor;

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

        wr.actor(PythonScriptActor.class)
          .config("onStart", "print('Hello Python-Actor!')");

        wr.run();

        assertEquals("", stderrBuffer.toString());
        assertEquals("Hello Python-Actor!" + EOL, stdoutBuffer.toString());
    }
}
