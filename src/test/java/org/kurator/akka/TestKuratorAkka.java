package org.kurator.akka;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.Test;

import junit.framework.TestCase;

public class TestKuratorAkka extends TestCase {

    static final String EOL = System.getProperty("line.separator");

    private OutputStream errOutputBuffer;
    private OutputStream stdOutputBuffer;
    private PrintStream errPrintStream;
    private PrintStream outPrintStream;

    private static String EXPECTED_HELP_OUTPUT =
        ""                                                                                  + EOL +
        "Option                                  Description                            "   + EOL +
        "------                                  -----------                            "   + EOL +
        "-f, --file <definition>                 workflow definition file               "   + EOL +
        "-h, --help                              display help                           "   + EOL +
        "-i, --input <key=value>                 key-valued inputs                      "   + EOL;            
    
    @Override
    public void setUp() {
        stdOutputBuffer = new ByteArrayOutputStream();
        outPrintStream = new PrintStream(stdOutputBuffer);
        errOutputBuffer = new ByteArrayOutputStream();
        errPrintStream = new PrintStream(errOutputBuffer);
    }
    
    @Test
    public void testKuratorAkka_NoArgs() throws Exception {
        String[] args = {};
        KuratorAkka.runWorkflowForArgs(args, null, errPrintStream);
        assertEquals(
            "Error: No workflow definition file was provided"   + EOL +
            EXPECTED_HELP_OUTPUT,
            errOutputBuffer.toString());
    }

    @Test
    public void testKuratorAkka_HelpOption() throws Exception {
        String[] args = {"--help"};
        KuratorAkka.runWorkflowForArgs(args, null, errPrintStream);
        assertEquals(
            EXPECTED_HELP_OUTPUT,
            errOutputBuffer.toString());
    }

    @Test
    public void testKuratorAkka_HelpOption_Abbreviation() throws Exception {
        String[] args = {"-h"};
        KuratorAkka.runWorkflowForArgs(args, null, errPrintStream);
        assertEquals(
            EXPECTED_HELP_OUTPUT,
            errOutputBuffer.toString());
    }
    
    @Test
    public void testKuratorAkka_FileOption_NoArgument() throws Exception {
        String[] args = {"-f"};
        KuratorAkka.runWorkflowForArgs(args, null, errPrintStream);
        assertEquals(
            "Error parsing command-line options:"               + EOL +
            "Option ['f', 'file'] requires an argument"         + EOL +
            EXPECTED_HELP_OUTPUT,
            errOutputBuffer.toString());
    }
    
    @Test
    public void testKuratorAkka_FileOption_ClasspathScheme_HammingWorkflow() throws Exception {
        String[] args = {"-f", "classpath:/org/kurator/akka/samples/hamming.yaml"};
        KuratorAkka.runWorkflowForArgs(args, outPrintStream, errPrintStream);
        assertEquals(
            "1"         + EOL +
            "2"         + EOL +
            "3"         + EOL +
            "4"         + EOL +
            "5"         + EOL +
            "6"         + EOL +
            "8"         + EOL +
            "9"         + EOL +
            "10"        + EOL +
            "12"        + EOL +
            "15"        + EOL +
            "16"        + EOL +
            "18"        + EOL +
            "20"        + EOL +
            "24"        + EOL +
            "25"        + EOL +
            "27"        + EOL +
            "30",
            stdOutputBuffer.toString());
    }

    
    @Test
    public void testKuratorAkka_FileOption_FileScheme_HammingWorkflow() throws Exception {
        String[] args = {"-f", "file:src/main/resources/org/kurator/akka/samples/hamming.yaml"};
        KuratorAkka.runWorkflowForArgs(args, outPrintStream, errPrintStream);
        assertEquals(
            "1"         + EOL +
            "2"         + EOL +
            "3"         + EOL +
            "4"         + EOL +
            "5"         + EOL +
            "6"         + EOL +
            "8"         + EOL +
            "9"         + EOL +
            "10"        + EOL +
            "12"        + EOL +
            "15"        + EOL +
            "16"        + EOL +
            "18"        + EOL +
            "20"        + EOL +
            "24"        + EOL +
            "25"        + EOL +
            "27"        + EOL +
            "30",
            stdOutputBuffer.toString());
    }
 
    @Test
    public void testKuratorAkka_FileOption_ImplicitFileScheme_HammingWorkflow() throws Exception {
        String[] args = {"-f", "src/main/resources/org/kurator/akka/samples/hamming.yaml"};
        KuratorAkka.runWorkflowForArgs(args, outPrintStream, errPrintStream);
        assertEquals(
            "1"         + EOL +
            "2"         + EOL +
            "3"         + EOL +
            "4"         + EOL +
            "5"         + EOL +
            "6"         + EOL +
            "8"         + EOL +
            "9"         + EOL +
            "10"        + EOL +
            "12"        + EOL +
            "15"        + EOL +
            "16"        + EOL +
            "18"        + EOL +
            "20"        + EOL +
            "24"        + EOL +
            "25"        + EOL +
            "27"        + EOL +
            "30",
            stdOutputBuffer.toString());
    }
    
    
    @Test
    public void testKuratorAkka_InputOption_HammingWorkflow() throws Exception {
        String[] args = {"-f", "classpath:/org/kurator/akka/samples/hamming.yaml", "--input", "max=5"};
        KuratorAkka.runWorkflowForArgs(args, outPrintStream, errPrintStream);
        assertEquals(
            "1"         + EOL +
            "2"         + EOL +
            "3"         + EOL +
            "4"         + EOL +
            "5",
            stdOutputBuffer.toString());
    }

    @Test
    public void testKuratorAkka_InputOption_Abbreviated_HammingWorkflow() throws Exception {
        String[] args = {"-f", "classpath:/org/kurator/akka/samples/hamming.yaml", "-i", "max=5"};
        KuratorAkka.runWorkflowForArgs(args, outPrintStream, errPrintStream);
        assertEquals(
            "1"         + EOL +
            "2"         + EOL +
            "3"         + EOL +
            "4"         + EOL +
            "5",
            stdOutputBuffer.toString());
    }

}
