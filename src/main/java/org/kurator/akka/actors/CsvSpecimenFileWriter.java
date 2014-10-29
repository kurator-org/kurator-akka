package org.kurator.akka.actors;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.kurator.akka.data.OrderedSpecimenRecord;
import org.kurator.akka.messages.EndOfStream;
import org.kurator.akka.messages.StartMessage;

import com.csvreader.CsvWriter;

import fp.util.SpecimenRecord;

public class CsvSpecimenFileWriter extends BroadcastActor {

    public String filePath = null;
    OutputStream os = new ByteArrayOutputStream();
    public Writer writer = new OutputStreamWriter(os);
    public OutputStreamWriter outputStreamWriter = null;
    
    private Boolean headerWritten = false;
    private List<String> headers = new ArrayList<String>();
    private CsvWriter csvWriter;
    
    @Override
    public void onReceive(Object message) {

        super.onReceive(message);
        
        if (message instanceof StartMessage) {
            
            if (writer == null) {
                writer = getFileWriterForPath(filePath);
            }
            
            csvWriter = new CsvWriter(writer, ',');
        
        } else if (message instanceof SpecimenRecord) {
        
            SpecimenRecord record = (SpecimenRecord) message;
            if (!headerWritten) {
                writeHeaderToFile(record);
                headerWritten = true;
            }
            
            writeRecordValuesToFile(record);
        
        } else if (message instanceof EndOfStream) {
            
            csvWriter.close();
            
            broadcast(message);
            getContext().stop(getSelf());
        }
    }

    
    
    private Writer getFileWriterForPath(String path) {
        
        Writer writer = null;
        
        try {        
            writer = new FileWriter(path, true);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
        return writer;
    }
    
    private void writeHeaderToFile(SpecimenRecord record) {
        try {

            if (record instanceof OrderedSpecimenRecord) {
                for (String label : ((OrderedSpecimenRecord)record).getKeyList()) {
                    csvWriter.write(label);
                    headers.add(label);
                }
            } else {            
                for (String label : record.keySet()) {
                    csvWriter.write(label);
                    headers.add(label);
                }
            }
            
            csvWriter.endRecord();
            
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
    private void writeRecordValuesToFile(SpecimenRecord record) {
        try {
            
            for (String header : headers) {
                csvWriter.write(record.get(header));
            }
            
            csvWriter.endRecord();
            
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}

