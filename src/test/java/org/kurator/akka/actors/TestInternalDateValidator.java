package org.kurator.akka.actors;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.kurator.akka.ActorBuilder;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.WorkflowBuilder;

public class TestInternalDateValidator extends KuratorAkkaTestCase {

    private WorkflowBuilder wfb;
    private OutputStream writeBuffer;
    private ActorBuilder csvReader;
    private ActorBuilder dateValidator;
    private ActorBuilder csvWriter;
    private Writer bufferWriter;
    
    @Override
    public void setUp() {
        
        super.setUp();
   
        writeBuffer = new ByteArrayOutputStream();
        bufferWriter = new OutputStreamWriter(writeBuffer);
       
        wfb = new WorkflowBuilder();
   
        csvReader = wfb.createActorBuilder()
                .actorClass(CsvFileReader.class)
                .parameter("recordClass", "org.kurator.akka.data.OrderedSpecimenRecord");
        
        dateValidator = wfb.createActorBuilder()
                .actorClass(InternalDateValidator.class)
                .listensTo(csvReader);
        
        csvWriter = wfb.createActorBuilder()
                .actorClass(CsvSpecimenFileWriter.class)
                .listensTo(dateValidator);
    }
    
    public void testInternalDateValidator_OneRecord() throws Exception {

        csvReader.parameter("filePath", "src/main/resources/org/kurator/akka/samples/data/one_specimen_record.csv" );
        csvWriter.parameter("outputWriter", bufferWriter);
        
        wfb.build();
        wfb.startWorkflow();
        wfb.awaitWorkflow();
        
        String expected = 
            "catalogNumber,recordedBy,fieldNumber,year,month,day,decimalLatitude,decimalLongitude,geodeticDatum,country,stateProvince,county,locality,family,scientificName,scientificNameAuthorship,reproductiveCondition,InstitutionCode,CollectionCode,DatasetName,Id,eventDate,dateComment,dateStatus,dateSource" + EOL +
            "100001,Megan A. Jensen,126,2007,6,29,47.1384,-120.9263,WGS84,United States,Washington,Chelan,Wenatchee National Forest. South Cle Elum Ridge.,Asteraceae,Taraxacum erythrospermum,auct.,Flower:March;April;May;June;July;August,DAV,FilteredPush,SPNHCDEMO,926137834,2007-06-29,| eventDate is null? | EventDate is constructed from atomic fields | Unable to get the Life span data of collector:Megan A. Jensen,Filled in,eventDate:null#Filteredpush Entomologists List" + EOL;
         
        assertEquals(expected, writeBuffer.toString());
    }

    public void testInternalDateValidator_EightRecords() throws Exception {

       csvReader.parameter("filePath", "src/main/resources/org/kurator/akka/samples/data/eight_specimen_records.csv" );
       csvWriter.parameter("outputWriter", bufferWriter);
       
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
       assertEquals(expected, writeBuffer.toString());
   }

