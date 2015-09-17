package org.kurator.akka;


import static java.util.Arrays.asList;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.kurator.exceptions.KuratorException;
import org.yaml.snakeyaml.Yaml;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/** 
 * Command-line interface for executing workflows using the kurator-akka framework.  
 * 
 * <p>This file is an adaptation of RestFlow.java in the org.restlow 
 * package as of 28Oct2014.  See <i>licenses/restflow_license.txt</i> for 
 * the copyright notice, license, and git repository URL for RestFlow.</p>
 */
public class KuratorAkkaCLI {

    public static void main(String[] args) throws Exception {
        
        int returnValue = -1;
        
        try {
         returnValue = runWorkflowForArgs(args, System.out, System.err);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.exit(returnValue);
    }

    public static int runWorkflowForArgs(String[] args, PrintStream outStream, PrintStream errStream) throws Exception {

        enableLog4J();
        
        OptionParser parser = null;        
        try {
            parser = createOptionsParser();
        }
        catch (OptionException exception) {
            errStream.print("Option definition error: ");
            errStream.println(exception.getMessage());
            return -1;
        }
            
        if (args.length == 0) {
            errStream.println("Error: No workflow definition file was provided");
            errStream.println();
            parser.printHelpOn(errStream);
            return -1;
        }
        
//        InputStream yamlInputStream = null;
        String yamlFilePath = null;
        Map<String,Object> settings = null;
        
        OptionSet options = null;
        
        try {

            options = parser.parse(args);

        } catch (OptionException exception) {
            errStream.println("Error parsing command-line options:");
            errStream.println(exception.getMessage());
            errStream.println();
            parser.printHelpOn(errStream);
            return -1;
        }
        
        
        if (options.has("h")) {
            errStream.println();
            parser.printHelpOn(errStream);
            return 0;            
        }
        
        yamlFilePath = extractYamlFilePathFromOptions(options);
            
        settings = parseParameterSettingsFromOptions(options);
        
//        if (yamlFilePath == null) {
//            yamlInputStream = System.in;
//        }
        
        
        if (yamlFilePath != null) {
            
            WorkflowRunner runner;
            
            try {
                runner = new YamlFileWorkflowRunner(yamlFilePath);                
            } catch(KuratorException ke) {
                errStream.println("Error loading workflow definition from " + yamlFilePath);
                errStream.println(ke.getMessage());
                return -1;
            }
            
            try {
                runner.apply(settings)
                       .outputStream(outStream)
                       .errorStream(errStream);
                
                runner.run();

            } catch(KuratorException ke) {
                errStream.println(ke.getMessage());
                return -1;
            }
        }
        
        return 0;
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
    
    
    private static Map<String, Object> parseParameterSettingsFromOptions(OptionSet options) throws Exception {    
        
        Map<String, Object> settings = new HashMap<String,Object>();

        Yaml yaml = new Yaml();

        for (Object inputOptionObject :  options.valuesOf("p")) {
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
    
    
    private static OptionParser createOptionsParser() throws Exception {

        OptionParser parser = null;
        
        parser = new OptionParser() {{
            
            acceptsAll(asList("f", "file"), "workflow definition file")
                .withRequiredArg()
                .ofType(String.class)
//              .defaultsTo("-")
                .describedAs("definition");

            acceptsAll(asList("p", "parameter"), "key-valued parameter assignment")
                .withRequiredArg()
                .ofType(String.class)
                .describedAs("input parameter")
                .describedAs("key=value");
            
            acceptsAll(asList("h", "help"), "display help");

        }};
            
        return parser;
    }
    
    public static void enableLog4J() {

        if (!new File("log4j.properties").exists()) {
            if (System.getProperty("org.apache.commons.logging.Log") == null) {
                System.setProperty("org.apache.commons.logging.Log",
                        "org.apache.commons.logging.impl.SimpleLog");
            }

            if (System.getProperty("org.apache.commons.logging.simplelog.defaultlog") == null) {
                System.setProperty(
                        "org.apache.commons.logging.simplelog.defaultlog",
                        "error");
            }
        }
    }
}
