
import os
import optparse
import requests
import tempfile

def download_default(options):
    return download('http://ipt.vertnet.org:8080/ipt/archive.do?r=ccber_mammals', options)

def download(url, options):
    """Download a file from a URL.
    url - full path to the file to download (e.g., 'http://ipt.vertnet.org:8080/ipt/archive.do?r=ccber_mammals')
    options - dictionary with configuration settings
        outputfile - optional path to the output file
        tempdir - optional path to directory to store automatically named file in
    returns name of downloaded file
    """

    outputfile = options.get('outputfile')
    tempdir = options.get('tempdir')

    with _create_output_file(outputfile, tempdir) as f:
        r = requests.get(url, stream=True)
        for block in r.iter_content(1024):
            f.write(block)

    return f.name

def _create_output_file(filepath, tempdir):
    if filepath is None:
        filepath = tempfile.NamedTemporaryFile(dir=tempdir, delete=False).name
    return open(filepath, 'wb')


if __name__ == '__main__':

    # define command line options
    parser = optparse.OptionParser()
    parser.add_option("-u", "--url", dest="url",
                      help="Url for the file to download",
                      default=None)
    parser.add_option("-o", "--output", dest="outputfile",
                      help="Output file",
                      default=None)
    parser.add_option("-t", "--tempdir", dest="tempdir",
                      help="Temporary file directory",
                      default=None)

    # parse command line
    options = parser.parse_args()[0]

    # download file from provided url
    try:
        outputfile = download(options.url, vars(options))
        print 'Downloaded', options.url, 'to',  outputfile
    except Exception as e:
        print e
