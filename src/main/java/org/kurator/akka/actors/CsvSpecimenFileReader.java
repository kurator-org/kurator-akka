package org.kurator.akka.actors;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.kurator.akka.messages.EndOfStream;
import org.kurator.akka.messages.StartMessage;

import com.csvreader.CsvReader;

import fp.util.SpecimenRecord;

public class CsvSpecimenFileReader extends BroadcastActor {

    public boolean sendEos = true;
    public String filePath = null;
    
    @Override
    public void onReceive(Object message) {

        super.onReceive(message);
        
        if (message instanceof StartMessage) {

            try {
                parseAndBroadcastRecords();    
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(-1);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }

            if (sendEos) {
                broadcast(new EndOfStream());
            }
            getContext().stop(getSelf());
        }
    }
    
    private void parseAndBroadcastRecords() throws IOException {

        CsvReader reader = new CsvReader(filePath);

        reader.readHeaders();
        
        while (reader.readRecord())
        {
            SpecimenRecord record = new SpecimenRecord();
            for (String header : reader.getHeaders()){
                record.put(header.replace("\"", ""), reader.get(header));
            }
            broadcast(record);
        }
        reader.close();
    }
}

