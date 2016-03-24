package org.kurator.akka.actors;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.kurator.akka.KuratorActor;
import org.python.core.PyNone;

public class CsvFileWriter extends KuratorActor {

    public Writer outputWriter = null;
    public String filePath = null;
    public boolean quoteValuesContainingDelimiter = true;
    public boolean quoteAllValues = false;
    public String recordSeparator = System.getProperty("line.separator");
    public boolean trimValues = false;
    public boolean showHeader = false;
    public char quoteCharacter = '"';
    public char fieldDelimiter = ',';
    public String[] headers = null;
    
    private CSVPrinter csvPrinter = null;

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

        if (value instanceof Map<?,?>) {

            @SuppressWarnings("unchecked")
            Map<String,Object> record = (Map<String,Object>) value;
            
            if (csvPrinter == null) {
                if (headers== null) buildHeader(record);
                createCsvPrinter();
            }

            for (Map.Entry<String, Object> entry : record.entrySet()) {
                if(entry.getValue() instanceof PyNone) {
                    entry.setValue(null);
                }
            }
            
            csvPrinter.printRecord(record.values());
        }
    }

    @Override
    public void onEnd() throws Exception {
        if (csvPrinter != null) {
            csvPrinter.close();
        }
    }
    
    private void createCsvPrinter() throws IOException {
        
        QuoteMode quoteModePolicy;
        if (quoteAllValues) {
            quoteModePolicy = QuoteMode.ALL;
        } else if (quoteValuesContainingDelimiter) {
            quoteModePolicy = QuoteMode.MINIMAL;
        } else {
            quoteModePolicy = QuoteMode.NONE;
        }
            
        CSVFormat csvFormat = CSVFormat.newFormat(fieldDelimiter)
                .withQuoteMode(quoteModePolicy)
                .withQuote(quoteCharacter)
                .withRecordSeparator(recordSeparator)
                .withSkipHeaderRecord(!showHeader)
                .withHeader(headers);
        
        csvPrinter = new CSVPrinter(outputWriter, csvFormat);
    }
    
    private void buildHeader(Map<String,Object> record) {
        headers = new String[record.size()];
        int i = 0;
        for (String label : record.keySet()) {
            headers[i++] = label;
        }
    }
}
