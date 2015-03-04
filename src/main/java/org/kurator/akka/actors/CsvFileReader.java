package org.kurator.akka.actors;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import com.csvreader.CsvReader;

import org.kurator.akka.data.GenericRecord;

public class CsvFileReader extends OneShot {

    public Reader inputReader = null;
    public String filePath = null;
    public String recordClass = null;
    public String[] headers = null;

    private Class<? extends Map<String, String>> _recordClass = GenericRecord.class;

    @SuppressWarnings("unchecked")
    @Override
    public void handleInitialize() throws Exception {
        if (recordClass != null) {
            _recordClass = (Class<? extends Map<String, String>>) Class.forName(recordClass);
        }
    }

    public void setHeaders(String[] headerArray) {
        this.headers = headerArray;
    }

    public void setHeaders(List<String> headerList) {
        headerList.toArray(this.headers = new String[0]);
    }
    
    @Override
    public void fireOnce() throws Exception {
        
        if (inputReader == null && filePath != null) {
            inputReader = getFileReaderForPath(filePath);
        }
        
        if (inputReader == null) {
            throw new Exception("No file or input reader specified for CsvStreamReader.");
        }

        CsvReader csvReader = new CsvReader(inputReader);
        
        if (headers!=null) {
            csvReader.setHeaders(headers);
        } else {
            csvReader.readHeaders();
            headers = csvReader.getHeaders();
        }
        
        while (csvReader.readRecord()) {
            
            if (csvReader.getColumnCount() < headers.length)  {
                throw new Exception("Too few fields in record: " + csvReader.getRawRecord());
            }
            
            Map<String, String> record = _recordClass.newInstance();
            
            for (String header : headers) {
                
                String value = csvReader.get(header);
                
                if (value == null) {
                    throw new Exception("No value in record for required field " + header + ": " +csvReader.getRawRecord());                    
                }
                
                record.put(header, value);
            }
            broadcast(record);
        }

        csvReader.close();
    }

    private Reader getFileReaderForPath(String path) throws FileNotFoundException {
        
        Reader reader = null;
        
        try {        
            reader = new FileReader(path);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Input CSV file not found: " + filePath);
        }
        
        return reader;
    }

}

