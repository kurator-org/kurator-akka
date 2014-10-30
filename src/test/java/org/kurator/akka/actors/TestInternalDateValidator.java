package org.kurator.akka.actors;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.kurator.akka.ActorBuilder;
import org.kurator.akka.WorkflowBuilder;

import junit.framework.TestCase;

public class TestInternalDateValidator extends TestCase {

    static final String EOL = System.getProperty("line.separator");
    private WorkflowBuilder wfb;
    private OutputStream outputBuffer;
    private ActorBuilder csvReader;
    private ActorBuilder csvWriter;
    private ActorBuilder sciNameValidator;
    private Writer bufferWriter;
    
    @Override
    public void setUp() {
   
        outputBuffer = new ByteArrayOutputStream();
        bufferWriter = new OutputStreamWriter(outputBuffer);
       
        wfb = new WorkflowBuilder();
   
        csvReader = wfb.createActorBuilder()
                .actorClass(CsvSpecimenFileReader.class)
                .parameter("useOrderedSpecimenRecord", true);
        
        sciNameValidator = wfb.createActorBuilder()
                .actorClass(InternalDateValidator.class)
                .listensTo(csvReader);
        
        csvWriter = wfb.createActorBuilder()
                .actorClass(CsvSpecimenFileWriter.class)
                .listensTo(sciNameValidator);
    }
    
    public void testInternalDateValidator_OneRecord() throws Exception {

        csvReader.parameter("filePath", "src/main/resources/org/kurator/akka/samples/data/one_specimen_record.csv" );
        csvWriter.parameter("writer", bufferWriter);
        
        wfb.build();
        wfb.startWorkflow();
        wfb.awaitWorkflow();
        
        String expected = 
            "catalogNumber,recordedBy,fieldNumber,year,month,day,decimalLatitude,decimalLongitude,geodeticDatum,country,stateProvince,county,locality,family,scientificName,scientificNameAuthorship,reproductiveCondition,InstitutionCode,CollectionCode,DatasetName,Id,eventDate,dateComment,dateStatus,dateSource" + EOL +
            "100001,Megan A. Jensen,126,2007,6,29,47.1384,-120.9263,WGS84,United States,Washington,Chelan,Wenatchee National Forest. South Cle Elum Ridge.,Asteraceae,Taraxacum erythrospermum,auct.,Flower:March;April;May;June;July;August,DAV,FilteredPush,SPNHCDEMO,926137834,2007-06-29,| eventDate is null? | EventDate is constructed from atomic fields | Unable to get the Life span data of collector:Megan A. Jensen,Filled in,eventDate:null#Filteredpush Entomologists List" + EOL;
         
        assertEquals(expected, outputBuffer.toString());
    }

