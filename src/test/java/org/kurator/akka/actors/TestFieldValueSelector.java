package org.kurator.akka.actors;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.kurator.akka.ActorConfig;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.WorkflowRunner;

public class TestFieldValueSelector extends KuratorAkkaTestCase {

    private WorkflowRunner wr;
    private ActorConfig csvReader;
    private ActorConfig selector;
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
    
         selector = wr.actor(FieldValueSelector.class)
                 .listensTo(csvReader);

         csvWriter = wr.actor(CsvFileWriter.class)
                 .param("outputWriter", bufferWriter)
                 .listensTo(selector);
     }

     public void testCsvFileReader_NoRequiredValues() throws Exception {

         Reader stringReader = new StringReader(
                 "A,B,C"    + EOL +
                 "1,2,3"    + EOL +
                 ",2,3"     + EOL +
                 "1,,3"     + EOL +
                 "1,2,"     + EOL
         );
         csvReader.param("inputReader", stringReader);
         
         wr.run();
         
         String expected =
                 "A,B,C"    + EOL +
                 "1,2,3"    + EOL +
                 "\"\",2,3" + EOL +
                 "1,,3"     + EOL +
                 "1,2,"     + EOL;         

         assertEquals(expected, writeBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
     
     public void testCsvFileReader_OneRequiredValue() throws Exception {

         Reader stringReader = new StringReader(
                 "A,B,C"    + EOL +
                 "1,2,3"    + EOL +
                 "1,4,6"    + EOL +
                 "7,8,9"    + EOL
         );
         csvReader.param("inputReader", stringReader);
         
         Map<String,String> where = new HashMap<String,String>();
         where.put("A", "1");
         selector.param("requiredValues", where);
         
         wr.run();
         
         String expected =
                 "A,B,C"    + EOL +
                 "1,2,3"    + EOL +
                 "1,4,6"    + EOL;  
         
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
         csvReader.param("inputReader", stringReader);
         
         Map<String,String> where = new HashMap<String,String>();
         where.put("A", "1");
         where.put("C", "3");
         selector.param("requiredValues", where);
         
         wr.run();
         
         String expected =
                 "A,B,C"    + EOL +
                 "1,2,3"    + EOL +
                 "1,,3"     + EOL;
         
         assertEquals(expected, writeBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }  
}
