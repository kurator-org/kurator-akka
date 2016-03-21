package org.kurator.akka.actors;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.kurator.akka.ActorConfig;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.PythonActor;
import org.kurator.akka.PythonClassActor;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.messages.EndOfStream;

public class TestTextChunker extends KuratorAkkaTestCase {

    private WorkflowRunner wr;
    private ActorConfig textSource;
    private ActorConfig textChunker;
    
    @Override
    public void setUp() throws Exception {

         super.setUp();
         buildWorkflow();
    }
    
    private void buildWorkflow() throws Exception {

        stdoutBuffer = new ByteArrayOutputStream();
        stdoutStream = new PrintStream(stdoutBuffer);
    
        stderrBuffer = new ByteArrayOutputStream();
        stderrStream = new PrintStream(stderrBuffer);

        wr = new WorkflowRunner()
            .outputStream(stdoutStream)
            .errorStream(stderrStream);

        textSource =  
        wr.actor(PythonActor.class)
          .config("code", "def on_data(text): "    + EOL +
                          "  return text"          + EOL);

        textChunker = 
        wr.actor(PythonClassActor.class)
          .config("pythonClass", "kurator_akka.word_count.WordCount.TextChunker")
          .config("onData", "split_text_with_counts")
          .listensTo(textSource);

        wr.actor(PythonActor.class)
          .listensTo(textChunker)
          .config("code", "def on_data(text): print text");
        
        wr.inputActor(textSource);
   }
   

    public void testTextChunker_DefaultMaxChunks() throws Exception {

        wr.begin();
        wr.tellWorkflow("The quick brown fox jumps over the lazy dog.");
        wr.tellWorkflow(new EndOfStream());
        wr.end();
      
        assertEquals(
                "(1, 1, 1, u'The quick brown fox jumps over the lazy dog.')"      + EOL,   
                stdoutBuffer.toString());
    }

    public void testTextChunker_TwoChunks() throws Exception {

        textChunker.param("max_chunks", 2);
        
        wr.begin();
        wr.tellWorkflow("The quick brown fox jumps over the lazy dog.");
        wr.tellWorkflow(new EndOfStream());
        wr.end();
      
        assertEquals(
                "(1, 1, 2, u'The quick brown fox jumps')"      + EOL +
                "(1, 2, 2, u' over the lazy dog.')"            + EOL,
                stdoutBuffer.toString());
    }

    public void testTextChunker_ThreeChunks() throws Exception {

        for (int i = 1; i <= 100; i++) {
            
//            System.out.println("Iteration " + i);
            
            textChunker.param("max_chunks", 3);    
            
            wr.begin();
            wr.tellWorkflow("The quick brown fox jumps over the lazy dog.");
            wr.tellWorkflow(new EndOfStream());
            wr.end();
          
            assertEquals(
                    "(1, 1, 3, u'The quick brown')"        + EOL +
                    "(1, 2, 3, u' fox jumps over')"        + EOL +
                    "(1, 3, 3, u' the lazy dog.')"         + EOL,
                    stdoutBuffer.toString());
            
            buildWorkflow();
        }
    }

    public void testTextChunker_NineChunks() throws Exception {

        textChunker.param("max_chunks", 9);
        
        wr.begin();
        wr.tellWorkflow("The quick brown fox jumps over the lazy dog.");
        wr.tellWorkflow(new EndOfStream());
        wr.end();
      
        assertEquals(
                "(1, 1, 9, u'The quick')"  + EOL +
                "(1, 2, 9, u' brown')"     + EOL +
                "(1, 3, 9, u' fox')"       + EOL +
                "(1, 4, 9, u' jumps')"     + EOL +
                "(1, 5, 9, u' over')"      + EOL +
                "(1, 6, 9, u' the')"       + EOL +
                "(1, 7, 9, u' lazy')"      + EOL +
                "(1, 8, 9, u' dog')"       + EOL +
                "(1, 9, 9, u'.')"          + EOL,
                stdoutBuffer.toString());
    }
    
    public void testTextChunker_TwelveChunks() throws Exception {

        textChunker.param("max_chunks", 12);    
        
        wr.begin();
        wr.tellWorkflow("The quick brown fox jumps over the lazy dog.");
        wr.tellWorkflow(new EndOfStream());
        wr.end();
      
        assertEquals(
                "(1, 1, 12, u'The')"       + EOL +
                "(1, 2, 12, u' quick')"    + EOL +
                "(1, 3, 12, u' brown')"    + EOL +
                "(1, 4, 12, u' fox')"      + EOL +
                "(1, 5, 12, u' jumps')"    + EOL +
                "(1, 6, 12, u' over')"     + EOL +
                "(1, 7, 12, u' the')"      + EOL +
                "(1, 8, 12, u' lazy')"     + EOL +
                "(1, 9, 12, u' dog')"      + EOL +
                "(1, 10, 12, u'.')"        + EOL +
                "(1, 11, 12, '')"          + EOL +
                "(1, 12, 12, '')"          + EOL,
                stdoutBuffer.toString());
    }    
    
    public void testTextChunker_MultipleTexts_DefaultMaxChunks() throws Exception {

        wr.begin();
        wr.tellWorkflow("The quick brown fox jumps over the lazy dog.");
        wr.tellWorkflow("Every good boy does fine.");
        wr.tellWorkflow("A man, a plan, a canal, Panama!");
        wr.tellWorkflow(new EndOfStream());
        wr.end();
      
        assertEquals(
                "(1, 1, 1, u'The quick brown fox jumps over the lazy dog.')"    + EOL +
                "(2, 1, 1, u'Every good boy does fine.')"                       + EOL +
                "(3, 1, 1, u'A man, a plan, a canal, Panama!')"                 + EOL,
                stdoutBuffer.toString());
    }
    
    public void testTextChunker_MultipleTexts_FiveChunks() throws Exception {

        textChunker.param("max_chunks", 5);
        
        wr.begin();
        wr.tellWorkflow("The quick brown fox jumps over the lazy dog.");
        wr.tellWorkflow("Every good boy does fine.");
        wr.tellWorkflow("A man, a plan, a canal, Panama!");
        wr.tellWorkflow(new EndOfStream());
        wr.end();
      
        assertEquals(
                "(1, 1, 5, u'The quick')"   + EOL +
                "(1, 2, 5, u' brown fox')"  + EOL +
                "(1, 3, 5, u' jumps over')" + EOL +
                "(1, 4, 5, u' the lazy')"   + EOL +
                "(1, 5, 5, u' dog.')"       + EOL +
                "(2, 1, 5, u'Every')"       + EOL +
                "(2, 2, 5, u' good')"       + EOL +
                "(2, 3, 5, u' boy does')"   + EOL +
                "(2, 4, 5, u' fine')"       + EOL +
                "(2, 5, 5, u'.')"           + EOL +
                "(3, 1, 5, u'A man,')"      + EOL +
                "(3, 2, 5, u' a plan')"     + EOL +
                "(3, 3, 5, u', a canal')"   + EOL +
                "(3, 4, 5, u', Panama')"    + EOL +
                "(3, 5, 5, u'!')"           + EOL,
                stdoutBuffer.toString());
    }
}
