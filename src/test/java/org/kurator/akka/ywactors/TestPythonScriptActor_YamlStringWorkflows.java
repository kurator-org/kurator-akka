package org.kurator.akka.ywactors;

import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.YamlStringWorkflowRunner;
import org.kurator.akka.messages.EndOfStream;

public class TestPythonScriptActor_YamlStringWorkflows extends KuratorAkkaTestCase {

    private YamlStringWorkflowRunner wr;
    
    @Override
    public void setUp() throws Exception {
         super.setUp();
         wr = (YamlStringWorkflowRunner) new YamlStringWorkflowRunner()
             .outputStream(stdoutStream)
             .errorStream(stderrStream);
    }
   
    public void testOneActorWorkflow() throws Exception {
        
        wr.yamlString(
            "imports:"                                                  + EOL +
            "- classpath:/org/kurator/akka/types.yaml"                  + EOL +
            ""                                                          + EOL +
            "components:"                                               + EOL +
            ""                                                          + EOL +
            "- id: OneActorWorkflow"                                    + EOL +
            "  type: Workflow"                                          + EOL +
            "  properties:"                                             + EOL +
            "    actors:"                                               + EOL +
            "    - !ref Printer"                                        + EOL +
            "    inputActor: !ref Printer"                              + EOL +
            ""                                                          + EOL +
            "- id: Printer"                                             + EOL +
            "  type: PythonScriptActor"                                 + EOL +
            "  properties:"                                             + EOL +
            "    input: greeting"                                       + EOL + 
            "    onInit: |"                                             + EOL +
            "      print('Initialize')"                                 + EOL +
            "    onStart: |"                                            + EOL +
            "      print('Start')"                                      + EOL +
            "    onData: |"                                             + EOL +
            "      print('Data: ' + greeting)"                          + EOL +
            "    onEnd: |"                                              + EOL +
            "      print('End')"                                        + EOL +
            ""                                                          + EOL
        );
            
        wr.build();
        wr.init();
        wr.start();        
        wr.tellWorkflow("Hello", new EndOfStream());
        wr.end();
        
        assertEquals("", stderrBuffer.toString());
        assertEquals(
            "Initialize"    + EOL +
            "Start"         + EOL +
            "Data: Hello"   + EOL +
            "End" + EOL, 
            stdoutBuffer.toString());
    }
}
