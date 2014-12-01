package org.kurator.akka.samples;

import java.util.concurrent.TimeoutException;

import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.samples.Hamming;

public class TestHamming_Java extends KuratorAkkaTestCase {
    
    @Override
    public void setUp() {
        super.setUp();
    }

    public void testHamming_Max1() throws TimeoutException,
            InterruptedException {

        Hamming wf = new Hamming(1, outPrintStream, ", ");
        wf.run();
        assertEquals("1", stdOutputBuffer.toString());
    }

    public void testHamming_Max10() throws TimeoutException,
            InterruptedException {
        Hamming wf = new Hamming(10, outPrintStream, ", ");
        wf.run();
        assertEquals("1, 2, 3, 4, 5, 6, 8, 9, 10", stdOutputBuffer.toString());
    }

    public void testHamming_Max30() throws TimeoutException,
            InterruptedException {
        Hamming wf = new Hamming(30, outPrintStream, ", ");
        wf.run();
        assertEquals("1, 2, 3, 4, 5, 6, 8, 9, 10, "
                + "12, 15, 16, 18, 20, 24, 25, 27, 30", stdOutputBuffer.toString());
    }

    public void testHamming_Max100() throws TimeoutException,
            InterruptedException {
        Hamming wf = new Hamming(100, outPrintStream, ", ");
        wf.run();
        assertEquals( "1, 2, 3, 4, 5, 6, 8, 9, 10, "
                    + "12, 15, 16, 18, 20, 24, 25, "
                    + "27, 30, 32, 36, 40, 45, 48, "
                    + "50, 54, 60, 64, 72, 75, 80, "
                    + "81, 90, 96, 100", stdOutputBuffer.toString());
    }

}
