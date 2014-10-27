package org.kurator.akka;

import java.io.PrintStream;
import java.util.concurrent.TimeoutException;

import org.kurator.akka.actors.Filter;
import org.kurator.akka.actors.IntegerStreamMerger;
import org.kurator.akka.actors.Multiplier;
import org.kurator.akka.actors.OneShot;
import org.kurator.akka.actors.PrintStreamWriter;
import akka.actor.ActorRef;

public class Hamming {

    public static void main(String[] args) throws TimeoutException, InterruptedException {
        int maxHammingValue = Integer.parseInt(args[0]);
        Hamming wf = new Hamming(maxHammingValue);
        wf.run();
    }

    private int maxHammingNumber;
    private PrintStream outputStream;
    private String separator;

    public Hamming(int maxHammingNumber, PrintStream outputStream, String separator) {
        this.maxHammingNumber = maxHammingNumber;
        this.outputStream = outputStream;
        this.separator = separator;
    }

    public Hamming(int maxHammingNumber) {
        this(maxHammingNumber, System.out, System.lineSeparator());
    }

    public void run() throws TimeoutException, InterruptedException {

        WorkflowRunner runner = new WorkflowRunner();
        
        ActorConfiguration oneShot = runner.createActor()
                .actorClass(OneShot.class);
        
        ActorConfiguration filter = runner.createActor()
                .actorClass(Filter.class)
                .parameter("max", maxHammingNumber)
                .listensTo(oneShot);
        
        ActorConfiguration multiplyByTwo = runner.createActor()
                .actorClass(Multiplier.class)
                .parameter("factor", 2)
                .listensTo(filter);

        ActorConfiguration multiplyByThree = runner.createActor()
                .actorClass(Multiplier.class)
                .parameter("factor", 3)
                .listensTo(filter);
        
        ActorConfiguration multiplyByFive = runner.createActor()
                .actorClass(Multiplier.class)
                .parameter("factor", 5)
                .listensTo(filter);
        
        ActorConfiguration mergeTwoThree = runner.createActor()
                .actorClass(IntegerStreamMerger.class)
                .parameter("streamCount", 2)
                .listensTo(multiplyByTwo)
                .listensTo(multiplyByThree);
           
        ActorConfiguration mergeTwoThreeFive = runner.createActor()
                .actorClass(IntegerStreamMerger.class)
                .parameter("streamCount", 2)
                .listensTo(multiplyByFive)
                .listensTo(mergeTwoThree);
        
        ActorConfiguration printStreamWriter = runner.createActor()
                .actorClass(PrintStreamWriter.class)
                .parameter("stream", outputStream)
                .parameter("separator", separator)
                .listensTo(filter);
        
        filter.listensTo(mergeTwoThreeFive);

        runner.instantiateWorkflow(oneShot);
        ActorRef workflow = runner.getWorkflowRef();
        
        runner.start();
        workflow.tell(new Integer(1), runner.getActorSystem().lookupRoot());
        runner.await();
    }
}