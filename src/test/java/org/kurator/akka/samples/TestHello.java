package org.kurator.akka.samples;

import org.kurator.akka.KuratorAkka;
import org.kurator.akka.KuratorAkkaTestCase;

public class TestHello extends KuratorAkkaTestCase {
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }
    
    public void testHello_DefaultGreeting() throws Exception {
        String[] args = { "-f", "classpath:/org/kurator/akka/samples/hello.yaml" };
        KuratorAkka.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals("Hello World!", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }

    public void testHello_CustomGreeting() throws Exception {
        String[] args = { "-f", "classpath:/org/kurator/akka/samples/hello.yaml",
                "-p", "greeting='Goodnight and good luck!'"};
        KuratorAkka.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals("Goodnight and good luck!", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }
}
