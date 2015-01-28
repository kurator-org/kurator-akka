package org.kurator.akka;

import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.YamlStringWorkflowRunner;
import org.kurator.akka.messages.EndOfStream;

import akka.actor.ActorRef;

public class TestYamlStringWorkflowRunner extends KuratorAkkaTestCase {
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }
    
    public void testEmptyWorkflow() throws Exception {
        
        String definition = 
                "components:"                                           + EOL +
                ""                                                      + EOL +
                "- id: EmptyWorkflow"                                   + EOL +
                "  className: org.kurator.akka.WorkflowConfiguration"   + EOL +
                "  singleton: true"                                     + EOL; 
        
        ActorRef workflowRef = new YamlStringWorkflowRunner(definition)
            .outputStream(stderrStream)
            .errorStream(stdoutStream)
            .build();
            
        assertNotNull(workflowRef);
}

    public void testOneActorWorkflow() throws Exception {
        
        String definition = 
                "components:"                                           + EOL +
                ""                                                      + EOL +
                "- id: Repeater"                                        + EOL +
                "  className: org.kurator.akka.ActorBuilder"            + EOL +
                "  singleton: true"                                     + EOL +
                "  properties:"                                         + EOL +
                "    actorClass: org.kurator.akka.actors.Repeater"      + EOL +
                ""                                                      + EOL +
                "- id: OneActorWorkflow"                                + EOL +
                "  className: org.kurator.akka.WorkflowConfiguration"   + EOL +
                "  singleton: true"                                     + EOL +
                "  properties:"                                         + EOL +
                "    actors:"                                           + EOL +
                "    - !ref Repeater"                                   + EOL +
                "    inputActor: !ref Repeater"                         + EOL;
        
        WorkflowRunner wr = new YamlStringWorkflowRunner(definition)
            .outputStream(stderrStream)
            .errorStream(stdoutStream);
            
        wr.build();

        wr.start();        
        wr.tellWorkflow(1);
        wr.tellWorkflow(new EndOfStream());
        wr.await();
    }
    
    public void testTwoActorWorkflow() throws Exception {
        
        String definition = 
                "components:"                                                   + EOL +
                ""                                                              + EOL +
                "- id: Repeater"                                                + EOL +
                "  className: org.kurator.akka.ActorBuilder"                    + EOL +
                "  singleton: true"                                             + EOL +
                "  properties:"                                                 + EOL +
                "    actorClass: org.kurator.akka.actors.Repeater"              + EOL +
                "    listeners:"                                                + EOL +
                "    - !ref Printer"                                            + EOL +
                ""                                                              + EOL +
                "- id: Printer"                                                 + EOL +
                "  className: org.kurator.akka.ActorBuilder"                    + EOL +
                "  singleton: true"                                             + EOL +
                "  properties:"                                                 + EOL +
                "    actorClass: org.kurator.akka.actors.PrintStreamWriter"     + EOL +
                ""                                                              + EOL +
                "- id: TwoActorWorkflow"                                        + EOL +
                "  className: org.kurator.akka.WorkflowConfiguration"           + EOL +
                "  singleton: true"                                             + EOL +
                "  properties:"                                                 + EOL +
                "    actors:"                                                   + EOL +
                "    - !ref Repeater"                                           + EOL +
                "    - !ref Printer"                                            + EOL +
                "    inputActor: !ref Repeater"                                 + EOL;
        
        WorkflowRunner wr = new YamlStringWorkflowRunner(definition)
            .outputStream(stderrStream)
            .errorStream(stdoutStream);
                
        wr.build();

        wr.start();
        
        wr.tellWorkflow(1);
        wr.tellWorkflow(2);
        wr.tellWorkflow(new EndOfStream());
        
        wr.await();
    }

    public void testThreeActorWorkflow() throws Exception {
        
        String definition = 
                "components:"                                                   + EOL +
                ""                                                              + EOL +
                "- id: Repeater"                                                + EOL +
                "  className: org.kurator.akka.ActorBuilder"                    + EOL +
                "  singleton: true"                                             + EOL +
                "  properties:"                                                 + EOL +
                "    actorClass: org.kurator.akka.actors.Repeater"              + EOL +
                "    listeners:"                                                + EOL +
                "    - !ref Filter"                                             + EOL +
                ""                                                              + EOL +
                "- id: Filter"                                                  + EOL +
                "  className: org.kurator.akka.ActorBuilder"                    + EOL +
                "  singleton: true"                                             + EOL +
                "  properties:"                                                 + EOL +
                "    actorClass: org.kurator.akka.actors.Filter"                + EOL +
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
                "    actorClass: org.kurator.akka.actors.PrintStreamWriter"     + EOL +
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
        
        WorkflowRunner wr = new YamlStringWorkflowRunner(definition)
            .outputStream(stderrStream)
            .errorStream(stdoutStream);
        
        wr.build();

        wr.start();
        
        wr.tellWorkflow(1);
        wr.tellWorkflow(2);
        wr.tellWorkflow(3);
        wr.tellWorkflow(4);
        wr.tellWorkflow(5);
        wr.tellWorkflow(6);
        wr.tellWorkflow(4);
        wr.tellWorkflow(3);
        wr.tellWorkflow(new EndOfStream());
        
        wr.await();
    }
    
}
   