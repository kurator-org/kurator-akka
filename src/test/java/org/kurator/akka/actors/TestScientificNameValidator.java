package org.kurator.akka.actors;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.kurator.akka.ActorBuilder;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.WorkflowBuilder;

public class TestScientificNameValidator extends KuratorAkkaTestCase {

    private WorkflowBuilder wfb;
    private OutputStream outputBuffer;
    private ActorBuilder csvReader;
    private ActorBuilder csvWriter;
    private ActorBuilder sciNameValidator;
    private Writer bufferWriter;
    
    @Override
    public void setUp() {
        
        super.setUp();
   
        outputBuffer = new ByteArrayOutputStream();
        bufferWriter = new OutputStreamWriter(outputBuffer);
       
        wfb = new WorkflowBuilder();
   
        csvReader = wfb.createActorBuilder()
                .actorClass(CsvFileReader.class)
                .parameter("recordClass", "org.kurator.akka.data.OrderedSpecimenRecord");
        
        sciNameValidator = wfb.createActorBuilder()
                .actorClass(ScientificNameValidator.class)
                .listensTo(csvReader);
        
        csvWriter = wfb.createActorBuilder()
                .actorClass(CsvSpecimenFileWriter.class)
                .listensTo(sciNameValidator);
    }
    
    public void testScientficNameValidator_OneRecord() throws Exception {

        csvReader.parameter("filePath", "src/main/resources/org/kurator/akka/samples/data/one_specimen_record.csv" );
        csvWriter.parameter("outputWriter", bufferWriter);
        
        wfb.build();
        wfb.startWorkflow();
        wfb.awaitWorkflow();
        
        String expected = 
            "catalogNumber,recordedBy,fieldNumber,year,month,day,decimalLatitude,decimalLongitude,geodeticDatum,country,stateProvince,county,locality,family,scientificName,scientificNameAuthorship,reproductiveCondition,InstitutionCode,CollectionCode,DatasetName,Id,scientificName,scientificNameAuthorship,scinComment,scinStatus,scinSource" + EOL +
            "100001,Megan A. Jensen,126,2007,6,29,47.1384,-120.9263,WGS84,United States,Washington,Chelan,Wenatchee National Forest. South Cle Elum Ridge.,Asteraceae,Taraxacum erythrospermum,Andrz. ex Besser,Flower:March;April;May;June;July;August,DAV,FilteredPush,SPNHCDEMO,926137834,Taraxacum erythrospermum,Andrz. ex Besser,| can't construct sciName from atomic fields | The provided name: Taraxacum+erythrospermum is valid after checking misspelling | The original SciName and Authorship are curated,Curated,scientificName:Taraxacum erythrospermum#scientificNameAuthorship:auct.#Global Name Resolver | Catalog of Life" + EOL;
         
        assertEquals(expected, outputBuffer.toString());
    }

