package org.kurator.akka.actors;

import org.kurator.akka.ActorConfig;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.PythonActor;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.messages.EndOfStream;
import org.kurator.akka.messages.Envelope;

public class TestPythonActor_EnvelopedData extends KuratorAkkaTestCase {

    private WorkflowRunner wr;
    
    @Override
    public void setUp() throws Exception {

         super.setUp();

         wr = new WorkflowRunner()
             .outputStream(stdoutStream)
             .errorStream(stderrStream);
    }
    
    public void testPythonActor_OneArgOnData_NoEnvelopes_NoInputMapping() throws Exception {
        
        ActorConfig actor = wr.actor(PythonActor.class)
                              .config("code", 
                                      "def on_data(a):                  " + EOL +
                                      "  print 'Received a = ' + str(a) " + EOL
                                      );
        wr.inputActor(actor)
          .begin()
          .tellWorkflow(1)
          .tellWorkflow(2)
          .tellWorkflow(3) 
          .tellWorkflow(new EndOfStream())
          .end();
        
        assertEquals(
                "Received a = 1"          + EOL +
                "Received a = 2"          + EOL +
                "Received a = 3"          + EOL,   
                stdoutBuffer.toString());
    }

    public void testPythonActor_TwoArgOnData_NoEnvelopes_OptionsWithParams_NoInputMapping() throws Exception {
        
        ActorConfig actor = wr.actor(PythonActor.class)
                              .param("p", 42)
                              .config("code", 
                                      "def on_data(a, options):         " + EOL +
                                      "  p=options['p']                 " + EOL +
                                      "  print 'Received a = ' + str(a) " + EOL +
                                      "  print 'Param    p = ' + str(p) " + EOL
                                      );
        wr.inputActor(actor)
          .begin()
          .tellWorkflow(1)
          .tellWorkflow(2)
          .tellWorkflow(3) 
          .tellWorkflow(new EndOfStream())
          .end();
        
        assertEquals(
                "Received a = 1"    + EOL +
                "Param    p = 42"   + EOL +
                "Received a = 2"    + EOL +
                "Param    p = 42"   + EOL +
                "Received a = 3"    + EOL +   
                "Param    p = 42"   + EOL,
                stdoutBuffer.toString());
    }

    public void testPythonActor_OneArgOnData_NoEnvelopes_OptionsWithParams_NoInputMapping() throws Exception {
        
        ActorConfig actor = wr.actor(PythonActor.class)
                              .param("p", 42)
                              .config("code", 
                                      "def on_data(a):                  " + EOL +
                                      "  print 'Received a = ' + str(a) " + EOL
                                      );
        wr.inputActor(actor)
          .begin()
          .tellWorkflow(1)
          .tellWorkflow(2)
          .tellWorkflow(3) 
          .tellWorkflow(new EndOfStream())
          .end();
        
        assertEquals(
                "Received a = 1"    + EOL +
                "Received a = 2"    + EOL +
                "Received a = 3"    + EOL,  
                stdoutBuffer.toString());
    }

    public void testPythonActor_TwoArgOnData_NoEnvelopes_NoInputMapping() throws Exception {
        
        ActorConfig actor = wr.actor(PythonActor.class)
                              .config("code", 
                                      "def on_data(a, options):         " + EOL +
                                      "  print 'Received a = ' + str(a) " + EOL
                                      );
        wr.inputActor(actor)
          .begin()
          .tellWorkflow(1)
          .tellWorkflow(2)
          .tellWorkflow(3) 
          .tellWorkflow(new EndOfStream())
          .end();
        
        assertEquals(
                "Received a = 1"          + EOL +
                "Received a = 2"          + EOL +
                "Received a = 3"          + EOL,   
                stdoutBuffer.toString());
    }
    
    public void testPythonActor_DefaultOnData_WithEnvelopes_NoInputMapping() throws Exception {
        
        ActorConfig actor = wr.actor(PythonActor.class)
                              .config("code", 
                                      "def on_data(inputs):             " + EOL +
                                      "  a=inputs['a']                  " + EOL +
                                      "  print 'Received a = ' + str(a) " + EOL
                                      );
        wr.inputActor(actor)
          .begin()
          .tellWorkflow(new Envelope("a",1))
          .tellWorkflow(new Envelope("a",2))
          .tellWorkflow(new Envelope("a",3)) 
          .tellWorkflow(new EndOfStream())
          .end();
        
        assertEquals(
                "Received a = 1"          + EOL +
                "Received a = 2"          + EOL +
                "Received a = 3"          + EOL,   
                stdoutBuffer.toString());
    }
    
    public void testPythonActor_DefaultOnData_WithEnvelopes_WithInputMapping() throws Exception {
        
        ActorConfig actor = wr.actor(PythonActor.class)
                              .input("a","x")
                              .config("code", 
                                      "def on_data(inputs):             " + EOL +
                                      "  x=inputs['x']                  " + EOL +
                                      "  print 'Received x = ' + str(x) " + EOL
                                      );        
        wr.inputActor(actor)
          .begin()
          .tellWorkflow(new Envelope("a",1))
          .tellWorkflow(new Envelope("a",2))
          .tellWorkflow(new Envelope("a",3)) 
          .tellWorkflow(new EndOfStream())
          .end();
        
        assertEquals(
                "Received x = 1"          + EOL +
                "Received x = 2"          + EOL +
                "Received x = 3"          + EOL,   
                stdoutBuffer.toString());
    }
    
    public void testPythonActor_DefaultOnData_WithEnvelopes_OptionsNoParams_NoInputMapping() throws Exception {
        
        ActorConfig actor = wr.actor(PythonActor.class)
                              .config("code",
                                      "def on_data(inputs, options):    " + EOL +
                                      "  a=inputs['a']                  " + EOL +
                                      "  print 'Received a = ' + str(a) " + EOL
                                      );
        wr.inputActor(actor)
          .begin()
          .tellWorkflow(new Envelope("a",1))
          .tellWorkflow(new Envelope("a",2))
          .tellWorkflow(new Envelope("a",3))
          .tellWorkflow(new EndOfStream())
          .end();
        
        assertEquals(
                "Received a = 1"          + EOL +
                "Received a = 2"          + EOL +
                "Received a = 3"          + EOL,   
                stdoutBuffer.toString());
    }

    public void testPythonActor_TwoArgOnData_WithEnvelopes_OptionsWithParams_NoInputMapping() throws Exception {
        
        ActorConfig actor = wr.actor(PythonActor.class)
                              .param("p", 42)
                              .config("code",
                                      "def on_data(inputs, options):    " + EOL +
                                      "  a=inputs['a']                  " + EOL +
                                      "  p=options['p']                 " + EOL +
                                      "  print 'Received a = ' + str(a) " + EOL +
                                      "  print 'Param    p = ' + str(p) " + EOL
                                      );
        wr.inputActor(actor)
          .begin()
          .tellWorkflow(new Envelope("a",1))
          .tellWorkflow(new Envelope("a",2))
          .tellWorkflow(new Envelope("a",3))
          .tellWorkflow(new EndOfStream())
          .end();
        
        assertEquals(
                "Received a = 1"          + EOL +
                "Param    p = 42"         + EOL +
                "Received a = 2"          + EOL +
                "Param    p = 42"         + EOL +
                "Received a = 3"          + EOL +
                "Param    p = 42"         + EOL,
                stdoutBuffer.toString());
    }

}
