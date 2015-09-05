package org.kurator.akka.actors;

import org.junit.Test;
import org.kurator.akka.ActorConfig;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.PythonActor;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.messages.EndOfStream;

public class TestPythonActorInlined extends KuratorAkkaTestCase {

    private WorkflowRunner wr;
    
    @Override
    public void setUp() throws Exception {

         super.setUp();

         wr = new WorkflowRunner()
             .outputStream(stdoutStream)
             .errorStream(stderrStream);
    }

    @Test
    public void testPythonActor_Multiplier() throws Exception {

        ActorConfig repeater = wr.actor(PythonActor.class)
                .config("onData", "repeat")
                .config("code", 
                        
                        "def repeat(n):"        + EOL +
                        "  return n"            + EOL);
        
        ActorConfig multiplier =  wr.actor(PythonActor.class)
                .listensTo(repeater)
                .param("factor", 2)
                .config("onData", "multiply")
                .config("code", 
                        
                        "def multiply(n):"      + EOL +
                        "  return factor*n"     + EOL);

        ActorConfig printer = wr.actor(PythonActor.class)
                .listensTo(multiplier)
                .config("onData", "printdata")
                .config("code", 
                
                        "def printdata(n):"     + EOL +
                        "  print n"             + EOL);
               
        wr.inputActor(repeater);

        wr.build();
        wr.start();
        wr.tellWorkflow(1);
        wr.tellWorkflow(2);
        wr.tellWorkflow(3);
        wr.tellWorkflow(4);
        wr.tellWorkflow(5);
        wr.tellWorkflow(new EndOfStream());
        wr.await();
        
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
                .config("onData", "repeat")
                .config("code", 
                        
                        "def repeat(n):"        + EOL +
                        "  return n"            + EOL);
        
        ActorConfig double_and_triple =  wr.actor(PythonActor.class)
                .listensTo(repeater)
                .param("factor", 2)
                .config("onData", "get_double_and_triple")
                .config("code", 
                        
                        "def get_double_and_triple(n):"      + EOL +
                        "  return n*2, n*3"                  + EOL);

        ActorConfig printer = wr.actor(PythonActor.class)
                .listensTo(double_and_triple)
                .config("onData", "printdata")
                .config("code", 
                
                        "def printdata(n):"     + EOL +
                        "  print n"             + EOL);
               
        wr.inputActor(repeater);

        wr.build();
        wr.start();
        wr.tellWorkflow(1);
        wr.tellWorkflow(2);
        wr.tellWorkflow(3);
        wr.tellWorkflow(4);
        wr.tellWorkflow(5);
        wr.tellWorkflow(new EndOfStream());
        wr.await();
        
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
                .config("onData", "repeat")
                .config("code", 
                        
                        "def repeat(n):"                    + EOL +
                        "  return n"                        + EOL);
        
        ActorConfig double_and_triple =  wr.actor(PythonActor.class)
                .listensTo(repeater)
                .param("factor", 2)
                .config("onData", "get_int_and_string")
                .config("code", 
                        
                        "def get_int_and_string(n):"        + EOL +
                        "  return n, str(n)"                + EOL);

        ActorConfig printer = wr.actor(PythonActor.class)
                .listensTo(double_and_triple)
                .config("onData", "printdata")
                .config("code", 
                
                        "def printdata(n):"                 + EOL +
                        "  print n"                         + EOL);
               
        wr.inputActor(repeater);

        wr.build();
        wr.start();
        wr.tellWorkflow(1);
        wr.tellWorkflow(2);
        wr.tellWorkflow(3);
        wr.tellWorkflow(4);
        wr.tellWorkflow(5);
        wr.tellWorkflow(new EndOfStream());
        wr.await();
        
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
                .config("onStart", "output_tuples")
                .config("code", 
                        
                        "def output_tuples():"                          + EOL +
                        "  yield u'eight_specimen_records', 'csv', 2"   + EOL +
                        "  yield u'eight_specimen_records', u'csv', 2"  + EOL +
                        "  yield 459, u'csv', 2"                        + EOL);

        ActorConfig printer = wr.actor(PythonActor.class)
                .listensTo(double_and_triple)
                .config("onData", "printdata")
                .config("code", 
                
                        "def printdata(n):"                 + EOL +
                        "  print n"                         + EOL);
        wr.build();
        wr.start();
        wr.await();
        
        assertEquals(
                "(u'eight_specimen_records', 'csv', 2)"     + EOL +
                "(u'eight_specimen_records', u'csv', 2)"    + EOL +
                "(459, u'csv', 2)"                          + EOL,   
                stdoutBuffer.toString());
    }    
}
