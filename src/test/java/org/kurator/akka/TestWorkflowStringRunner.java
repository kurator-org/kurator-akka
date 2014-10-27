package org.kurator.akka;

import junit.framework.TestCase;

import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.YamlStringRunner;
import org.kurator.akka.messages.EndOfStream;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class TestWorkflowStringRunner extends TestCase {
    
    public static final String EOL = System.getProperty("line.separator");
    
    public void testEmptyWorkflow() throws Exception {
        
        String definition = 
                "components:"                                           + EOL +
                ""                                                      + EOL +
                "- id: EmptyWorkflow"                                   + EOL +
                "  className: org.kurator.akka.WorkflowConfiguration"   + EOL +
                "  singleton: true"                                     + EOL; 
        
        WorkflowRunner runner = new YamlStringRunner(definition);   
        ActorRef workflowRef = runner.getWorkflowRef();
        assertNotNull(workflowRef);
}

    public void testOneActorWorkflow() throws Exception {
        
        String definition = 
                "components:"                                           + EOL +
                ""                                                      + EOL +
                "- id: OneShot"                                         + EOL +
                "  className: org.kurator.akka.ActorConfiguration"      + EOL +
                "  singleton: true"                                     + EOL +
                "  properties:"                                         + EOL +
                "    actorClassName: org.kurator.akka.actors.OneShot"       + EOL +
                ""                                                      + EOL +
                "- id: OneActorWorkflow"                                + EOL +
                "  className: org.kurator.akka.WorkflowConfiguration"   + EOL +
                "  singleton: true"                                     + EOL +
                "  properties:"                                         + EOL +
                "    actors:"                                           + EOL +
                "    - !ref OneShot"                                    + EOL +
                "    inputActor: !ref OneShot"                          + EOL;
        
        WorkflowRunner runner = new YamlStringRunner(definition);
        ActorRef workflow = runner.getWorkflowRef();
        ActorSystem system = runner.getActorSystem();
        
        runner.start();
        workflow.tell(new Integer(1), system.lookupRoot());
        runner.await();
    }
    
    public void testTwoActorWorkflow() throws Exception {
        
        String definition = 
                "components:"                                                   + EOL +
                ""                                                              + EOL +
                "- id: Repeater"                                                + EOL +
                "  className: org.kurator.akka.ActorConfiguration"              + EOL +
                "  singleton: true"                                             + EOL +
                "  properties:"                                                 + EOL +
                "    actorClassName: org.kurator.akka.actors.Repeater"              + EOL +
                "    listeners:"                                                + EOL +
                "    - !ref Printer"                                            + EOL +
                ""                                                              + EOL +
                "- id: Printer"                                                 + EOL +
                "  className: org.kurator.akka.ActorConfiguration"              + EOL +
                "  singleton: true"                                             + EOL +
                "  properties:"                                                 + EOL +
                "    actorClassName: org.kurator.akka.actors.PrintStreamWriter"     + EOL +
                ""                                                              + EOL +
                "- id: TwoActorWorkflow"                                        + EOL +
                "  className: org.kurator.akka.WorkflowConfiguration"           + EOL +
                "  singleton: true"                                             + EOL +
                "  properties:"                                                 + EOL +
                "    actors:"                                                   + EOL +
                "    - !ref Repeater"                                           + EOL +
                "    - !ref Printer"                                            + EOL +
                "    inputActor: !ref Repeater"                                 + EOL;
        
        WorkflowRunner runner = new YamlStringRunner(definition);
        ActorRef workflow = runner.getWorkflowRef();
        ActorSystem system = runner.getActorSystem();
        
        runner.start();
        workflow.tell(new Integer(1), system.lookupRoot());
        workflow.tell(new Integer(2), system.lookupRoot());
        workflow.tell(new EndOfStream(), system.lookupRoot());
        runner.await();
    }

    public void testThreeActorWorkflow() throws Exception {
        
        String definition = 
                "components:"                                                   + EOL +
                ""                                                              + EOL +
                "- id: Repeater"                                                + EOL +
                "  className: org.kurator.akka.ActorConfiguration"              + EOL +
                "  singleton: true"                                             + EOL +
                "  properties:"                                                 + EOL +
                "    actorClassName: org.kurator.akka.actors.Repeater"          + EOL +
                "    listeners:"                                                + EOL +
                "    - !ref Filter"                                             + EOL +
                ""                                                              + EOL +
                "- id: Filter"                                                  + EOL +
                "  className: org.kurator.akka.ActorConfiguration"              + EOL +
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
                "  className: org.kurator.akka.ActorConfiguration"              + EOL +
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
        
        WorkflowRunner runner = new YamlStringRunner(definition);
        ActorRef workflow = runner.getWorkflowRef();
        ActorSystem system = runner.getActorSystem();
        
        runner.start();
        workflow.tell(new Integer(1), system.lookupRoot());
        workflow.tell(new Integer(2), system.lookupRoot());
        workflow.tell(new Integer(3), system.lookupRoot());
        workflow.tell(new Integer(4), system.lookupRoot());
        workflow.tell(new Integer(5), system.lookupRoot());
        workflow.tell(new Integer(6), system.lookupRoot());
        workflow.tell(new Integer(4), system.lookupRoot());
        workflow.tell(new Integer(3), system.lookupRoot());
        workflow.tell(new EndOfStream(), system.lookupRoot());
        runner.await();
    }
    
}
   