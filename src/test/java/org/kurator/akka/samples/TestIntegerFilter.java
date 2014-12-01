package org.kurator.akka.samples;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.kurator.akka.KuratorAkka;

import junit.framework.TestCase;

public class TestIntegerFilter extends TestCase {

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
    
    public void testHello_DefaultMax() throws Exception {
        String[] args = { "-f", "classpath:/org/kurator/akka/samples/integer_filter.yaml" };
        KuratorAkka.runWorkflowForArgs(args, outPrintStream, errPrintStream);
        assertEquals("5, 7, -18, 55, 100, 99", stdOutputBuffer.toString());
        assertEquals("", errOutputBuffer.toString());
    }

    public void testHello_CustomMax() throws Exception {
        String[] args = { "-f", "classpath:/org/kurator/akka/samples/integer_filter.yaml",
                          "-i", "max=60"};
        KuratorAkka.runWorkflowForArgs(args, outPrintStream, errPrintStream);
        assertEquals("5, 7, -18, 55", stdOutputBuffer.toString());
        assertEquals("", errOutputBuffer.toString());
    }

    public void testHello_CustomValues() throws Exception {
        String[] args = { "-f", "classpath:/org/kurator/akka/samples/integer_filter.yaml",
                          "-i", "values=[1, 10, 50, 100, 150, 200, -10, 0, 500, 74]" };
        KuratorAkka.runWorkflowForArgs(args, outPrintStream, errPrintStream);
        assertEquals("1, 10, 50, 100, -10, 0, 74", stdOutputBuffer.toString());
        assertEquals("", errOutputBuffer.toString());
    }
}
