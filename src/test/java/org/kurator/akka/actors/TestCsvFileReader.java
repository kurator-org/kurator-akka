package org.kurator.akka.actors;

import java.io.Reader;
import java.io.StringReader;

import org.kurator.akka.ActorConfig;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.WorkflowRunner;

public class TestCsvFileReader extends KuratorAkkaTestCase {

    private WorkflowRunner wr;
    private ActorConfig readerActor;
    
    @SuppressWarnings("unused")
    private ActorConfig printerActor;
    
     @Override
     public void setUp() throws Exception {
         
         super.setUp();

         wr = new WorkflowRunner()
                 .outputStream(stdoutStream)
                 .errorStream(stderrStream);
    
         readerActor = wr.actor(CsvFileReader.class);
    
         printerActor = wr.actor(PrintStreamWriter.class)
                 .param("separator", EOL)
                 .listensTo(readerActor);
     }

     public void testCsvFileReader_OneRecord_ExplicitHeaderArray() throws Exception {

         Reader stringReader = new StringReader(
                 "1,2,3"    + EOL
         );
         
         readerActor.param("inputReader", stringReader)
                    .param("headers", new String[] {"A", "B", "C"});
         
         wr.run();
         
         String expected = "{A=1, B=2, C=3}";
         assertEquals("", stderrBuffer.toString());
         assertEquals(expected, stdoutBuffer.toString());
     }

     public void testCsvFileReader_OneRecordMissingField_ExplicitHeaderArray() throws Exception {

         Reader stringReader = new StringReader(
                 "1,2"    + EOL
         );
         
         readerActor.param("inputReader", stringReader)
                    .param("headers", new String[] {"A", "B", "C"});
         
         wr.begin();
         
         Exception exception = null;
         try {
             wr.end();
         } catch(Exception e) {
             exception = e;
         }
         
         assertNotNull(exception);
         assertEquals("Wrong number of fields in record 1", exception.getMessage());
     }
     
     public void testCsvFileReader_HeaderAndOneRecord() throws Exception {

         Reader stringReader = new StringReader(
                 "A,B,C"    + EOL +
                 "1,2,3"    + EOL
         );
         
         readerActor.param("inputReader", stringReader);
         
         wr.run();
         
         String expected = "{A=1, B=2, C=3}";
         assertEquals(expected, stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
     
     public void testCsvFileReader_MissingInputFile() throws Exception {

         readerActor.param("filePath", "src/main/resources/org/kurator/akka/samples/data/no_such_file.csv" );

         wr.begin();

         Exception caught = null;
         try { 
             wr.end();
         } catch(Exception e) {
             caught = e;
         }
         
         String expectedError = "Input CSV file not found";         

         assertNotNull(caught);
         assertTrue(caught.getMessage().contains(expectedError));
         
         assertEquals("", stdoutBuffer.toString());
         assertTrue(stderrBuffer.toString().contains(expectedError));
     }
}
