package org.kurator.akka.samples;

import org.kurator.akka.KuratorAkka;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.WorkflowBuilder;
import org.kurator.akka.YamlFileWorkflowBuilder;

public class TestHamming_Yaml extends KuratorAkkaTestCase {

    static final String RESOURCE_PATH = "classpath:/org/kurator/akka/samples/";

    @Override
    public void setUp() {
        super.setUp();
        KuratorAkka.enableLog4J();
    }
    
    public void testHammingYaml() throws Exception {        
        WorkflowBuilder builder = new YamlFileWorkflowBuilder(RESOURCE_PATH + "hamming.yaml");
        builder.apply("max", 100);
        builder.apply("separator", ", ");
        builder.outputStream(outPrintStream);
        builder.build();
        builder.run();
        assertEquals( "1, 2, 3, 4, 5, 6, 8, 9, 10, "
                    + "12, 15, 16, 18, 20, 24, 25, "
                    + "27, 30, 32, 36, 40, 45, 48, "
                    + "50, 54, 60, 64, 72, 75, 80, "
                    + "81, 90, 96, 100", stdOutputBuffer.toString());
    }   
}