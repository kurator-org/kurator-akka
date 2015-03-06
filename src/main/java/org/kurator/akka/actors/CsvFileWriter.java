package org.kurator.akka.actors;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.csvreader.CsvWriter;

import org.kurator.akka.AkkaActor;

public class CsvFileWriter extends AkkaActor {

    public Writer outputWriter = null;
    public String filePath = null;
    public boolean quoteValuesContainingDelimiter = true;
    public boolean quoteAllValues = false;
    public boolean quoteEmptyValues = false;
    public boolean trimValues = false;
    public boolean showHeader = true;
    public char quoteCharacter = '"';
    public char fieldDelimiter = ',';
    public List<String> headers = null;
    
    private Boolean headerReady = false;
    private CsvWriter csvWriter;

    @Override
    public void handleStart() throws Exception {

        if (outputWriter == null) {
            if (filePath != null) {
                outputWriter = new FileWriter(filePath, false);
            } else {
                outputWriter = new OutputStreamWriter(outStream);
            }
        }

        csvWriter = new CsvWriter(outputWriter, fieldDelimiter);
        csvWriter.setForceQualifier(quoteAllValues);
        csvWriter.setTextQualifier(quoteCharacter);
        csvWriter.setUseTextQualifier(quoteValuesContainingDelimiter);
    }

    @Override
    public void handleEnd() throws Exception {
        csvWriter.close();
    }

    @Override
    public void handleData(Object value) throws Exception {

        if (value instanceof Map<?,?>) {

            @SuppressWarnings("unchecked")
            Map<String,String> record = (Map<String,String>) value;
            if (!headerReady) {
                if (headers== null) buildHeader(record);
                headerReady = true;
                if (showHeader) writeHeader(record);
            }

            writeRecord(record);
        }
    }

    private void buildHeader(Map<String,String> record) {
        headers = new LinkedList<String>();
        for (String label : record.keySet()) {
            headers.add(label);
        }
    }
    
    private void writeHeader(Map<String,String> record) throws IOException {
        for (String label : headers) {
            csvWriter.write(label);
        }
        csvWriter.endRecord();
    }

    private void writeRecord(Map<String,String> record) throws IOException {

        for (String header : headers) {
            String value = record.get(header);
            if (value == null) value = "";
            if (trimValues) value = value.trim();
            if (value.isEmpty()) {
                csvWriter.setUseTextQualifier(this.quoteEmptyValues);
                csvWriter.write(value);
                csvWriter.setUseTextQualifier(quoteValuesContainingDelimiter);
            } else {
                csvWriter.write(value);
            }
        }

        csvWriter.endRecord();
    }
}
