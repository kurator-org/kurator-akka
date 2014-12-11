package org.kurator.akka.actors;

import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.csvreader.CsvReader;

public class CsvStreamReader extends OneShot {

    @SuppressWarnings("serial")
    public class DefaultRecordType extends LinkedHashMap<String,String> {}

    public String filePath = null;
    public String recordClass = null;
    public boolean removeHeaderQuotes = false;    

    private Class<? extends Map<String, String>> _recordClass = DefaultRecordType.class;
        
    @SuppressWarnings("unchecked")
    @Override
    public void handleInitialize() throws Exception {
        if (recordClass != null) {
            _recordClass = (Class<? extends Map<String, String>>) Class.forName(recordClass);
        }
    }
    
    @Override
    public void fireOnce() throws Exception {
        
        CsvReader csvReader;

        try {
            csvReader = new CsvReader(filePath);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Input CSV file not found: " + filePath);
        }

        csvReader.readHeaders();
        String[] headers = csvReader.getHeaders();

        if (removeHeaderQuotes) {
            for (int i = 0; i < headers.length; ++i) {
                headers[i] = headers[i].replace("\"", "");
            }
        }
        
        while (csvReader.readRecord())
        {
            Map<String, String> record = _recordClass.newInstance();
            for (String header : headers) {                
                    record.put(header, csvReader.get(header));
            }
            broadcast(record);
        }

        csvReader.close();
    }
}

