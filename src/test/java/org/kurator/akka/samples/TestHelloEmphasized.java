package org.kurator.akka.samples;

import org.kurator.akka.KuratorAkka;
import org.kurator.akka.KuratorAkkaTestCase;

public class TestHelloEmphasized extends KuratorAkkaTestCase {
    
    @Override
    public void setUp() {
        super.setUp();
    }
    
    public void testHello_Defaults() throws Exception {
        String[] args = { "-f", "classpath:/org/kurator/akka/samples/hello_emphasized.yaml" };
        KuratorAkka.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals("Hello World!", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }

    public void testHello_CustomGreeting() throws Exception {
        String[] args = { "-f", "classpath:/org/kurator/akka/samples/hello_emphasized.yaml",
                "-p", "greeting='Goodnight and good luck'"};
        KuratorAkka.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals("Goodnight and good luck!", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }

    public void testHello_CustomEmphasis() throws Exception {
        String[] args = { "-f", "classpath:/org/kurator/akka/samples/hello_emphasized.yaml",
                "-p", "emphasis='?'"};
        KuratorAkka.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals("Hello World?", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }

    public void testHello_CustomGreetingAndEmphasis() throws Exception {
        String[] args = { "-f", "classpath:/org/kurator/akka/samples/hello_emphasized.yaml",
                "-p", "greeting='Goodnight and good luck'",
                "-p", "emphasis='?'"};
        KuratorAkka.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals("Goodnight and good luck?", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }
}
