package org.kurator.akka;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

public class KuratorAkkaTestCase extends TestCase {
    
    static final String EOL = System.getProperty("line.separator");

    protected OutputStream errOutputBuffer;
    protected OutputStream stdOutputBuffer;
    protected PrintStream errPrintStream;
    protected PrintStream outPrintStream;

    @Override
    public void setUp() {
        KuratorAkka.enableLog4J();
        stdOutputBuffer = new ByteArrayOutputStream();
        outPrintStream = new PrintStream(stdOutputBuffer);
        errOutputBuffer = new ByteArrayOutputStream();
        errPrintStream = new PrintStream(errOutputBuffer);
    }
}
