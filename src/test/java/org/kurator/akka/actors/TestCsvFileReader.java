package org.kurator.akka.actors;

import java.io.Reader;
import java.io.StringReader;

import org.kurator.akka.ActorBuilder;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.WorkflowBuilder;

public class TestCsvFileReader extends KuratorAkkaTestCase {

    private WorkflowBuilder wfb;
    private ActorBuilder readerActor;
    
    @SuppressWarnings("unused")
    private ActorBuilder printerActor;
    
     @Override
     public void setUp() {
         
         super.setUp();

         wfb = new WorkflowBuilder()
                 .outputStream(stdoutStream)
                 .errorStream(stderrStream);
    
         readerActor = wfb.createActorBuilder()
                 .actorClass(CsvFileReader.class);
    
         printerActor = wfb.createActorBuilder()
                 .actorClass(PrintStreamWriter.class)
                 .parameter("separator", EOL)
                 .listensTo(readerActor);
     }

     public void testCsvFileReader_OneRecord_ExplicitHeaderArray() throws Exception {

         Reader stringReader = new StringReader(
                 "1,2,3"    + EOL
         );
         
         readerActor.parameter("inputReader", stringReader)
                    .parameter("headers", new String[] {"A", "B", "C"});
         
         wfb.build();
         wfb.startWorkflow();
         wfb.awaitWorkflow();
         
         String expected = "{A=1, B=2, C=3}";
         assertEquals("", stderrBuffer.toString());
         assertEquals(expected, stdoutBuffer.toString());
     }

     public void testCsvFileReader_OneRecordMissingField_ExplicitHeaderArray() throws Exception {

         Reader stringReader = new StringReader(
                 "1,2"    + EOL
         );
         
         readerActor.parameter("inputReader", stringReader)
                    .parameter("headers", new String[] {"A", "B", "C"});
         
         wfb.build();
         wfb.startWorkflow();
         
         Exception exception = null;
         try {
             wfb.awaitWorkflow();
         } catch(Exception e) {
             exception = e;
         }
         
         assertNotNull(exception);
         assertTrue(exception.getMessage().contains("Too few fields in record"));
         assertTrue(stderrBuffer.toString().contains("Too few fields in record"));
     }
     
