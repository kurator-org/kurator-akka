package org.kurator.akka.samples;

import junit.framework.TestCase;

import org.kurator.akka.KuratorAkka;
import org.kurator.akka.WorkflowBuilder;
import org.kurator.akka.YamlFileWorkflowBuilder;

public class TestHamming_Yaml extends TestCase {
    
    static final String EOL = System.getProperty("line.separator");
    static final String RESOURCE_PATH = "classpath:/org/kurator/akka/samples/";
    
    @Override
    public void setUp() {
        KuratorAkka.enableLog4J();
    }
    
    public void testHammingYaml() throws Exception {        
        WorkflowBuilder builder = new YamlFileWorkflowBuilder(RESOURCE_PATH + "hamming.yaml");
        builder.apply("max", 100);
        builder.build();
        builder.run();
    }   
}