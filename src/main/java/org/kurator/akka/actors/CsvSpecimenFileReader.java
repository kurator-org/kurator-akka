package org.kurator.akka.actors;

import java.io.FileNotFoundException;

import org.kurator.akka.data.OrderedSpecimenRecord;

import com.csvreader.CsvReader;

import fp.util.SpecimenRecord;

public class CsvSpecimenFileReader extends OneShot {

    public boolean sendEos = true;
    public String filePath = null;
    public boolean useOrderedSpecimenRecord = false;
    
    @Override
    public void fireOnce() throws Exception {
        
        CsvReader csvReader;

        try {
            csvReader = new CsvReader(filePath);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Input CSV file not found: " + filePath);
        }

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

