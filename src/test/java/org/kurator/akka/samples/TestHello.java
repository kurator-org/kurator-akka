package org.kurator.akka.samples;

import org.kurator.akka.KuratorAkka;
import org.kurator.akka.KuratorAkkaTestCase;

public class TestHello extends KuratorAkkaTestCase {
    
    @Override
    public void setUp() {
        super.setUp();
    }
    
    public void testHello_DefaultGreeting() throws Exception {
        String[] args = { "-f", "classpath:/org/kurator/akka/samples/hello.yaml" };
        KuratorAkka.runWorkflowForArgs(args, outPrintStream, errPrintStream);
        assertEquals("Hello World!", stdOutputBuffer.toString());
        assertEquals("", errOutputBuffer.toString());
    }

    public void testHello_CustomGreeting() throws Exception {
        String[] args = { "-f", "classpath:/org/kurator/akka/samples/hello.yaml",
                "-p", "greeting='Goodnight and good luck!'"};
        KuratorAkka.runWorkflowForArgs(args, outPrintStream, errPrintStream);
        assertEquals("Goodnight and good luck!", stdOutputBuffer.toString());
        assertEquals("", errOutputBuffer.toString());
    }
}
