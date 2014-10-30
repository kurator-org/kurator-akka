package org.kurator.akka.actors;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.kurator.akka.ActorBuilder;
import org.kurator.akka.WorkflowBuilder;

import junit.framework.TestCase;

public class TestCsvSpecimenFileReader extends TestCase {

    static final String EOL = System.getProperty("line.separator");
    private WorkflowBuilder wfb;
    private OutputStream outputBuffer;
    private ActorBuilder reader;
    
     @Override
     public void setUp() {
    
         outputBuffer = new ByteArrayOutputStream();
         PrintStream printStream = new PrintStream(outputBuffer);
        
         wfb = new WorkflowBuilder();
    
         reader = wfb.createActorBuilder()
                 .actorClass(CsvSpecimenFileReader.class)
                 .parameter("useOrderedSpecimenRecord", true);
    
         @SuppressWarnings("unused")
         ActorBuilder printer = wfb.createActorBuilder()
                 .actorClass(PrintStreamWriter.class)
                 .parameter("stream", printStream)
                 .parameter("separator", EOL)
                 .listensTo(reader);
     }
     
    public void testCsvSpecimenFileReader_EightLineFile() throws Exception {

        reader.parameter("filePath", "src/main/resources/org/kurator/akka/samples/data/eight_specimen_records.csv" );
        wfb.build();
        wfb.startWorkflow();
        wfb.awaitWorkflow();
        
        String expected = 
            "{catalogNumber=100001, recordedBy=Megan A. Jensen, fieldNumber=126, year=2007, month=6, day=29, decimalLatitude=47.1384, decimalLongitude=-120.9263, geodeticDatum=WGS84, country=United States, stateProvince=Washington, county=Chelan, locality=Wenatchee National Forest. South Cle Elum Ridge., family=Asteraceae, scientificName=Taraxacum erythrospermum, scientificNameAuthorship=auct., reproductiveCondition=Flower:March;April;May;June;July;August, InstitutionCode=DAV, CollectionCode=FilteredPush, DatasetName=SPNHCDEMO, Id=926137834}" + EOL +
                "{catalogNumber=100002, recordedBy=G. Rink, fieldNumber=2503, year=2003, month=7, day=27, decimalLatitude=-37.25, decimalLongitude=-108.68, geodeticDatum=WGS84, country=United States, stateProvince=Colorado, county=, locality=Yucca House National Monument, family=Asteraceae, scientificName=Acroptilon repens, scientificNameAuthorship=(L.) DC., reproductiveCondition=Flower:March;April;May;June;July;August;September, InstitutionCode=DAV, CollectionCode=FilteredPush, DatasetName=SPNHCDEMO, Id=925533578}" + EOL +
                "{catalogNumber=100003, recordedBy=Mark Elvin, fieldNumber=2938, year=1990, month=5, day=11, decimalLatitude=34.0, decimalLongitude=-117.0, geodeticDatum=WGS84, country=United States, stateProvince=California, county=San Bernardino, locality=400 m north of Cushenbury Springs, family=Asteraceae, scientificName=Cirsium mohavense, scientificNameAuthorship=(Greene) Petr., reproductiveCondition=Flower:June;July;August;September;October;November, InstitutionCode=DAV, CollectionCode=FilteredPush, DatasetName=SPNHCDEMO, Id=1024940716}" + EOL +
                "{catalogNumber=100004, recordedBy=Mark Elvin, fieldNumber=3000, year=1990, month=5, day=21, decimalLatitude=37.0, decimalLongitude=-118.0, geodeticDatum=WGS84, country=United States, stateProvince=California, county=, locality=Northern end of The Owens Valle Bishop, family=Asteraceae, scientificName=Cirsium mohavense, scientificNameAuthorship=(Greene) Petr., reproductiveCondition=Flower:June;July;August;September;October;November, InstitutionCode=DAV, CollectionCode=FilteredPush, DatasetName=SPNHCDEMO, Id=1024940765}" + EOL +
                "{catalogNumber=100005, recordedBy=Mark Elvin, fieldNumber=2940, year=1990, month=5, day=12, decimalLatitude=34.0, decimalLongitude=-117.0, geodeticDatum=WGS84, country=United States, stateProvince=California, county=San Bernardino, locality=l mi. NW of Lucerne Valley town center, family=Asteraceae, scientificName=Cirsium mohavense, scientificNameAuthorship=(Greene) Petr., reproductiveCondition=Flower:June;July;August;September;October;November, InstitutionCode=DAV, CollectionCode=FilteredPush, DatasetName=SPNHCDEMO, Id=1024940674}" + EOL +
                "{catalogNumber=100006, recordedBy=Mark Elvin, fieldNumber=1940, year=1990, month=5, day=20, decimalLatitude=36.0, decimalLongitude=-118.0, geodeticDatum=WGS84, country=United States, stateProvince=California, county=Kern, locality=Weldon Rancheria, family=Asteraceae, scientificName=Cirsium mohavense, scientificNameAuthorship=(Greene) Petr., reproductiveCondition=Flower:June;July;August;September;October;November, InstitutionCode=DAV, CollectionCode=FilteredPush, DatasetName=SPNHCDEMO, Id=1024940053}" + EOL +
                "{catalogNumber=100007, recordedBy=Mark Elvin, fieldNumber=606, year=1990, month=5, day=16, decimalLatitude=21.312, decimalLongitude=-157.8055, geodeticDatum=WGS84, country=United States, stateProvince=Hawaii, county=Honolulu, locality=Honolulu 3115 Kaloaluiki Place, family=Asteraceae, scientificName=Tragopogon porrifolius, scientificNameAuthorship=L., reproductiveCondition=Flower:April;May;June;July;August, InstitutionCode=DAV, CollectionCode=FilteredPush, DatasetName=SPNHCDEMO, Id=927140834}" + EOL +
                "{catalogNumber=100008, recordedBy=Joseph P. Tracy, fieldNumber=107702, year=1973, month=7, day=31, decimalLatitude=40.0, decimalLongitude=-104.0, geodeticDatum=WGS84, country=United States, stateProvince=California, county=, locality=Carlotta Northern Coast Ranges, family=Asteraceae, scientificName=Logfia gallica, scientificNameAuthorship=Coss. & Germ., reproductiveCondition=Flower:July;August; September, InstitutionCode=DAV, CollectionCode=FilteredPush, DatasetName=SPNHCDEMO, Id=127140835}";  
        assertEquals(expected, outputBuffer.toString());
    }

}
