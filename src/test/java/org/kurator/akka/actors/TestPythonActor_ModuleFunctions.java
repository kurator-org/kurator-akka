package org.kurator.akka.actors;

import org.junit.Test;
import org.kurator.akka.KuratorAkkaTestCase;

public class TestPythonActor_ModuleFunctions extends KuratorAkkaTestCase {
    
    static final String RESOURCE_PATH = "file:packages/kurator_akka/math/";
    
    @Test
    public void testPythonActor_MultiplierWorkflow() throws Exception {

//        WorkflowRunner wr = new YamlFileWorkflowRunner(RESOURCE_PATH + "multiplier_wf.yaml");
//        wr.outputStream(stdoutStream);
//        
//        wr.begin();
//        wr.end();
//        
//        assertEquals("2,4,6,8,10", stdoutBuffer.toString());
    }

 
}
