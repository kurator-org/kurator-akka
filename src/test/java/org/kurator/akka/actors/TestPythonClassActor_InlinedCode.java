package org.kurator.akka.actors;

import org.kurator.akka.ActorConfig;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.PythonActor;
import org.kurator.akka.PythonClassActor;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.messages.EndOfStream;

public class TestPythonClassActor_InlinedCode extends KuratorAkkaTestCase {

    private WorkflowRunner wr;
    
    @Override
    public void setUp() throws Exception {

         super.setUp();

         wr = new WorkflowRunner()
             .outputStream(stdoutStream)
             .errorStream(stderrStream);
    }
    
  public void testPythonClassActor_NoOnData() throws Exception {
        
        ActorConfig actor = 
        wr.actor(PythonClassActor.class)
          .config("pythonClass", "actor")
          .config("code",
                  "class actor(object):"                                            + EOL +
                  "  a=3"   );
        wr.build()
          .init();
        
    }
  
    public void testPythonClassActor_DefaultOnData() throws Exception {
        
        ActorConfig actor = 
        wr.actor(PythonClassActor.class)
          .config("pythonClass", "actor")
          .config("code",
                  "class actor(object):"                                            + EOL +
                  "  def on_data(self, n):  print 'Received data: ' + str(n)"   );
               
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
    
    public void testPythonClassActor_CustomOnData() throws Exception {
        
        ActorConfig actor = 
        wr.actor(PythonClassActor.class)
          .config("pythonClass", "actor")
          .config("onData", "echo")
          .config("code",
                  "class actor(object):"                                    + EOL +
                  "  def echo(self, n):  print 'Received data: ' + str(n)"  );
               
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
                "Error initializing workflow"   +  EOL +
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
                "Error initializing workflow"  +  EOL +
                "Error importing class 'my_module.my_submodule.my_actor': No module named my_module",
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
                "Error initializing workflow"   +  EOL +
                "Error binding to onData method 'echo': AttributeError(\"'my_actor' object has no attribute 'echo'\",)", 
                caught.getMessage());
    }


    public void testPythonClassActor_DefaultOnDataIsNotMethod() throws Exception {
        
        ActorConfig actor = 
        wr.actor(PythonClassActor.class)
          .config("pythonClass", "actor")
          .config("code",
                  "class actor(object):"                                            + EOL +
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
                "Error initializing workflow"   +  EOL +
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
                "Error initializing workflow"   +  EOL +
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
    
    public void testPythonActor_MixedTuples() throws Exception {
        
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
