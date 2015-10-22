package org.kurator.akka.actors;

import org.kurator.akka.ActorConfig;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.PythonActor;
import org.kurator.akka.PythonClassActor;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.messages.EndOfStream;
import org.python.core.PyInteger;
import org.python.core.PyString;
import org.python.core.PyTuple;

public class TestWordCounter extends KuratorAkkaTestCase {

    private WorkflowRunner wr;
    private ActorConfig textChunkSource;
    private ActorConfig wordCounter;
    
    @Override
    public void setUp() throws Exception {

         super.setUp();

         wr = new WorkflowRunner()
             .outputStream(stdoutStream)
             .errorStream(stderrStream);

         textChunkSource =  
         wr.actor(PythonActor.class)
           .config("code", "def on_data(text_chunk_tuple): "    + EOL +
                           "  return text_chunk_tuple"          + EOL);

         wordCounter = 
         wr.actor(PythonClassActor.class)
           .config("pythonClass", "org.kurator.akka.word_count.WordCount.WordCounter")
           .config("onData", "count_words_in_chunk")
           .listensTo(textChunkSource);

         wr.actor(PythonActor.class)
           .listensTo(wordCounter)
           .config("code", "def on_data(word_counts): print word_counts");
         
         wr.inputActor(textChunkSource);
    }

    private PyTuple textChunkTuple(int textId, int chunkId, int chunkCount, String textChunk) {
        return new PyTuple(
            new PyInteger(textId), 
            new PyInteger(chunkId), 
            new PyInteger(chunkCount), 
            new PyString(textChunk)
        );
    }
    
    public void testWordCounter_OneChunk() throws Exception {
        
        wr.begin();
        wr.tellWorkflow(textChunkTuple(1,1,1,"The quick brown fox jumps over the lazy dog."));
        wr.tellWorkflow(new EndOfStream());
        wr.end();
      
        assertEquals(
                "(1, 1, 1, OrderedDict([('the', 2), ('quick', 1), ('brown', 1), ('fox', 1), "   +
                "('jumps', 1), ('over', 1), ('lazy', 1), ('dog', 1)]))"                         + EOL,
                stdoutBuffer.toString());
    }
    
    public void testWordCounter_ThreeChunks() throws Exception {
        
        wr.begin();
        wr.tellWorkflow(textChunkTuple(1,1,1,"The quick brown fox jumps over the lazy dog."));
        wr.tellWorkflow(textChunkTuple(2,1,1,"Every good boy does fine."));
        wr.tellWorkflow(textChunkTuple(3,1,1,"A man, a plan, a canal, Panama!"));
        wr.tellWorkflow(new EndOfStream());
        wr.end();
      
        assertEquals(
                "(1, 1, 1, OrderedDict([('the', 2), ('quick', 1), ('brown', 1), ('fox', 1), "               +
                "('jumps', 1), ('over', 1), ('lazy', 1), ('dog', 1)]))"                                     + EOL +
                "(2, 1, 1, OrderedDict([('every', 1), ('good', 1), ('boy', 1), ('does', 1), ('fine', 1)]))" + EOL +
                "(3, 1, 1, OrderedDict([('a', 3), ('man', 1), ('plan', 1), ('canal', 1), ('panama', 1)]))"  + EOL,
                stdoutBuffer.toString());
    }

    public void testWordCounter_LargeChunk() throws Exception {
        
        String warAndPeaceExcerpt = 
                "Each visitor performed the ceremony of greeting this old aunt whom"        + EOL +
                "not one of them knew, not one of them wanted to know, and not one of"      + EOL +
                "them cared about; Anna Pavlovna observed these greetings with mournful"    + EOL +
                "and solemn interest and silent approval. The aunt spoke to each of"        + EOL +
                "them in the same words, about their health and her own, and the health"    + EOL +
                "of Her Majesty, 'who, thank God, was better today.' And each"              + EOL +
                "visitor, though politeness prevented his showing impatience, left"         + EOL +
                "the old woman with a sense of relief at having performed a vexatious"      + EOL +
                "duty and did not return to her the whole evening."                         + EOL;
                
        wr.begin();
        wr.tellWorkflow(textChunkTuple(1,1,1,warAndPeaceExcerpt));
        wr.tellWorkflow(new EndOfStream());
        wr.end();
      
        assertEquals(
                "(1, 1, 1, OrderedDict([('each', 3), ('visitor', 2), ('performed', 2), ('the', 6), ('ceremony', 1), "   + 
                "('of', 7), ('greeting', 1), ('this', 1), ('old', 2), ('aunt', 2), ('whom', 1), ('not', 4), "           +
                "('one', 3), ('them', 4), ('knew', 1), ('wanted', 1), ('to', 3), ('know', 1), ('and', 7), "             +
                "('cared', 1), ('about', 2), ('anna', 1), ('pavlovna', 1), ('observed', 1), ('these', 1), "             +
                "('greetings', 1), ('with', 2), ('mournful', 1), ('solemn', 1), ('interest', 1), ('silent', 1), "       +
                "('approval', 1), ('spoke', 1), ('in', 1), ('same', 1), ('words', 1), ('their', 1), ('health', 2), "    +
                "('her', 3), ('own', 1), ('majesty', 1), ('who', 1), ('thank', 1), ('god', 1), ('was', 1), "            +
                "('better', 1), ('today', 1), ('though', 1), ('politeness', 1), ('prevented', 1), ('his', 1), "         +
                "('showing', 1), ('impatience', 1), ('left', 1), ('woman', 1), ('a', 2), ('sense', 1), ('relief', 1), " +
                "('at', 1), ('having', 1), ('vexatious', 1), ('duty', 1), ('did', 1), ('return', 1), ('whole', 1), "    +
                "('evening', 1)]))" + EOL,
                stdoutBuffer.toString());
    }
}
