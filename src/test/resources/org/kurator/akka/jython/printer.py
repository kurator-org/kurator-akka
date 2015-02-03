
import sys

first = 1
delimiter = ","

def printMessage(message):
    
    # print delimiter if not first call to this function
    global first
    if (first):
        first = 0
    else:
        sys.stdout.write(delimiter)

    # print the current message
    sys.stdout.write(str(message))

    # all done
    return 0
