package org.kurator.akka;

import static java.util.Arrays.asList;

import java.io.InputStream;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class KuratorAkkaCLI {

    public static void main(String[] args) throws Exception {
        runWorkflowForCLIArgs(args);
    }

    public static void runWorkflowForCLIArgs(String[] args) throws Exception {
     
        OptionParser parser = createOptionsParser();
        InputStream yamlInputStream = null;
        String yamlFilePath = null;
        
        try {

            OptionSet options = parser.parse(args);

            yamlFilePath = extractYamlFilePathFromOptions(options);
            
            if (yamlFilePath == null) {
                yamlInputStream = System.in;
            }
            
        } catch (OptionException exception) {
            System.err.print("Error parsing command-line optins ");
            System.err.println(exception.getMessage());
            parser.printHelpOn(System.err);
        }
        
        if (yamlFilePath != null) {
            WorkflowBuilder builder = new YamlFileWorkflowBuilder(yamlFilePath);
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
    
    private static OptionParser createOptionsParser() {

        OptionParser parser = null;
        
        try {
            parser = new OptionParser() {{
                
                acceptsAll(asList("f", "definition"))
                    .withRequiredArg().ofType(String.class)
                    .describedAs("workflow definition file or stream")
                    .defaultsTo("-");

                acceptsAll(asList("h", "?"), "display help for Kurator Akka CLI");

            }};
            
        } catch (OptionException exception) {
            System.err.print("Option definition error: ");
            System.err.println(exception.getMessage());
            System.exit(-1);
        }
            
        return parser;
    }
}
