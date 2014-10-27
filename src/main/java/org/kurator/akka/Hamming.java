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

        WorkflowBuilder builder = new WorkflowBuilder();
        
        ActorBuilder oneShot = builder.createActorBuilder()
                .actorClass(OneShot.class);
        
        ActorBuilder filter = builder.createActorBuilder()
                .actorClass(Filter.class)
                .parameter("max", maxHammingNumber)
                .listensTo(oneShot);
        
        ActorBuilder multiplyByTwo = builder.createActorBuilder()
                .actorClass(Multiplier.class)
                .parameter("factor", 2)
                .listensTo(filter);

        ActorBuilder multiplyByThree = builder.createActorBuilder()
                .actorClass(Multiplier.class)
                .parameter("factor", 3)
                .listensTo(filter);
        
        ActorBuilder multiplyByFive = builder.createActorBuilder()
                .actorClass(Multiplier.class)
                .parameter("factor", 5)
                .listensTo(filter);
        
        ActorBuilder mergeTwoThree = builder.createActorBuilder()
                .actorClass(IntegerStreamMerger.class)
                .parameter("streamCount", 2)
                .listensTo(multiplyByTwo)
                .listensTo(multiplyByThree);
           
        ActorBuilder mergeTwoThreeFive = builder.createActorBuilder()
                .actorClass(IntegerStreamMerger.class)
                .parameter("streamCount", 2)
                .listensTo(multiplyByFive)
                .listensTo(mergeTwoThree);
        
        ActorBuilder printStreamWriter = builder.createActorBuilder()
                .actorClass(PrintStreamWriter.class)
                .parameter("stream", outputStream)
                .parameter("separator", separator)
                .listensTo(filter);
        
        filter.listensTo(mergeTwoThreeFive);

        builder.inputActor(oneShot);
        ActorRef workflow = builder.build();
        
        builder.startWorkflow();
        workflow.tell(new Integer(1), builder.getActorSystem().lookupRoot());
        builder.awaitWorkflow();
    }
}