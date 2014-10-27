package org.kurator.akka;

import junit.framework.TestCase;

import org.kurator.akka.WorkflowBuilder;
import org.kurator.akka.YamlStringWorkflowBuilder;
import org.kurator.akka.messages.EndOfStream;

import akka.actor.ActorRef;

public class TestYamlFileWorkflowBuilder extends TestCase {
    
    public static final String EOL = System.getProperty("line.separator");
    
    public void testEmptyWorkflow() throws Exception {
        
        WorkflowBuilder builder = new YamlFileWorkflowBuilder("classpath:/org/kurator/akka/test/TestYamlFileWorkflowBuilder/empty_workflow.yaml");
        builder.build();
        ActorRef workflowRef = builder.getWorkflowRef();
        assertNotNull(workflowRef);
}

    public void testOneActorWorkflow() throws Exception {
        WorkflowBuilder builder = new YamlFileWorkflowBuilder("classpath:/org/kurator/akka/test/TestYamlFileWorkflowBuilder/one_actor_workflow.yaml");
        builder.build();
        builder.startWorkflow();
        builder.tellWorkflow(1);
        builder.tellWorkflow(new EndOfStream());
        builder.awaitWorkflow();
    }
    
    public void testTwoActorWorkflow() throws Exception {
        WorkflowBuilder builder = new YamlFileWorkflowBuilder("classpath:/org/kurator/akka/test/TestYamlFileWorkflowBuilder/two_actor_workflow.yaml");
        builder.build();
        builder.startWorkflow();
        builder.tellWorkflow(1);
        builder.tellWorkflow(2);
        builder.tellWorkflow(new EndOfStream());
        builder.awaitWorkflow();
    }

    public void testThreeActorWorkflow() throws Exception {        
        WorkflowBuilder builder = new YamlFileWorkflowBuilder("classpath:/org/kurator/akka/test/TestYamlFileWorkflowBuilder/three_actor_workflow.yaml");
        builder.build();
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
    }
    
}
   