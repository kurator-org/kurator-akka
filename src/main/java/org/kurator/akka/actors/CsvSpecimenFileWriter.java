package org.kurator.akka.actors;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kurator.akka.messages.EndOfStream;
import org.kurator.akka.messages.StartMessage;

import com.csvreader.CsvWriter;

import fp.util.SpecimenRecord;

public class CsvSpecimenFileWriter extends BroadcastActor {

    public String filePath = null;
    
    private Boolean headerWritten = false;
    private List<String> headers = new ArrayList<String>();
    private CsvWriter csvWriter;

    
    @Override
    public void onReceive(Object message) {

        super.onReceive(message);
        
        if (message instanceof StartMessage) {
            
            openOutputFile();
        
        } else if (message instanceof SpecimenRecord) {
        
            SpecimenRecord record = (SpecimenRecord) message;
            if (!headerWritten) {
                writeHeaderToFile(record);
            }
            
            writeRecordValuesToFile(record);
        
        } else if (message instanceof EndOfStream) {
            
            csvWriter.close();
            
            broadcast(message);
            getContext().stop(getSelf());
        }
    }
    
    private void openOutputFile() {
        
        try {
        
            csvWriter = new CsvWriter(new FileWriter(filePath, true), ',');
        
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
    private void writeHeaderToFile(SpecimenRecord record) {
        try {
            
            for (String label : record.keySet()) {
                csvWriter.write(label);
                headers.add(label);
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

