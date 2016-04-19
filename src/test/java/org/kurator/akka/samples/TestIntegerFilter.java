package org.kurator.akka.samples;

import org.kurator.akka.KuratorCLI;
import org.kurator.akka.KuratorAkkaTestCase;

public class TestIntegerFilter extends KuratorAkkaTestCase {
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }
    
    public void testIntegerFilter_DefaultMax() throws Exception {
        String[] args = { "-f", "classpath:/org/kurator/akka/samples/integer_filter.yaml",
                          "-p", "values=[5, 7, 254, -18, 55, 100, 99, 101]" };
        KuratorCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals("5, 7, -18, 55, 100, 99", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }

    public void testIntegerFilter_CustomMax() throws Exception {
        String[] args = { "-f", "classpath:/org/kurator/akka/samples/integer_filter.yaml",
                          "-p", "values=[5, 7, 254, -18, 55, 100, 99, 101]" ,
                          "-p", "max=60"};
        KuratorCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals("5, 7, -18, 55", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }

    public void testIntegerFilter_NoValues() throws Exception {
        String[] args = { "-f", "classpath:/org/kurator/akka/samples/integer_filter.yaml" };
        KuratorCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals("", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }

}