    public void testInternalDateValidator_EightRecords() throws Exception {

       csvReader.parameter("filePath", "src/main/resources/org/kurator/akka/samples/data/eight_specimen_records.csv" );
       csvWriter.parameter("writer", bufferWriter);
       
       wfb.build();
       wfb.startWorkflow();
       wfb.awaitWorkflow();
       
       String expected = 
           "catalogNumber,recordedBy,fieldNumber,year,month,day,decimalLatitude,decimalLongitude,geodeticDatum,country,stateProvince,county,locality,family,scientificName,scientificNameAuthorship,reproductiveCondition,InstitutionCode,CollectionCode,DatasetName,Id,eventDate,dateComment,dateStatus,dateSource" + EOL +
           "100001,Megan A. Jensen,126,2007,6,29,47.1384,-120.9263,WGS84,United States,Washington,Chelan,Wenatchee National Forest. South Cle Elum Ridge.,Asteraceae,Taraxacum erythrospermum,auct.,Flower:March;April;May;June;July;August,DAV,FilteredPush,SPNHCDEMO,926137834,2007-06-29,| eventDate is null? | EventDate is constructed from atomic fields | Unable to get the Life span data of collector:Megan A. Jensen,Filled in,eventDate:null#Filteredpush Entomologists List" + EOL +
           "100002,G. Rink,2503,2003,7,27,-37.25,-108.68,WGS84,United States,Colorado,,Yucca House National Monument,Asteraceae,Acroptilon repens,(L.) DC.,Flower:March;April;May;June;July;August;September,DAV,FilteredPush,SPNHCDEMO,925533578,2003-07-27,| eventDate is null? | EventDate is constructed from atomic fields | Unable to get the Life span data of collector:G. Rink,Filled in,eventDate:null#Filteredpush Entomologists List" + EOL +
           "100003,Mark Elvin,2938,1990,5,11,34.0,-117.0,WGS84,United States,California,San Bernardino,400 m north of Cushenbury Springs,Asteraceae,Cirsium mohavense,(Greene) Petr.,Flower:June;July;August;September;October;November,DAV,FilteredPush,SPNHCDEMO,1024940716,1990-05-11,| eventDate is null? | EventDate is constructed from atomic fields | Unable to get the Life span data of collector:Mark Elvin,Filled in,eventDate:null#Filteredpush Entomologists List" + EOL +
           "100004,Mark Elvin,3000,1990,5,21,37.0,-118.0,WGS84,United States,California,,Northern end of The Owens Valle Bishop,Asteraceae,Cirsium mohavense,(Greene) Petr.,Flower:June;July;August;September;October;November,DAV,FilteredPush,SPNHCDEMO,1024940765,1990-05-21,| eventDate is null? | EventDate is constructed from atomic fields | Unable to get the Life span data of collector:Mark Elvin,Filled in,eventDate:null#Filteredpush Entomologists List" + EOL +
           "100005,Mark Elvin,2940,1990,5,12,34.0,-117.0,WGS84,United States,California,San Bernardino,l mi. NW of Lucerne Valley town center,Asteraceae,Cirsium mohavense,(Greene) Petr.,Flower:June;July;August;September;October;November,DAV,FilteredPush,SPNHCDEMO,1024940674,1990-05-12,| eventDate is null? | EventDate is constructed from atomic fields | Unable to get the Life span data of collector:Mark Elvin,Filled in,eventDate:null#Filteredpush Entomologists List" + EOL +
           "100006,Mark Elvin,1940,1990,5,20,36.0,-118.0,WGS84,United States,California,Kern,Weldon Rancheria,Asteraceae,Cirsium mohavense,(Greene) Petr.,Flower:June;July;August;September;October;November,DAV,FilteredPush,SPNHCDEMO,1024940053,1990-05-20,| eventDate is null? | EventDate is constructed from atomic fields | Unable to get the Life span data of collector:Mark Elvin,Filled in,eventDate:null#Filteredpush Entomologists List" + EOL +
           "100007,Mark Elvin,606,1990,5,16,21.312,-157.8055,WGS84,United States,Hawaii,Honolulu,Honolulu 3115 Kaloaluiki Place,Asteraceae,Tragopogon porrifolius,L.,Flower:April;May;June;July;August,DAV,FilteredPush,SPNHCDEMO,927140834,1990-05-16,| eventDate is null? | EventDate is constructed from atomic fields | Unable to get the Life span data of collector:Mark Elvin,Filled in,eventDate:null#Filteredpush Entomologists List" + EOL +
           "100008,Joseph P. Tracy,107702,1973,7,31,40.0,-104.0,WGS84,United States,California,,Carlotta Northern Coast Ranges,Asteraceae,Logfia gallica,Coss. & Germ.,Flower:July;August; September,DAV,FilteredPush,SPNHCDEMO,127140835,1973-07-31,| eventDate is null? | EventDate is constructed from atomic fields | Unable to get the Life span data of collector:Joseph P. Tracy,Filled in,eventDate:null#Filteredpush Entomologists List" + EOL;
       assertEquals(expected, outputBuffer.toString());
   }
}
