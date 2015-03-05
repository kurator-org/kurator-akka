package org.kurator.akka.actors;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.csvreader.CsvWriter;

import org.kurator.akka.AkkaActor;

public class CsvFileWriter extends AkkaActor {

    public Writer outputWriter;
    public String filePath = null;
    public boolean quoteValuesContainingDelimiter = true;
    public boolean quoteAllValues = false;
    public boolean quoteEmptyValues = false;
    public boolean trimValues = false;
    public char quoteCharacter = '"';
    public char fieldDelimiter = ',';
    
    private Boolean headerWritten = false;
    private List<String> headers = new ArrayList<String>();
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
            if (!headerWritten) {
                writeHeader(record);
                headerWritten = true;
            }

            writeRecord(record);
        }
    }

    private void writeHeader(Map<String,String> record) throws IOException {

        for (String label : record.keySet()) {
            csvWriter.write(label);
            headers.add(label);
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
