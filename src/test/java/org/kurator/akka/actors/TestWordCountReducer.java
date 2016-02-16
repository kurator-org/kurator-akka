package org.kurator.akka.actors;

import java.util.HashMap;
import java.util.Map;

import org.kurator.akka.ActorConfig;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.PythonActor;
import org.kurator.akka.PythonClassActor;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.messages.EndOfStream;
import org.python.core.PyDictionary;
import org.python.core.PyInteger;
import org.python.core.PyTuple;

public class TestWordCountReducer extends KuratorAkkaTestCase {

    private WorkflowRunner wr;
    private ActorConfig wordCountSource;
    private ActorConfig countReducer;
    
    @Override
    public void setUp() throws Exception {

         super.setUp();

         wr = new WorkflowRunner()
             .outputStream(stdoutStream)
             .errorStream(stderrStream);

         wordCountSource =  
         wr.actor(PythonActor.class)
           .config("code", "def on_data(word_count_tuple): "    + EOL +
                           "  return word_count_tuple"          + EOL);

         countReducer = 
         wr.actor(PythonClassActor.class)
           .config("pythonClass", "kurator_akka.word_count.WordCount.WordCountReducer")
           .config("onData", "reduce_word_counts_sorted")
           .listensTo(wordCountSource);
         
         wr.actor(PythonActor.class)
           .listensTo(countReducer)
           .config("code", "def on_data(reduced_counts): print reduced_counts");
         
         wr.inputActor(wordCountSource);
    }

    private PyTuple wordCountTuple(int textId, int chunkId, int chunkCount, Map<String,Integer> wordCounts) {
        
        PyDictionary pyWordCounts = new PyDictionary();
        for (Map.Entry<String,Integer> wordCount: wordCounts.entrySet()) {
            pyWordCounts.put(wordCount.getKey(), wordCount.getValue());
        }
        
        return new PyTuple(
                new PyInteger(textId), new PyInteger(chunkId), 
                new PyInteger(chunkCount), pyWordCounts);
    }
    
    public void testWordCounter_OneChunk() throws Exception {
        
        Map<String,Integer> wordCounts = new HashMap<String,Integer>();
        wordCounts.put("the", 2);
        wordCounts.put("quick", 1);
        wordCounts.put("brown", 1);
        wordCounts.put("fox", 1);
        wordCounts.put("jumps", 1);
        wordCounts.put("over", 1);
        wordCounts.put("lazy", 1);
        wordCounts.put("dog", 1);
        
        wr.begin();
        wr.tellWorkflow(wordCountTuple(1, 1, 1, wordCounts));
        wr.tellWorkflow(new EndOfStream());
        wr.end();

        assertEquals(                
                "(1, OrderedDict([(u'brown', 1), (u'dog', 1), (u'fox', 1), " +
                "(u'jumps', 1), (u'lazy', 1), (u'over', 1), (u'quick', 1), (u'the', 2)]))"                         + EOL,
                stdoutBuffer.toString());
    }
    
    public void testWordCounter_ThreeRepeatedChunks() throws Exception {
        
        Map<String,Integer> wordCounts = new HashMap<String,Integer>();
        wordCounts.put("the", 2);
        wordCounts.put("quick", 1);
        wordCounts.put("brown", 1);
        wordCounts.put("fox", 1);
        wordCounts.put("jumps", 1);
        wordCounts.put("over", 1);
        wordCounts.put("lazy", 1);
        wordCounts.put("dog", 1);
        
        wr.begin();
        wr.tellWorkflow(wordCountTuple(1, 1, 3, wordCounts));
        wr.tellWorkflow(wordCountTuple(1, 2, 3, wordCounts));
        wr.tellWorkflow(wordCountTuple(1, 3, 3, wordCounts));
        wr.tellWorkflow(new EndOfStream());
        wr.end();

        assertEquals(                
                "(1, OrderedDict([(u'brown', 3), (u'dog', 3), (u'fox', 3), " +
                "(u'jumps', 3), (u'lazy', 3), (u'over', 3), (u'quick', 3), (u'the', 6)]))"                         + EOL,
                stdoutBuffer.toString());
    }
    
    public void testWordCounter_ThreeChunks() throws Exception {
        
        Map<String,Integer> chunkOneCounts = new HashMap<String,Integer>();
        chunkOneCounts.put("the", 2);
        chunkOneCounts.put("quick", 1);
        chunkOneCounts.put("brown", 1);
        chunkOneCounts.put("fox", 1);
        chunkOneCounts.put("jumps", 1);
        chunkOneCounts.put("over", 1);
        chunkOneCounts.put("lazy", 1);
        chunkOneCounts.put("dog", 1);

        Map<String,Integer> chunkTwoCounts = new HashMap<String,Integer>();
        chunkTwoCounts.put("every", 1);
        chunkTwoCounts.put("good", 1);
        chunkTwoCounts.put("boy", 1);
        chunkTwoCounts.put("does", 1);
        chunkTwoCounts.put("fine", 1);

        Map<String,Integer> chunkThreeCounts = new HashMap<String,Integer>();
        chunkThreeCounts.put("a", 3);
        chunkThreeCounts.put("man", 1);
        chunkThreeCounts.put("plan", 1);
        chunkThreeCounts.put("canal", 1);
        chunkThreeCounts.put("panama", 1);

        
        wr.begin();
        wr.tellWorkflow(wordCountTuple(1, 1, 3, chunkOneCounts));
        wr.tellWorkflow(wordCountTuple(1, 2, 3, chunkTwoCounts));
        wr.tellWorkflow(wordCountTuple(1, 3, 3, chunkThreeCounts));
        wr.tellWorkflow(new EndOfStream());
        wr.end();

        assertEquals(                
                "(1, OrderedDict([(u'a', 3), (u'boy', 1), (u'brown', 1), (u'canal', 1), (u'does', 1), "     +
                "(u'dog', 1), (u'every', 1), (u'fine', 1), (u'fox', 1), (u'good', 1), (u'jumps', 1), "      +
                "(u'lazy', 1), (u'man', 1), (u'over', 1), (u'panama', 1), (u'plan', 1), (u'quick', 1), "    +
                "(u'the', 2)]))"                                                                            + EOL,
                stdoutBuffer.toString());
    }}
