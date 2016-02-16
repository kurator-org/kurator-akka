package org.kurator.akka.actors;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.kurator.akka.data.GenericMapRecord;

public class CsvFileReader extends OneShot {

    public char fieldDelimiter = ',';
    public Character quote = '"';
    public boolean trimWhitespace = true;

    public Reader inputReader = null;
    public String filePath = null;
    public String recordClass = null;
    public String[] headers = new String[]{};

    private Class<? extends Map<String, String>> _recordClass = GenericMapRecord.class;

    @SuppressWarnings("unchecked")
    @Override
    public void onInitialize() throws Exception {
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
        
        if (inputReader == null) {
            if (filePath != null) {
                inputReader = getFileReaderForPath(filePath);
            } else {
                inputReader = new InputStreamReader(inStream);
            }
        }
        
        CSVFormat csvFormat = CSVFormat.newFormat(fieldDelimiter)
                .withIgnoreSurroundingSpaces(trimWhitespace)
                .withQuote(quote)
                .withHeader(headers);
        
        try (CSVParser csvParser = new CSVParser(inputReader, csvFormat)) {
        
            Map<String,Integer> csvHeader = csvParser.getHeaderMap();
            headers = new String[csvHeader.size()];
            int i = 0;
            for (String header: csvHeader.keySet()) {
                headers[i++] = header;
            }
            
            for (Iterator<CSVRecord> iterator = csvParser.iterator();iterator.hasNext();) {
    
                CSVRecord csvRecord = iterator.next();
                
                if (!csvRecord.isConsistent()) {
                  throw new Exception("Wrong number of fields in record " + csvRecord.getRecordNumber());                    
                }
                
                Map<String, String> record = _recordClass.newInstance();
                
                for (String header : headers) {
                    String value = csvRecord.get(header);                
                    record.put(header, value);
                }
                broadcast(record);
            }
        }
    }

    private Reader getFileReaderForPath(String path) throws FileNotFoundException {
        
        Reader reader = null;
        
        try {        
            reader = new FileReader(path);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Input CSV file not found: " + path);
        }
        
        return reader;
    }

}

