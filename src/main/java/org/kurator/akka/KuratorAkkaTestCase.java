package org.kurator.akka;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

public abstract class KuratorAkkaTestCase extends TestCase {
    
    public static final String EOL = System.getProperty("line.separator");

    protected volatile OutputStream stdoutBuffer;
    protected volatile OutputStream stderrBuffer;
    
    protected volatile PrintStream stdoutStream;
    protected volatile PrintStream stderrStream;

    @Override
    public void setUp() throws Exception {
        
        super.setUp();
        
        KuratorCLI.enableLog4J();
        
        stdoutBuffer = new ByteArrayOutputStream();
        stdoutStream = new PrintStream(stdoutBuffer);
    
        stderrBuffer = new ByteArrayOutputStream();
        stderrStream = new PrintStream(stderrBuffer);
        
        Contract.enforceContract(true);
    }
}
