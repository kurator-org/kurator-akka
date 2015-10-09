package org.kurator.akka;

import java.io.InputStream;
import java.io.StringBufferInputStream;

@SuppressWarnings("deprecation")
public class YamlStringWorkflowRunner extends YamlWorkflowRunner {
    
    public YamlStringWorkflowRunner(String yamlString) throws Exception {
        super();
        InputStream stream = new StringBufferInputStream(yamlString);
        yamlBeanReader.loadBeanDefinitions(stream, "-");
        super.loadWorkflowFromSpringContext();
    }
}
