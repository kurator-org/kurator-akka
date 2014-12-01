package org.kurator.akka.samples;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.kurator.akka.KuratorAkka;

import junit.framework.TestCase;

public class TestHello extends TestCase {

    static final String EOL = System.getProperty("line.separator");

    private OutputStream errOutputBuffer;
    private OutputStream stdOutputBuffer;
    private PrintStream errPrintStream;
    private PrintStream outPrintStream;

    @Override
    public void setUp() {
        stdOutputBuffer = new ByteArrayOutputStream();
        outPrintStream = new PrintStream(stdOutputBuffer);
        errOutputBuffer = new ByteArrayOutputStream();
        errPrintStream = new PrintStream(errOutputBuffer);
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