    public void testInternalDateValidator_MCZ_IPT_FirstRecord() throws Exception {

        csvReader.parameter("filePath", "src/main/resources/org/kurator/akka/samples/data/mcz_ipt_first_record.csv" );
        csvWriter.parameter("outputWriter", bufferWriter);
        
        wfb.build();
        wfb.startWorkflow();
        wfb.awaitWorkflow();
        
        String expected = 
            "id,type,modified,language,rightsHolder,references,institutionID,institutionCode,collectionCode,ownerInstitutionCode,basisOfRecord,informationWithheld,dynamicProperties,catalogNumber,recordNumber,recordedBy,individualCount,sex,lifeStage,preparations,disposition,otherCatalogNumbers,associatedMedia,associatedOccurrences,associatedSequences,associatedTaxa,samplingProtocol,eventDate,startDayOfYear,year,month,day,verbatimEventDate,habitat,fieldNumber,higherGeography,continent,waterBody,islandGroup,island,country,stateProvince,county,locality,verbatimElevation,minimumElevationInMeters,maximumElevationInMeters,minimumDepthInMeters,maximumDepthInMeters,verbatimLatitude,verbatimLongitude,verbatimCoordinateSystem,decimalLatitude,decimalLongitude,geodeticDatum,coordinateUncertaintyInMeters,coordinatePrecision,georeferencedBy,georeferenceProtocol,georeferenceSources,geo referenceRemarks,earliestEraOrLowestErathem,latestEraOrHighestErathem,earliestPeriodOrLowestSystem,latestPeriodOrHighestSystem,earliestEpochOrLowestSeries,latestEpochOrHighestSeries,earliestAgeOrLowestStage,latestAgeOrHighestStage,lithostratigraphicTerms,group,formation,member,bed,identifiedBy,dateIdentified,identificationRemarks,identificationQualifier,identificationVerificationStatus,typeStatus,scientificName,higherClassification,kingdom,phylum,class,order,family,genus,specificEpithet,infraspecificEpithet,taxonRank,verbatimTaxonRank,scientificNameAuthorship,nomenclaturalCode,dateComment,dateStatus,dateSource" + EOL +
            "MCZ:Herp:R-90780,PhysicalObject,2012-12-06 08:47:03.0,en,President and Fellows of Harvard College,http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90780,b4640710-8e03-11d8-b956-b8a03c50a862,MCZ,Herp,\"Museum of Comparative Zoology, Harvard University\",PreservedSpecimen,,,R-90780,14942,Fred Parker,1,,,whole animal (ethanol),in collection,collector number=14942; muse location number=ZR90753,http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90780,,,,,1965-09-30,0,,,,30/9/1965-30/9/1965,,,\"Papua New Guinea, Eastern Highlands Province\",,,,,Papua New Guinea,Eastern Highlands Province,,New Guinea: Purosa,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,Catalog,,,,,,Emoia pallidiceps,Animalia Chordata Reptilia Lepidosauromorpha Squamata Sauria Scincidae Emoia pallidiceps,Animalia,Chordata,Reptilia,Squamata,Scincidae,Emoia,pallidiceps,,,,,ICZN,| can't construct eventDate from atomic fields: string casting error | eventDate is in ISO format | eventDate is consistent with modified date | Unable to get the Life span data of collector:Fred Parker,Valid,eventDate:1965-09-30#Filteredpush Entomologists List" + EOL;
        assertEquals(expected, writeBuffer.toString());
    }

