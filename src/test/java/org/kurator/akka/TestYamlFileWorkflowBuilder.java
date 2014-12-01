package org.kurator.akka;

import org.kurator.akka.WorkflowBuilder;
import org.kurator.akka.messages.EndOfStream;

import akka.actor.ActorRef;

public class TestYamlFileWorkflowBuilder extends KuratorAkkaTestCase {
    
    static final String RESOURCE_PATH = "classpath:/org/kurator/akka/test/TestYamlFileWorkflowBuilder/";
    
    @Override
    public void setUp() {
        super.setUp();
    }
    
    public void testEmptyWorkflow() throws Exception {
        WorkflowBuilder builder = new YamlFileWorkflowBuilder(RESOURCE_PATH + "empty_workflow.yaml");
        builder.build();
        ActorRef workflowRef = builder.getWorkflowRef();
        assertNotNull(workflowRef);
}

    public void testOneActorWorkflow() throws Exception {

        WorkflowBuilder builder = new YamlFileWorkflowBuilder(RESOURCE_PATH + "one_actor_workflow.yaml");
        
        builder.outputStream(stdoutStream)
               .errorStream(stderrStream)
               .build();
        
        builder.startWorkflow();
        builder.tellWorkflow(1);
        builder.tellWorkflow(new EndOfStream());
        builder.awaitWorkflow();
        
        assertEquals("1", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }
    
    public void testTwoActorWorkflow() throws Exception {
        
        WorkflowBuilder builder = new YamlFileWorkflowBuilder(RESOURCE_PATH + "two_actor_workflow.yaml");
        
        builder.outputStream(stdoutStream)
               .errorStream(stderrStream)
               .build();
        
        builder.startWorkflow();
        builder.tellWorkflow(1);
        builder.tellWorkflow(2);
        builder.tellWorkflow(new EndOfStream());
        builder.awaitWorkflow();
        
        assertEquals("1, 2", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
}

    public void testThreeActorWorkflow() throws Exception { 
        
        WorkflowBuilder builder = new YamlFileWorkflowBuilder(RESOURCE_PATH + "three_actor_workflow.yaml");
        
        builder.outputStream(stdoutStream)
               .errorStream(stderrStream)
               .build();
        
        builder.startWorkflow();
        builder.tellWorkflow(1);
        builder.tellWorkflow(2);
        builder.tellWorkflow(3);
        builder.tellWorkflow(4);
        builder.tellWorkflow(5);
        builder.tellWorkflow(6);
        builder.tellWorkflow(4);
        builder.tellWorkflow(3);
        builder.tellWorkflow(new EndOfStream());
        builder.awaitWorkflow();
        
        assertEquals("1, 2, 3, 4, 5", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }
    
    public void testThreeActorWorkflowSimplified() throws Exception {        

        WorkflowBuilder builder = new YamlFileWorkflowBuilder(RESOURCE_PATH + "three_actor_workflow_simplified.yaml");
        
        builder.outputStream(stdoutStream)
                .errorStream(stderrStream)
                .build();
                
        builder.startWorkflow();
        builder.tellWorkflow(1);
        builder.tellWorkflow(2);
        builder.tellWorkflow(3);
        builder.tellWorkflow(4);
        builder.tellWorkflow(5);
        builder.tellWorkflow(6);
        builder.tellWorkflow(4);
        builder.tellWorkflow(3);
        builder.tellWorkflow(new EndOfStream());
        builder.awaitWorkflow();
        
        assertEquals("1, 2, 3, 4, 5", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }
}
   