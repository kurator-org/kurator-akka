import sys
import csv

##################################################################################################
# @BEGIN change_field_delimiter
# @PARAM input_file_name
# @PARAM output_file_name
# @PARAM input_field_delimiter
# @PARAM output_field_delimiter
# @IN input_data @URI file:{input_file_name}
# @OUT output_data @URI file:{output_file_name}

def change_field_delimiter(
    input_file_name, 
    output_file_name,
    input_field_delimiter,
    output_field_delimiter
    ):  
    
    ##############################################################################################
    # @BEGIN read_input_data
    # @PARAM input_file_name
    # @PARAM input_field_delimiter
    # @IN input_data @URI file:{input_file_name}
    # @OUT record
    
    # create CSV reader for input records
    input_data = csv.DictReader(open(input_file_name, 'r'),
                                delimiter=input_field_delimiter)

    # iterate over input data records
    for record in input_data:
    
    # @END read_input_data


    ##############################################################################################
    # @BEGIN write_output_data
    # @PARAM output_file_name
    # @PARAM output_field_delimiter
    # @IN record
    # @OUT output_data  @URI file:{output_file_name}

        # open file for storing cleaned data if not already open
        if 'output_data' not in locals():
            output_data = csv.DictWriter(open(output_file_name, 'w'),
                                          input_data.fieldnames, 
                                          delimiter=output_field_delimiter)
            output_data.writeheader()
    
        output_data.writerow(record)
        
    # @END write_output_data

# @END change_field_delimiter


if __name__ == '__main__':

    change_field_delimiter(
        input_file_name=sys.argv[1],
        output_file_name=sys.argv[2],
        input_field_delimiter=',',
        output_field_delimiter=','
    )
