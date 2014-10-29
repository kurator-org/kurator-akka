package org.kurator.akka.actors;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.kurator.akka.data.OrderedSpecimenRecord;
import org.kurator.akka.messages.EndOfStream;
import org.kurator.akka.messages.StartMessage;

import com.csvreader.CsvReader;

import fp.util.SpecimenRecord;

public class CsvSpecimenFileReader extends BroadcastActor {

    public boolean sendEos = true;
    public String filePath = null;
    public boolean useOrderedSpecimenRecord = false;
    
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

        CsvReader csvReader = new CsvReader(filePath);

        csvReader.readHeaders();
        
        while (csvReader.readRecord())
        {
            SpecimenRecord record = useOrderedSpecimenRecord ? new OrderedSpecimenRecord() : new SpecimenRecord();
            for (String header : csvReader.getHeaders()){
                record.put(header.replace("\"", ""), csvReader.get(header));
            }
            broadcast(record);
        }

        csvReader.close();
    }
}

