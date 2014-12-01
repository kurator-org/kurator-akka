package org.kurator.akka.samples;

import org.kurator.akka.KuratorAkka;
import org.kurator.akka.KuratorAkkaTestCase;

public class TestIntegerFilter extends KuratorAkkaTestCase {
    
    @Override
    public void setUp() {
        super.setUp();
    }
    
    public void testIntegerFilter_DefaultMax() throws Exception {
        String[] args = { "-f", "classpath:/org/kurator/akka/samples/integer_filter.yaml",
                          "-p", "values=[5, 7, 254, -18, 55, 100, 99, 101]" };
        KuratorAkka.runWorkflowForArgs(args, outPrintStream, errPrintStream);
        assertEquals("5, 7, -18, 55, 100, 99", stdOutputBuffer.toString());
        assertEquals("", errOutputBuffer.toString());
    }

    public void testIntegerFilter_CustomMax() throws Exception {
        String[] args = { "-f", "classpath:/org/kurator/akka/samples/integer_filter.yaml",
                          "-p", "values=[5, 7, 254, -18, 55, 100, 99, 101]" ,
                          "-p", "max=60"};
        KuratorAkka.runWorkflowForArgs(args, outPrintStream, errPrintStream);
        assertEquals("5, 7, -18, 55", stdOutputBuffer.toString());
        assertEquals("", errOutputBuffer.toString());
    }

    public void testIntegerFilter_NoValues() throws Exception {
        String[] args = { "-f", "classpath:/org/kurator/akka/samples/integer_filter.yaml" };
        KuratorAkka.runWorkflowForArgs(args, outPrintStream, errPrintStream);
        assertEquals("", stdOutputBuffer.toString());
        assertEquals("", errOutputBuffer.toString());
    }

}
