import sys
import csv
import time
from datetime import datetime
import uuid
import os
import glob
from shutil import copy2

#######################################################################

"""
@begin clean_name_workflow 
@param local_authority_source @uri file:{CurrentRunDir}/demo_localDB.csv
@in input1_data @uri file:{CurrentRunDir}/demo_input.csv @as original_dataset
@out CurrentRunDir @as newly_created_RunID_directory
@out output1_data  @uri file:{CurrentRunDir}/demo_output_name_val.csv @as data_with_cleaned_names
@out name_val_log @uri file:{CurrentRunDir}/name_val_log.txt @as name_cleaning_log
@out record_id_data @uri file:{CurrentRunDir}/record_id.txt
"""

"""
@begin create_RunID_directory @desc create a separate directory for individual run
@param RunIDList @as existing_RunID_structure
@out CurrentRunDir @as newly_created_RunID_directory
"""

def create_RunID_directory():
    RunList = glob.glob('Run_*')
    if len(RunList) > 0:
        RunIDList = []
        for RunPath in RunList:
            RunID = int(RunPath.split('_')[1])
            RunIDList.append(RunID)
        MaxRunID = max(RunIDList)
        CurrentRunID = MaxRunID + 1
    else:
        CurrentRunID = 1
    CurrentRunDir = 'Run_' + str(CurrentRunID)
    os.makedirs(CurrentRunDir) 
    return CurrentRunDir
"""
@end create_RunID_directory
"""
    
"""
@begin copy_file @desc copy related file (script, data file, script, etc.) into a designated directory 
@param filename @as source_file_to_be_copied
@param CurrentRunDir @as current_RunID_directory_path    
"""

# copy file into RunID directory
def copy_file(filename, destdir):
    copy2(filename, destdir) 

"""
@end copy_file
"""

"""
@begin clean_scientific_name @desc Clean scientificName field
@param local_authority_source @uri file:{CurrentRunDir}/demo_localDB.csv
@param CurrentRunDir
@in input1_data @uri file:{CurrentRunDir}/demo_input_1.csv @as original_dataset
@out output1_data  @uri file:{CurrentRunDir}/demo_output_name_val.csv 
    @as data_with_cleaned_names
@out name_val_log @uri file:{CurrentRunDir}/name_val_log.txt 
    @as name_cleaning_log
@out record_id_data @uri file:{CurrentRunDir}/record_id.txt
"""

