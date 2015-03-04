package org.kurator.akka.actors;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.csvreader.CsvWriter;

import org.kurator.akka.actors.AkkaActor;

public class CsvFileWriter extends AkkaActor {

    public Writer outputWriter;
    public String filePath = null;

    private Boolean headerWritten = false;
    private List<String> headers = new ArrayList<String>();
    private CsvWriter csvWriter;

    @Override
    public void handleStart() throws Exception {

        if (outputWriter == null && filePath != null) {
            outputWriter = new FileWriter(filePath, false);
        }

        if (outputWriter == null) {
            throw new Exception(
                    "No file or output writer specified for CsVSpecimenFileWriter.");
        }

        csvWriter = new CsvWriter(outputWriter, ',');
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
                writeHeaderToFile(record);
                headerWritten = true;
            }

            writeRecordValuesToFile(record);
        }
    }

    private void writeHeaderToFile(Map<String,String> record) throws IOException {

        for (String label : record.keySet()) {
            csvWriter.write(label);
            headers.add(label);
        }

        csvWriter.endRecord();
    }

    private void writeRecordValuesToFile(Map<String,String> record) throws IOException {

        for (String header : headers) {
            csvWriter.write(record.get(header));
        }

        csvWriter.endRecord();
    }
}