    public void testInternalDateValidator_MCZ_IPT_Snippet() throws Exception {

        csvReader.parameter("filePath", "src/main/resources/org/kurator/akka/samples/data/mcz_ipt_snippet.csv" );
        csvWriter.parameter("outputWriter", bufferWriter);
        
        wfb.build();
        wfb.startWorkflow();
        wfb.awaitWorkflow();
        
        String expected = 
            "id,type,modified,language,rightsHolder,references,institutionID,institutionCode,collectionCode,ownerInstitutionCode,basisOfRecord,informationWithheld,dynamicProperties,catalogNumber,recordNumber,recordedBy,individualCount,sex,lifeStage,preparations,disposition,otherCatalogNumbers,associatedMedia,associatedOccurrences,associatedSequences,associatedTaxa,samplingProtocol,eventDate,startDayOfYear,year,month,day,verbatimEventDate,habitat,fieldNumber,higherGeography,continent,waterBody,islandGroup,island,country,stateProvince,county,locality,verbatimElevation,minimumElevationInMeters,maximumElevationInMeters,minimumDepthInMeters,maximumDepthInMeters,verbatimLatitude,verbatimLongitude,verbatimCoordinateSystem,decimalLatitude,decimalLongitude,geodeticDatum,coordinateUncertaintyInMeters,coordinatePrecision,georeferencedBy,georeferenceProtocol,georeferenceSources,geo referenceRemarks,earliestEraOrLowestErathem,latestEraOrHighestErathem,earliestPeriodOrLowestSystem,latestPeriodOrHighestSystem,earliestEpochOrLowestSeries,latestEpochOrHighestSeries,earliestAgeOrLowestStage,latestAgeOrHighestStage,lithostratigraphicTerms,group,formation,member,bed,identifiedBy,dateIdentified,identificationRemarks,identificationQualifier,identificationVerificationStatus,typeStatus,scientificName,higherClassification,kingdom,phylum,class,order,family,genus,specificEpithet,infraspecificEpithet,taxonRank,verbatimTaxonRank,scientificNameAuthorship,nomenclaturalCode,dateComment,dateStatus,dateSource" + EOL +
            "MCZ:Herp:R-90780,PhysicalObject,2012-12-06 08:47:03.0,en,President and Fellows of Harvard College,http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90780,b4640710-8e03-11d8-b956-b8a03c50a862,MCZ,Herp,\"Museum of Comparative Zoology, Harvard University\",PreservedSpecimen,,,R-90780,14942,Fred Parker,1,,,whole animal (ethanol),in collection,collector number=14942; muse location number=ZR90753,http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90780,,,,,1965-09-30,0,,,,30/9/1965-30/9/1965,,,\"Papua New Guinea, Eastern Highlands Province\",,,,,Papua New Guinea,Eastern Highlands Province,,New Guinea: Purosa,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,Catalog,,,,,,Emoia pallidiceps,Animalia Chordata Reptilia Lepidosauromorpha Squamata Sauria Scincidae Emoia pallidiceps,Animalia,Chordata,Reptilia,Squamata,Scincidae,Emoia,pallidiceps,,,,,ICZN,| can't construct eventDate from atomic fields: string casting error | eventDate is in ISO format | eventDate is consistent with modified date | Unable to get the Life span data of collector:Fred Parker,Valid,eventDate:1965-09-30#Filteredpush Entomologists List" + EOL +
            "MCZ:Herp:R-90781,PhysicalObject,2012-12-06 08:47:03.0,en,President and Fellows of Harvard College,http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90781,b4640710-8e03-11d8-b956-b8a03c50a862,MCZ,Herp,\"Museum of Comparative Zoology, Harvard University\",PreservedSpecimen,,,R-90781,14942,Fred Parker,1,,,whole animal (ethanol),in collection,collector number=14942; muse location number=ZR90753,http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90781,,,,,1965-09-30,0,,,,30/9/1965-30/9/1965,,,\"Papua New Guinea, Eastern Highlands Province\",,,,,Papua New Guinea,Eastern Highlands Province,,New Guinea: Purosa,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,Catalog,,,,,,Emoia pallidiceps,Animalia Chordata Reptilia Lepidosauromorpha Squamata Sauria Scincidae Emoia pallidiceps,Animalia,Chordata,Reptilia,Squamata,Scincidae,Emoia,pallidiceps,,,,,ICZN,| can't construct eventDate from atomic fields: string casting error | eventDate is in ISO format | eventDate is consistent with modified date | Unable to get the Life span data of collector:Fred Parker,Valid,eventDate:1965-09-30#Filteredpush Entomologists List" + EOL +
            "MCZ:Herp:R-90782,PhysicalObject,2012-12-06 08:47:03.0,en,President and Fellows of Harvard College,http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90782,b4640710-8e03-11d8-b956-b8a03c50a862,MCZ,Herp,\"Museum of Comparative Zoology, Harvard University\",PreservedSpecimen,,,R-90782,14942,Fred Parker,1,,,whole animal (ethanol),in collection,collector number=14942; muse location number=ZR90753,http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90782,,,,,1965-09-30,0,,,,30/9/1965-30/9/1965,,,\"Papua New Guinea, Eastern Highlands Province\",,,,,Papua New Guinea,Eastern Highlands Province,,New Guinea: Purosa,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,Catalog,,,,,,Emoia pallidiceps,Animalia Chordata Reptilia Lepidosauromorpha Squamata Sauria Scincidae Emoia pallidiceps,Animalia,Chordata,Reptilia,Squamata,Scincidae,Emoia,pallidiceps,,,,,ICZN,| can't construct eventDate from atomic fields: string casting error | eventDate is in ISO format | eventDate is consistent with modified date | Unable to get the Life span data of collector:Fred Parker,Valid,eventDate:1965-09-30#Filteredpush Entomologists List" + EOL +
            "MCZ:Herp:R-90783,PhysicalObject,2012-12-06 08:47:03.0,en,President and Fellows of Harvard College,http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90783,b4640710-8e03-11d8-b956-b8a03c50a862,MCZ,Herp,\"Museum of Comparative Zoology, Harvard University\",PreservedSpecimen,,,R-90783,14942,Fred Parker,1,,,whole animal (ethanol),in collection,collector number=14942; muse location number=ZR90753,http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90783,,,,,1965-09-30,0,,,,30/9/1965-30/9/1965,,,\"Papua New Guinea, Eastern Highlands Province\",,,,,Papua New Guinea,Eastern Highlands Province,,New Guinea: Purosa,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,Catalog,,,,,,Emoia pallidiceps,Animalia Chordata Reptilia Lepidosauromorpha Squamata Sauria Scincidae Emoia pallidiceps,Animalia,Chordata,Reptilia,Squamata,Scincidae,Emoia,pallidiceps,,,,,ICZN,| can't construct eventDate from atomic fields: string casting error | eventDate is in ISO format | eventDate is consistent with modified date | Unable to get the Life span data of collector:Fred Parker,Valid,eventDate:1965-09-30#Filteredpush Entomologists List" + EOL +
            "MCZ:Herp:R-90784,PhysicalObject,2012-12-06 08:47:03.0,en,President and Fellows of Harvard College,http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90784,b4640710-8e03-11d8-b956-b8a03c50a862,MCZ,Herp,\"Museum of Comparative Zoology, Harvard University\",PreservedSpecimen,,,R-90784,14942,Fred Parker,1,,,whole animal (ethanol),in collection,collector number=14942; muse location number=ZR90753,http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90784,,,,,1965-09-30,0,,,,30/9/1965-30/9/1965,,,\"Papua New Guinea, Eastern Highlands Province\",,,,,Papua New Guinea,Eastern Highlands Province,,New Guinea: Purosa,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,Catalog,,,,,,Emoia pallidiceps,Animalia Chordata Reptilia Lepidosauromorpha Squamata Sauria Scincidae Emoia pallidiceps,Animalia,Chordata,Reptilia,Squamata,Scincidae,Emoia,pallidiceps,,,,,ICZN,| can't construct eventDate from atomic fields: string casting error | eventDate is in ISO format | eventDate is consistent with modified date | Unable to get the Life span data of collector:Fred Parker,Valid,eventDate:1965-09-30#Filteredpush Entomologists List" + EOL +
            "MCZ:Herp:R-90785,PhysicalObject,2012-12-06 08:47:03.0,en,President and Fellows of Harvard College,http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90785,b4640710-8e03-11d8-b956-b8a03c50a862,MCZ,Herp,\"Museum of Comparative Zoology, Harvard University\",PreservedSpecimen,,,R-90785,14942,Fred Parker,1,,,whole animal (ethanol),in collection,collector number=14942; muse location number=ZR90753,http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90785,,,,,1965-09-30,0,,,,30/9/1965-30/9/1965,,,\"Papua New Guinea, Eastern Highlands Province\",,,,,Papua New Guinea,Eastern Highlands Province,,New Guinea: Purosa,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,Catalog,,,,,,Emoia pallidiceps,Animalia Chordata Reptilia Lepidosauromorpha Squamata Sauria Scincidae Emoia pallidiceps,Animalia,Chordata,Reptilia,Squamata,Scincidae,Emoia,pallidiceps,,,,,ICZN,| can't construct eventDate from atomic fields: string casting error | eventDate is in ISO format | eventDate is consistent with modified date | Unable to get the Life span data of collector:Fred Parker,Valid,eventDate:1965-09-30#Filteredpush Entomologists List" + EOL +
            "MCZ:Herp:R-90786,PhysicalObject,2012-12-06 08:47:03.0,en,President and Fellows of Harvard College,http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90786,b4640710-8e03-11d8-b956-b8a03c50a862,MCZ,Herp,\"Museum of Comparative Zoology, Harvard University\",PreservedSpecimen,,,R-90786,14942,Fred Parker,1,,,whole animal (ethanol),in collection,collector number=14942; muse location number=ZR90753,http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90786,,,,,1965-09-30,0,,,,30/9/1965-30/9/1965,,,\"Papua New Guinea, Eastern Highlands Province\",,,,,Papua New Guinea,Eastern Highlands Province,,New Guinea: Purosa,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,Catalog,,,,,,Emoia pallidiceps,Animalia Chordata Reptilia Lepidosauromorpha Squamata Sauria Scincidae Emoia pallidiceps,Animalia,Chordata,Reptilia,Squamata,Scincidae,Emoia,pallidiceps,,,,,ICZN,| can't construct eventDate from atomic fields: string casting error | eventDate is in ISO format | eventDate is consistent with modified date | Unable to get the Life span data of collector:Fred Parker,Valid,eventDate:1965-09-30#Filteredpush Entomologists List" + EOL +
            "MCZ:Herp:R-90787,PhysicalObject,2012-12-06 08:47:03.0,en,President and Fellows of Harvard College,http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90787,b4640710-8e03-11d8-b956-b8a03c50a862,MCZ,Herp,\"Museum of Comparative Zoology, Harvard University\",PreservedSpecimen,,,R-90787,14942,Fred Parker,1,,,whole animal (ethanol),in collection,collector number=14942; muse location number=ZR90753,http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90787,,,,,1965-09-30,0,,,,30/9/1965-30/9/1965,,,\"Papua New Guinea, Eastern Highlands Province\",,,,,Papua New Guinea,Eastern Highlands Province,,New Guinea: Purosa,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,Catalog,,,,,,Emoia pallidiceps,Animalia Chordata Reptilia Lepidosauromorpha Squamata Sauria Scincidae Emoia pallidiceps,Animalia,Chordata,Reptilia,Squamata,Scincidae,Emoia,pallidiceps,,,,,ICZN,| can't construct eventDate from atomic fields: string casting error | eventDate is in ISO format | eventDate is consistent with modified date | Unable to get the Life span data of collector:Fred Parker,Valid,eventDate:1965-09-30#Filteredpush Entomologists List" + EOL +
            "MCZ:Herp:R-90788,PhysicalObject,2012-12-06 08:47:03.0,en,President and Fellows of Harvard College,http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90788,b4640710-8e03-11d8-b956-b8a03c50a862,MCZ,Herp,\"Museum of Comparative Zoology, Harvard University\",PreservedSpecimen,,,R-90788,14942,Fred Parker,1,,,whole animal (ethanol),in collection,collector number=14942; muse location number=ZR90753,http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90788,,,,,1965-09-30,0,,,,30/9/1965-30/9/1965,,,\"Papua New Guinea, Eastern Highlands Province\",,,,,Papua New Guinea,Eastern Highlands Province,,New Guinea: Purosa,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,Catalog,,,,,,Emoia pallidiceps,Animalia Chordata Reptilia Lepidosauromorpha Squamata Sauria Scincidae Emoia pallidiceps,Animalia,Chordata,Reptilia,Squamata,Scincidae,Emoia,pallidiceps,,,,,ICZN,| can't construct eventDate from atomic fields: string casting error | eventDate is in ISO format | eventDate is consistent with modified date | Unable to get the Life span data of collector:Fred Parker,Valid,eventDate:1965-09-30#Filteredpush Entomologists List" + EOL;;
        assertEquals(expected, writeBuffer.toString());
    }
}
