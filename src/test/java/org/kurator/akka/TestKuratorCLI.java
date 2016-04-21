package org.kurator.akka;

import java.io.StringBufferInputStream;

public class TestKuratorCLI extends KuratorAkkaTestCase {

    private static String EXPECTED_HELP_OUTPUT =
        ""                                                                                  + EOL +
        "Option                                  Description                            "   + EOL +
        "------                                  -----------                            "   + EOL +
        "-f, --file <definition>                 workflow definition file               "   + EOL +
        "-h, --help                              display help                           "   + EOL +
        "-l, --loglevel <severity>               minimum severity of log entries shown: "   + EOL + 
        "                                          ALL, TRACE, VALUE, DEBUG, INFO,      "   + EOL +
        "                                          WARN, ERROR, FATAL, OFF              "   + EOL +
        "-p, --parameter <key=value>             key-valued parameter assignment        "   + EOL;            
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }
    
    public void testKuratorAkka_NoArgs() throws Exception {
        String[] args = {};
        KuratorCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals("", stdoutBuffer.toString());
        assertEquals(
            "Error: No workflow definition was provided."   + EOL +
            EXPECTED_HELP_OUTPUT,
            stderrBuffer.toString());
    }

    public void testKuratorAkka_HelpOption() throws Exception {
        String[] args = {"--help"};
        KuratorCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals("", stdoutBuffer.toString());
        assertEquals(
            EXPECTED_HELP_OUTPUT,
            stderrBuffer.toString());
    }

    public void testKuratorAkka_HelpOption_Abbreviation() throws Exception {
        String[] args = {"-h"};
        KuratorCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals("", stdoutBuffer.toString());
        assertEquals(
            EXPECTED_HELP_OUTPUT,
            stderrBuffer.toString());
    }
    
    public void testKuratorAkka_FileOption_NoArgument() throws Exception {
        String[] args = {"-f"};
        KuratorCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals("", stdoutBuffer.toString());
        assertEquals(
            "Error parsing command-line options:"               + EOL +
            "Option ['f', 'file'] requires an argument"         + EOL +
            EXPECTED_HELP_OUTPUT,
            stderrBuffer.toString());
    }

    public void testKuratorAkka_FileOption_MissingFile_Classpath() throws Exception {
        String[] args = {"-f", "classpath:/org/kurator/akka/samples/no_such_file.yaml"};
        KuratorCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals("", stdoutBuffer.toString());
        assertEquals(
                "Error loading workflow definition from file classpath:/org/kurator/akka/samples/no_such_file.yaml"             + EOL +
                "class path resource [org/kurator/akka/samples/no_such_file.yaml]"                                              + EOL +
                "class path resource [org/kurator/akka/samples/no_such_file.yaml] cannot be opened because it does not exist"   + EOL, 
                stderrBuffer.toString());
    }

    public void testKuratorAkka_FileOption_MissingFile_FileSystem() throws Exception {
        String[] args = {"-f", "file:src/main/resources/org/kurator/akka/samples/no_such_file.yaml"};
        KuratorCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals("", stdoutBuffer.toString());
        assertEquals(
                "Error loading workflow definition from file file:src/main/resources/org/kurator/akka/samples/no_such_file.yaml" + EOL +
                "URL [file:src/main/resources/org/kurator/akka/samples/no_such_file.yaml]"                                       + EOL +
                "src/main/resources/org/kurator/akka/samples/no_such_file.yaml (No such file or directory)"                      + EOL, 
                stderrBuffer.toString());
    }

    
    public void testKuratorAkka_FileOption_ClasspathScheme_HammingWorkflow() throws Exception {
        String[] args = {"-f", "classpath:/org/kurator/akka/samples/hamming.yaml"};
        KuratorCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
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
        KuratorCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
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
        KuratorCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
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
        KuratorCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
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
        KuratorCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals(
            "1"         + EOL +
            "2"         + EOL +
            "3"         + EOL +
            "4"         + EOL +
            "5",
            stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }
    
    @SuppressWarnings("deprecation")
    public void testKuratorAkkaCLI_YamlInputStream() throws Exception {
        String yaml = 
                "imports:"                                      + EOL +
                "  - classpath:/org/kurator/akka/types.yaml"    + EOL +
                ""                                              + EOL +
                "components:"                                   + EOL +
                ""                                              + EOL +
                "  - id: SendIntegers"                          + EOL +
                "    type: PythonActor"                         + EOL +
                "    properties:"                               + EOL +
                "      code: |"                                 + EOL +
                "        def on_start():"                       + EOL +
                "          yield 1"                             + EOL +
                "          yield 7"                             + EOL +
                "          yield -31"                           + EOL +
                ""                                              + EOL +
                "  - id: MultiplyByThree"                       + EOL +
                "    type: PythonActor"                         + EOL +
                "    properties:"                               + EOL +
                "      code: |"                                 + EOL +
                "        def on_data(n):"                       + EOL +
                "          return 3 * n"                        + EOL +
                "      listensTo:"                              + EOL +
                "        - !ref SendIntegers"                   + EOL +
                ""                                              + EOL +
                "  - id: PrintProducts"                         + EOL +
                "    type: PythonActor"                         + EOL +
                "    properties:"                               + EOL +
                "      code: |"                                 + EOL +
                "        def on_data(n):"                       + EOL +
                "           print n"                            + EOL +
                "      listensTo:"                              + EOL +
                "        - !ref MultiplyByThree"                + EOL +
                ""                                              + EOL +
                "  - id: MultiplyByTwoWorkflow"                 + EOL +
                "    type: Workflow"                            + EOL +
                "    properties:"                               + EOL +
                "      actors:"                                 + EOL +
                "        - !ref SendIntegers"                   + EOL +                
                "        - !ref MultiplyByThree"                + EOL +
                "        - !ref PrintProducts"                  + EOL +
                "";

        String[] args = {};
        KuratorCLI.runWorkflowForArgs(args, new StringBufferInputStream(yaml), stdoutStream, stderrStream);
        assertEquals("", stderrBuffer.toString());
        assertEquals(
            "3"     + EOL +
            "21"    + EOL +
            "-93"   + EOL,
            stdoutBuffer.toString());
    }

}
