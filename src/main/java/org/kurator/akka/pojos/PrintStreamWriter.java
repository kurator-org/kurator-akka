package org.kurator.akka.pojos;

import java.io.PrintStream;

public class PrintStreamWriter {

    public String separator = System.lineSeparator();
    public boolean endWithSeparator = false;
    public PrintStream outStream = System.out;
    
    private boolean isFirst = true;    

    public Object onData(Object value) throws Exception {
        
        if (isFirst) {
            isFirst = false;
        } else {
            outStream.print(separator);
        }
        outStream.print(value);
        
        return 0;
    }
    
    protected void onEnd() throws Exception {
        if (endWithSeparator) {
            outStream.print(separator);
        }
    }
}
