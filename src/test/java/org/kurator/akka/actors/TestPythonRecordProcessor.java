package org.kurator.akka.actors;

import org.junit.Test;
import org.kurator.akka.KuratorCLI;
import org.kurator.akka.KuratorAkkaTestCase;

public class TestPythonRecordProcessor extends KuratorAkkaTestCase {
    
    static final String RESOURCE_PATH = "packages/kurator_akka/records/";
    
    @Test
    public void testPythonRecordProcessor() throws Exception {

        String[] args = {
                "-f", RESOURCE_PATH + "record_filter.yaml", 
                "-p", "in=src/test/resources/org/kurator/akka/samples/data/eight_specimen_records.csv"
        };
        
        KuratorCLI.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals("", stderrBuffer.toString());
        assertEquals(
                "catalogNumber,recordedBy,fieldNumber,year,month,day,decimalLatitude,decimalLongitude,geodeticDatum,country,stateProvince,county,locality,family,scientificName,scientificNameAuthorship,reproductiveCondition,InstitutionCode,CollectionCode,DatasetName,Id" + EOL + 
                "100003,Mark Elvin,2938,1990,5,11,34.0,-117.0,WGS84,United States,California,San Bernardino,400 m north of Cushenbury Springs,Asteraceae,Cirsium mohavense,(Greene) Petr.,Flower:June;July;August;September;October;November,DAV,FilteredPush,SPNHCDEMO,1024940716" + EOL + 
                "100004,Mark Elvin,3000,1990,5,21,37.0,-118.0,WGS84,United States,California,,Northern end of The Owens Valle Bishop,Asteraceae,Cirsium mohavense,(Greene) Petr.,Flower:June;July;August;September;October;November,DAV,FilteredPush,SPNHCDEMO,1024940765" + EOL + 
                "100005,Mark Elvin,2940,1990,5,12,34.0,-117.0,WGS84,United States,California,San Bernardino,l mi. NW of Lucerne Valley town center,Asteraceae,Cirsium mohavense,(Greene) Petr.,Flower:June;July;August;September;October;November,DAV,FilteredPush,SPNHCDEMO,1024940674" + EOL + 
                "100006,Mark Elvin,1940,1990,5,20,36.0,-118.0,WGS84,United States,California,Kern,Weldon Rancheria,Asteraceae,Cirsium mohavense,(Greene) Petr.,Flower:June;July;August;September;October;November,DAV,FilteredPush,SPNHCDEMO,1024940053" + EOL + 
                "100007,Mark Elvin,606,1990,5,16,21.312,-157.8055,WGS84,United States,Hawaii,Honolulu,Honolulu 3115 Kaloaluiki Place,Asteraceae,Tragopogon porrifolius,L.,Flower:April;May;June;July;August,DAV,FilteredPush,SPNHCDEMO,927140834" + EOL,
            stdoutBuffer.toString());
    }

}
