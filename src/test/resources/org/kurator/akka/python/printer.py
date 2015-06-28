
import sys

# default settings
delimiter = ""

def start():
    global first
    first = True

def printMessage(message):
    
    # print delimiter if not first call to this function
    global first
    global delimiter
    if (first):
        first = False
    else:
        sys.stdout.write(delimiter)

    # print the current message
    sys.stdout.write(str(message))

    # all done
    return 0
