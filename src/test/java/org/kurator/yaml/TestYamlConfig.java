package org.kurator.yaml;

import org.junit.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;

/**
 * Test yaml configuration serialization and deserialization
 */
public class TestYamlConfig {

    @Test
    public void testDeserialize() throws IOException {

        YamlConfig config = YamlConfig.read(
                TestYamlConfig.class.getResourceAsStream("/org/kurator/yaml/dwca_term_values.yaml"));

        for (YamlComponent component : config.getComponents()) {

            if (component instanceof ActorConfig) {
                ActorConfig actorConfig = (ActorConfig) component;

                Assert.assertNotNull(actorConfig.getId());
            } else if (component instanceof WorkflowConfig) {
                WorkflowConfig workflowConfig = (WorkflowConfig) component;

                Assert.assertNotNull(workflowConfig.getId());
            }

        }

    }

    @Test
    public void testSerializeYaml() throws IOException {
        YamlConfig config = YamlConfig.read(
                TestYamlConfig.class.getResourceAsStream("/org/kurator/yaml/dwca_term_values.yaml"));

        System.out.println(config.asYaml());
    }

    @Test
    public void testSerializeJson() throws IOException {
        YamlConfig config = YamlConfig.read(
                TestYamlConfig.class.getResourceAsStream("/org/kurator/yaml/dwca_term_values.yaml"));

        System.out.println(config.asJson());
    }
}
