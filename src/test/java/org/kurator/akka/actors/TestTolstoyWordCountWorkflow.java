package org.kurator.akka.actors;

import org.kurator.akka.ActorConfig;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.PythonActor;
import org.kurator.akka.PythonClassActor;
import org.kurator.akka.WorkflowRunner;

public class TestTolstoyWordCountWorkflow extends KuratorAkkaTestCase {

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
         wr.actor(TextFileReader.class)
           .param("filePath", "./src/main/python/kurator_akka/word_count/war-and-peace.txt");

         textChunker = 
         wr.actor(PythonClassActor.class)
           .config("pythonClass", "kurator_akka.word_count.WordCount.TextChunker")
           .config("onData", "split_text_with_counts")
           .listensTo(textSource);
         
         wordCounter = 
         wr.actor(PythonClassActor.class)
           .config("pythonClass", "kurator_akka.word_count.WordCount.WordCounter")
           .config("onData", "count_words_in_chunk")
           .listensTo(textChunker);

         countReducer = 
         wr.actor(PythonClassActor.class)
           .config("pythonClass", "kurator_akka.word_count.WordCount.WordCountReducer")
           .config("onData", "reduce_word_counts_sorted")
           .listensTo(wordCounter);
         
         wr.actor(PythonActor.class)
           .listensTo(countReducer)
           .config("code", "def on_data(word_counts): print word_counts");
         
         wr.inputActor(textSource);
    }
    
    public void testWordCounter_OneChunk() throws Exception {
        wr.run();
    } 

    public void testWordCounter_TenChunks() throws Exception {
        textChunker.param("max_chunks", 10);
        wr.run();
    } 

    public void testWordCounter_HundredChunks() throws Exception {
        textChunker.param("max_chunks", 100);
        wr.run();
    } 

}
