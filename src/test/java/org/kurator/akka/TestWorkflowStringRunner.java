package org.kurator.akka;

import junit.framework.TestCase;

import org.kurator.akka.WorkflowBuilder;
import org.kurator.akka.YamlStringWorkflowBuilder;
import org.kurator.akka.messages.EndOfStream;

import akka.actor.ActorRef;

public class TestWorkflowStringRunner extends TestCase {
    
    public static final String EOL = System.getProperty("line.separator");
    
    public void testEmptyWorkflow() throws Exception {
        
        String definition = 
                "components:"                                           + EOL +
                ""                                                      + EOL +
                "- id: EmptyWorkflow"                                   + EOL +
                "  className: org.kurator.akka.WorkflowConfiguration"   + EOL +
                "  singleton: true"                                     + EOL; 
        
        WorkflowBuilder builder = new YamlStringWorkflowBuilder(definition);
        builder.build();
        ActorRef workflowRef = builder.getWorkflowRef();
        assertNotNull(workflowRef);
}

    public void testOneActorWorkflow() throws Exception {
        
        String definition = 
                "components:"                                           + EOL +
                ""                                                      + EOL +
                "- id: OneShot"                                         + EOL +
                "  className: org.kurator.akka.ActorBuilder"            + EOL +
                "  singleton: true"                                     + EOL +
                "  properties:"                                         + EOL +
                "    actorClassName: org.kurator.akka.actors.OneShot"   + EOL +
                ""                                                      + EOL +
                "- id: OneActorWorkflow"                                + EOL +
                "  className: org.kurator.akka.WorkflowConfiguration"   + EOL +
                "  singleton: true"                                     + EOL +
                "  properties:"                                         + EOL +
                "    actors:"                                           + EOL +
                "    - !ref OneShot"                                    + EOL +
                "    inputActor: !ref OneShot"                          + EOL;
        
        WorkflowBuilder builder = new YamlStringWorkflowBuilder(definition);
        builder.build();

        builder.startWorkflow();
        
        builder.tellWorkflow(1);
        
        builder.awaitWorkflow();
    }
    
    public void testTwoActorWorkflow() throws Exception {
        
        String definition = 
                "components:"                                                   + EOL +
                ""                                                              + EOL +
                "- id: Repeater"                                                + EOL +
                "  className: org.kurator.akka.ActorBuilder"                    + EOL +
                "  singleton: true"                                             + EOL +
                "  properties:"                                                 + EOL +
                "    actorClassName: org.kurator.akka.actors.Repeater"          + EOL +
                "    listeners:"                                                + EOL +
                "    - !ref Printer"                                            + EOL +
                ""                                                              + EOL +
                "- id: Printer"                                                 + EOL +
                "  className: org.kurator.akka.ActorBuilder"                    + EOL +
                "  singleton: true"                                             + EOL +
                "  properties:"                                                 + EOL +
                "    actorClassName: org.kurator.akka.actors.PrintStreamWriter" + EOL +
                ""                                                              + EOL +
                "- id: TwoActorWorkflow"                                        + EOL +
                "  className: org.kurator.akka.WorkflowConfiguration"           + EOL +
                "  singleton: true"                                             + EOL +
                "  properties:"                                                 + EOL +
                "    actors:"                                                   + EOL +
                "    - !ref Repeater"                                           + EOL +
                "    - !ref Printer"                                            + EOL +
                "    inputActor: !ref Repeater"                                 + EOL;
        
        WorkflowBuilder builder = new YamlStringWorkflowBuilder(definition);
        builder.build();

        builder.startWorkflow();
        
        builder.tellWorkflow(1);
        builder.tellWorkflow(2);
        builder.tellWorkflow(new EndOfStream());
        
        builder.awaitWorkflow();
    }

    public void testThreeActorWorkflow() throws Exception {
        
        String definition = 
                "components:"                                                   + EOL +
                ""                                                              + EOL +
                "- id: Repeater"                                                + EOL +
                "  className: org.kurator.akka.ActorBuilder"                    + EOL +
                "  singleton: true"                                             + EOL +
                "  properties:"                                                 + EOL +
                "    actorClassName: org.kurator.akka.actors.Repeater"          + EOL +
                "    listeners:"                                                + EOL +
                "    - !ref Filter"                                             + EOL +
                ""                                                              + EOL +
                "- id: Filter"                                                  + EOL +
                "  className: org.kurator.akka.ActorBuilder"                    + EOL +
                "  singleton: true"                                             + EOL +
                "  properties:"                                                 + EOL +
                "    actorClassName: org.kurator.akka.actors.Filter"            + EOL +
                "    listeners:"                                                + EOL +
                "    - !ref Printer"                                            + EOL +
                "    parameters:"                                               + EOL +
                "      max: 5"                                                  + EOL +
                "      sendEosOnExceed: true"                                   + EOL +
                ""                                                              + EOL +
                "- id: Printer"                                                 + EOL +
                "  className: org.kurator.akka.ActorBuilder"                    + EOL +
                "  singleton: true"                                             + EOL +
                "  properties:"                                                 + EOL +
                "    actorClassName: org.kurator.akka.actors.PrintStreamWriter" + EOL +
                ""                                                              + EOL +
                "- id: ThreeActorWorkflow"                                      + EOL +
                "  className: org.kurator.akka.WorkflowConfiguration"           + EOL +
                "  singleton: true"                                             + EOL +
                "  properties:"                                                 + EOL +
                "    actors:"                                                   + EOL +
                "    - !ref Repeater"                                           + EOL +
                "    - !ref Filter"                                             + EOL +
                "    - !ref Printer"                                            + EOL +
                "    inputActor: !ref Repeater"                                 + EOL;
        
        WorkflowBuilder builder = new YamlStringWorkflowBuilder(definition);
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
   