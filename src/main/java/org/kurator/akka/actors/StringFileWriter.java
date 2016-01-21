package org.kurator.akka.actors;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.kurator.akka.AkkaActor;
import org.python.core.PyNone;

/**
 * An actor, that when configured with a filePath, on receipt of 
 * a message that is typed as a String object, writes that message 
 * to a file.
 * 
 * @author mole
 *
 */
public class StringFileWriter extends AkkaActor {

    public Writer outputWriter = null;
    public String filePath = null;
    public String lineSeparator = System.getProperty("line.separator");

    @Override
    public void onStart() throws Exception {

        if (outputWriter == null) {
            if (filePath != null) {
                outputWriter = new FileWriter(filePath, false);
            } else {
                outputWriter = new OutputStreamWriter(outStream);
            }
        }
    }

    @Override
    public void onData(Object value) throws Exception {

        if (value instanceof String) {

        	if (value!=null) { 
        		outputWriter.write((String)value);
        		outputWriter.write(lineSeparator);
        	}
        }
    }

    @Override
    public void onEnd() throws Exception {
        if (outputWriter != null) {
            outputWriter.close();
        }
    }
    
}