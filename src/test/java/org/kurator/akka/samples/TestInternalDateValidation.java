package org.kurator.akka.samples;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.kurator.akka.KuratorAkka;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.WorkflowBuilder;
import org.kurator.akka.YamlFileWorkflowBuilder;

public class TestInternalDateValidation extends KuratorAkkaTestCase {

    static final String RESOURCE_PATH = "classpath:/org/kurator/akka/samples/";

    private OutputStream outputBuffer;
    private Writer bufferWriter;
   
    @Override
    public void setUp() throws Exception {
        super.setUp();
        KuratorAkka.enableLog4J();
        
        outputBuffer = new ByteArrayOutputStream();
        bufferWriter = new OutputStreamWriter(outputBuffer);
    }
    
    public void testInternalDataValidation_OneSpecimenRecord() throws Exception {        

        WorkflowBuilder builder = new YamlFileWorkflowBuilder(RESOURCE_PATH + "internal_date_validation.yaml");
        builder.apply("in", "src/main/resources/org/kurator/akka/samples/data/one_specimen_record.csv");
        builder.apply("writer", bufferWriter);
        builder.build();
        builder.run();
        
        String expected = 
            "catalogNumber,recordedBy,fieldNumber,year,month,day,decimalLatitude,decimalLongitude,geodeticDatum,country,stateProvince,county,locality,family,scientificName,scientificNameAuthorship,reproductiveCondition,InstitutionCode,CollectionCode,DatasetName,Id,eventDate,dateComment,dateStatus,dateSource" + EOL +
            "100001,Megan A. Jensen,126,2007,6,29,47.1384,-120.9263,WGS84,United States,Washington,Chelan,Wenatchee National Forest. South Cle Elum Ridge.,Asteraceae,Taraxacum erythrospermum,auct.,Flower:March;April;May;June;July;August,DAV,FilteredPush,SPNHCDEMO,926137834,2007-06-29,| eventDate is null? | EventDate is constructed from atomic fields | Unable to get the Life span data of collector:Megan A. Jensen,Filled in,eventDate:null#Filteredpush Entomologists List" + EOL;
         
        assertEquals(expected, outputBuffer.toString());
    }
}