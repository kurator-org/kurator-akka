package org.kurator.akka;

public class TestKuratorAkka extends KuratorAkkaTestCase {

    private static String EXPECTED_HELP_OUTPUT =
        ""                                                                                  + EOL +
        "Option                                  Description                            "   + EOL +
        "------                                  -----------                            "   + EOL +
        "-f, --file <definition>                 workflow definition file               "   + EOL +
        "-h, --help                              display help                           "   + EOL +
        "-p, --parameter <key=value>             key-valued parameter assignment        "   + EOL;            
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }
    
    public void testKuratorAkka_NoArgs() throws Exception {
        String[] args = {};
        KuratorAkkaCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals("", stdoutBuffer.toString());
        assertEquals(
            "Error: No workflow definition file was provided"   + EOL +
            EXPECTED_HELP_OUTPUT,
            stderrBuffer.toString());
    }

    public void testKuratorAkka_HelpOption() throws Exception {
        String[] args = {"--help"};
        KuratorAkkaCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals("", stdoutBuffer.toString());
        assertEquals(
            EXPECTED_HELP_OUTPUT,
            stderrBuffer.toString());
    }

    public void testKuratorAkka_HelpOption_Abbreviation() throws Exception {
        String[] args = {"-h"};
        KuratorAkkaCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals("", stdoutBuffer.toString());
        assertEquals(
            EXPECTED_HELP_OUTPUT,
            stderrBuffer.toString());
    }
    
    public void testKuratorAkka_FileOption_NoArgument() throws Exception {
        String[] args = {"-f"};
        KuratorAkkaCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals("", stdoutBuffer.toString());
        assertEquals(
            "Error parsing command-line options:"               + EOL +
            "Option ['f', 'file'] requires an argument"         + EOL +
            EXPECTED_HELP_OUTPUT,
            stderrBuffer.toString());
    }

    public void testKuratorAkka_FileOption_MissingFile_Classpath() throws Exception {
        String[] args = {"-f", "classpath:/org/kurator/akka/samples/no_such_file.yaml"};
        Exception exception = null;
        try {
            KuratorAkkaCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
        } catch (Exception e) {
            exception = e;
        }
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains(
                "class path resource [org/kurator/akka/samples/no_such_file.yaml] cannot be opened because it does not exist"));
    }

    public void testKuratorAkka_FileOption_MissingFile_FileSystem() throws Exception {
        String[] args = {"-f", "file:src/main/resources/org/kurator/akka/samples/no_such_file.yaml"};
        Exception exception = null;
        try {
            KuratorAkkaCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
        } catch (Exception e) {
            exception = e;
        }
        assertNotNull(exception);
//        assertTrue(exception.getMessage().contains("(The system cannot find the file specified"));
    }

    
    public void testKuratorAkka_FileOption_ClasspathScheme_HammingWorkflow() throws Exception {
        String[] args = {"-f", "classpath:/org/kurator/akka/samples/hamming.yaml"};
        KuratorAkkaCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
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
            stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }

    
    public void testKuratorAkka_FileOption_FileScheme_HammingWorkflow() throws Exception {
        String[] args = {"-f", "file:src/main/resources/org/kurator/akka/samples/hamming.yaml"};
        KuratorAkkaCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
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
            stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }
 
    public void testKuratorAkka_FileOption_ImplicitFileScheme_HammingWorkflow() throws Exception {
        String[] args = {"-f", "src/main/resources/org/kurator/akka/samples/hamming.yaml"};
        KuratorAkkaCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
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
            stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }
    
    public void testKuratorAkka_InputOption_HammingWorkflow() throws Exception {
        String[] args = {"-f", "classpath:/org/kurator/akka/samples/hamming.yaml", "--parameter", "max=5"};
        KuratorAkkaCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals(
            "1"         + EOL +
            "2"         + EOL +
            "3"         + EOL +
            "4"         + EOL +
            "5",
            stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }

    public void testKuratorAkka_InputOption_Abbreviated_HammingWorkflow() throws Exception {
        String[] args = {"-f", "classpath:/org/kurator/akka/samples/hamming.yaml", "-p", "max=5"};
        KuratorAkkaCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals(
            "1"         + EOL +
            "2"         + EOL +
            "3"         + EOL +
            "4"         + EOL +
            "5",
            stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }

}
