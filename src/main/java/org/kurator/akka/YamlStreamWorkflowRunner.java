package org.kurator.akka;

import java.io.InputStream;

public class YamlStreamWorkflowRunner extends YamlWorkflowRunner {
    
    public YamlStreamWorkflowRunner(InputStream yamlStream) throws Exception {
        super();
        yamlBeanReader.loadBeanDefinitions(yamlStream, "-");
        super.loadWorkflowFromSpringContext();
    }
}