def clean_scientific_name(input1_data_file_name, local_authority_source_file_name):  
    
    CurrentRunDir = create_RunID_directory()
    copy_file('clean_name.py',CurrentRunDir)
    
    # print 'Please enter the input csv file path: '
    # 'demo_input.csv'
    # input1_data_file_name = raw_input()
    copy_file(input1_data_file_name,CurrentRunDir)
    # print 'Please enter the authority source file path: '
    # 'demo_localDB.csv'
    # local_authority_source_file_name = raw_input()
    copy_file(local_authority_source_file_name,CurrentRunDir)
    output1_data_file_name = CurrentRunDir + '/' + 'demo_output_name_val.csv'
    name_val_log_file_name = CurrentRunDir + '/' + 'name_val_log.txt'
    record_id_file_name = CurrentRunDir + '/' + 'record_id.txt'
    
    accepted_record_count = 0
    rejected_record_count = 0 
    log_record_count = 0
    output1_record_count = 0
    match_result = None
    final_result = None

    # create log file 
    """
    @begin initialize_run @desc Create the run log file
    @out name_val_log @uri file:{CurrentRunDir}/name_val_log.txt @as name_cleaning_log
        @log {timestamp} Reading input records from {input1_data_file_name}
    """
    name_val_log = open(name_val_log_file_name,'w')    
    name_val_log.write(timestamp("Reading input records from '{0}'\n".format(input1_data_file_name))) 
    
    """
    @end initialize_run
    """
    
    """
    @begin read_scientific_name @desc Read scientificName from local authority source
    @param local_authority_source @uri file:{CurrentRunDir}/demo_localDB.csv
    @call fieldmatch
    @out local_authority_source_scientificName_lst @as local_authority_source_scientificName_list
    """
    # create CSV reader for local_authority_source records
    local_authority_source = csv.DictReader(open(local_authority_source_file_name, 'r'),
                                delimiter=',')
    # fieldnames/keys of original input data (dictionary)
    local_authority_source_fieldnames = local_authority_source.fieldnames
    
    # find corresponding column position for specified header
    scientificName_pos = fieldmatch(local_authority_source_fieldnames,'name')[1]
    authorship_pos = fieldmatch(local_authority_source_fieldnames,'author')[1]
    eventDate_pos = fieldmatch(local_authority_source_fieldnames,'date')[1]
    locality_pos = fieldmatch(local_authority_source_fieldnames,'locality')[1]
    state_pos = fieldmatch(local_authority_source_fieldnames,'state')[1]
    geography_pos = fieldmatch(local_authority_source_fieldnames,'geography')[1]
       
    # iterate over local_authority_source data records
    local_authority_source_record_num = 0
    
    # find values of specific fields
    local_authority_source_scientificName_lst = []
    local_authority_source_authorship_lst = []
    
    for local_authority_source_record in local_authority_source:
        local_authority_source_record_num += 1 
        local_authority_source_scientificName = local_authority_source_record[scientificName_pos]
        local_authority_source_scientificName_lst.append(local_authority_source_scientificName)
        local_authority_source_authorship = local_authority_source_record[authorship_pos]
        local_authority_source_authorship_lst.append(local_authority_source_authorship)
    """
    @end read_scientific_name
    """
    
    """
    @begin read_data_records @desc Read original dataset
    @in input1_data @uri file:{CurrentRunDir}/demo_input.csv @as original_dataset
    @out original_scientificName @as scientificName
    @out original_authorship @as authorship
    @out RecordID 
    @out original_others @as other_fields
    @out record_id_data @uri file:{CurrentRunDir}/record_id.txt
    @out name_val_log @uri file:{CurrentRunDir}/name_val_log.txt @as name_cleaning_log
      @log {timestamp} Reading input record {RecordID}
    """
    # create CSV reader for input records
    input1_data = csv.DictReader(open(input1_data_file_name, 'r'),
                                delimiter=',')

    # iterate over input data records
    record_num = 0
    RecordID_lst = []
    
    # open file for storing output data if not already open
    output1_data = csv.DictWriter(open(output1_data_file_name, 'w'), 
                                      input1_data.fieldnames, 
                                      delimiter=',')
    output1_data.writeheader()
    record_id_data = open(record_id_file_name,'w')    
    all_record_dict_lst = []
       
    for original1_record in input1_data:
        RecordID = str(uuid.uuid4().fields[-1])[:8]
        RecordID_lst.append(RecordID)
        record_id_data.write(RecordID + '\n')
        output1_record = original1_record
        record_num += 1
        print

        # extract values of fields to be validated
        # original_catalogNumber = original1_record['id']
        original_scientificName = original1_record['scientificName']
        original_authorship = original1_record['scientificNameAuthorship']
        name_val_log.write('\n')
        name_val_log.write(timestamp("Reading input record '{0}'\n".format(RecordID)))
        """
        @end read_data_records
        """
 
        """
        @begin check_if_name_is_nonempty @desc Check if scientificName value is present
        @in original_scientificName @as scientificName
        @out original_scientificName @as empty_scientificName
        @out original_scientificName @as nonEmpty_scientificName
        @out name_val_log @uri file:{CurrentRunDir}/name_val_log.txt @as name_cleaning_log
                          @log {timestamp} Checking if {field_name} value is Empty - {check_result}
         """
        check_type = 'external check'
        source_used = 'local_authority_source'
        match_method = 'EXACT'
        field_name = 'scientificName'
        field_name_value = original_scientificName
        # reject the currect record if no value
        if len(original_scientificName) < 1:
            check_result = 'YES'
            match_result = None
        else:
            check_result = 'NO'
            match_result = 'FAILED'
        name_val_log.write(timestamp("Checking if scientificName value is Empty - '{0}'\n".format(check_result)))
        """
        @end check_if_name_is_nonempty
        """
        
        """
        @begin log_name_is_empty @desc Log records of empty scientific name with final status as unable to validate
        @param RecordID 
        @in original_scientificName @as empty_scientificName
        @out final_result @as record_final_status
        @out rejected_record_count @as unable-to-validate_record_count
        @out name_val_log @uri file:{CurrentRunDir}/name_val_log.txt @as name_cleaning_log
            @log {timestamp} {final_result} the record {RecordID}
        """
        if check_result == 'YES':
            final_result = 'UNABLE-to-validate'
            name_val_log.write(timestamp("UNABLE-to-validate the record '{0}'\n".format(RecordID)))
            rejected_record_count += 1
            
            # write output record to output file
            output1_data.writerow(output1_record)
            output1_record_count += 1
            # skip to processing of next record in input file
            continue
            """
            @end log_name_is_empty
            """
        else:
            final_result = None
        
        """   
        @begin find_name_match 
            @desc Find if the scientificName matches any record in the local authority source using exact and fuzzy match
        @in original_scientificName @as nonEmpty_scientificName
        @param local_authority_source_scientificName_lst @as local_authority_source_scientificName_list
        @call exactmatch
        @call fieldmatch
        @out matching_local_authority_source_record @as matching_record
        @out match_result
        @out final_result @as record_final_status
        @out matching_method
        @out name_val_log @uri file:{CurrentRunDir}/name_val_log.txt @as name_cleaning_log
            @log {timestamp} Trying {check_type} {source_used} {match_method} match for validating {field_name}: {field_name_value}
            @log {timestamp} {match_method} match was {match_result}, compliant with {source_used}: {compliant_result}.
        """
        name_val_log.write(timestamp("Trying external_check local_authority_source EXACT match for validating scientificName: '{0}'\n".format(original_scientificName))) 
        matching_method = None
        # first try exact match of the scientific name against local_authority_source
        matching_local_authority_source_record = exactmatch(local_authority_source_scientificName_lst, original_scientificName)[1]
        match_result = exactmatch(local_authority_source_scientificName_lst, original_scientificName)[0]
        if match_result == "SUCCESSFUL":
            name_val_log.write(timestamp("EXACT match was SUCCESSFUL, compliant with local_authority_source: SUCCESSFUL\n"))
            matching_method = match_method
            final_result = "ACCEPTED"

        # otherwise try a fuzzy match
        else:
            match_method = "FUZZY"
            name_val_log.write(timestamp("EXACT match was FAILED, compliant with local_authority_source: FAILED\n"))
            name_val_log.write(timestamp("Trying external_check local_authority_source FUZZY match for validating scientificName: '{0}'\n".format(original_scientificName)))
            matching_local_authority_source_record = fieldmatch(local_authority_source_scientificName_lst, original_scientificName)[1]
            match_result = fieldmatch(local_authority_source_scientificName_lst, original_scientificName)[0]
            if match_result == "SUCCESSFUL":
                name_val_log.write(timestamp("FUZZY match was SUCCESSFUL, compliant with local_authority_source: SUCCESSFUL\n"))
                matching_method = match_method
                final_result = "ACCEPTED"
            else:
                name_val_log.write(timestamp("FUZZY match was FAILED, compliant with local_authority_source: FAILED\n"))   
                final_result= "UNABLE-to-validate"
        """
        @end find_name_match
        """

    #########################################################
        # reject the currect record if not matched successfully against local_authority_source
        """
        @begin log_match_not_found @desc Log record where no match is found in authority source final status as unable to validate
        @param RecordID
        @in final_result @as record_final_status
        @in match_result
        @out rejected_record_count @as unable-to-validate_record_count
        @out name_val_log @uri file:{CurrentRunDir}/name_val_log.txt @as name_cleaning_log
            @log {timestamp} {final_result} the record {RecordID}
        """
        if final_result == "UNABLE-to-validate":
            name_val_log.write(timestamp("UNABLE-to-validate the record '{0}'\n".format(RecordID)))
            rejected_record_count += 1
            
            # write output record to output file
            output1_data.writerow(output1_record)
            output1_record_count += 1
            
            # skip to processing of next record in input file
            continue
            """
            @end log_match_not_found
            """
  
     #############################################################
        """
        @begin update_scientific_name @desc Update scientificName if fuzzy match is found
        @in matching_method
        @in match_result
        @in matching_local_authority_source_record @as matching_record
        @out updated_scientificName
        """
        updated_scientificName = None
        
        # get scientific name from local_authority_source record if the taxon name match was fuzzy
        if matching_method == 'FUZZY':
            updated_scientificName = matching_local_authority_source_record
        """
        @end update_scientific_name
        """

    #####################################################################
        """
        @begin update_authorship @desc Update authorship if fuzzy match is found
        @in original_authorship @as authorship
        @in matching_method
        @in matching_local_authority_source_record @as matching_record
        @out updated_authorship
        """
        updated_authorship = None
        
        # get the scientific name authorship from the local_authority_source record if different from input record
        local_authority_source_name_authorship = local_authority_source_authorship_lst[local_authority_source_scientificName_lst.index(matching_local_authority_source_record)]
        if local_authority_source_name_authorship != original_authorship:
            updated_authorship = local_authority_source_name_authorship

        """
        @end update_authorship
        """

    #####################################################################
        # compose_output1_record
        """
        @begin log_updated_record @desc Log records updating from old value to new value
        @in updated_scientificName
        @in updated_authorship
        @in original_authorship @as authorship
        @out name_val_log @uri file:{CurrentRunDir}/name_val_log.txt @as name_cleaning_log
            @log {timestamp} UPDATING record {Record}: {field_name} from {original_value} to {updated_value}
        """
        if updated_scientificName is not None:
            field_name = "scientificName"
            original_value = original_scientificName
            updated_value = updated_scientificName
            name_val_log.write(timestamp("UPDATING record '{0}': scientificName from '{1}' to '{2}'\n".format(
                     RecordID, original_value, updated_value)))
            output1_record['scientificName'] = updated_scientificName
            
        if updated_authorship is not None:
            field_name = "scientificNameAuthorship"
            original_value = original_authorship
            updated_value = updated_authorship
            name_val_log.write(timestamp("UPDATING record '{0}': scientificNameAuthorship from '{1}' to '{2}'\n".format(
                RecordID, original_value, updated_value)))
            output1_record['scientificNameAuthorship'] = updated_authorship
        """
        @end log_updated_record
        """
    #####################################################################
        """
        @begin log_accepted_record @desc Log record final status as accepted
        @param RecordID
        @in final_result @as record_final_status
        @out accepted_record_count
        @out name_val_log @uri file:{CurrentRunDir}/name_val_log.txt @as name_cleaning_log
            @log {timestamp} {final_result} the record {RecordID}
        """
        name_val_log.write(timestamp("ACCEPTED the record '{0}'\n".format(RecordID)))
        accepted_record_count += 1
        """
        @end log_accepted_record
        """
        
        # write output record to output file
        """
        @begin write_data_into_file @desc Write data into a new file
        @in original_others @as other_fields
        @in original_scientificName @as scientificName
        @in original_authorship @as authorship
        @in updated_scientificName
        @in updated_authorship
        @out output1_data  @uri file:{CurrentRunDir}/demo_output_name_val.csv @as data_with_cleaned_names
        """
        output1_data.writerow(output1_record)
        output1_record_count += 1
        """
        @end write_output1_data
        """
        output1_record['record_id'] = RecordID
        all_record_dict_lst.append(output1_record)
        

    """
    @begin log_summary @desc Summarize on all the records
    @in accepted_record_count
    @in rejected_record_count @as unable-to-validate_record_count
    @out name_val_log @uri file:{CurrentRunDir}/name_val_log.txt @as name_cleaning_log
        @log {timestamp} Wrote {accepted_record_count} ACCEPTED records to {output1_data_file_name}
        @log {timestamp} Wrote {rejected_record_count} UNABLE-to-validate records to {output1_data_file_name}
    """   
    print
    name_val_log.write("\n")
    name_val_log.write(timestamp("Wrote {0} ACCEPTED records to {1}\n".format(accepted_record_count, output1_data_file_name)))
    name_val_log.write(timestamp("Wrote {0} UNABLE-to-validate records to {1}\n".format(rejected_record_count, output1_data_file_name)))
    """
    @end log_summary
    """
    all_record_dict = {}
    for k in all_record_dict_lst[0].keys():
        all_record_dict[k] = [all_record_dict[k] for all_record_dict in all_record_dict_lst]
    # print all_record_dict
    # return all_record_dict
    return [output1_data_file_name,record_id_file_name]
    
