package org.kurator.akka.actors;

import org.junit.Test;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.YamlFileWorkflowRunner;
import org.kurator.akka.messages.EndOfStream;

public class TestPythonActorYaml extends KuratorAkkaTestCase {
    
    static final String RESOURCE_PATH = "classpath:/org/kurator/akka/python/";
    
    @Test
    public void testPythonActor_MultiplierWorkflow() throws Exception {

        WorkflowRunner wr = new YamlFileWorkflowRunner(RESOURCE_PATH + "multiplier_wf.yaml");
        wr.outputStream(stdoutStream);
        
        wr.build();
        wr.start();
        wr.tellWorkflow(1);
        wr.tellWorkflow(2);
        wr.tellWorkflow(3);
        wr.tellWorkflow(4);
        wr.tellWorkflow(5);
        wr.tellWorkflow(new EndOfStream());
        wr.await();
        
        assertEquals("2,4,6,8,10", stdoutBuffer.toString());
    }

    @Test
    public void testPythonActor_RampWorkflow() throws Exception {

        WorkflowRunner wr = new YamlFileWorkflowRunner(RESOURCE_PATH + "ramp_wf.yaml");
        wr.outputStream(stdoutStream);
        
        wr.build();
        wr.start();
        wr.tellWorkflow(5);
        wr.tellWorkflow(1);
        wr.tellWorkflow(3);
        wr.tellWorkflow(new EndOfStream());
        wr.await();
        
        assertEquals("1,2,3,4,5,1,1,2,3", stdoutBuffer.toString());
    }
}
