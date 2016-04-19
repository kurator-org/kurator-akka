package org.kurator.akka;

import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.messages.EndOfStream;

public class TestPojoActor extends KuratorAkkaTestCase {
    
    static final String RESOURCE_PATH = "classpath:/org/kurator/akka/TestPojoActor/";
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }
        
    public void testOneActorWorkflow() throws Exception {

        WorkflowRunner wr = new YamlFileWorkflowRunner()
            .yamlFile(RESOURCE_PATH + "one_actor_workflow.yaml");
        
        wr.outputStream(stdoutStream)
               .errorStream(stderrStream);
        
        wr.begin();
        wr.tellWorkflow(1, new EndOfStream());
        wr.end();
        
        assertEquals("1", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }
    
    public void testTwoActorWorkflow() throws Exception {
        
        WorkflowRunner wr = new YamlFileWorkflowRunner()
            .yamlFile(RESOURCE_PATH + "two_actor_workflow.yaml");
        
        wr.outputStream(stdoutStream)
               .errorStream(stderrStream);
        
        wr.begin();
        wr.tellWorkflow(1, 2, new EndOfStream());
        wr.end();
        
        assertEquals("1, 2", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
}

    public void testThreeActorWorkflow() throws Exception { 
        
        WorkflowRunner wr = new YamlFileWorkflowRunner()
            .yamlFile(RESOURCE_PATH + "three_actor_workflow.yaml");
        
        wr.outputStream(stdoutStream)
          .errorStream(stderrStream);
        
        wr.begin();
        wr.tellWorkflow(1, 2, 3, 4, 5, 6, 4, 3, new EndOfStream());
        wr.end();
        
        assertEquals("1, 2, 3, 4, 5", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }
}
   