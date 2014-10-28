package org.kurator.akka;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class KuratorAkka {

    public static void main(String[] args) throws Exception {
        runWorkflowForArgs(args);
    }

    public static void runWorkflowForArgs(String[] args) throws Exception {
     
        enableLog4J();
        
        OptionParser parser = createOptionsParser();
        InputStream yamlInputStream = null;
        String yamlFilePath = null;
        Map<String,Object> settings = null;
        
        try {

            OptionSet options = parser.parse(args);

            yamlFilePath = extractYamlFilePathFromOptions(options);
            
            settings = parseParameterSettingsFromOptions(options);
            
            if (yamlFilePath == null) {
                yamlInputStream = System.in;
            }
            
        } catch (OptionException exception) {
            System.err.print("Error parsing command-line options ");
            System.err.println(exception.getMessage());
            parser.printHelpOn(System.err);
        }
        
        if (yamlFilePath != null) {
            WorkflowBuilder builder = new YamlFileWorkflowBuilder(yamlFilePath);
            
            try {
                builder.apply(settings);
            } catch(Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
            
            builder.build();
            builder.run();
        }
        
    }

    private static String extractYamlFilePathFromOptions(OptionSet options) {
        
        String yamlFilePath = null;
        String workflowDefinitionArg = null;

        // if there is only one non-option argument assume this is the path the workflow definition
        if (options.nonOptionArguments().size() == 1) {
            workflowDefinitionArg = options.nonOptionArguments().get(0);
        } else if (options.hasArgument("f") && (!options.valueOf("f").equals("-"))) {
            workflowDefinitionArg = (String) options.valueOf("f");
        }

        // assume workflow definition is a file resource if not otherwise specified
        if (workflowDefinitionArg != null) {
            if (workflowDefinitionArg.contains(":")) {
                yamlFilePath = workflowDefinitionArg;
            } else {
                yamlFilePath = "file:" + workflowDefinitionArg;
            }
        }
        
        return yamlFilePath;
    }
    
    
    static Map<String, Object> parseParameterSettingsFromOptions(OptionSet options) throws Exception {    
        
        Map<String, Object> settings = new HashMap<String,Object>();

        Yaml yaml = new Yaml();

        for (Object inputOptionObject :  options.valuesOf("i")) {
            String inputOption = (String) inputOptionObject;
            int indexOfFirstEquals = inputOption.indexOf("=");
            if (indexOfFirstEquals == -1) {
                throw new Exception(
                    "Input options should be key-value pairs separated by equal signs. Example: -i count=12");
            } else {
                String inputName = inputOption.substring(0, indexOfFirstEquals);
                String inputValueString = inputOption.substring(indexOfFirstEquals + 1);
                Object inputValue = yaml.load(inputValueString);
                settings.put(inputName, inputValue);
            }
        }
        
        return settings;
    }
    
    
    private static OptionParser createOptionsParser() {

        OptionParser parser = null;
        
        try {
            parser = new OptionParser() {{
                
                acceptsAll(asList("f", "definition"))
                    .withRequiredArg().ofType(String.class)
                    .describedAs("workflow definition file or stream")
                    .defaultsTo("-");

                acceptsAll(asList("i", "input"), "key-valued inputs")
                    .withRequiredArg().describedAs("input parameters")
                    .ofType(String.class).describedAs("key=value");
                
                acceptsAll(asList("h", "?"), "display help for Kurator Akka CLI");

            }};
            
        } catch (OptionException exception) {
            System.err.print("Option definition error: ");
            System.err.println(exception.getMessage());
            System.exit(-1);
        }
            
        return parser;
    }
    
    public static void enableLog4J() {

        if (!new File("log4j.properties").exists()) {
            if (System.getProperty("org.apache.commons.logging.Log") == null) {
                System.setProperty("org.apache.commons.logging.Log",
                        "org.apache.commons.logging.impl.SimpleLog");
            }

            if (System
                    .getProperty("org.apache.commons.logging.simplelog.defaultlog") == null) {
                System.setProperty(
                        "org.apache.commons.logging.simplelog.defaultlog",
                        "error");
            }
        }
    }
}
