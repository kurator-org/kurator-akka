package org.kurator.akka.actors;

import org.junit.Test;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.YamlFileWorkflowRunner;
import org.kurator.akka.messages.EndOfStream;

public class TestPythonActorYaml extends KuratorAkkaTestCase {
    
    static final String RESOURCE_PATH = "classpath:/org/kurator/akka/python/";
    
    @Test
    public void testPythonActor_MultiplierWorkflow() throws Exception {

        WorkflowRunner wr = new YamlFileWorkflowRunner(RESOURCE_PATH + "multiplier_wf.yaml");
        wr.outputStream(stdoutStream);
        
        wr.build();
        wr.start();
        wr.tellWorkflow(1);
        wr.tellWorkflow(2);
        wr.tellWorkflow(3);
        wr.tellWorkflow(4);
        wr.tellWorkflow(5);
        wr.tellWorkflow(new EndOfStream());
        wr.await();
        
        assertEquals("2,4,6,8,10", stdoutBuffer.toString());
    }

    @Test
    public void testPythonActor_TriggeredRampWorkflow() throws Exception {

        WorkflowRunner wr = new YamlFileWorkflowRunner(RESOURCE_PATH + "triggered_ramp_wf.yaml");
        wr.outputStream(stdoutStream);
        
        wr.build();
        wr.start();
        wr.tellWorkflow(5);
        wr.tellWorkflow(1);
        wr.tellWorkflow(3);
        wr.tellWorkflow(new EndOfStream());
        wr.await();
        
        assertEquals("1,2,3,4,5,1,1,2,3", stdoutBuffer.toString());
    }
    
    @Test
    public void testPythonActor_RampWorkflow() throws Exception {

        WorkflowRunner wr = new YamlFileWorkflowRunner(RESOURCE_PATH + "ramp_wf.yaml");
        wr.outputStream(stdoutStream);
        wr.apply("Ramp.start", 3);
        wr.apply("Ramp.end", 30);
        wr.apply("Ramp.step", 3);
        wr.build();
        wr.start();
        wr.await();
        
        assertEquals("3,6,9,12,15,18,21,24,27,30", stdoutBuffer.toString());
    }
    
    @Test
    public void testPythonActor_CsvReadWriteWorkflow() throws Exception {

        WorkflowRunner wr = new YamlFileWorkflowRunner(RESOURCE_PATH + "csv_read_write_wf.yaml");
        wr.outputStream(stdoutStream);
        wr.apply("ReadInputCsv.filename", "src/test/resources/org/kurator/akka/samples/eight_specimen_records.csv");
        wr.apply("WriteOutputCsv.quoteCharacter", '\'');
        wr.build();
        wr.start();
        wr.await();
        
        assertEquals(
            "'scientificNameAuthorship ',year,'fieldNumber ','scientificName ',stateProvince,Id,county,day,'CollectionCode ','catalogNumber ','geodeticDatum ',locality,'decimalLongitude ','decimalLatitude ','reproductiveCondition ','family ','DatasetName ',country,recordedBy,'InstitutionCode ',month"   + EOL +
            "auct.,2007,126,Taraxacum erythrospermum,Washington,926137834,Chelan,29,FilteredPush,100001,WGS84,Wenatchee National Forest. South Cle Elum Ridge.,-120.9263,47.1384,Flower:March;April;May;June;July;August,'Asteraceae  ',SPNHCDEMO,United States,Megan A. Jensen,'DAV ',6"                       + EOL +
            "'(L.) DC. ',2003,2503,Acroptilon repens,Colorado,925533578,,27,FilteredPush,100002,WGS84,Yucca House National Monument,-108.68,-37.25,Flower:March;April;May;June;July;August;September,'Asteraceae  ',SPNHCDEMO,United States,G. Rink,'DAV ',7"                                                   + EOL +
            "'(Greene) Petr.',1990,2938,Cirsium mohavense,California,1024940716,San Bernardino,11,FilteredPush,100003,WGS84,400 m north of Cushenbury Springs,-117.0,34.0,Flower:June;July;August;September;October;November,'Asteraceae  ',SPNHCDEMO,United States,Mark Elvin,'DAV ',5"                        + EOL +
            "'(Greene) Petr.',1990,3000,Cirsium mohavense,California,1024940765,,21,FilteredPush,100004,WGS84,Northern end of The Owens Valle Bishop,-118.0,37.0,Flower:June;July;August;September;October;November,'Asteraceae  ',SPNHCDEMO,United States,Mark Elvin,'DAV ',5"                                 + EOL +
            "'(Greene) Petr.',1990,2940,Cirsium mohavense,California,1024940674,San Bernardino,12,FilteredPush,100005,WGS84,l mi. NW of Lucerne Valley town center,-117.0,34.0,Flower:June;July;August;September;October;November,'Asteraceae  ',SPNHCDEMO,United States,Mark Elvin,'DAV ',5"                   + EOL +
            "'(Greene) Petr.',1990,1940,Cirsium mohavense,California,1024940053,Kern,20,FilteredPush,100006,WGS84,Weldon Rancheria,-118.0,36.0,Flower:June;July;August;September;October;November,'Asteraceae  ',SPNHCDEMO,United States,Mark Elvin,'DAV ',5"                                                   + EOL +
            "L.,1990,606,Tragopogon porrifolius,Hawaii,927140834,Honolulu,16,FilteredPush,100007,WGS84,'Honolulu 3115 Kaloaluiki Place ',-157.8055,21.312,Flower:April;May;June;July;August,'Asteraceae ',SPNHCDEMO,United States,'Mark Elvin ','DAV ',5"                                                       + EOL +
            "'Coss. & Germ. ',1973,107702,Logfia gallica,California,127140835,,31,FilteredPush,100008,WGS84,Carlotta Northern Coast Ranges,-104.0,40.0,Flower:July;August; September,'Asteraceae  ',SPNHCDEMO,United States,Joseph P. Tracy,'DAV ',7"                                                           + EOL,
            stdoutBuffer.toString());
    }
}
