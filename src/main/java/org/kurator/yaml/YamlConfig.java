package org.kurator.yaml;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.USE_NATIVE_TYPE_ID;

/**
 * Yaml configuration
 */
public class YamlConfig {

    private List<String> imports;
    private List<YamlComponent> components;

    /**
     * Deserialize yaml config
     *
     * @param yamlStream input
     * @throws IOException
     */
    public static YamlConfig read(InputStream yamlStream) throws IOException {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory().configure(USE_NATIVE_TYPE_ID, false));
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // omit null values from serialization

        return mapper.readValue(yamlStream, YamlConfig.class);

    }

    /**
     * Serialize yaml config to file
     *
     * @param fileName output file
     * @throws IOException
     */
    public static void write(String fileName, YamlConfig config) throws IOException {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory().configure(USE_NATIVE_TYPE_ID, false));
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // omit null values from serialization

        mapper.writeValue(new File(fileName), config);

    }

    /**
     * Serialize as yaml String
     *
     * @return yaml
     */
    public String asYaml() {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory().configure(USE_NATIVE_TYPE_ID, false));
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // omit null values from serialization

        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Serialize as json String
     *
     * @return json
     */
    public String asJson() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // omit null values from serialization

        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getImports() {
        return imports;
    }

    public void setImports(List<String> imports) {
        this.imports = imports;
    }

    public List<YamlComponent> getComponents() {
        return components;
    }

    public void setComponents(List<YamlComponent>  components) {
        this.components = components;
    }

}