    public void testScientficNameValidator_EightRecords() throws Exception {

       csvReader.parameter("filePath", "src/main/resources/org/kurator/akka/samples/data/eight_specimen_records.csv" );
       csvWriter.parameter("outputWriter", bufferWriter);
       
       wfb.build();
       wfb.startWorkflow();
       wfb.awaitWorkflow();
       
       String expected = 
           "catalogNumber,recordedBy,fieldNumber,year,month,day,decimalLatitude,decimalLongitude,geodeticDatum,country,stateProvince,county,locality,family,scientificName,scientificNameAuthorship,reproductiveCondition,InstitutionCode,CollectionCode,DatasetName,Id,scientificName,scientificNameAuthorship,scinComment,scinStatus,scinSource" + EOL +
           "100001,Megan A. Jensen,126,2007,6,29,47.1384,-120.9263,WGS84,United States,Washington,Chelan,Wenatchee National Forest. South Cle Elum Ridge.,Asteraceae,Taraxacum erythrospermum,Andrz. ex Besser,Flower:March;April;May;June;July;August,DAV,FilteredPush,SPNHCDEMO,926137834,Taraxacum erythrospermum,Andrz. ex Besser,| can't construct sciName from atomic fields | The provided name: Taraxacum+erythrospermum is valid after checking misspelling | The original SciName and Authorship are curated,Curated,scientificName:Taraxacum erythrospermum#scientificNameAuthorship:auct.#Global Name Resolver | Catalog of Life" + EOL +
           "100002,G. Rink,2503,2003,7,27,-37.25,-108.68,WGS84,United States,Colorado,,Yucca House National Monument,Asteraceae,Acroptilon repens,(L.) Hidalgo,Flower:March;April;May;June;July;August;September,DAV,FilteredPush,SPNHCDEMO,925533578,Acroptilon repens,(L.) Hidalgo,| can't construct sciName from atomic fields | The provided name: Acroptilon+repens is valid after checking misspelling | found and solved synonym | The original SciName and Authorship are curated,Curated,scientificName:Acroptilon repens#scientificNameAuthorship:(L.) DC.#Global Name Resolver | Catalog of Life" + EOL +
           "100003,Mark Elvin,2938,1990,5,11,34.0,-117.0,WGS84,United States,California,San Bernardino,400 m north of Cushenbury Springs,Asteraceae,Cirsium mohavense,(Greene) Petr.,Flower:June;July;August;September;October;November,DAV,FilteredPush,SPNHCDEMO,1024940716,Cirsium mohavense,(Greene) Petr.,| can't construct sciName from atomic fields | The provided name: Cirsium+mohavense is valid after checking misspelling | The original SciName and Authorship are valid,Valid,scientificName:Cirsium mohavense#scientificNameAuthorship:(Greene) Petr.#Global Name Resolver | Catalog of Life" + EOL +
           "100004,Mark Elvin,3000,1990,5,21,37.0,-118.0,WGS84,United States,California,,Northern end of The Owens Valle Bishop,Asteraceae,Cirsium mohavense,(Greene) Petr.,Flower:June;July;August;September;October;November,DAV,FilteredPush,SPNHCDEMO,1024940765,Cirsium mohavense,(Greene) Petr.,| can't construct sciName from atomic fields | The provided name: Cirsium+mohavense is valid after checking misspelling | The original SciName and Authorship are valid,Valid,scientificName:Cirsium mohavense#scientificNameAuthorship:(Greene) Petr.#Global Name Resolver | Catalog of Life" + EOL +
           "100005,Mark Elvin,2940,1990,5,12,34.0,-117.0,WGS84,United States,California,San Bernardino,l mi. NW of Lucerne Valley town center,Asteraceae,Cirsium mohavense,(Greene) Petr.,Flower:June;July;August;September;October;November,DAV,FilteredPush,SPNHCDEMO,1024940674,Cirsium mohavense,(Greene) Petr.,| can't construct sciName from atomic fields | The provided name: Cirsium+mohavense is valid after checking misspelling | The original SciName and Authorship are valid,Valid,scientificName:Cirsium mohavense#scientificNameAuthorship:(Greene) Petr.#Global Name Resolver | Catalog of Life" + EOL +
           "100006,Mark Elvin,1940,1990,5,20,36.0,-118.0,WGS84,United States,California,Kern,Weldon Rancheria,Asteraceae,Cirsium mohavense,(Greene) Petr.,Flower:June;July;August;September;October;November,DAV,FilteredPush,SPNHCDEMO,1024940053,Cirsium mohavense,(Greene) Petr.,| can't construct sciName from atomic fields | The provided name: Cirsium+mohavense is valid after checking misspelling | The original SciName and Authorship are valid,Valid,scientificName:Cirsium mohavense#scientificNameAuthorship:(Greene) Petr.#Global Name Resolver | Catalog of Life" + EOL +
           "100007,Mark Elvin,606,1990,5,16,21.312,-157.8055,WGS84,United States,Hawaii,Honolulu,Honolulu 3115 Kaloaluiki Place,Asteraceae,Tragopogon porrifolius,L.,Flower:April;May;June;July;August,DAV,FilteredPush,SPNHCDEMO,927140834,Tragopogon porrifolius,L.,| can't construct sciName from atomic fields | The provided name: Tragopogon+porrifolius is valid after checking misspelling | The original SciName and Authorship are valid,Valid,scientificName:Tragopogon porrifolius#scientificNameAuthorship:L.#Global Name Resolver | Catalog of Life" + EOL +
           "100008,Joseph P. Tracy,107702,1973,7,31,40.0,-104.0,WGS84,United States,California,,Carlotta Northern Coast Ranges,Asteraceae,Logfia gallica,(L.) L.,Flower:July;August; September,DAV,FilteredPush,SPNHCDEMO,127140835,Logfia gallica,(L.) L.,| can't construct sciName from atomic fields | The provided name: Logfia+gallica is valid after checking misspelling | found and solved synonym | The original SciName and Authorship are curated,Curated,scientificName:Logfia gallica#scientificNameAuthorship:Coss. & Germ.#Global Name Resolver | Catalog of Life" + EOL;
       
       assertEquals(expected, outputBuffer.toString());
   }
}
