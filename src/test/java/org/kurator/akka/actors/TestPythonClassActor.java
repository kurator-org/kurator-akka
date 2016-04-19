package org.kurator.akka.actors;

import org.kurator.akka.ActorConfig;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.PythonActor;
import org.kurator.akka.PythonClassActor;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.messages.EndOfStream;

public class TestPythonClassActor extends KuratorAkkaTestCase {

    private WorkflowRunner wr;
    
    @Override
    public void setUp() throws Exception {

         super.setUp();

         wr = new WorkflowRunner()
             .outputStream(stdoutStream)
             .errorStream(stderrStream);
    }
    
    public void testPythonClassActor_NoEventHandlers() throws Exception {
        
        wr.actor(PythonClassActor.class)
          .config("pythonClass", "actor")
          .config("code",
                  "class actor(object):"    + EOL +
                  "  a=3"   );
        wr.run();
    }

    public void testPythonClassActor_DefaultOnInit() throws Exception {
      
        wr.actor(PythonClassActor.class)
          .config("pythonClass", "actor")
          .config("code",
                  "class actor(object):"          + EOL +
                  "  def on_init(self):"          + EOL +
                  "    print 'Initializing.'"     );
    
        wr.run();
      
        assertEquals(
                "Initializing."      + EOL,   
                stdoutBuffer.toString());
    }

    public void testPythonClassActor_DefaultOnStart() throws Exception {
      
        ActorConfig sender = 
        wr.actor(PythonClassActor.class)
        .config("pythonClass", "actor")
        .config("code",
                "class actor(object):"        + EOL +
                "  def on_start(self):"       + EOL +
                "    print 'Sending data'"    + EOL +
                "    return 42"               );

        wr.actor(PythonClassActor.class)
        .config("pythonClass", "actor")
        .listensTo(sender)
        .config("code",
                "class actor(object):"    + EOL +
                "  def on_data(self, n):" + EOL +
                "    print 'Received: ' + str(n)"   );
                     
        wr.run();
      
        assertEquals(
              "Sending data"      + EOL +
              "Received: 42"      + EOL,   
              stdoutBuffer.toString());
    }
  
    public void testPythonClassActor_DefaultOnData() throws Exception {
        
        ActorConfig actor = 
        wr.actor(PythonClassActor.class)
          .config("pythonClass", "actor")
          .config("code",
                  "class actor(object):"    + EOL +
                  "  def on_data(self, n):" + EOL +
                  "    print 'Received data: ' + str(n)"   );
               
        wr.inputActor(actor);
        
        wr.begin()
          .tellWorkflow(1, 2, 3, new EndOfStream())
          .end();
        
        assertEquals(
                "Received data: 1"      + EOL +
                "Received data: 2"      + EOL +
                "Received data: 3"      + EOL,   
                stdoutBuffer.toString());
    }
    
    public void testPythonClassActor_DefaultOnEnd() throws Exception {
        
        wr.actor(PythonClassActor.class)
          .config("pythonClass", "actor")
          .config("code",
                  "class actor(object):"        + EOL +
                  "  def on_end(self):"         + EOL +
                  "    print 'Ending.'"         );
    
        wr.run();
      
        assertEquals(
                "Ending."      + EOL,   
                stdoutBuffer.toString());
    }
    
    
    public void testPythonClassActor_AllDefaultHandlers() throws Exception {
        
        ActorConfig sender = 
        wr.actor(PythonClassActor.class)
          .config("pythonClass", "actor")
          .config("code",
                  "class actor(object):"            + EOL +
                  "  def on_init(self):"            + EOL +
                  "    print 'Initializing.'"       + EOL +
                  "  def on_start(self):"           + EOL +
                  "    print 'Starting.'"           + EOL +
                  "  def on_data(self, n):"         + EOL +
                  "    print 'Received ' + str(n)"  + EOL +
                  "  def on_end(self):"             + EOL +
                  "    print 'Ending.'"             );
        
        wr.begin()
          .tellActor(sender, 1, 2, 3, new EndOfStream())
          .end();

        assertEquals(
                "Initializing."     + EOL +
                "Starting."         + EOL +
                "Received 1"        + EOL +
                "Received 2"        + EOL +
                "Received 3"        + EOL +
                "Ending."           + EOL,   
                stdoutBuffer.toString());
    }
    
