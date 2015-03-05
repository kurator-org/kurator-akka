package org.kurator.akka.actors;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Arrays;

import org.kurator.akka.ActorConfig;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.WorkflowRunner;

public class TestRequiredFieldsRecordFilter extends KuratorAkkaTestCase {

    private WorkflowRunner wr;
    private ActorConfig csvReader;
    private ActorConfig recordFilter;
    private OutputStream writeBuffer;
    private Writer bufferWriter;
    
    @SuppressWarnings("unused")
    private ActorConfig csvWriter;
    
     @Override
     public void setUp() throws Exception {
         
         super.setUp();

         writeBuffer = new ByteArrayOutputStream();
         bufferWriter = new OutputStreamWriter(writeBuffer);
         
         wr = new WorkflowRunner()
                 .outputStream(stdoutStream)
                 .errorStream(stderrStream);
    
         csvReader = wr.actor(CsvFileReader.class);
    
         recordFilter = wr.actor(RequiredFieldsRecordSelector.class)
                 .listensTo(csvReader);

         csvWriter = wr.actor(CsvFileWriter.class)
                 .parameter("outputWriter", bufferWriter)
                 .listensTo(recordFilter);
     }

     public void testCsvFileReader_NoRequiredFields() throws Exception {

         String expected =
                 "A,B,C"    + EOL +
                 "1,2,3"    + EOL +
                 ",2,3"     + EOL +
                 "1,,3"     + EOL +
                 "1,2,"     + EOL;         
         
         Reader stringReader = new StringReader(expected);
         csvReader.parameter("inputReader", stringReader);
         
         wr.build();
         wr.start();
         wr.await();
         
         assertEquals(expected, writeBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
     
     public void testCsvFileReader_OneRequiredFields() throws Exception {

         Reader stringReader = new StringReader(
                 "A,B,C"    + EOL +
                 "1,2,3"    + EOL +
                 ",2,3"     + EOL +
                 "1,,3"     + EOL +
                 "1,2,"     + EOL
         );
         csvReader.parameter("inputReader", stringReader);
         recordFilter.parameter("requiredFields", Arrays.asList(new String[] {"A"}));
         
         wr.build();
         wr.start();
         wr.await();
         
         String expected =
                 "A,B,C"    + EOL +
                 "1,2,3"    + EOL +
                 "1,,3"     + EOL +
                 "1,2,"     + EOL;  
         
         assertEquals(expected, writeBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
     
     public void testCsvFileReader_TwoRequiredFields() throws Exception {

         Reader stringReader = new StringReader(
                 "A,B,C"    + EOL +
                 "1,2,3"    + EOL +
                 ",2,3"     + EOL +
                 "1,,3"     + EOL +
                 "1,2,"     + EOL
         );
         csvReader.parameter("inputReader", stringReader);
         recordFilter.parameter("requiredFields", Arrays.asList(new String[] {"A", "C"}));
         
         wr.build();
         wr.start();
         wr.await();
         
         String expected =
                 "A,B,C"    + EOL +
                 "1,2,3"    + EOL +
                 "1,,3"     + EOL;  
         
         assertEquals(expected, writeBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }  
}
