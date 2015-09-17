package org.kurator.akka.actors;

import org.kurator.akka.ActorConfig;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.PythonActor;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.messages.EndOfStream;

public class TestPythonActor_InlinedCode extends KuratorAkkaTestCase {

    private WorkflowRunner wr;
    
    @Override
    public void setUp() throws Exception {

         super.setUp();

         wr = new WorkflowRunner()
             .outputStream(stdoutStream)
             .errorStream(stderrStream);
    }
    
    public void testPythonActor_DefaultOnData() throws Exception {
        
        ActorConfig actor = wr.actor(PythonActor.class)
                              .config("code", "def on_data(n):  print 'Received data: ' + str(n)");
               
        wr.inputActor(actor)
          .begin()
          .tellWorkflow(1, 2, 3, new EndOfStream())
          .end();
        
        assertEquals(
                "Received data: 1"      + EOL +
                "Received data: 2"      + EOL +
                "Received data: 3"      + EOL,   
                stdoutBuffer.toString());
    }
    
    public void testPythonActor_CustomOnData() throws Exception {
        
        ActorConfig actor = wr.actor(PythonActor.class)
                              .config("onData", "echo")
                              .config("code", "def echo(n):  print 'Received data: ' + str(n)");
               
        wr.inputActor(actor)
          .begin()
          .tellWorkflow(1, 2, 3, new EndOfStream())
          .end();
        
        assertEquals(
                "Received data: 1"      + EOL +
                "Received data: 2"      + EOL +
                "Received data: 3"      + EOL,   
                stdoutBuffer.toString());
    }
    
    public void testPythonActor_MissingCustomOnData() throws Exception {
        
        wr.actor(PythonActor.class)
          .config("name", "Repeater")
          .config("onData", "echo");
               
        wr.build();

        Exception caught = null;
        try {
          wr.init();
        } catch (Exception e) {
            caught = e;
        }
        
        assertNotNull(caught);
        assertEquals(
                "Error initializing workflow 'Workflow'" + EOL +
                "Error intializing actor 'Repeater'" + EOL +
                "Custom onData handler 'echo' not defined for actor 'Repeater'", 
                caught.getMessage());
    }

    public void testPythonActor_Multiplier() throws Exception {

        ActorConfig repeater = wr.actor(PythonActor.class)
                .config("code", "on_data = lambda n: n");
        
        ActorConfig multiplier =  wr.actor(PythonActor.class)
                .listensTo(repeater)
                .param("factor", 2)
                .config("code", "on_data = lambda n: factor*n");

        ActorConfig printer = wr.actor(PythonActor.class)
                .listensTo(multiplier)
                .config("code", "def on_data(n):  print n");
               
        wr.inputActor(repeater)
          .begin()
          .tellWorkflow(1, 2, 3, 4, 5, new EndOfStream())
          .end();
        
        assertEquals(
                "2"     + EOL +
                "4"     + EOL + 
                "6"     + EOL + 
                "8"     + EOL + 
                "10"    + EOL,   
                stdoutBuffer.toString());
    }

    
    public void testPythonActor_IntegerTuples() throws Exception {

        ActorConfig repeater = wr.actor(PythonActor.class)
                .config("code", "on_data = lambda n: n");
        
        ActorConfig double_and_triple =  wr.actor(PythonActor.class)
                .listensTo(repeater)
                .param("factor", 2)
                .config("code", "on_data = lambda n: (n*2, n*3)");

        ActorConfig printer = wr.actor(PythonActor.class)
                .listensTo(double_and_triple)
                .config("code", "def on_data(n):  print n");
               
        wr.inputActor(repeater)
          .begin()
          .tellWorkflow(1, 2, 3, 4, 5, new EndOfStream())
          .end();
        
        assertEquals(
                "(2, 3)"        + EOL +
                "(4, 6)"        + EOL +
                "(6, 9)"        + EOL +
                "(8, 12)"       + EOL +
                "(10, 15)"      + EOL,   
                stdoutBuffer.toString());
    }
    
    public void testPythonActor_MixedTuples() throws Exception {

        ActorConfig repeater = wr.actor(PythonActor.class)
                .config("code", "on_data = lambda n: n");
        
        ActorConfig double_and_triple =  wr.actor(PythonActor.class)
                .listensTo(repeater)
                .param("factor", 2)
                .config("code", "on_data = lambda n: (n, str(n))");

        ActorConfig printer = wr.actor(PythonActor.class)
                .listensTo(double_and_triple)
                .config("code", "def on_data(n):  print n");
               
        wr.inputActor(repeater)
          .begin()
          .tellWorkflow(1, 2, 3, 4, 5, new EndOfStream())
          .end();
        
        assertEquals(
                "(1, '1')"      + EOL +
                "(2, '2')"      + EOL +
                "(3, '3')"      + EOL +
                "(4, '4')"      + EOL +
                "(5, '5')"      + EOL,   
                stdoutBuffer.toString());
    }
    
    public void testPythonActor_MixedTuplesWithUnicode() throws Exception {
        
        ActorConfig double_and_triple =  wr.actor(PythonActor.class)
                .config("code", "def on_start():"                               + EOL +
                                "  yield u'eight_specimen_records', 'csv', 2"   + EOL +
                                "  yield u'eight_specimen_records', u'csv', 2"  + EOL +
                                "  yield 459, u'csv', 2"                        + EOL);

        ActorConfig printer = wr.actor(PythonActor.class)
                .listensTo(double_and_triple)
                .config("code", "def on_data(n):  print n");
        
        wr.run();
        
        assertEquals(
                "(u'eight_specimen_records', 'csv', 2)"     + EOL +
                "(u'eight_specimen_records', u'csv', 2)"    + EOL +
                "(459, u'csv', 2)"                          + EOL,   
                stdoutBuffer.toString());
    }    
}
