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
    
    public void testPythonScriptActor_PrintHelloWorld_OnInit() throws Exception {

        wr.actor(PythonScriptActor.class)
          .config("onInit", "print('Initializing: Hello Python-Actor!')");

        wr.run();

        assertEquals("", stderrBuffer.toString());
        assertEquals("Initializing: Hello Python-Actor!" + EOL, stdoutBuffer.toString());
    }

    public void testPythonScriptActor_PrintHelloWorld_OnStart() throws Exception {

        wr.actor(PythonScriptActor.class)
          .config("onStart", "print('Starting: Hello Python-Actor!')");

        wr.run();

        assertEquals("", stderrBuffer.toString());
        assertEquals("Starting: Hello Python-Actor!" + EOL, stdoutBuffer.toString());
    }
    
    public void testPythonScriptActor_PrintHelloWorld_OnEnd() throws Exception {

        wr.actor(PythonScriptActor.class)
          .config("onEnd", "print('Ending: Hello Python-Actor!')");

        wr.run();

        assertEquals("", stderrBuffer.toString());
        assertEquals("Ending: Hello Python-Actor!" + EOL, stdoutBuffer.toString());
    }
    
    public void testPythonScriptActor_PrintHelloWorld_OnInitStartEnd() throws Exception {

        wr.actor(PythonScriptActor.class)
          .config("onInit",  "print('Initializing: Hello Python-Actor!')")
          .config("onStart", "print('Starting: Hello Python-Actor!')")
          .config("onEnd",   "print('Ending: Hello Python-Actor!')");

        wr.run();

        assertEquals("", stderrBuffer.toString());
        assertEquals("Initializing: Hello Python-Actor!" + EOL +
                     "Starting: Hello Python-Actor!"     + EOL +
                     "Ending: Hello Python-Actor!"       + EOL,
                     stdoutBuffer.toString());
    }
    
}
