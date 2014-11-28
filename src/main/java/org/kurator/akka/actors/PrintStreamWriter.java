package org.kurator.akka.actors;

public class PrintStreamWriter extends Transformer {

    public String separator = System.lineSeparator();    
    private boolean isFirst = true;
    
    @Override
    public void handleDataMessage(Object message) throws Exception {
        
        if (isFirst) {
            isFirst = false;
        } else {
            outStream.print(separator);
        }
        outStream.print(message);
    }
}