     public void testCsvFileReader_HeaderAndOneRecord() throws Exception {

         Reader stringReader = new StringReader(
                 "A,B,C"    + EOL +
                 "1,2,3"    + EOL
         );
         
         readerActor.parameter("inputReader", stringReader);
         
         wfb.build();
         wfb.startWorkflow();
         wfb.awaitWorkflow();
         
         String expected = "{A=1, B=2, C=3}";
         assertEquals(expected, stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
     
     public void testCsvFileReader_MissingInputFile() throws Exception {

         readerActor.parameter("filePath", "src/main/resources/org/kurator/akka/samples/data/no_such_file.csv" );

         wfb.build();
         wfb.startWorkflow();

         Exception caught = null;
         try { 
             wfb.awaitWorkflow();
         } catch(Exception e) {
             caught = e;
         }
         
         String expectedError = "Input CSV file not found";         

         assertNotNull(caught);
         assertTrue(caught.getMessage().contains(expectedError));
         
         assertEquals("", stdoutBuffer.toString());
         assertTrue(stderrBuffer.toString().contains(expectedError));
     }
     
    public void testCsvFileReader_EightSpecimenRecordFile() throws Exception {

        readerActor.parameter("filePath", "src/main/resources/org/kurator/akka/samples/data/eight_specimen_records.csv");

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
        assertEquals(expected, stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }
    
    public void testCsvFileReader_NineMczIptRecords() throws Exception {

        readerActor.parameter("filePath", "src/main/resources/org/kurator/akka/samples/data/mcz_ipt_snippet.csv");

        wfb.build();
        wfb.startWorkflow();
        wfb.awaitWorkflow();
        
        String expected = 
                "{id=MCZ:Herp:R-90780, type=PhysicalObject, modified=2012-12-06 08:47:03.0, language=en, rightsHolder=President and Fellows of Harvard College, references=http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90780, institutionID=b4640710-8e03-11d8-b956-b8a03c50a862, institutionCode=MCZ, collectionCode=Herp, ownerInstitutionCode=Museum of Comparative Zoology, Harvard University, basisOfRecord=PreservedSpecimen, informationWithheld=, dynamicProperties=, catalogNumber=R-90780, recordNumber=14942, recordedBy=Fred Parker, individualCount=1, sex=, lifeStage=, preparations=whole animal (ethanol), disposition=in collection, otherCatalogNumbers=collector number=14942; muse location number=ZR90753, associatedMedia=http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90780, associatedOccurrences=, associatedSequences=, associatedTaxa=, samplingProtocol=, eventDate=1965-09-30, startDayOfYear=0, year=, month=, day=, verbatimEventDate=30/9/1965-30/9/1965, habitat=, fieldNumber=, higherGeography=Papua New Guinea, Eastern Highlands Province, continent=, waterBody=, islandGroup=, island=, country=Papua New Guinea, stateProvince=Eastern Highlands Province, county=, locality=New Guinea: Purosa, verbatimElevation=, minimumElevationInMeters=, maximumElevationInMeters=, minimumDepthInMeters=, maximumDepthInMeters=, verbatimLatitude=, verbatimLongitude=, verbatimCoordinateSystem=, decimalLatitude=, decimalLongitude=, geodeticDatum=, coordinateUncertaintyInMeters=, coordinatePrecision=, georeferencedBy=, georeferenceProtocol=, georeferenceSources=, geo referenceRemarks=, earliestEraOrLowestErathem=, latestEraOrHighestErathem=, earliestPeriodOrLowestSystem=, latestPeriodOrHighestSystem=, earliestEpochOrLowestSeries=, latestEpochOrHighestSeries=, earliestAgeOrLowestStage=, latestAgeOrHighestStage=, lithostratigraphicTerms=, group=, formation=, member=, bed=, identifiedBy=Catalog, dateIdentified=, identificationRemarks=, identificationQualifier=, identificationVerificationStatus=, typeStatus=, scientificName=Emoia pallidiceps, higherClassification=Animalia Chordata Reptilia Lepidosauromorpha Squamata Sauria Scincidae Emoia pallidiceps, kingdom=Animalia, phylum=Chordata, class=Reptilia, order=Squamata, family=Scincidae, genus=Emoia, specificEpithet=pallidiceps, infraspecificEpithet=, taxonRank=, verbatimTaxonRank=, scientificNameAuthorship=, nomenclaturalCode=ICZN}" + EOL +
                "{id=MCZ:Herp:R-90781, type=PhysicalObject, modified=2012-12-06 08:47:03.0, language=en, rightsHolder=President and Fellows of Harvard College, references=http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90781, institutionID=b4640710-8e03-11d8-b956-b8a03c50a862, institutionCode=MCZ, collectionCode=Herp, ownerInstitutionCode=Museum of Comparative Zoology, Harvard University, basisOfRecord=PreservedSpecimen, informationWithheld=, dynamicProperties=, catalogNumber=R-90781, recordNumber=14942, recordedBy=Fred Parker, individualCount=1, sex=, lifeStage=, preparations=whole animal (ethanol), disposition=in collection, otherCatalogNumbers=collector number=14942; muse location number=ZR90753, associatedMedia=http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90781, associatedOccurrences=, associatedSequences=, associatedTaxa=, samplingProtocol=, eventDate=1965-09-30, startDayOfYear=0, year=, month=, day=, verbatimEventDate=30/9/1965-30/9/1965, habitat=, fieldNumber=, higherGeography=Papua New Guinea, Eastern Highlands Province, continent=, waterBody=, islandGroup=, island=, country=Papua New Guinea, stateProvince=Eastern Highlands Province, county=, locality=New Guinea: Purosa, verbatimElevation=, minimumElevationInMeters=, maximumElevationInMeters=, minimumDepthInMeters=, maximumDepthInMeters=, verbatimLatitude=, verbatimLongitude=, verbatimCoordinateSystem=, decimalLatitude=, decimalLongitude=, geodeticDatum=, coordinateUncertaintyInMeters=, coordinatePrecision=, georeferencedBy=, georeferenceProtocol=, georeferenceSources=, geo referenceRemarks=, earliestEraOrLowestErathem=, latestEraOrHighestErathem=, earliestPeriodOrLowestSystem=, latestPeriodOrHighestSystem=, earliestEpochOrLowestSeries=, latestEpochOrHighestSeries=, earliestAgeOrLowestStage=, latestAgeOrHighestStage=, lithostratigraphicTerms=, group=, formation=, member=, bed=, identifiedBy=Catalog, dateIdentified=, identificationRemarks=, identificationQualifier=, identificationVerificationStatus=, typeStatus=, scientificName=Emoia pallidiceps, higherClassification=Animalia Chordata Reptilia Lepidosauromorpha Squamata Sauria Scincidae Emoia pallidiceps, kingdom=Animalia, phylum=Chordata, class=Reptilia, order=Squamata, family=Scincidae, genus=Emoia, specificEpithet=pallidiceps, infraspecificEpithet=, taxonRank=, verbatimTaxonRank=, scientificNameAuthorship=, nomenclaturalCode=ICZN}" + EOL +
                "{id=MCZ:Herp:R-90782, type=PhysicalObject, modified=2012-12-06 08:47:03.0, language=en, rightsHolder=President and Fellows of Harvard College, references=http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90782, institutionID=b4640710-8e03-11d8-b956-b8a03c50a862, institutionCode=MCZ, collectionCode=Herp, ownerInstitutionCode=Museum of Comparative Zoology, Harvard University, basisOfRecord=PreservedSpecimen, informationWithheld=, dynamicProperties=, catalogNumber=R-90782, recordNumber=14942, recordedBy=Fred Parker, individualCount=1, sex=, lifeStage=, preparations=whole animal (ethanol), disposition=in collection, otherCatalogNumbers=collector number=14942; muse location number=ZR90753, associatedMedia=http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90782, associatedOccurrences=, associatedSequences=, associatedTaxa=, samplingProtocol=, eventDate=1965-09-30, startDayOfYear=0, year=, month=, day=, verbatimEventDate=30/9/1965-30/9/1965, habitat=, fieldNumber=, higherGeography=Papua New Guinea, Eastern Highlands Province, continent=, waterBody=, islandGroup=, island=, country=Papua New Guinea, stateProvince=Eastern Highlands Province, county=, locality=New Guinea: Purosa, verbatimElevation=, minimumElevationInMeters=, maximumElevationInMeters=, minimumDepthInMeters=, maximumDepthInMeters=, verbatimLatitude=, verbatimLongitude=, verbatimCoordinateSystem=, decimalLatitude=, decimalLongitude=, geodeticDatum=, coordinateUncertaintyInMeters=, coordinatePrecision=, georeferencedBy=, georeferenceProtocol=, georeferenceSources=, geo referenceRemarks=, earliestEraOrLowestErathem=, latestEraOrHighestErathem=, earliestPeriodOrLowestSystem=, latestPeriodOrHighestSystem=, earliestEpochOrLowestSeries=, latestEpochOrHighestSeries=, earliestAgeOrLowestStage=, latestAgeOrHighestStage=, lithostratigraphicTerms=, group=, formation=, member=, bed=, identifiedBy=Catalog, dateIdentified=, identificationRemarks=, identificationQualifier=, identificationVerificationStatus=, typeStatus=, scientificName=Emoia pallidiceps, higherClassification=Animalia Chordata Reptilia Lepidosauromorpha Squamata Sauria Scincidae Emoia pallidiceps, kingdom=Animalia, phylum=Chordata, class=Reptilia, order=Squamata, family=Scincidae, genus=Emoia, specificEpithet=pallidiceps, infraspecificEpithet=, taxonRank=, verbatimTaxonRank=, scientificNameAuthorship=, nomenclaturalCode=ICZN}" + EOL +
                "{id=MCZ:Herp:R-90783, type=PhysicalObject, modified=2012-12-06 08:47:03.0, language=en, rightsHolder=President and Fellows of Harvard College, references=http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90783, institutionID=b4640710-8e03-11d8-b956-b8a03c50a862, institutionCode=MCZ, collectionCode=Herp, ownerInstitutionCode=Museum of Comparative Zoology, Harvard University, basisOfRecord=PreservedSpecimen, informationWithheld=, dynamicProperties=, catalogNumber=R-90783, recordNumber=14942, recordedBy=Fred Parker, individualCount=1, sex=, lifeStage=, preparations=whole animal (ethanol), disposition=in collection, otherCatalogNumbers=collector number=14942; muse location number=ZR90753, associatedMedia=http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90783, associatedOccurrences=, associatedSequences=, associatedTaxa=, samplingProtocol=, eventDate=1965-09-30, startDayOfYear=0, year=, month=, day=, verbatimEventDate=30/9/1965-30/9/1965, habitat=, fieldNumber=, higherGeography=Papua New Guinea, Eastern Highlands Province, continent=, waterBody=, islandGroup=, island=, country=Papua New Guinea, stateProvince=Eastern Highlands Province, county=, locality=New Guinea: Purosa, verbatimElevation=, minimumElevationInMeters=, maximumElevationInMeters=, minimumDepthInMeters=, maximumDepthInMeters=, verbatimLatitude=, verbatimLongitude=, verbatimCoordinateSystem=, decimalLatitude=, decimalLongitude=, geodeticDatum=, coordinateUncertaintyInMeters=, coordinatePrecision=, georeferencedBy=, georeferenceProtocol=, georeferenceSources=, geo referenceRemarks=, earliestEraOrLowestErathem=, latestEraOrHighestErathem=, earliestPeriodOrLowestSystem=, latestPeriodOrHighestSystem=, earliestEpochOrLowestSeries=, latestEpochOrHighestSeries=, earliestAgeOrLowestStage=, latestAgeOrHighestStage=, lithostratigraphicTerms=, group=, formation=, member=, bed=, identifiedBy=Catalog, dateIdentified=, identificationRemarks=, identificationQualifier=, identificationVerificationStatus=, typeStatus=, scientificName=Emoia pallidiceps, higherClassification=Animalia Chordata Reptilia Lepidosauromorpha Squamata Sauria Scincidae Emoia pallidiceps, kingdom=Animalia, phylum=Chordata, class=Reptilia, order=Squamata, family=Scincidae, genus=Emoia, specificEpithet=pallidiceps, infraspecificEpithet=, taxonRank=, verbatimTaxonRank=, scientificNameAuthorship=, nomenclaturalCode=ICZN}" + EOL +
                "{id=MCZ:Herp:R-90784, type=PhysicalObject, modified=2012-12-06 08:47:03.0, language=en, rightsHolder=President and Fellows of Harvard College, references=http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90784, institutionID=b4640710-8e03-11d8-b956-b8a03c50a862, institutionCode=MCZ, collectionCode=Herp, ownerInstitutionCode=Museum of Comparative Zoology, Harvard University, basisOfRecord=PreservedSpecimen, informationWithheld=, dynamicProperties=, catalogNumber=R-90784, recordNumber=14942, recordedBy=Fred Parker, individualCount=1, sex=, lifeStage=, preparations=whole animal (ethanol), disposition=in collection, otherCatalogNumbers=collector number=14942; muse location number=ZR90753, associatedMedia=http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90784, associatedOccurrences=, associatedSequences=, associatedTaxa=, samplingProtocol=, eventDate=1965-09-30, startDayOfYear=0, year=, month=, day=, verbatimEventDate=30/9/1965-30/9/1965, habitat=, fieldNumber=, higherGeography=Papua New Guinea, Eastern Highlands Province, continent=, waterBody=, islandGroup=, island=, country=Papua New Guinea, stateProvince=Eastern Highlands Province, county=, locality=New Guinea: Purosa, verbatimElevation=, minimumElevationInMeters=, maximumElevationInMeters=, minimumDepthInMeters=, maximumDepthInMeters=, verbatimLatitude=, verbatimLongitude=, verbatimCoordinateSystem=, decimalLatitude=, decimalLongitude=, geodeticDatum=, coordinateUncertaintyInMeters=, coordinatePrecision=, georeferencedBy=, georeferenceProtocol=, georeferenceSources=, geo referenceRemarks=, earliestEraOrLowestErathem=, latestEraOrHighestErathem=, earliestPeriodOrLowestSystem=, latestPeriodOrHighestSystem=, earliestEpochOrLowestSeries=, latestEpochOrHighestSeries=, earliestAgeOrLowestStage=, latestAgeOrHighestStage=, lithostratigraphicTerms=, group=, formation=, member=, bed=, identifiedBy=Catalog, dateIdentified=, identificationRemarks=, identificationQualifier=, identificationVerificationStatus=, typeStatus=, scientificName=Emoia pallidiceps, higherClassification=Animalia Chordata Reptilia Lepidosauromorpha Squamata Sauria Scincidae Emoia pallidiceps, kingdom=Animalia, phylum=Chordata, class=Reptilia, order=Squamata, family=Scincidae, genus=Emoia, specificEpithet=pallidiceps, infraspecificEpithet=, taxonRank=, verbatimTaxonRank=, scientificNameAuthorship=, nomenclaturalCode=ICZN}" + EOL +
                "{id=MCZ:Herp:R-90785, type=PhysicalObject, modified=2012-12-06 08:47:03.0, language=en, rightsHolder=President and Fellows of Harvard College, references=http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90785, institutionID=b4640710-8e03-11d8-b956-b8a03c50a862, institutionCode=MCZ, collectionCode=Herp, ownerInstitutionCode=Museum of Comparative Zoology, Harvard University, basisOfRecord=PreservedSpecimen, informationWithheld=, dynamicProperties=, catalogNumber=R-90785, recordNumber=14942, recordedBy=Fred Parker, individualCount=1, sex=, lifeStage=, preparations=whole animal (ethanol), disposition=in collection, otherCatalogNumbers=collector number=14942; muse location number=ZR90753, associatedMedia=http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90785, associatedOccurrences=, associatedSequences=, associatedTaxa=, samplingProtocol=, eventDate=1965-09-30, startDayOfYear=0, year=, month=, day=, verbatimEventDate=30/9/1965-30/9/1965, habitat=, fieldNumber=, higherGeography=Papua New Guinea, Eastern Highlands Province, continent=, waterBody=, islandGroup=, island=, country=Papua New Guinea, stateProvince=Eastern Highlands Province, county=, locality=New Guinea: Purosa, verbatimElevation=, minimumElevationInMeters=, maximumElevationInMeters=, minimumDepthInMeters=, maximumDepthInMeters=, verbatimLatitude=, verbatimLongitude=, verbatimCoordinateSystem=, decimalLatitude=, decimalLongitude=, geodeticDatum=, coordinateUncertaintyInMeters=, coordinatePrecision=, georeferencedBy=, georeferenceProtocol=, georeferenceSources=, geo referenceRemarks=, earliestEraOrLowestErathem=, latestEraOrHighestErathem=, earliestPeriodOrLowestSystem=, latestPeriodOrHighestSystem=, earliestEpochOrLowestSeries=, latestEpochOrHighestSeries=, earliestAgeOrLowestStage=, latestAgeOrHighestStage=, lithostratigraphicTerms=, group=, formation=, member=, bed=, identifiedBy=Catalog, dateIdentified=, identificationRemarks=, identificationQualifier=, identificationVerificationStatus=, typeStatus=, scientificName=Emoia pallidiceps, higherClassification=Animalia Chordata Reptilia Lepidosauromorpha Squamata Sauria Scincidae Emoia pallidiceps, kingdom=Animalia, phylum=Chordata, class=Reptilia, order=Squamata, family=Scincidae, genus=Emoia, specificEpithet=pallidiceps, infraspecificEpithet=, taxonRank=, verbatimTaxonRank=, scientificNameAuthorship=, nomenclaturalCode=ICZN}" + EOL +
                "{id=MCZ:Herp:R-90786, type=PhysicalObject, modified=2012-12-06 08:47:03.0, language=en, rightsHolder=President and Fellows of Harvard College, references=http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90786, institutionID=b4640710-8e03-11d8-b956-b8a03c50a862, institutionCode=MCZ, collectionCode=Herp, ownerInstitutionCode=Museum of Comparative Zoology, Harvard University, basisOfRecord=PreservedSpecimen, informationWithheld=, dynamicProperties=, catalogNumber=R-90786, recordNumber=14942, recordedBy=Fred Parker, individualCount=1, sex=, lifeStage=, preparations=whole animal (ethanol), disposition=in collection, otherCatalogNumbers=collector number=14942; muse location number=ZR90753, associatedMedia=http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90786, associatedOccurrences=, associatedSequences=, associatedTaxa=, samplingProtocol=, eventDate=1965-09-30, startDayOfYear=0, year=, month=, day=, verbatimEventDate=30/9/1965-30/9/1965, habitat=, fieldNumber=, higherGeography=Papua New Guinea, Eastern Highlands Province, continent=, waterBody=, islandGroup=, island=, country=Papua New Guinea, stateProvince=Eastern Highlands Province, county=, locality=New Guinea: Purosa, verbatimElevation=, minimumElevationInMeters=, maximumElevationInMeters=, minimumDepthInMeters=, maximumDepthInMeters=, verbatimLatitude=, verbatimLongitude=, verbatimCoordinateSystem=, decimalLatitude=, decimalLongitude=, geodeticDatum=, coordinateUncertaintyInMeters=, coordinatePrecision=, georeferencedBy=, georeferenceProtocol=, georeferenceSources=, geo referenceRemarks=, earliestEraOrLowestErathem=, latestEraOrHighestErathem=, earliestPeriodOrLowestSystem=, latestPeriodOrHighestSystem=, earliestEpochOrLowestSeries=, latestEpochOrHighestSeries=, earliestAgeOrLowestStage=, latestAgeOrHighestStage=, lithostratigraphicTerms=, group=, formation=, member=, bed=, identifiedBy=Catalog, dateIdentified=, identificationRemarks=, identificationQualifier=, identificationVerificationStatus=, typeStatus=, scientificName=Emoia pallidiceps, higherClassification=Animalia Chordata Reptilia Lepidosauromorpha Squamata Sauria Scincidae Emoia pallidiceps, kingdom=Animalia, phylum=Chordata, class=Reptilia, order=Squamata, family=Scincidae, genus=Emoia, specificEpithet=pallidiceps, infraspecificEpithet=, taxonRank=, verbatimTaxonRank=, scientificNameAuthorship=, nomenclaturalCode=ICZN}" + EOL +
                "{id=MCZ:Herp:R-90787, type=PhysicalObject, modified=2012-12-06 08:47:03.0, language=en, rightsHolder=President and Fellows of Harvard College, references=http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90787, institutionID=b4640710-8e03-11d8-b956-b8a03c50a862, institutionCode=MCZ, collectionCode=Herp, ownerInstitutionCode=Museum of Comparative Zoology, Harvard University, basisOfRecord=PreservedSpecimen, informationWithheld=, dynamicProperties=, catalogNumber=R-90787, recordNumber=14942, recordedBy=Fred Parker, individualCount=1, sex=, lifeStage=, preparations=whole animal (ethanol), disposition=in collection, otherCatalogNumbers=collector number=14942; muse location number=ZR90753, associatedMedia=http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90787, associatedOccurrences=, associatedSequences=, associatedTaxa=, samplingProtocol=, eventDate=1965-09-30, startDayOfYear=0, year=, month=, day=, verbatimEventDate=30/9/1965-30/9/1965, habitat=, fieldNumber=, higherGeography=Papua New Guinea, Eastern Highlands Province, continent=, waterBody=, islandGroup=, island=, country=Papua New Guinea, stateProvince=Eastern Highlands Province, county=, locality=New Guinea: Purosa, verbatimElevation=, minimumElevationInMeters=, maximumElevationInMeters=, minimumDepthInMeters=, maximumDepthInMeters=, verbatimLatitude=, verbatimLongitude=, verbatimCoordinateSystem=, decimalLatitude=, decimalLongitude=, geodeticDatum=, coordinateUncertaintyInMeters=, coordinatePrecision=, georeferencedBy=, georeferenceProtocol=, georeferenceSources=, geo referenceRemarks=, earliestEraOrLowestErathem=, latestEraOrHighestErathem=, earliestPeriodOrLowestSystem=, latestPeriodOrHighestSystem=, earliestEpochOrLowestSeries=, latestEpochOrHighestSeries=, earliestAgeOrLowestStage=, latestAgeOrHighestStage=, lithostratigraphicTerms=, group=, formation=, member=, bed=, identifiedBy=Catalog, dateIdentified=, identificationRemarks=, identificationQualifier=, identificationVerificationStatus=, typeStatus=, scientificName=Emoia pallidiceps, higherClassification=Animalia Chordata Reptilia Lepidosauromorpha Squamata Sauria Scincidae Emoia pallidiceps, kingdom=Animalia, phylum=Chordata, class=Reptilia, order=Squamata, family=Scincidae, genus=Emoia, specificEpithet=pallidiceps, infraspecificEpithet=, taxonRank=, verbatimTaxonRank=, scientificNameAuthorship=, nomenclaturalCode=ICZN}" + EOL +
                "{id=MCZ:Herp:R-90788, type=PhysicalObject, modified=2012-12-06 08:47:03.0, language=en, rightsHolder=President and Fellows of Harvard College, references=http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90788, institutionID=b4640710-8e03-11d8-b956-b8a03c50a862, institutionCode=MCZ, collectionCode=Herp, ownerInstitutionCode=Museum of Comparative Zoology, Harvard University, basisOfRecord=PreservedSpecimen, informationWithheld=, dynamicProperties=, catalogNumber=R-90788, recordNumber=14942, recordedBy=Fred Parker, individualCount=1, sex=, lifeStage=, preparations=whole animal (ethanol), disposition=in collection, otherCatalogNumbers=collector number=14942; muse location number=ZR90753, associatedMedia=http://mczbase.mcz.harvard.edu/guid/MCZ:Herp:R-90788, associatedOccurrences=, associatedSequences=, associatedTaxa=, samplingProtocol=, eventDate=1965-09-30, startDayOfYear=0, year=, month=, day=, verbatimEventDate=30/9/1965-30/9/1965, habitat=, fieldNumber=, higherGeography=Papua New Guinea, Eastern Highlands Province, continent=, waterBody=, islandGroup=, island=, country=Papua New Guinea, stateProvince=Eastern Highlands Province, county=, locality=New Guinea: Purosa, verbatimElevation=, minimumElevationInMeters=, maximumElevationInMeters=, minimumDepthInMeters=, maximumDepthInMeters=, verbatimLatitude=, verbatimLongitude=, verbatimCoordinateSystem=, decimalLatitude=, decimalLongitude=, geodeticDatum=, coordinateUncertaintyInMeters=, coordinatePrecision=, georeferencedBy=, georeferenceProtocol=, georeferenceSources=, geo referenceRemarks=, earliestEraOrLowestErathem=, latestEraOrHighestErathem=, earliestPeriodOrLowestSystem=, latestPeriodOrHighestSystem=, earliestEpochOrLowestSeries=, latestEpochOrHighestSeries=, earliestAgeOrLowestStage=, latestAgeOrHighestStage=, lithostratigraphicTerms=, group=, formation=, member=, bed=, identifiedBy=Catalog, dateIdentified=, identificationRemarks=, identificationQualifier=, identificationVerificationStatus=, typeStatus=, scientificName=Emoia pallidiceps, higherClassification=Animalia Chordata Reptilia Lepidosauromorpha Squamata Sauria Scincidae Emoia pallidiceps, kingdom=Animalia, phylum=Chordata, class=Reptilia, order=Squamata, family=Scincidae, genus=Emoia, specificEpithet=pallidiceps, infraspecificEpithet=, taxonRank=, verbatimTaxonRank=, scientificNameAuthorship=, nomenclaturalCode=ICZN}";
          assertEquals(expected, stdoutBuffer.toString());
          assertEquals("", stderrBuffer.toString());
    }

}
