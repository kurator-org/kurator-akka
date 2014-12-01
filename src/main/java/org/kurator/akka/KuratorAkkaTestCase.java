package org.kurator.akka;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

public class KuratorAkkaTestCase extends TestCase {
    
    public static final String EOL = System.getProperty("line.separator");

    protected OutputStream stdoutBuffer;
    protected OutputStream stderrBuffer;
    
    protected PrintStream stdoutStream;
    protected PrintStream stderrStream;

    @Override
    public void setUp() {
        
        KuratorAkka.enableLog4J();
        
        stdoutBuffer = new ByteArrayOutputStream();
        stdoutStream = new PrintStream(stdoutBuffer);
    
        stderrBuffer = new ByteArrayOutputStream();
        stderrStream = new PrintStream(stderrBuffer);
    }
}
