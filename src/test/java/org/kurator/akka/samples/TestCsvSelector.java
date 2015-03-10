package org.kurator.akka.samples;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.kurator.akka.ActorConfig;
import org.kurator.akka.KuratorAkkaCLI;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.actors.CsvFileReader;
import org.kurator.akka.actors.CsvFileWriter;
import org.kurator.akka.actors.FieldValueSelector;

public class TestCsvSelector extends KuratorAkkaTestCase {

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
                 .parameter("outputWriter", bufferWriter)
                 .listensTo(selector);
     }

     public void testCsvSelector_OneRequiredValue() throws Exception {
         
         String[] args = {
                 "-f", "classpath:/org/kurator/akka/samples/csv_selector.yaml", 
                 "-p", "in=src/test/resources/org/kurator/akka/samples/eight_specimen_records.csv",
                 "-p", "where={year: '1990'}"
         };
         
         KuratorAkkaCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
         assertEquals(
                 "catalogNumber,recordedBy,fieldNumber,year,month,day,decimalLatitude,decimalLongitude,geodeticDatum,country,stateProvince,county,locality,family,scientificName,scientificNameAuthorship,reproductiveCondition,InstitutionCode,CollectionCode,DatasetName,Id" + EOL + 
                 "100003,Mark Elvin,2938,1990,5,11,34.0,-117.0,WGS84,United States,California,San Bernardino,400 m north of Cushenbury Springs,Asteraceae,Cirsium mohavense,(Greene) Petr.,Flower:June;July;August;September;October;November,DAV,FilteredPush,SPNHCDEMO,1024940716" + EOL + 
                 "100004,Mark Elvin,3000,1990,5,21,37.0,-118.0,WGS84,United States,California,,Northern end of The Owens Valle Bishop,Asteraceae,Cirsium mohavense,(Greene) Petr.,Flower:June;July;August;September;October;November,DAV,FilteredPush,SPNHCDEMO,1024940765" + EOL + 
                 "100005,Mark Elvin,2940,1990,5,12,34.0,-117.0,WGS84,United States,California,San Bernardino,l mi. NW of Lucerne Valley town center,Asteraceae,Cirsium mohavense,(Greene) Petr.,Flower:June;July;August;September;October;November,DAV,FilteredPush,SPNHCDEMO,1024940674" + EOL + 
                 "100006,Mark Elvin,1940,1990,5,20,36.0,-118.0,WGS84,United States,California,Kern,Weldon Rancheria,Asteraceae,Cirsium mohavense,(Greene) Petr.,Flower:June;July;August;September;October;November,DAV,FilteredPush,SPNHCDEMO,1024940053" + EOL + 
                 "100007,Mark Elvin,606,1990,5,16,21.312,-157.8055,WGS84,United States,Hawaii,Honolulu,Honolulu 3115 Kaloaluiki Place,Asteraceae,Tragopogon porrifolius,L.,Flower:April;May;June;July;August,DAV,FilteredPush,SPNHCDEMO,927140834" + EOL,
             stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }

     public void testCsvSelector_TwoRequiredValues() throws Exception {
         
         String[] args = {
                 "-f", "classpath:/org/kurator/akka/samples/csv_selector.yaml", 
                 "-p", "in=src/test/resources/org/kurator/akka/samples/eight_specimen_records.csv",
                 "-p", "where={year: '1990', day: '11'}"
         };
         
         KuratorAkkaCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
         assertEquals(
                 "catalogNumber,recordedBy,fieldNumber,year,month,day,decimalLatitude,decimalLongitude,geodeticDatum,country,stateProvince,county,locality,family,scientificName,scientificNameAuthorship,reproductiveCondition,InstitutionCode,CollectionCode,DatasetName,Id" + EOL + 
                 "100003,Mark Elvin,2938,1990,5,11,34.0,-117.0,WGS84,United States,California,San Bernardino,400 m north of Cushenbury Springs,Asteraceae,Cirsium mohavense,(Greene) Petr.,Flower:June;July;August;September;October;November,DAV,FilteredPush,SPNHCDEMO,1024940716" + EOL,
             stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }

}
