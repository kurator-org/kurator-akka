package org.kurator.akka.ywactors;

import org.kurator.akka.ActorConfig;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.messages.EndOfStream;
import org.kurator.akka.ywactors.GroovyScriptActor;

public class TestGroovyScriptActor_HelloWorld extends KuratorAkkaTestCase {

    private WorkflowRunner wr;
    
    @Override
    public void setUp() throws Exception {
         super.setUp();
         wr = new WorkflowRunner()
    		  .outputStream(stdoutStream)
    		  .errorStream(stderrStream);
    }

    public void testGroovyScriptActor_PrintHelloWorld_OnInit() throws Exception {
        wr.actor(GroovyScriptActor.class)
          .config("onInit", "println('Initializing: Hello Groovy-Actor!')");
        wr.run();
        assertEquals("", stderrBuffer.toString());
        assertEquals("Initializing: Hello Groovy-Actor!" + EOL, stdoutBuffer.toString());
    }

    public void testGroovyScriptActor_PrintHelloWorld_OnStart() throws Exception {
        wr.actor(GroovyScriptActor.class)
          .config("onStart", "println('Starting: Hello Groovy-Actor!')");
        wr.run();
        assertEquals("", stderrBuffer.toString());
        assertEquals("Starting: Hello Groovy-Actor!" + EOL, stdoutBuffer.toString());
    }
    
    public void testGroovyScriptActor_PrintHelloWorld_OnData() throws Exception {
      ActorConfig actor = 
      wr.actor(GroovyScriptActor.class)
        .input("greeting")
        .config("onData", "println('Data: ' + greeting)");
      wr.inputActor(actor)
        .begin()
        .tellWorkflow("Hello Groovy-Actor!", new EndOfStream())
        .end();
      assertEquals("", stderrBuffer.toString());
      assertEquals("Data: Hello Groovy-Actor!" + EOL, stdoutBuffer.toString());
    }

    public void testGroovyScriptActor_PrintHelloWorld_OnEnd() throws Exception {
        wr.actor(GroovyScriptActor.class)
          .config("onEnd", "println('Ending: Hello Groovy-Actor!')");
        wr.run();
        assertEquals("", stderrBuffer.toString());
        assertEquals("Ending: Hello Groovy-Actor!" + EOL, stdoutBuffer.toString());
    }
    
    public void testGroovyScriptActor_PrintHelloWorld_OnInitStartEnd() throws Exception {
        wr.actor(GroovyScriptActor.class)
          .config("onInit",  "println('Initializing: Hello Groovy-Actor!')")
          .config("onStart", "println('Starting: Hello Groovy-Actor!')")
          .config("onEnd",   "println('Ending: Hello Groovy-Actor!')");
        wr.run();
        assertEquals("", stderrBuffer.toString());
        assertEquals(
            "Initializing: Hello Groovy-Actor!" + EOL +
            "Starting: Hello Groovy-Actor!"     + EOL +
            "Ending: Hello Groovy-Actor!"       + EOL,
            stdoutBuffer.toString());
    }

    public void testGroovyScriptActor_PrintHelloWorld_OnInitStartDataEnd() throws Exception {
        ActorConfig actor = 
        wr.actor(GroovyScriptActor.class)
          .input("greeting")
          .config("onInit",  "println('Initializing: Hello Groovy-Actor!')")
          .config("onStart", "println('Starting: Hello Groovy-Actor!')")
          .config("onData",  "println('Data: ' + greeting)")
          .config("onEnd",   "println('Ending: Hello Groovy-Actor!')");
        wr.inputActor(actor)
          .begin()
          .tellWorkflow("Hello Groovy-Actor!", new EndOfStream())
          .end();
        assertEquals("", stderrBuffer.toString());
        assertEquals(
            "Initializing: Hello Groovy-Actor!" + EOL +
            "Starting: Hello Groovy-Actor!"     + EOL +
            "Data: Hello Groovy-Actor!"         + EOL +
            "Ending: Hello Groovy-Actor!"       + EOL,
            stdoutBuffer.toString());
    }
}
