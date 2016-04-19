package org.kurator.akka.samples;

import org.kurator.akka.KuratorCLI;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.YamlFileWorkflowRunner;

public class TestHamming_Yaml extends KuratorAkkaTestCase {

    static final String RESOURCE_PATH = "classpath:/org/kurator/akka/samples/";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        KuratorCLI.enableLog4J();
    }
    
    public void testHammingYaml() throws Exception {        
        WorkflowRunner wr = new YamlFileWorkflowRunner(RESOURCE_PATH + "hamming.yaml");
        wr.apply("max", 100)
          .apply("separator", ", ")
          .outputStream(stdoutStream)
          .run();
        assertEquals( "1, 2, 3, 4, 5, 6, 8, 9, 10, "
                    + "12, 15, 16, 18, 20, 24, 25, "
                    + "27, 30, 32, 36, 40, 45, 48, "
                    + "50, 54, 60, 64, 72, 75, 80, "
                    + "81, 90, 96, 100", stdoutBuffer.toString());
    }   
}