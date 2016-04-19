package org.kurator.akka.samples;

import org.kurator.akka.KuratorCLI;
import org.kurator.akka.KuratorAkkaTestCase;

public class TestHelloEmphasized extends KuratorAkkaTestCase {
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }
    
    public void testHello_Defaults() throws Exception {
        String[] args = { "-f", "classpath:/org/kurator/akka/samples/hello_emphasized.yaml" };
        KuratorCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals("Hello World!", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }

    public void testHello_CustomGreeting() throws Exception {
        String[] args = { "-f", "classpath:/org/kurator/akka/samples/hello_emphasized.yaml",
                "-p", "greeting='Goodnight and good luck'"};
        KuratorCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals("Goodnight and good luck!", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }

    public void testHello_CustomEmphasis() throws Exception {
        String[] args = { "-f", "classpath:/org/kurator/akka/samples/hello_emphasized.yaml",
                "-p", "emphasis='?'"};
        KuratorCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals("Hello World?", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }

    public void testHello_CustomGreetingAndEmphasis() throws Exception {
        String[] args = { "-f", "classpath:/org/kurator/akka/samples/hello_emphasized.yaml",
                "-p", "greeting='Goodnight and good luck'",
                "-p", "emphasis='?'"};
        KuratorCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals("Goodnight and good luck?", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }
}
