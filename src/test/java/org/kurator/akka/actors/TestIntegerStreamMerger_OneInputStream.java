package org.kurator.akka.actors;

import org.kurator.akka.ActorConfig;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.messages.EndOfStream;

public class TestIntegerStreamMerger_OneInputStream extends KuratorAkkaTestCase {

    private WorkflowRunner wr;

     @Override
     public void setUp() throws Exception {

         super.setUp();
         
         wr = new WorkflowRunner()
             .outputStream(stdoutStream)
             .errorStream(stderrStream);
    
         ActorConfig repeater = wr.actor(Repeater.class);
    
         ActorConfig merge = wr.actor(IntegerStreamMerger.class)
                 .param("streamCount", 1)
                 .listensTo(repeater);
        
         @SuppressWarnings("unused")
         ActorConfig printer = wr.actor(PrintStreamWriter.class)
                 .param("separator", ", ")
                 .listensTo(merge);
        
         wr.inputActor(repeater);
         
     }
     
    public void testIntegerStreamMerger_NoValues() throws Exception {
        wr.begin();
        wr.tell(new EndOfStream());
        wr.end();
        assertEquals("", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }
     
     public void testIntegerStreamMerger_DistinctValues() throws Exception {
         wr.begin();
         wr.tell(1, 2, 3, 4, new EndOfStream());
         wr.end();
         assertEquals("1, 2, 3, 4", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
    
     public void testIntegerStreamMerger_IdenticalValues() throws Exception {
         wr.begin();
         wr.tell(7, 7, 7, 7, new EndOfStream());
         wr.end();
         assertEquals("7", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
    
     public void testIntegerStreamMerger_ValuesWithDuplicates() throws Exception {
         wr.begin();
         wr.tell(1, 2, 2, 3, 3, 4, 5, 5, new EndOfStream());
         wr.end();
         assertEquals("1, 2, 3, 4, 5", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
}
