package org.kurator.akka.actors;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;

import org.kurator.util.FileIO;

public class TextFileReader extends OneShot {

    public Reader inputReader = null;
    public String filePath = null;

    @Override
    public void fireOnce() throws Exception {
        
        if (inputReader == null) {
            if (filePath != null) {
                inputReader = getFileReaderForPath(filePath);
            } else {
                inputReader = new InputStreamReader(inStream);
            }
        }
        
        String text = FileIO.readTextFromReader(inputReader);
                
        broadcast(text);
    }

    private Reader getFileReaderForPath(String path) throws FileNotFoundException {
        
        Reader reader = null;
        
        try {        
            reader = new FileReader(path);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Input file not found: " + filePath);
        }
        
        return reader;
    }

}

