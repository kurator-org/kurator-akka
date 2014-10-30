package org.kurator.akka.actors;

/*
 * This code is an adaptation of akka.fp.NewScientificNameValidator 
 * in the FP-Akka package as of 28Oct2014.
 */

import org.kurator.akka.messages.EndOfStream;
import org.kurator.akka.messages.Initialize;

import fp.services.INewScientificNameValidationService;
import fp.util.CurationComment;
import fp.util.CurationCommentType;
import fp.util.CurationStatus;
import fp.util.SpecimenRecord;
import fp.util.SpecimenRecordTypeConf;

public class ScientificNameValidator extends BroadcastActor {
   
    public String serviceClassQN = "fp.services.COLService";
    public boolean insertLSID = true;

    private String scientificNameLabel;
    private String authorLabel;
    private String LSIDLabel;

    private INewScientificNameValidationService scientificNameService;


    public void initialize() {
        
        SpecimenRecordTypeConf specimenRecordTypeConf = SpecimenRecordTypeConf.getInstance();

        try {
            scientificNameLabel = specimenRecordTypeConf.getLabel("ScientificName");
            if(scientificNameLabel == null){
                scientificNameLabel = "scientificName";
            }

            authorLabel = specimenRecordTypeConf.getLabel("ScientificNameAuthorship");
            if(authorLabel == null){
                authorLabel = "scientificNameAuthorship";
            }

            if (insertLSID) {
                LSIDLabel = specimenRecordTypeConf.getLabel("IdentificationTaxon");
                if(LSIDLabel == null){
                    LSIDLabel = "IdentificationTaxon";
                }
            }

            scientificNameService = (INewScientificNameValidationService)Class.forName(serviceClassQN).newInstance();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
    public void onReceive(Object message) throws Exception {

        super.onReceive(message);
        
        if (message instanceof Initialize) {
            
            initialize();
            return;
            
        } else if (message instanceof SpecimenRecord) {
            
            SpecimenRecord inputSpecimenRecord = (SpecimenRecord) message;
            
            String scientificName = inputSpecimenRecord.get(scientificNameLabel);
            
            if(scientificName == null){
                
                CurationCommentType curationComment = CurationComment.construct(
                        CurationComment.UNABLE_DETERMINE_VALIDITY, 
                        scientificNameLabel + " is missing in the incoming SpecimenRecord", "ScientificNameValidator"
                );
                
                updateAndSendRecord(new SpecimenRecord(inputSpecimenRecord), curationComment);
                return;
            }

            String author = inputSpecimenRecord.get(authorLabel);
            String genus = inputSpecimenRecord.get("genus");
            String subgenus = inputSpecimenRecord.get("subgenus");
            String specificEpithet = inputSpecimenRecord.get("specificEpithet");
            String verbatimTaxonRank = inputSpecimenRecord.get("verbatimTaxonRank");
            String infraspecificEpithet = inputSpecimenRecord.get("infraspecificEpithet");
            String taxonRank = inputSpecimenRecord.get("taxonRank");
            String kingdom = inputSpecimenRecord.get("kingdom");
            String phylum = inputSpecimenRecord.get("phylum");
            String tclass = inputSpecimenRecord.get("tclass");
            String order = inputSpecimenRecord.get("order");
            String family = inputSpecimenRecord.get("family");

            scientificNameService.validateScientificName(
                    scientificName, author, genus, subgenus,
                    specificEpithet, verbatimTaxonRank, infraspecificEpithet,
                    taxonRank, kingdom, phylum, tclass, order, family);

            CurationStatus curationStatus = scientificNameService.getCurationStatus();
            if (curationStatus == CurationComment.CURATED || curationStatus == CurationComment.Filled_in){
                inputSpecimenRecord.put("scientificName", scientificNameService.getCorrectedScientificName());
                inputSpecimenRecord.put("scientificNameAuthorship", scientificNameService.getCorrectedAuthor());
            }

            CurationCommentType curationComment = CurationComment.construct(curationStatus,scientificNameService.getComment(),scientificNameService.getServiceName());
            
            updateAndSendRecord(inputSpecimenRecord, curationComment);

        } else if (message instanceof EndOfStream) {            

            broadcast(message);
            getContext().stop(getSelf());
        }
    }


    private void updateAndSendRecord(SpecimenRecord result, CurationCommentType comment) {
        
        if (comment != null){
            result.put("scinComment", comment.getDetails());
            result.put("scinStatus", comment.getStatus());
            result.put("scinSource", comment.getSource());
        }
        
        broadcast(result);
    }
    
   
}

