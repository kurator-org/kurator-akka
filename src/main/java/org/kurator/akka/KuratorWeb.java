package org.kurator.akka;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.kurator.akka.data.WorkflowProduct;
import org.kurator.exceptions.KuratorException;
import org.kurator.log.DefaultLogger;
import org.kurator.log.LogLevel;
import org.kurator.log.Logger;
import org.kurator.log.SilentLogger;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Command-line interface for executing workflows from kurator-web via json.
 *
 * @author lowery
 *
 */
public class KuratorWeb {
    private static ByteArrayOutputStream stdOutStream = new ByteArrayOutputStream();
    private static ByteArrayOutputStream stdErrStream = new ByteArrayOutputStream();

    private static ByteArrayOutputStream sysOutStream = new ByteArrayOutputStream();

    // keeps track of any internal errors encountered in actor internals
    private static boolean hasInternalErrors = false;

    private static final int SUCCESS = 0;    // exit code for successful run
    private static final int ERRORS = 1;     // exit code for a completed run with errors
    private static final int FAILURE = 2;    // exit code for failure to run to completion

    /**
     * Reads json input from the console via stdin and output a json array via stdout
     * containing a list of workflow artifact metadata.
     *
     */
    public static void main(String[] args) {

        // TODO: if input stream is empty or json parse exception occurs, print usage

        try {
            // Get json input as String from stdin
            StringBuilder input = new StringBuilder();

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                input.append(scanner.nextLine());
            }

            // Parse input json into workflow runner args
            JSONParser parser = new JSONParser();
            Map<String, Object> jsonIn = (Map) parser.parse(input.toString());

            Map<String, Object> settings = (Map) jsonIn.get("parameters");
            Map<String, Object> config = (Map) jsonIn.get("config");

            String yamlFile = (String) jsonIn.get("yaml");
            String loglevel = (String) jsonIn.get("loglevel");

            // Run the workflow
            List<WorkflowProduct> artifacts = runWorkflow(yamlFile, config, settings, loglevel);

            // Create output json array containing the workflow artifacts
            JSONArray jsonOut = new JSONArray();

            for (WorkflowProduct product : artifacts) {
                JSONObject artifactJson = new JSONObject();

                artifactJson.put("name", product.label);
                artifactJson.put("type", product.type);
                artifactJson.put("path", product.value);

                jsonOut.add(artifactJson);
            }

            // Output json array to stdout
            System.out.println(jsonOut.toJSONString());

            // Errors were encountered during a completed run
            if (hasInternalErrors) {
                System.exit(ERRORS);
            }
        } catch (Exception e) {
            // Severe error caused workflow to run to completion
            e.printStackTrace();
            System.exit(FAILURE);
        }

        // Ran successfully with no errors
        System.exit(SUCCESS);
    }

    /**
     * Helper method runs the yaml workflow and returns the list of products
     *
     * @param yamlFile workflow yaml filename
     * @param config runner config
     * @param settings workflow parameters
     * @param loglevel logging statement level
     *
     * @return metadata about the workflow artifacts produced
     *
     */
    private static List<WorkflowProduct> runWorkflow(String yamlFile, Map<String, Object> config, Map<String, Object> settings,
                                                    String loglevel) throws Exception {
        // Temporarily redirect System.out to avoid picking up any println statements
        PrintStream origOut = System.out;
        System.setOut(new PrintStream(sysOutStream));

        // Create the workflow runner and load the yaml file as an input stream
        YamlStreamWorkflowRunner runner;
        try {
            runner = new YamlStreamWorkflowRunner();
            FileInputStream yamlStream = new FileInputStream(yamlFile);

            runner.yamlStream(yamlStream);
        } catch (IOException e) {
            throw new KuratorException("Error loading workflow definition from file " + yamlFile, e);
        }

        // Create and configure the logger
        Logger cliLogger;
        if (loglevel != null) {
            cliLogger = new DefaultLogger();
            String logLevelOption = loglevel;
            LogLevel logLevel = LogLevel.toLogLevel(logLevelOption);
            cliLogger.setLevel(logLevel);
            cliLogger.setSource("CLI");
        } else {
            cliLogger = new SilentLogger();
        }

        Logger runnerLogger = cliLogger.createChild();
        runner.logger(runnerLogger);

        // Run the workflow
        runner.configure(config)
                .apply(settings)
                .outputStream(new PrintStream(stdOutStream))
                .errorStream(new PrintStream(stdErrStream))
                .run();

        // Construct a logfile from stdout, stderr, and any Java println statements that occurred during the run
        processOutput();

        // Stop redirecting System.out by setting back to default stream
        System.setOut(origOut);

        return runner.getWorkflowProducts();
    }

    /**
     * Helper method proccesses the output streams to produce a log on stderr
     *
     */
    private static void processOutput() {
        String errors = new String(stdErrStream.toByteArray());
        String output = new String(stdOutStream.toByteArray());
        String logging =  new String(sysOutStream.toByteArray());

        StringBuilder log = new StringBuilder();

        log.append(errors);

        log.append(output);
        log.append(logging);

        // Using stderr and reserving stdout for json output
        System.err.println(log.toString());

        // Check the error stream, if it has output then assume workflow encountered errors
        if (errors.trim().length() > 0) {
            hasInternalErrors = true;
        }
    }
}
