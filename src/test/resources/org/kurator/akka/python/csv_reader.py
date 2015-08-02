import csv

filename=None

def read_file():
  dr = csv.DictReader(open(filename, 'r'))
  for r in dr:
    yield r
