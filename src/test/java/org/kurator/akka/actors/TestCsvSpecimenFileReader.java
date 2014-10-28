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
                 .actorClass(CsvSpecimenFileReader.class);
    
         @SuppressWarnings("unused")
         ActorBuilder printer = wfb.createActorBuilder()
                 .actorClass(PrintStreamWriter.class)
                 .parameter("stream", printStream)
                 .parameter("separator", EOL)
                 .listensTo(reader);
        
     }
     
    public void testCsvSpecimenFileReader_EightLightFile() throws Exception {

        reader.parameter("filePath", "src/main/resources/org/kurator/akka/samples/data/2011Demo.csv" );
        wfb.build();
        wfb.startWorkflow();
        wfb.awaitWorkflow();
        
        String expected = 
            "{scientificName=Taraxacum erythrospermum, reproductiveCondition=Flower:March;April;May;June;July;August, scientificNameAuthorship=auct., country=United States, decimalLatitude=47.1384, DatasetName=SPNHCDEMO, fieldNumber=126, geodeticDatum=WGS84, county=Chelan, family=Asteraceae, catalogNumber=100001, recordedBy=Megan A. Jensen, month=6, decimalLongitude=-120.9263, locality=Wenatchee National Forest. South Cle Elum Ridge., stateProvince=Washington, Id=926137834, year=2007, day=29, CollectionCode=FilteredPush, InstitutionCode=DAV}" + EOL +
            "{scientificName=Acroptilon repens, reproductiveCondition=Flower:March;April;May;June;July;August;September, scientificNameAuthorship=(L.) DC., country=United States, decimalLatitude=-37.25, DatasetName=SPNHCDEMO, fieldNumber=2503, geodeticDatum=WGS84, county=, family=Asteraceae, catalogNumber=100002, recordedBy=G. Rink, month=7, decimalLongitude=-108.68, locality=Yucca House National Monument, stateProvince=Colorado, Id=925533578, year=2003, day=27, CollectionCode=FilteredPush, InstitutionCode=DAV}" + EOL +
            "{scientificName=Cirsium mohavense, reproductiveCondition=Flower:June;July;August;September;October;November, scientificNameAuthorship=(Greene) Petr., country=United States, decimalLatitude=34.0, DatasetName=SPNHCDEMO, fieldNumber=2938, geodeticDatum=WGS84, county=San Bernardino, family=Asteraceae, catalogNumber=100003, recordedBy=Mark Elvin, month=5, decimalLongitude=-117.0, locality=400 m north of Cushenbury Springs, stateProvince=California, Id=1024940716, year=1990, day=11, CollectionCode=FilteredPush, InstitutionCode=DAV}" + EOL +
            "{scientificName=Cirsium mohavense, reproductiveCondition=Flower:June;July;August;September;October;November, scientificNameAuthorship=(Greene) Petr., country=United States, decimalLatitude=37.0, DatasetName=SPNHCDEMO, fieldNumber=3000, geodeticDatum=WGS84, county=, family=Asteraceae, catalogNumber=100004, recordedBy=Mark Elvin, month=5, decimalLongitude=-118.0, locality=Northern end of The Owens Valle Bishop, stateProvince=California, Id=1024940765, year=1990, day=21, CollectionCode=FilteredPush, InstitutionCode=DAV}" + EOL +
            "{scientificName=Cirsium mohavense, reproductiveCondition=Flower:June;July;August;September;October;November, scientificNameAuthorship=(Greene) Petr., country=United States, decimalLatitude=34.0, DatasetName=SPNHCDEMO, fieldNumber=2940, geodeticDatum=WGS84, county=San Bernardino, family=Asteraceae, catalogNumber=100005, recordedBy=Mark Elvin, month=5, decimalLongitude=-117.0, locality=l mi. NW of Lucerne Valley town center, stateProvince=California, Id=1024940674, year=1990, day=12, CollectionCode=FilteredPush, InstitutionCode=DAV}" + EOL +
            "{scientificName=Cirsium mohavense, reproductiveCondition=Flower:June;July;August;September;October;November, scientificNameAuthorship=(Greene) Petr., country=United States, decimalLatitude=36.0, DatasetName=SPNHCDEMO, fieldNumber=1940, geodeticDatum=WGS84, county=Kern, family=Asteraceae, catalogNumber=100006, recordedBy=Mark Elvin, month=5, decimalLongitude=-118.0, locality=Weldon Rancheria, stateProvince=California, Id=1024940053, year=1990, day=20, CollectionCode=FilteredPush, InstitutionCode=DAV}" + EOL +
            "{scientificName=Tragopogon porrifolius, reproductiveCondition=Flower:April;May;June;July;August, scientificNameAuthorship=L., country=United States, decimalLatitude=21.312, DatasetName=SPNHCDEMO, fieldNumber=606, geodeticDatum=WGS84, county=Honolulu, family=Asteraceae, catalogNumber=100007, recordedBy=Mark Elvin, month=5, decimalLongitude=-157.8055, locality=Honolulu 3115 Kaloaluiki Place, stateProvince=Hawaii, Id=927140834, year=1990, day=16, CollectionCode=FilteredPush, InstitutionCode=DAV}" + EOL +
            "{scientificName=Logfia gallica, reproductiveCondition=Flower:July;August; September, scientificNameAuthorship=Coss. & Germ., country=United States, decimalLatitude=40.0, DatasetName=SPNHCDEMO, fieldNumber=107702, geodeticDatum=WGS84, county=, family=Asteraceae, catalogNumber=100008, recordedBy=Joseph P. Tracy, month=7, decimalLongitude=-104.0, locality=Carlotta Northern Coast Ranges, stateProvince=California, Id=127140835, year=1973, day=31, CollectionCode=FilteredPush, InstitutionCode=DAV}";
        
        assertEquals(expected, outputBuffer.toString());
    }

}
