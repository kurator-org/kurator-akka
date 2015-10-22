package org.kurator.akka.actors;

import org.kurator.akka.ActorConfig;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.PythonActor;
import org.kurator.akka.PythonClassActor;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.messages.EndOfStream;

public class TestWordCountWorkflow extends KuratorAkkaTestCase {

    private WorkflowRunner wr;
    private ActorConfig textSource;
    private ActorConfig textChunker;
    private ActorConfig wordCounter;
    private ActorConfig countReducer;
    
    @Override
    public void setUp() throws Exception {

         super.setUp();

         wr = new WorkflowRunner()
             .outputStream(stdoutStream)
             .errorStream(stderrStream);

         textSource =  
         wr.actor(PythonActor.class)
           .config("code", "def on_data(text): "    + EOL +
                           "  return text"          + EOL);

         textChunker = 
         wr.actor(PythonClassActor.class)
           .config("pythonClass", "org.kurator.akka.word_count.WordCount.TextChunker")
           .config("onData", "split_text_with_counts")
           .listensTo(textSource);
         
         wordCounter = 
         wr.actor(PythonClassActor.class)
           .config("pythonClass", "org.kurator.akka.word_count.WordCount.WordCounter")
           .config("onData", "count_words_in_chunk")
           .listensTo(textChunker);

         countReducer = 
         wr.actor(PythonClassActor.class)
           .config("pythonClass", "org.kurator.akka.word_count.WordCount.WordCountReducer")
           .config("onData", "reduce_word_counts_sorted")
           .listensTo(wordCounter);
         
         wr.actor(PythonActor.class)
           .listensTo(countReducer)
           .config("code", "def on_data(word_counts): print word_counts");
         
         wr.inputActor(textSource);
    }
    
    public void testWordCounter_OneChunk() throws Exception {
        
        wr.begin();
        wr.tellWorkflow("The quick brown fox jumps over the lazy dog.");
        wr.tellWorkflow(new EndOfStream());
        wr.end();
      
        assertEquals(
                "(1, OrderedDict([(u'brown', 1), (u'dog', 1), (u'fox', 1), (u'jumps', 1), " + 
                "(u'lazy', 1), (u'over', 1), (u'quick', 1), (u'the', 2)]))"                 + EOL,
                stdoutBuffer.toString());
    } 

    public void testWordCounter_TwoChunks() throws Exception {
        
        textChunker.param("max_chunks", 2);
        
        wr.begin();
        wr.tellWorkflow("The quick brown fox jumps over the lazy dog");
        wr.tellWorkflow(new EndOfStream());
        wr.end();
      
        assertEquals(
                "(1, OrderedDict([(u'brown', 1), (u'dog', 1), (u'fox', 1), (u'jumps', 1), " + 
                "(u'lazy', 1), (u'over', 1), (u'quick', 1), (u'the', 2)]))"                 + EOL,
                stdoutBuffer.toString());
    } 

    public void testWordCounter_TwelveChunks() throws Exception {
        
        textChunker.param("max_chunks", 12);
        
        wr.begin();
        wr.tellWorkflow("The quick brown fox jumps over the lazy dog");
        wr.tellWorkflow(new EndOfStream());
        wr.end();
      
        assertEquals(
                "(1, OrderedDict([(u'brown', 1), (u'dog', 1), (u'fox', 1), (u'jumps', 1), " + 
                "(u'lazy', 1), (u'over', 1), (u'quick', 1), (u'the', 2)]))"                 + EOL,
                stdoutBuffer.toString());
    } 

    public void testWordCounter_ThreeTexts_FiveChunks() throws Exception {
        
        textChunker.param("max_chunks", 12);
        
        wr.begin();
        wr.tellWorkflow("The quick brown fox jumps over the lazy dog");
        wr.tellWorkflow("Every good boy does fine.");
        wr.tellWorkflow("A man, a plan, a canal, Panama!");
        wr.tellWorkflow(new EndOfStream());
        wr.end();
      
        assertEquals(
                "(1, OrderedDict([(u'brown', 1), (u'dog', 1), (u'fox', 1), (u'jumps', 1), "                 + 
                "(u'lazy', 1), (u'over', 1), (u'quick', 1), (u'the', 2)]))"                                 + EOL +
                "(2, OrderedDict([(u'boy', 1), (u'does', 1), (u'every', 1), (u'fine', 1), (u'good', 1)]))"  + EOL +
                "(3, OrderedDict([(u'a', 3), (u'canal', 1), (u'man', 1), (u'panama', 1), (u'plan', 1)]))"   + EOL,
                stdoutBuffer.toString());
    } 
    
}
