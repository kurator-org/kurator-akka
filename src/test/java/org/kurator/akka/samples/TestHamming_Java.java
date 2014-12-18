package org.kurator.akka.samples;

import java.util.concurrent.TimeoutException;

import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.samples.Hamming;

public class TestHamming_Java extends KuratorAkkaTestCase {
    
    @Override
    public void setUp() {
        super.setUp();
    }

    public void testHamming_Max1() throws Exception {

        Hamming wf = new Hamming(1, stdoutStream, ", ");
        wf.run();
        assertEquals("1", stdoutBuffer.toString());
    }

    public void testHamming_Max10() throws Exception {
        Hamming wf = new Hamming(10, stdoutStream, ", ");
        wf.run();
        assertEquals("1, 2, 3, 4, 5, 6, 8, 9, 10", stdoutBuffer.toString());
    }

    public void testHamming_Max30() throws Exception {
        Hamming wf = new Hamming(30, stdoutStream, ", ");
        wf.run();
        assertEquals("1, 2, 3, 4, 5, 6, 8, 9, 10, "
                + "12, 15, 16, 18, 20, 24, 25, 27, 30", stdoutBuffer.toString());
    }

    public void testHamming_Max100() throws Exception {
        Hamming wf = new Hamming(100, stdoutStream, ", ");
        wf.run();
        assertEquals( "1, 2, 3, 4, 5, 6, 8, 9, 10, "
                    + "12, 15, 16, 18, 20, 24, 25, "
                    + "27, 30, 32, 36, 40, 45, 48, "
                    + "50, 54, 60, 64, 72, 75, 80, "
                    + "81, 90, 96, 100", stdoutBuffer.toString());
    }

}