    public void testPythonClassActor_CustomOnInit() throws Exception {
        
        wr.actor(PythonClassActor.class)
          .config("pythonClass", "actor")
          .config("onInit", "custom_on_init")
          .config("code",
                  "class actor(object):"        + EOL +
                  "  def custom_on_init(self):" + EOL +
                  "    print 'Initializing.'"     );
    
        wr.run();
      
        assertEquals(
                "Initializing."      + EOL,   
                stdoutBuffer.toString());
    }

    public void testPythonClassActor_CustomOnData() throws Exception {
        
        ActorConfig actor = 
        wr.actor(PythonClassActor.class)
          .config("pythonClass", "actor")
          .config("onData", "echo")
          .config("code",
                  "class actor(object):"    + EOL +
                  "  def echo(self, n):"    + EOL +
                  "    print 'Received data: ' + str(n)"   );

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
    
    public void testPythonClassActor_CustomOnStart() throws Exception {
        
        ActorConfig sender = 
        wr.actor(PythonClassActor.class)
          .config("pythonClass", "actor")
          .config("onStart", "send")
          .config("code",
                  "class actor(object):"        + EOL +
                  "  def send(self):"           + EOL +
                  "    print 'Sending data'"    + EOL +
                  "    yield 42"                + EOL +
                  "    yield 21"                + EOL +
                  "    yield 7"                 );

        wr.actor(PythonClassActor.class)
        .config("pythonClass", "actor")
        .listensTo(sender)
        .config("code",
                "class actor(object):"    + EOL +
                "  def on_data(self, n):" + EOL +
                "    print 'Received: ' + str(n)"   );
                       
        wr.run();
        
        assertEquals(
                "Sending data"      + EOL +
                "Received: 42"      + EOL +
                "Received: 21"      + EOL +
                "Received: 7"       + EOL,   
                stdoutBuffer.toString());
    }

    public void testPythonClassActor_CustomOnEnd() throws Exception {
        
        wr.actor(PythonClassActor.class)
          .config("pythonClass", "actor")
          .config("onEnd", "custom_on_end")
          .config("code",
                  "class actor(object):"        + EOL +
                  "  def custom_on_end(self):"  + EOL +
                  "    print 'Ending.'"         );
    
        wr.run();
      
        assertEquals(
                "Ending."      + EOL,   
                stdoutBuffer.toString());
    }

    
    public void testPythonClassActor_AllCustomHandlers() throws Exception {
        
        ActorConfig sender = 
        wr.actor(PythonClassActor.class)
          .config("pythonClass", "actor")
          .config("onInit", "custom_on_init")
          .config("onStart", "custom_on_start")
          .config("onData", "custom_on_data")
          .config("onEnd", "custom_on_end")
          .config("code",
                  "class actor(object):"                + EOL +
                  "  def custom_on_init(self):"         + EOL +
                  "    print 'Initializing.'"           + EOL +
                  "  def custom_on_start(self):"        + EOL +
                  "    print 'Starting.'"               + EOL +
                  "  def custom_on_data(self, n):"      + EOL +
                  "    print 'Received ' + str(n)"      + EOL +
                  "  def custom_on_end(self):"          + EOL +
                  "    print 'Ending.'"                 );
        
        wr.begin()
          .tellActor(sender, 1, 2, 3, new EndOfStream())
          .end();

        assertEquals(
                "Initializing."     + EOL +
                "Starting."         + EOL +
                "Received 1"        + EOL +
                "Received 2"        + EOL +
                "Received 3"        + EOL +
                "Ending."           + EOL,   
                stdoutBuffer.toString());
    }
    
    public void testPythonClassActor_MissingPythonClass() throws Exception {
        
        wr.actor(PythonClassActor.class)
          .config("pythonClass", "my_actor");
               
        wr.build();

        Exception caught = null;
        try {
          wr.init();
        } catch (Exception e) {
            caught = e;
        }

        assertNotNull(caught);
        assertEquals(
                "Error initializing workflow 'Workflow'"                               + EOL +
                "Error initializing actor 'PythonClassActor_1'"       + EOL +
                "Error instantiating class 'my_actor': name 'my_actor' is not defined",
                caught.getMessage());
    }

    public void testPythonClassActor_MissingModule() throws Exception {
        
        wr.actor(PythonClassActor.class)
          .config("pythonClass", "my_module.my_submodule.my_actor");
        
        wr.build();

        Exception caught = null;
        try {
          wr.init();
        } catch (Exception e) {
            caught = e;
        }
        
        assertNotNull(caught);
        assertEquals(
                "Error initializing workflow 'Workflow'"                            + EOL +
                "Error initializing actor 'PythonClassActor_1'"    + EOL +
                "Error importing class 'my_module.my_submodule.my_actor': No module named my_module",
                caught.getMessage());
    }
    
    
    public void testPythonClassActor_MissingCustomOnInit() throws Exception {
        
        wr.actor(PythonClassActor.class)
          .config("pythonClass", "actor")
          .config("onInit", "custom_on_init")
          .config("code",
                  "class actor(object):"        + EOL +
                  "  def on_init(self):"        + EOL +
                  "    print 'Initializing.'"   );
    
        wr.build();

        Exception caught = null;
        try {
          wr.init();
        } catch (Exception e) {
            caught = e;
        }
        
        assertNotNull(caught);
        assertEquals(
                "Error initializing workflow 'Workflow'"                        + EOL +
                "Error initializing actor 'PythonClassActor_1'" + EOL +
                "Error binding to onInit method 'custom_on_init': "             +
                "AttributeError(\"'actor' object has no attribute 'custom_on_init'\",)", 
                caught.getMessage());
    }

    public void testPythonClassActor_MissingCustomOnStart() throws Exception {
        
        wr.actor(PythonClassActor.class)
          .config("pythonClass", "my_actor")
          .config("onStart", "send")
          .config("code",
                  "class my_actor(object):"  + EOL +
                  "  def on_data(self, n): pass"    );

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
                "Error initializing actor 'PythonClassActor_1'" + EOL +
                "Error binding to onStart method 'send': 'my_actor' object has no attribute 'send'",
                caught.getMessage());
    }
    
