import sys
import csv
import time
from datetime import datetime
import re
import os
import glob
from shutil import copy2

#######################################################################

"""
@begin clean_event_date @desc Clean eventDate field
@in output1_data  @uri file:demo_output_name_val.csv @as data_with_cleaned_names
@param record_id_data @uri file:record_id.txt
@out output2_data  @uri file:demo_output_name_date_val.csv @as data_with_cleaned_names_and_dates
@out date_val_log @uri file:date_val_log.txt @as date_cleaning_log
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

# copy file into RunID directory
def copy_file(filename, destdir):
    copy2(filename, destdir) 

def clean_event_date(input2_data_file_name, record_id_file_name):
    
    CurrentRunDir = create_RunID_directory()
    copy2('clean_event_date.py', CurrentRunDir)
    
    # print 'Please enter the input csv file path: '
    # 'Run_1/demo_output_name_val.csv'
    # input2_data_file_name = raw_input()
    copy_file(input2_data_file_name,CurrentRunDir)
    # print 'Please enter the unique record_id file path: '
    # 'Run_1/record_id.txt'
    # record_id_file_name = raw_input()
    # record_id_file_name = 'Run_1/record_id.txt'
    copy_file(record_id_file_name,CurrentRunDir)
    output2_data_file_name = CurrentRunDir + '/' + 'demo_output_name_date_val.csv'
    date_val_log_file_name = CurrentRunDir + '/' + 'date_val_log.txt'
    
    accepted2_record_count = 0
    rejected2_record_count = 0
    log2_record_count = 0
    output2_record_count = 0
    match_result = None
    final_result = None
    # create log file
    """
    @begin initialize_run @desc Create the run log file
    @out date_val_log @uri file:date_val_log.txt @as date_cleaning_log
        @log {timestamp} Reading input records from {input2_data_file_name}
    """
    date_val_log = open(date_val_log_file_name,'w')    
    date_val_log.write(timestamp("Reading input records from '{0}'\n".format(input2_data_file_name)))
    
    """
    @end initialize_run
    """
        
    """
    @begin read_data_records @desc Read data with cleaned names
    @in input2_data  @uri file:demo_output_name_val.csv @as data_with_cleaned_names
    @in record_id_data @uri file:record_id.txt
    @out original2_eventDate @as eventDate
    @out RecordID 
    @out original2_others @as other_fields
    @out date_val_log @uri file:date_val_log.txt @as date_cleaning_log
                      @log {timestamp} Reading input record {RecordID}
    """
    input2_data = csv.DictReader(open(input2_data_file_name, 'r'),
                                delimiter=',')
    # iterate over input data records
    record2_num = 0
    RecordID_lst = []
    record_id_data = open(record_id_file_name, 'r') 
    for line in record_id_data:
        line = line.rstrip('\n')
        RecordID_lst.append(line)
    
    # open file for storing output data if not already open
    output2_data = csv.DictWriter(open(output2_data_file_name, 'w'), 
                                      input2_data.fieldnames, 
                                      delimiter=',')
    output2_data.writeheader()
    
    for original2_record, ReID in zip(input2_data, RecordID_lst):
        RecordID = ReID
        output2_record = original2_record
        record2_num += 1
        print

        # extract values of fields to be validated
        original2_eventDate = original2_record['eventDate']

        date_val_log.write('\n')
        date_val_log.write(timestamp("Reading input record '{0}'\n".format(RecordID)))        
        """
        @end read_input2_data_records
        """
        
        """
        @begin check_if_date_is_nonempty @desc Check if eventDate value is present
        @in original2_eventDate @as eventDate
        @out original2_eventDate @as empty_eventDate
        @out original2_eventDate @as nonEmpty_eventDate
        @out date_val_log @uri file:date_val_log.txt @as date_cleaning_log
                       @log {timestamp} Checking if {field_name} value is Empty: {check_result}       
        """
        check_type = 'self_check'
        source_used = 'ISO_date_format-(YYYY-MM-DD)'
        match_method = 'EXACT'
        field_name = 'eventDate'
        field_name_value = original2_eventDate
        # reject the currect record if no value
        if len(original2_eventDate) < 1:
            check_result = 'YES'
            match_result = None
        else: 
            check_result = 'NO'
            match_result = 'FAILED'
        date_val_log.write(timestamp("Checking if eventDate value is Empty: '{0}'\n".format(check_result)))
        """
        @end check_if_date_is_nonempty 
        """
        
        """
        @begin log_date_is_empty @desc Log records of empty event date with final status as unable to validate
        @param RecordID 
        @in original2_eventDate @as empty_eventDate
        @out rejected2_record_count @as unable-to-validate_record_count
        @out date_val_log @uri file:date_val_log.txt @as date_cleaning_log
            @log {timestamp} {final_result} the record {RecordID}
        """
        
        if check_result == 'YES':
            final_result = 'UNABLE-to-validate'
            date_val_log.write(timestamp("UNABLE-to-validate the record '{0}'\n".format(RecordID)))
            rejected2_record_count += 1
            
            # write output record to output file
            output2_data.writerow(output2_record)
            output2_record_count += 1
            # skip to processing of next record in input file
            continue
            """
            @end log_date_is_empty
            """
        else:
            final_result = 'ACCEPTED'
                    
        """
        @begin check_ISO_date_compliant 
            @desc Check if the eventDate is compliant with ISO date format (YYYY-MM-DD)
        @in original2_eventDate @as nonEmpty_eventDate
        @out original2_eventDate @as compliant_eventDate
        @out original2_eventDate @as nonCompliant_eventDate
        @out date_val_log @uri file:date_val_log.txt @as date_cleaning_log
                       @log {timestamp} Trying {check_type} {source_used} {match_method} match for validating {field_name}: {field_name_value}            
                       @log {timestamp} {match_method} match was {match_result}, compliant with {source_used}: {match_result}.
        """                
        date_val_log.write(timestamp("Trying self_check ISO_date_format-(YYYY-MM-DD) EXACT match for validating eventDate: '{0}'\n".format(original2_eventDate)))
        # date format: xxxx-xx-xx
        if re.match(r'^(\d{4}\-)+(\d{1,2}\-)+(\d{1,2})$',original2_eventDate):
            match_result = 'SUCESSFUL'
            compliant_eventDate = original2_eventDate
            date_val_log.write(timestamp("EXACT match was SUCESSFUL, compliant with ISO_date_format-(YYYY-MM-DD): SUCCESSFUL\n"))
        # date format: xxxx-xx-xx/xxxx-xx-xx
        elif re.match(r'^(\d{4}\-)+(\d{1,2}\-)+(\d{1,2}\/)+(\d{4}\-)+(\d{1,2}\-)+(\d{1,2})$',original2_eventDate):
            match_result = 'SUCESSFUL'
            compliant_eventDate = original2_eventDate
            date_val_log.write(timestamp("EXACT match was SUCESSFUL, compliant with ISO_date_format-(YYYY-MM-DD): SUCCESSFUL\n"))
        else: 
            match_result = 'FAILED'
            nonCompliant_eventDate = original2_eventDate
            date_val_log.write(timestamp("EXACT match was FAILED, compliant with ISO_date_format-(YYYY-MM-DD): FAILED\n"))
        """
        @end check_ISO_date_compliant
        """
            
        """
        @begin update_event_date @desc Update eventDate by date format conversion
        @in original2_eventDate @as nonCompliant_eventDate
        @out updated2_eventDate @as updated_eventDate
        @out date_val_log @uri file:date_val_log.txt @as date_cleaning_log
                          @log {timestamp} UPDATING record {Record}: {field_name} from {original_value} to {updated_value} 
        """
        updated2_eventDate = None
        # date format: xx/xx/xx
        if match_result == "FAILED":
            if re.match(r'^(\d{1,2}\/)+(\d{1,2}\/)+(\d{4})$',original2_eventDate):
                dateparts_slash = original2_eventDate.split('/')
                par0 = dateparts_slash[0]
                par1 = dateparts_slash[1]  
                par2 = dateparts_slash[2]
                par_mon = par0.zfill(2)
                par_day = par1.zfill(2)
                par_yr = par2
                updated2_eventDate = par_yr + '-' + par_mon + '-' + par_day
                field_name = "eventDate"
                original_value = original2_eventDate
                updated_value = updated2_eventDate
                date_val_log.write(timestamp("UPDATING record '{0}': eventDate from '{1}' to '{2}'\n".format(RecordID, original2_eventDate, updated2_eventDate)))
                output2_record['eventDate'] = updated2_eventDate
            elif re.match(r'^(\d{1,2}\/)+(\d{1,2}\/)+(\d{2})$',original2_eventDate):
                dateparts_slash = original2_eventDate.split('/')
                par0 = dateparts_slash[0]
                par1 = dateparts_slash[1]  
                par2 = dateparts_slash[2]
                par_mon = par0.zfill(2)
                par_day = par1.zfill(2)
                par_yr = par2
                prefix_yr = '19'
                fix_yr = prefix_yr + par2
                updated2_eventDate = fix_yr + '-' + par_mon + '-' + par_day
                field_name = "eventDate"
                original_value = original2_eventDate
                updated_value = updated2_eventDate
                date_val_log.write(timestamp("UPDATING record '{0}': eventDate from '{1}' to '{2}'\n".format(RecordID, original2_eventDate, updated2_eventDate)))
                output2_record['eventDate'] = updated2_eventDate
                
        """
        @end update_event_date       
        """
                
        """
        @begin log_accepted_record @desc Log record final status as accepted
        @param RecordID
        @in updated2_eventDate @as updated_eventDate
        @in original2_eventDate @as compliant_eventDate
        @out accepted2_record_count @as accepted_record_count
        @out date_val_log @uri file:date_val_log.txt @as date_cleaning_log
            @log {timestamp} {final_result} the record {RecordID}
        """
        date_val_log.write(timestamp("ACCEPTED the record '{0}'\n".format(RecordID)))    
        accepted2_record_count += 1  
        """
        @end log_accepted_record
        """            
        
        # write output record to output file
        """
        @begin write_data_into_file @desc Write data into a new file
        @in original2_others @as other_fields
        @in updated2_eventDate @as updated_eventDate
        @in original2_eventDate @as eventDate
        @out output2_data  @uri file:demo_output_name_date_val.csv @as data_with_cleaned_names_and_dates
        """
        output2_data.writerow(output2_record)
        output2_record_count += 1
        """
        @end write_output2_data
        """
        
    """
    @begin log_summary @desc Summarize on all the records
    @in accepted2_record_count @as accepted_record_count
    @in rejected2_record_count @as unable-to-validate_record_count
    @out date_val_log @uri file:date_val_log.txt @as date_cleaning_log
                   @log {timestamp} Wrote {accepted2_record_count} accepted records to {output2_data_file_name}
                   @log {timestamp} Wrote {rejected2_record_count} UNABLE-to-validate records to {rejected2_data_file_name}
    """
    print
    date_val_log.write("\n")
    date_val_log.write(timestamp("Wrote {0} accepted records to {1}\n".format(accepted2_record_count, output2_data_file_name)))
    date_val_log.write(timestamp("Wrote {0} UNABLE-to-validate records to {1}\n".format(rejected2_record_count, output2_data_file_name)))
    """
    @end log_summary
    """

"""
@end clean_event_date               
"""

"""
@end clean_name_and_date_workflow
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
    """ Demo of clean_name_and_date_workflow script """
    clean_event_date('Run_1/demo_output_name_val.csv','Run_1/record_id.txt')