"""
@end clean_scientific_name
"""
    
"""
@end clean_name_workflow
"""
    
"""    
@begin exactmatch
@param lst
@param label_str
@return key
@return None            
"""
def exactmatch(lst, label_str):
    match_result = "FAILED"
    matching_record = None
    for key in lst:
        if key.lower() == label_str.lower():
            match_result = 'SUCCESSFUL'
            matching_record = key
            break
    return [match_result,matching_record]
"""
@end exactmatch
"""

"""
@begin fuzzymatch
@param str1
@param str2
@return [match_result, str1[x_longest - longest: x_longest], x_longest - longest]            
"""
def fuzzymatch(str1, str2):
    match_result = "FAILED"
    len1 = len(str1)
    len2 = len(str2)
    str1 = str1.lower()
    str2 = str2.lower()
        
    counter = [[0] * (1 + len2) for k in range(1 + len1)]
    longest = 0
    x_longest = 0
    for i in range(1, 1 + len(str1)):
        for j in range(1, 1 + len(str2)):
            if str1[i - 1] == str2[j - 1]:
                counter[i][j] = counter[i - 1][j - 1] + 1
                if counter[i][j] > longest:
                    longest = counter[i][j]
                    x_longest = i
            else:
                counter[i][j] = 0
    
    if longest > min(len(str1),len(str2))/2:
        match_result = 'SUCCESSFUL'
    
    return [match_result, str1[x_longest - longest: x_longest], x_longest - longest]
    
"""
@end fuzzymatch
"""
 
"""
@begin fieldmatch
@param lst
@param str2
@call fuzzymatch
@return match_str            
 """
def fieldmatch(lst, str2):
    matching_str = None
    for str in lst: 
        match_result = fuzzymatch(str, str2)[0]
        if match_result == 'SUCCESSFUL':
            matching_str = str
            break
    return [match_result,matching_str]
"""
@end fieldmatch
"""   
    

"""
@begin timestamp
@param message
@return timestamp_message
"""            
def timestamp(message):
    current_time = time.time()
    timestamp = datetime.fromtimestamp(current_time).strftime('%Y-%m-%d-%H:%M:%S')
    print "{0}  {1}".format(timestamp, message)
    timestamp_message = (timestamp, message)
    return '  '.join(timestamp_message)
"""
@end timestamp
"""
    
    
if __name__ == '__main__':
    """ Demo of clean_name_workflow script """
    clean_scientific_name('demo_input.csv', 'demo_localDB.csv')    

