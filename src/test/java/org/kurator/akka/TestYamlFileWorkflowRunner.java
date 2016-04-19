package org.kurator.akka;

import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.messages.EndOfStream;

public class TestYamlFileWorkflowRunner extends KuratorAkkaTestCase {
    
    static final String RESOURCE_PATH = "classpath:/org/kurator/akka/test/TestYamlFileWorkflowBuilder/";
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }
    
    public void testEmptyWorkflow() throws Exception {
        WorkflowRunner wr = new YamlFileWorkflowRunner()
                .yamlFile(RESOURCE_PATH + "empty_workflow.yaml");
        Exception caught = null;
        try {
            wr.build();
        } catch(Exception e) {
            caught = e;
        }
        
        assertNotNull(caught);
        assertEquals("Workflow definition contains no actors.", caught.getMessage());
}

    public void testOneActorWorkflow() throws Exception {

        WorkflowRunner wr = new YamlFileWorkflowRunner()
            .yamlFile(RESOURCE_PATH + "one_actor_workflow.yaml");
        
        wr.outputStream(stdoutStream)
          .errorStream(stderrStream)
          .begin()
          .tellWorkflow(1, new EndOfStream())
          .end();
        
        assertEquals("1", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }
    
    public void testTwoActorWorkflow() throws Exception {
        
        WorkflowRunner wr = new YamlFileWorkflowRunner()
            .yamlFile(RESOURCE_PATH + "two_actor_workflow.yaml");
        
        wr.outputStream(stdoutStream)
         .errorStream(stderrStream)
         .begin()
         .tellWorkflow(1,2, new EndOfStream())
         .end();
        
        assertEquals("1, 2", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
}

    public void testThreeActorWorkflow() throws Exception { 
        
        WorkflowRunner wr = new YamlFileWorkflowRunner()
            .yamlFile(RESOURCE_PATH + "three_actor_workflow.yaml");
        
        wr.outputStream(stdoutStream)
          .errorStream(stderrStream)
          .begin()
          .tellWorkflow(1,2,3,4,5,6,4,3,new EndOfStream())
          .end();
        
        assertEquals("1, 2, 3, 4, 5", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }
    
    public void testThreeActorWorkflowSimplified() throws Exception {        

        WorkflowRunner wr = new YamlFileWorkflowRunner()
            .yamlFile(RESOURCE_PATH + "three_actor_workflow_simplified.yaml");
        
        wr.outputStream(stdoutStream)
          .errorStream(stderrStream)
          .begin()
          .tellWorkflow(1,2,3,4,5,6,4,3,new EndOfStream())
          .end();
        
        assertEquals("1, 2, 3, 4, 5", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }
}
   