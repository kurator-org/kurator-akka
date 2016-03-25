
# actor parameters
start=1
end=1
step=1

# actor on_start method
def ramp():
    global start
    global end
    for value in range(start, end + 1, step):
        yield value
