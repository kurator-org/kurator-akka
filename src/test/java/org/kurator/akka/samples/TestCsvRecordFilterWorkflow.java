package org.kurator.akka.samples;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;

import org.kurator.akka.ActorConfig;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.YamlFileWorkflowRunner;

public class TestCsvRecordFilterWorkflow extends KuratorAkkaTestCase {

    private WorkflowRunner wr;
    private static final String SAMPLES_DIR = "src/main/resources/org/kurator/akka/samples/";

    @SuppressWarnings("unused")
    private ActorConfig printerActor;

    private OutputStream writeBuffer;
    private Writer bufferWriter;
    
    @Override
    public void setUp() throws Exception {

        super.setUp();

        writeBuffer = new ByteArrayOutputStream();
        bufferWriter = new OutputStreamWriter(writeBuffer);
       
        wr = new YamlFileWorkflowRunner("file:" + SAMPLES_DIR + "record_filter.yaml");
        wr.apply("CsvWriter.outputWriter", bufferWriter);
        wr.apply("required", Arrays.asList(new String[] {"county"}));
    }

    public void testCsvFileReader_EightSpecimenRecordFile() throws Exception {

        wr.apply("in", SAMPLES_DIR + "/data/eight_specimen_records.csv");

        wr.run();

        String expected =
            "catalogNumber,recordedBy,fieldNumber,year,month,day,decimalLatitude,decimalLongitude,geodeticDatum,country,stateProvince,county,locality,family,scientificName,scientificNameAuthorship,reproductiveCondition,InstitutionCode,CollectionCode,DatasetName,Id" + EOL +
            "100001,Megan A. Jensen,126,2007,6,29,47.1384,-120.9263,WGS84,United States,Washington,Chelan,Wenatchee National Forest. South Cle Elum Ridge.,Asteraceae,Taraxacum erythrospermum,auct.,Flower:March;April;May;June;July;August,DAV,FilteredPush,SPNHCDEMO,926137834" + EOL +
            "100003,Mark Elvin,2938,1990,5,11,34.0,-117.0,WGS84,United States,California,San Bernardino,400 m north of Cushenbury Springs,Asteraceae,Cirsium mohavense,(Greene) Petr.,Flower:June;July;August;September;October;November,DAV,FilteredPush,SPNHCDEMO,1024940716" + EOL +
            "100005,Mark Elvin,2940,1990,5,12,34.0,-117.0,WGS84,United States,California,San Bernardino,l mi. NW of Lucerne Valley town center,Asteraceae,Cirsium mohavense,(Greene) Petr.,Flower:June;July;August;September;October;November,DAV,FilteredPush,SPNHCDEMO,1024940674" + EOL +
            "100006,Mark Elvin,1940,1990,5,20,36.0,-118.0,WGS84,United States,California,Kern,Weldon Rancheria,Asteraceae,Cirsium mohavense,(Greene) Petr.,Flower:June;July;August;September;October;November,DAV,FilteredPush,SPNHCDEMO,1024940053" + EOL +
            "100007,Mark Elvin,606,1990,5,16,21.312,-157.8055,WGS84,United States,Hawaii,Honolulu,Honolulu 3115 Kaloaluiki Place,Asteraceae,Tragopogon porrifolius,L.,Flower:April;May;June;July;August,DAV,FilteredPush,SPNHCDEMO,927140834" + EOL
        ;
        assertEquals(expected, writeBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }
}
