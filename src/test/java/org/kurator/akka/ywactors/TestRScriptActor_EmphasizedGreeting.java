package org.kurator.akka.ywactors;

import org.kurator.akka.ActorConfig;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.messages.EndOfStream;
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
    
    public void testRScriptActor_SingleEmphasizedGreeting() throws Exception {
        ActorConfig greetingSource = 
            wr.actor(RScriptActor.class)
              .config("onStart", "out <- 'Hello'");
        ActorConfig greetingEmphasizer = 
            wr.actor(RScriptActor.class)
              .config("onData", "out <- paste(inp,'!!',sep='')")
              .listensTo(greetingSource);
        ActorConfig greetingPrinter = 
            wr.actor(RScriptActor.class)
              .config("onData", "cat(inp,'\n',sep='')")
              .listensTo(greetingEmphasizer);
        wr.run();
        assertEquals("", stderrBuffer.toString());
        assertEquals("Hello!!" + EOL, stdoutBuffer.toString());
    }
    
    public void testRScriptActor_MultipleEmphasizedGreetings() throws Exception {
        ActorConfig greetingSource = 
            wr.actor(RScriptActor.class)
              .config("onData", "out <- inp");
        ActorConfig greetingEmphasizer = 
            wr.actor(RScriptActor.class)
              .config("onData", "out <- paste(inp,'!!',sep='')")
              .listensTo(greetingSource);
        ActorConfig greetingPrinter = 
            wr.actor(RScriptActor.class)
              .config("onData", "cat(inp,'\n',sep='')")
              .listensTo(greetingEmphasizer);

        wr.inputActor(greetingSource)
          .begin()
          .tellWorkflow("Hello", "Goodbye", "Good morning", new EndOfStream())
          .end();
        
        assertEquals("", stderrBuffer.toString());
        assertEquals(
            "Hello!!"           + EOL +
            "Goodbye!!"         + EOL +
            "Good morning!!"    + EOL,
            stdoutBuffer.toString());
    }  
}
