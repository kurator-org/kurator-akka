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
                 .parameter("streamCount", 1)
                 .listensTo(repeater);
        
         @SuppressWarnings("unused")
         ActorConfig printer = wr.actor(PrintStreamWriter.class)
                 .parameter("separator", ", ")
                 .listensTo(merge);
        
         wr.inputActor(repeater);
         
         wr.build();
     }
     
    public void testIntegerStreamMerger_NoValues() throws Exception {
        wr.start();
        wr.tellWorkflow(new EndOfStream());
        wr.await();
        assertEquals("", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }
     
     public void testIntegerStreamMerger_DistinctValues() throws Exception {
         wr.start();
         wr.tellWorkflow(1);
         wr.tellWorkflow(2);
         wr.tellWorkflow(3);
         wr.tellWorkflow(4);
         wr.tellWorkflow(new EndOfStream());
         wr.await();
         assertEquals("1, 2, 3, 4", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
    
     public void testIntegerStreamMerger_IdenticalValues() throws Exception {
         wr.start();
         wr.tellWorkflow(7);
         wr.tellWorkflow(7);
         wr.tellWorkflow(7);
         wr.tellWorkflow(7);
         wr.tellWorkflow(new EndOfStream());
         wr.await();
         assertEquals("7", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
    
     public void testIntegerStreamMerger_ValuesWithDuplicates() throws Exception {
         wr.start();
         wr.tellWorkflow(1);
         wr.tellWorkflow(2);
         wr.tellWorkflow(2);
         wr.tellWorkflow(3);
         wr.tellWorkflow(3);
         wr.tellWorkflow(4);
         wr.tellWorkflow(5);
         wr.tellWorkflow(5);
         wr.tellWorkflow(new EndOfStream());
         wr.await();
         assertEquals("1, 2, 3, 4, 5", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
}