    public void testPythonClassActor_MissingCustomOnData() throws Exception {
        
        wr.actor(PythonClassActor.class)
          .config("pythonClass", "my_actor")
          .config("onData", "echo")
          .config("code",
                  "class my_actor(object):"  + EOL +
                  "  def on_data(self, n): pass"    );
               
        wr.build();

        Exception caught = null;
        try {
          wr.init();
        } catch (Exception e) {
            caught = e;
        }
        
        assertNotNull(caught);
        assertEquals(
                "Error initializing workflow 'Workflow'"                        + EOL +
                "Error initializing actor 'PythonClassActor_1'" + EOL +
                "Error binding to onData method 'echo': 'my_actor' object has no attribute 'echo'", 
                caught.getMessage());
    }
    
    public void testPythonClassActor_MissingCustomOnEnd() throws Exception {
        
        wr.actor(PythonClassActor.class)
          .config("pythonClass", "actor")
          .config("onEnd", "custom_on_end")
          .config("code",
                  "class actor(object):"    + EOL +
                  "  def on_end(self):"     + EOL +
                  "    print 'Ending.'"         );
    
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
                "Error initializing actor 'PythonClassActor_1'" + EOL +
                "Error binding to onEnd method 'custom_on_end': 'actor' object has no attribute 'custom_on_end'", 
                caught.getMessage());
    }

    public void testPythonClassActor_DefaultOnDataIsNotMethod() throws Exception {
        
        ActorConfig actor = 
        wr.actor(PythonClassActor.class)
          .config("pythonClass", "actor")
          .config("code",
                  "class actor(object):"    + EOL +
                  "  on_data=3"   );
        wr.build();

        Exception caught = null;
        try {
          wr.init();
        } catch (Exception e) {
            caught = e;
        }
        
        assertNotNull(caught);
        assertEquals(
                "Error initializing workflow 'Workflow'"                        + EOL +
                "Error initializing actor 'PythonClassActor_1'" + EOL +
                "Error binding to default onData method: 'on_data' is not a method on actor", 
                caught.getMessage());
    }
    
    public void testPythonClassActor_CustomOnDataIsNotMethod() throws Exception {
        
        wr.actor(PythonClassActor.class)
          .config("pythonClass", "my_actor")
          .config("onData", "echo")
          .config("code",
                  "class my_actor(object):"  + EOL +
                  "  echo=3"    );
               
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
                "Error initializing actor 'PythonClassActor_1'" + EOL +
                "Error binding to onData method: 'echo' is not a method on my_actor", 
                caught.getMessage());
    }

    public void testPythonClassActor_Multiplier() throws Exception {

        WorkflowRunner wr = new WorkflowRunner()
             .outputStream(stdoutStream)
             .errorStream(stderrStream);
        
        ActorConfig multiplier =  
        wr.actor(PythonClassActor.class)
          .config("pythonClass", "multiplier_actor")
          .config("code", 
                  "class multiplier_actor(object):"             + EOL +
                  "  def __init__(self):"                       + EOL +
                  "    self.factor = 1"                         + EOL +
                  "  def on_data(self,n): return self.factor*n")
          .param("factor", 2);

        wr.actor(PythonClassActor.class)
          .config("pythonClass", "printer_actor")
          .config("code", 
                  "class printer_actor(object):" + EOL +
                  "  def on_data(self, n): print n")
          .listensTo(multiplier);
               
        wr.begin();
        
        Thread.sleep(10);
        
        wr.tellActor(multiplier, 1, 2, 3, 4, 5, new EndOfStream())
          .end();
        
        assertEquals(
                "2"     + EOL +
                "4"     + EOL + 
                "6"     + EOL + 
                "8"     + EOL + 
                "10"    + EOL,   
                stdoutBuffer.toString());
    }

    
    public void testPythonClassActor_IntegerTuples() throws Exception {

        ActorConfig double_and_triple =  
        wr.actor(PythonClassActor.class)
          .config("pythonClass", "multiplier_actor")
          .config("code", 
                  "class multiplier_actor(object):" + EOL +
                  "  on_data = lambda self, n: (n*2, n*3)");

        wr.actor(PythonClassActor.class)
          .config("pythonClass", "printer_actor")
          .config("code", 
                  "class printer_actor(object):" + EOL +
                  "  def on_data(self, n): print n")
          .listensTo(double_and_triple);
               
        wr.begin()
          .tellActor(double_and_triple, 1, 2, 3, 4, 5, new EndOfStream())
          .end();
        
        assertEquals(
                "(2, 3)"        + EOL +
                "(4, 6)"        + EOL +
                "(6, 9)"        + EOL +
                "(8, 12)"       + EOL +
                "(10, 15)"      + EOL,   
                stdoutBuffer.toString());
    }
    
    public void testPythonClassActor_MixedTuples() throws Exception {
        
        ActorConfig to_int_string_tuple =  
        wr.actor(PythonClassActor.class)
          .config("pythonClass", "to_int_string_tuple")
          .config("code", 
                  "class to_int_string_tuple(object):" + EOL +
                  "  on_data = lambda self, n: (n, str(n))");

        wr.actor(PythonClassActor.class)
          .config("pythonClass", "printer_actor")
          .config("code", 
                "class printer_actor(object):" + EOL +
                "  def on_data(self, n): print n")
          .listensTo(to_int_string_tuple);
               
        wr.begin()
          .tellActor(to_int_string_tuple, 1, 2, 3, 4, 5, new EndOfStream())
          .end();
        
        assertEquals(
                "(1, '1')"      + EOL +
                "(2, '2')"      + EOL +
                "(3, '3')"      + EOL +
                "(4, '4')"      + EOL +
                "(5, '5')"      + EOL,   
                stdoutBuffer.toString());
    }
    
    public void testPythonClassActor_MixedTuplesWithUnicode() throws Exception {
        
        ActorConfig double_and_triple =  wr.actor(PythonClassActor.class)
                .config("pythonClass", "tuple_actor")
                .config("code",
                        "class tuple_actor(object):"                      + EOL +
                        "  def on_start(self):"                           + EOL +
                        "    yield u'eight_specimen_records', 'csv', 2"   + EOL +
                        "    yield u'eight_specimen_records', u'csv', 2"  + EOL +
                        "    yield 459, u'csv', 2"                        + EOL);

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
        ActorConfig test_actor =  wr.actor(PythonClassActor.class)
                .config("pythonClass", "test_actor")
                .config("code",  
                        "class test_actor(object):"     + EOL +
                        "  def on_init(self):"          + EOL +
                        "    print 'Initializing'"      + EOL);
        wr.run();
        
        assertEquals(
                "Initializing"              + EOL,   
                stdoutBuffer.toString());
    }
    
    public void testPythonActor_OnInitWithArgument_NoParams() throws Exception {
        
        @SuppressWarnings("unused")
        ActorConfig test_actor =  wr.actor(PythonClassActor.class)
                .config("pythonClass", "test_actor")
                .config("code",  
                        "class test_actor(object):"     + EOL +
                        "  def on_init(self, state):"   + EOL +
                        "    print 'Initializing'"      + EOL +
                        "    print len(state)"          + EOL);
        wr.run();
        
        assertEquals(
                "Initializing"              + EOL +
                "0"                         + EOL,
                stdoutBuffer.toString());
    }
    
    public void testPythonClassActor_OnInitWithArgument_OneParam() throws Exception {
        
        @SuppressWarnings("unused")
        ActorConfig test_actor =  wr.actor(PythonClassActor.class)
                .param("factor", 2)
                .config("pythonClass", "test_actor")
                .config("code",  
                        "class test_actor(object):"     + EOL +
                        "  def on_init(self, state):"   + EOL +
                        "    print 'Initializing'"      + EOL +
                        "    print len(state)"          + EOL +
                        "    print state"               + EOL);
        wr.run();
        
        assertEquals(
                "Initializing"              + EOL +
                "1"                         + EOL +
                "{factor: 2}"            + EOL,
                stdoutBuffer.toString());
    }

    public void testPythonClassnActor_OnInitWithArgument_TwoParams() throws Exception {
        
        @SuppressWarnings("unused")
        ActorConfig test_actor =  wr.actor(PythonClassActor.class)
                .param("factor", 2)
                .param("prompt", "Command")
                .config("pythonClass", "test_actor")
                .config("code",  
                        "class test_actor(object):"     + EOL +
                        "  def on_init(self, state):"   + EOL +
                        "    print 'Initializing'"      + EOL +
                        "    print len(state)"          + EOL +
                        "    print state"               + EOL);
        wr.run();
        
        assertEquals(
                "Initializing"                      + EOL +
                "2"                                 + EOL +
                "{factor: 2, prompt: Command}"      + EOL,
                stdoutBuffer.toString());
    }

    public void testPythonClassActor_OnEndNoArgument() throws Exception {

        @SuppressWarnings("unused")
        ActorConfig test_actor =  wr.actor(PythonClassActor.class)
                .config("pythonClass", "test_actor")
                .config("code",  
                        "class test_actor(object):"     + EOL +
                        "  def on_end(self):"           + EOL +
                        "    print 'Ending'"            + EOL);        
        wr.run();
        
        assertEquals(
                "Ending"                    + EOL,   
                stdoutBuffer.toString());
    }
    
    public void testPythonClassActor_OnEndWithArgument_NoParams() throws Exception {
        
        @SuppressWarnings("unused")
        ActorConfig test_actor =  wr.actor(PythonClassActor.class)
                .config("pythonClass", "test_actor")
                .config("code",  
                        "class test_actor(object):"     + EOL +
                        "  def on_end(self, state):"    + EOL +
                        "    print 'Ending'"            + EOL +
                        "    print len(state)"          + EOL);

        wr.run();
        
        assertEquals(
                "Ending"                    + EOL +
                "0"                         + EOL,
                stdoutBuffer.toString());
    }
    
    public void testPythonClassActor_OnEndWithArgument_OneParam() throws Exception {
        
        @SuppressWarnings("unused")
        ActorConfig test_actor =  wr.actor(PythonClassActor.class)
                .param("factor", 2)
                .config("pythonClass", "test_actor")
                .config("code",  
                        "class test_actor(object):"     + EOL +
                        "  def on_end(self, state):"    + EOL +
                        "    print 'Ending'"            + EOL +
                        "    print len(state)"          + EOL +
                        "    print state"               + EOL);

        wr.run();
        
        assertEquals(
                "Ending"                    + EOL +
                "1"                         + EOL +
                "{factor: 2}"               + EOL,
                stdoutBuffer.toString());
    }
    
    
    public void testPythonClassActor_OnEndWithArgument_TwoParams() throws Exception {
        
        @SuppressWarnings("unused")
        ActorConfig test_actor =  wr.actor(PythonClassActor.class)
                .param("factor", 2)
                .param("prompt", "Command")
                .config("pythonClass", "test_actor")
                .config("code",  
                        "class test_actor(object):"     + EOL +
                        "  def on_end(self, state):"    + EOL +
                        "    print 'Ending'"            + EOL +
                        "    print len(state)"          + EOL +
                        "    print state"               + EOL);

        wr.run();
        
        assertEquals(
                "Ending"                        + EOL +
                "2"                             + EOL +
                "{factor: 2, prompt: Command}"  + EOL,
                stdoutBuffer.toString());
    }
    
   public void testPythonClassActor_OnStartNoArgument() throws Exception {

       @SuppressWarnings("unused")
       ActorConfig test_actor =  wr.actor(PythonClassActor.class)
               .config("pythonClass", "test_actor")
               .config("code",  
                       "class test_actor(object):"  + EOL +
                       "  def on_start(self):"      + EOL +
                       "    print 'Starting'"       + EOL);
        wr.run();
        
        assertEquals(
                "Starting"                  + EOL,   
                stdoutBuffer.toString());
    }
    
    public void testPythonClassActor_OnStartWithArgument_NoParams() throws Exception {
        
        @SuppressWarnings("unused")
        ActorConfig test_actor =  wr.actor(PythonClassActor.class)
                .config("pythonClass", "test_actor")
                .config("code",  
                        "class test_actor(object):"     + EOL +
                        "  def on_start(self, state):"  + EOL +
                        "    print 'Starting'"          + EOL +
                        "    print len(state)"          + EOL);

        wr.run();
        
        assertEquals(
                "Starting"              + EOL +
                "0"                     + EOL,
                stdoutBuffer.toString());
    }
    
    public void testPythonClassActor_OnStartWithArgument_OneParam() throws Exception {

        @SuppressWarnings("unused")
        ActorConfig test_actor =  wr.actor(PythonClassActor.class)
                .param("factor", 2)
                .config("pythonClass", "test_actor")
                .config("code",  
                        "class test_actor(object):"     + EOL +
                        "  def on_start(self, state):"  + EOL +
                        "    print 'Starting'"          + EOL +
                        "    print len(state)"          + EOL +
                        "    print state"               + EOL);
        wr.run();
        
        assertEquals(
                "Starting"                  + EOL +
                "1"                         + EOL +
                "{factor: 2}"               + EOL,
                stdoutBuffer.toString());
    }
    
    
    public void testPythonClassActor_OnStartWithArgument_TwoParams() throws Exception {

        ActorConfig test_actor =  wr.actor(PythonClassActor.class)
                .param("factor", 2)
                .param("prompt", "Command")
                .config("pythonClass", "test_actor")
                .config("code",  
                        "class test_actor(object):"     + EOL +
                        "  def on_start(self, state):"  + EOL +
                        "    print 'Starting'"          + EOL +
                        "    print len(state)"          + EOL +
                        "    print state"               + EOL);
        wr.run();
        
        assertEquals(
                "Starting"                          + EOL +
                "2"                                 + EOL +
                "{factor: 2, prompt: Command}"      + EOL,
                stdoutBuffer.toString());
    }
    
   public void testPythonClassActor_OnDataNoArgument() throws Exception {
    
       ActorConfig test_actor =  wr.actor(PythonClassActor.class)
               .config("pythonClass", "test_actor")
               .config("code",  
                       "class test_actor(object):"      + EOL +
                       "  def on_data(self, value):"    + EOL +
                       "    print 'in on_data'"         + EOL +
                       "    print value"                + EOL);

        wr.inputActor(test_actor)
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
    
    public void testPythonClassActor_OnDataWithArgument_NoParams() throws Exception {
                
        ActorConfig test_actor =  wr.actor(PythonClassActor.class)
                .config("pythonClass", "test_actor")
                .config("code",  
                        "class test_actor(object):"             + EOL +
                        "  def on_data(self, value, state):"    + EOL +
                        "    print 'in on_data'"                + EOL +
                        "    print state"                       + EOL +
                        "    print value"                       + EOL);

        wr.inputActor(test_actor)
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
    
    public void testPythonClassActor_OnDataWithArgument_OneParam() throws Exception {
        
        ActorConfig test_actor =  wr.actor(PythonClassActor.class)
                .param("factor", 2)
                .config("pythonClass", "test_actor")
                .config("code",  
                        "class test_actor(object):"             + EOL +
                        "  def on_data(self, value, state):"    + EOL +
                        "    print 'in on_data'"                + EOL +
                        "    print state"                       + EOL +
                        "    print value"                       + EOL);

        wr.inputActor(test_actor)
          .begin()
          .tellWorkflow(1, 2, 3, new EndOfStream())
          .end();
        
        assertEquals(
                "in on_data"        + EOL +
                "{factor: 2}"       + EOL +
                "1"                 + EOL +
                "in on_data"        + EOL +
                "{factor: 2}"       + EOL +
                "2"                 + EOL +
                "in on_data"        + EOL +
                "{factor: 2}"       + EOL +
                "3"                 + EOL,
                stdoutBuffer.toString());    
    }
    
    
    public void testPythonClassActor_OnDataWithArgument_TwoParams() throws Exception {

        ActorConfig test_actor =  wr.actor(PythonClassActor.class)
                .param("factor", 2)
                .param("prompt", "Command")                
                .config("pythonClass", "test_actor")
                .config("code",  
                        "class test_actor(object):"             + EOL +
                        "  def on_data(self, value, state):"    + EOL +
                        "    print 'in on_data'"                + EOL +
                        "    print state"                       + EOL +
                        "    print value"                       + EOL);

        wr.inputActor(test_actor)
          .begin()
          .tellWorkflow(1, 2, 3, new EndOfStream())
          .end();
        
        assertEquals(
                "in on_data"                        + EOL +
                "{factor: 2, prompt: Command}"      + EOL +
                "1"                                 + EOL +
                "in on_data"                        + EOL +
                "{factor: 2, prompt: Command}"      + EOL +
                "2"                                 + EOL +
                "in on_data"                        + EOL +
                "{factor: 2, prompt: Command}"      + EOL +
                "3"                                 + EOL,
                stdoutBuffer.toString());    
    }
}
