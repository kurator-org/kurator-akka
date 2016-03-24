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
                "Error initializing actor 'Repeater'" + EOL +
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

        @SuppressWarnings("unused")
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

        @SuppressWarnings("unused")
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

        @SuppressWarnings("unused")
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

        @SuppressWarnings("unused")
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
    
    public void testPythonActor_OnInitNoArgument() throws Exception {
        
        @SuppressWarnings("unused")
        ActorConfig just_init =  wr.actor(PythonActor.class)
                .config("code", "def on_init():"            + EOL +
                                "  print 'Initializing'"    + EOL);
        wr.run();
        
        assertEquals(
                "Initializing"              + EOL,   
                stdoutBuffer.toString());
    }
    
    public void testPythonActor_OnInitWithArgument_NoParams() throws Exception {
        
        @SuppressWarnings("unused")
        ActorConfig just_init =  wr.actor(PythonActor.class)
                .config("code", "def on_init(state):"       + EOL +
                                "  print 'Initializing'"    + EOL +
                                "  print len(state)"        + EOL);
        wr.run();
        
        assertEquals(
                "Initializing"              + EOL +
                "0"                         + EOL,
                stdoutBuffer.toString());
    }
    
    public void testPythonActor_OnInitWithArgument_OneParam() throws Exception {
        
        @SuppressWarnings("unused")
        ActorConfig just_init =  wr.actor(PythonActor.class)
                .param("factor", 2)
                .config("code", "def on_init(state):"       + EOL +
                                "  print 'Initializing'"    + EOL +
                                "  print len(state)"        + EOL +
                                "  print state"             + EOL);
        wr.run();
        
        assertEquals(
                "Initializing"              + EOL +
                "1"                         + EOL +
                "{u'factor': 2}"            + EOL,
                stdoutBuffer.toString());
    }

    public void testPythonActor_OnInitWithArgument_TwoParams() throws Exception {
        
        @SuppressWarnings("unused")
        ActorConfig just_init =  wr.actor(PythonActor.class)
                .param("factor", 2)
                .param("prompt", "Command")
                .config("code", "def on_init(state):"       + EOL +
                                "  print 'Initializing'"    + EOL +
                                "  print len(state)"        + EOL +
                                "  print state"             + EOL);
        wr.run();
        
        assertEquals(
                "Initializing"                              + EOL +
                "2"                                         + EOL +
                "{u'factor': 2, u'prompt': u'Command'}"     + EOL,
                stdoutBuffer.toString());
    }

    public void testPythonActor_OnEndNoArgument() throws Exception {
        
        @SuppressWarnings("unused")
        ActorConfig just_init =  wr.actor(PythonActor.class)
                .config("code", "def on_end():"            + EOL +
                                "  print 'Ending'"         + EOL);
        wr.run();
        
        assertEquals(
                "Ending"                    + EOL,   
                stdoutBuffer.toString());
    }
    
    public void testPythonActor_OnEndWithArgument_NoParams() throws Exception {
        
        @SuppressWarnings("unused")
        ActorConfig just_init =  wr.actor(PythonActor.class)
                .config("code", "def on_end(state):"        + EOL +
                                "  print 'Ending'"          + EOL +
                                "  print len(state)"        + EOL);

        wr.run();
        
        assertEquals(
                "Ending"                    + EOL +
                "0"                         + EOL,
                stdoutBuffer.toString());
    }
    
    public void testPythonActor_OnEndWithArgument_OneParam() throws Exception {
        
        @SuppressWarnings("unused")
        ActorConfig just_init =  wr.actor(PythonActor.class)
                .param("factor", 2)
                .config("code", "def on_end(state):"        + EOL +
                                "  print 'Ending'"          + EOL +
                                "  print len(state)"        + EOL +
                                "  print state"             + EOL);

        wr.run();
        
        assertEquals(
                "Ending"                    + EOL +
                "1"                         + EOL +
                "{u'factor': 2}"            + EOL,
                stdoutBuffer.toString());
    }
    
    
    public void testPythonActor_OnEndWithArgument_TwoParams() throws Exception {
        
        @SuppressWarnings("unused")
        ActorConfig just_init =  wr.actor(PythonActor.class)
                .param("factor", 2)
                .param("prompt", "Command")
                .config("code", "def on_end(state):"        + EOL +
                                "  print 'Ending'"          + EOL +
                                "  print len(state)"        + EOL +
                                "  print state"             + EOL);

        wr.run();
        
        assertEquals(
                "Ending"                                    + EOL +
                "2"                                         + EOL +
                "{u'factor': 2, u'prompt': u'Command'}"     + EOL,
                stdoutBuffer.toString());
    }
    
   public void testPythonActor_OnStartNoArgument() throws Exception {
        
        @SuppressWarnings("unused")
        ActorConfig just_init =  wr.actor(PythonActor.class)
                .config("code", "def on_start():"            + EOL +
                                "  print 'Starting'"         + EOL);
        wr.run();
        
        assertEquals(
                "Starting"                    + EOL,   
                stdoutBuffer.toString());
    }
    
    public void testPythonActor_OnStartWithArgument_NoParams() throws Exception {
        
        @SuppressWarnings("unused")
        ActorConfig just_init =  wr.actor(PythonActor.class)
                .config("code", "def on_start(state):"      + EOL +
                                "  print 'Starting'"        + EOL +
                                "  print len(state)"        + EOL);

        wr.run();
        
        assertEquals(
                "Starting"                    + EOL +
                "0"                         + EOL,
                stdoutBuffer.toString());
    }
    
    public void testPythonActor_OnStartWithArgument_OneParam() throws Exception {
        
        @SuppressWarnings("unused")
        ActorConfig just_init =  wr.actor(PythonActor.class)
                .param("factor", 2)
                .config("code", "def on_start(state):"      + EOL +
                                "  print 'Starting'"        + EOL +
                                "  print len(state)"        + EOL +
                                "  print state"             + EOL);

        wr.run();
        
        assertEquals(
                "Starting"                  + EOL +
                "1"                         + EOL +
                "{u'factor': 2}"            + EOL,
                stdoutBuffer.toString());
    }
    
    
    public void testPythonActor_OnStartWithArgument_TwoParams() throws Exception {
        
        @SuppressWarnings("unused")
        ActorConfig just_init =  wr.actor(PythonActor.class)
                .param("factor", 2)
                .param("prompt", "Command")
                .config("code", "def on_start(state):"      + EOL +
                                "  print 'Starting'"        + EOL +
                                "  print len(state)"        + EOL +
                                "  print state"             + EOL);

        wr.run();
        
        assertEquals(
                "Starting"                                  + EOL +
                "2"                                         + EOL +
                "{u'factor': 2, u'prompt': u'Command'}"     + EOL,
                stdoutBuffer.toString());
    }
    
    


   public void testPythonActor_OnDataNoArgument() throws Exception {
        
        ActorConfig actor =  wr.actor(PythonActor.class)
                               .config("code", "def on_data(value):"   + EOL +
                                               "  print 'in on_data'"  + EOL +
                                               "  print value"         + EOL);

        wr.inputActor(actor)
          .begin()
          .tellWorkflow(1, 2, 3, new EndOfStream())
          .end();
        
        assertEquals(
                "in on_data"        + EOL +
                "1"                 + EOL +
                "in on_data"        + EOL +
                "2"                 + EOL +
                "in on_data"        + EOL +
                "3"                 + EOL,
                stdoutBuffer.toString());
    }
    
    public void testPythonActor_OnDataWithArgument_NoParams() throws Exception {
                
        ActorConfig actor =  wr.actor(PythonActor.class)
                               .config("code", "def on_data(value, state):" + EOL +
                                               "  print 'in on_data'"       + EOL +
                                               "  print state"              + EOL +
                                               "  print value"              + EOL);

        wr.inputActor(actor)
          .begin()
          .tellWorkflow(1, 2, 3, new EndOfStream())
          .end();
        
        assertEquals(
                "in on_data"        + EOL +
                "{}"                + EOL +
                "1"                 + EOL +
                "in on_data"        + EOL +
                "{}"                + EOL +
                "2"                 + EOL +
                "in on_data"        + EOL +
                "{}"                + EOL +
                "3"                 + EOL,
                stdoutBuffer.toString());
    }
    
    public void testPythonActor_OnDataWithArgument_OneParam() throws Exception {
        
        ActorConfig actor =  wr.actor(PythonActor.class)
                               .param("factor", 2)
                               .config("code", "def on_data(value, state):" + EOL +
                                               "  print 'in on_data'"       + EOL +
                                               "  print state"              + EOL +
                                               "  print value"              + EOL);

        wr.inputActor(actor)
          .begin()
          .tellWorkflow(1, 2, 3, new EndOfStream())
          .end();
        
        assertEquals(
                "in on_data"        + EOL +
                "{u'factor': 2}"    + EOL +
                "1"                 + EOL +
                "in on_data"        + EOL +
                "{u'factor': 2}"    + EOL +
                "2"                 + EOL +
                "in on_data"        + EOL +
                "{u'factor': 2}"    + EOL +
                "3"                 + EOL,
                stdoutBuffer.toString());    
    }
    
    
    public void testPythonActor_OnDataWithArgument_TwoParams() throws Exception {
        
        ActorConfig actor =  wr.actor(PythonActor.class)
                               .param("factor", 2)
                               .param("prompt", "Command")
                               .config("code", "def on_data(value, state):" + EOL +
                                               "  print 'in on_data'"       + EOL +
                                               "  print state"              + EOL +
                                               "  print value"              + EOL);

        wr.inputActor(actor)
          .begin()
          .tellWorkflow(1, 2, 3, new EndOfStream())
          .end();
        
        assertEquals(
                "in on_data"                            + EOL +
                "{u'factor': 2, u'prompt': u'Command'}" + EOL +
                "1"                                     + EOL +
                "in on_data"                            + EOL +
                "{u'factor': 2, u'prompt': u'Command'}" + EOL +
                "2"                                     + EOL +
                "in on_data"                            + EOL +
                "{u'factor': 2, u'prompt': u'Command'}" + EOL +
                "3"                                     + EOL,
                stdoutBuffer.toString());    
    }
    
    public void testPythonActor_TwoActors_onStart_onData() throws Exception {
    
        ActorConfig start_actor =  wr.actor(PythonActor.class)
                .param("factor", 2)
                .param("prompt", "Command")
                .config("code", "def on_start(state):"      + EOL +
                                "  print 'Starting'"        + EOL +
                                "  print state"             + EOL +
                                "  yield 1"                 + EOL +
                                "  yield 2"                 + EOL +
                                "  yield 3"                 + EOL);
        
        @SuppressWarnings("unused")
        ActorConfig printer =  wr.actor(PythonActor.class)
                               .listensTo(start_actor)
                               .param("factor", 2)
                               .param("prompt", "Command")
                               .config("code", "def on_data(value, state):" + EOL +
                                               "  print 'in on_data'"       + EOL +
                                               "  print state"              + EOL +
                                               "  print value"              + EOL);

        wr.run();
        
        assertEquals(
                "Starting"                                  + EOL +
                "{u'factor': 2, u'prompt': u'Command'}"     + EOL +
                "in on_data"                                + EOL +
                "{u'factor': 2, u'prompt': u'Command'}"     + EOL +
                "1"                                         + EOL +
                "in on_data"                                + EOL +
                "{u'factor': 2, u'prompt': u'Command'}"     + EOL +
                "2"                                         + EOL +
                "in on_data"                                + EOL +
                "{u'factor': 2, u'prompt': u'Command'}"     + EOL +
                "3"                                         + EOL,
                stdoutBuffer.toString());    
    }
    
    public void testPythonActor_UpdatingState() throws Exception {
        
        ActorConfig actor =  wr.actor(PythonActor.class)
                               .param("factor", 2)
                               .param("prompt", "Command")
                               .config("code", "def on_data(value, state):"                 + EOL +
                                               "  print 'in on_data'"                       + EOL +
                                               "  print state"                              + EOL +
                                               "  print value"                              + EOL +
                                               "  state['factor'] = state['factor'] + 1"    + EOL);

        wr.inputActor(actor)
          .begin()
          .tellWorkflow(1, 2, 3, new EndOfStream())
          .end();
        
        assertEquals(
                "in on_data"                            + EOL +
                "{u'factor': 2, u'prompt': u'Command'}" + EOL +
                "1"                                     + EOL +
                "in on_data"                            + EOL +
                "{u'factor': 3, u'prompt': u'Command'}" + EOL +
                "2"                                     + EOL +
                "in on_data"                            + EOL +
                "{u'factor': 4, u'prompt': u'Command'}" + EOL +
                "3"                                     + EOL,
                stdoutBuffer.toString());    
    }  

}
