package org.kurator.akka;


import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
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
        
        ActorConfiguration oneShot = new ActorConfiguration(OneShot.class);
        
        ActorConfiguration filter = new ActorConfiguration(Filter.class);
        filter.set("max", maxHammingNumber);
        
        ActorConfiguration multiplyByTwo = new ActorConfiguration(Multiplier.class);
        multiplyByTwo.set("factor", 2);

        ActorConfiguration multiplyByThree = new ActorConfiguration(Multiplier.class);
        multiplyByThree.set("factor", 3);

        ActorConfiguration multiplyByFive = new ActorConfiguration(Multiplier.class);
        multiplyByFive.set("factor", 5);

        ActorConfiguration mergeTwoThree = new ActorConfiguration(IntegerStreamMerger.class);
        mergeTwoThree.set("streamCount", 2);
        
        ActorConfiguration mergeTwoThreeFive = new ActorConfiguration(IntegerStreamMerger.class);
        mergeTwoThreeFive.set("streamCount", 2);
        
        ActorConfiguration printStreamWriter = new ActorConfiguration(PrintStreamWriter.class);
        printStreamWriter.set("stream", outputStream);
        printStreamWriter.set("separator", separator);
        
        oneShot.listener(filter);
        filter.listener(printStreamWriter);
        filter.listener(multiplyByTwo);
        filter.listener(multiplyByThree);
        filter.listener(multiplyByFive);
        multiplyByTwo.listener(mergeTwoThree);
        multiplyByThree.listener(mergeTwoThree);
        multiplyByFive.listener(mergeTwoThreeFive);
        mergeTwoThree.listener(mergeTwoThreeFive);
        mergeTwoThreeFive.listener(filter);
        
        List<ActorConfiguration> actors = new LinkedList<ActorConfiguration>();
        actors.add(oneShot);
        actors.add(filter);
        actors.add(multiplyByTwo);
        actors.add(multiplyByThree);
        actors.add(multiplyByFive);
        actors.add(mergeTwoThree);
        actors.add(mergeTwoThreeFive);
        actors.add(printStreamWriter);

        WorkflowRunner runner = new WorkflowRunner(actors, oneShot);
        ActorRef workflow = runner.getWorkflowRef();
        
        runner.start();
        workflow.tell(new Integer(1), runner.getActorSystem().lookupRoot());
        runner.await();
    }
}