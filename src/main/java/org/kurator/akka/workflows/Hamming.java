package org.kurator.akka.workflows;

import java.io.PrintStream;
import java.util.concurrent.TimeoutException;

import org.kurator.akka.ActorBuilder;
import org.kurator.akka.WorkflowBuilder;
import org.kurator.akka.actors.ConstantSource;
import org.kurator.akka.actors.Filter;
import org.kurator.akka.actors.IntegerStreamMerger;
import org.kurator.akka.actors.Multiplier;
import org.kurator.akka.actors.PrintStreamWriter;

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

        WorkflowBuilder wfb = new WorkflowBuilder();
        
        ActorBuilder oneShot = wfb.createActorBuilder()
                .actorClass(ConstantSource.class)
                .parameter("value", 1)
                .parameter("sendEos", false);
        
        ActorBuilder filter = wfb.createActorBuilder()
                .actorClass(Filter.class)
                .parameter("max", maxHammingNumber)
                .listensTo(oneShot);
        
        ActorBuilder multiplyByTwo = wfb.createActorBuilder()
                .actorClass(Multiplier.class)
                .parameter("factor", 2)
                .listensTo(filter);

        ActorBuilder multiplyByThree = wfb.createActorBuilder()
                .actorClass(Multiplier.class)
                .parameter("factor", 3)
                .listensTo(filter);
        
        ActorBuilder multiplyByFive = wfb.createActorBuilder()
                .actorClass(Multiplier.class)
                .parameter("factor", 5)
                .listensTo(filter);
        
        ActorBuilder mergeTwoThree = wfb.createActorBuilder()
                .actorClass(IntegerStreamMerger.class)
                .parameter("streamCount", 2)
                .listensTo(multiplyByTwo)
                .listensTo(multiplyByThree);
           
        ActorBuilder mergeTwoThreeFive = wfb.createActorBuilder()
                .actorClass(IntegerStreamMerger.class)
                .parameter("streamCount", 2)
                .listensTo(multiplyByFive)
                .listensTo(mergeTwoThree);
        
        @SuppressWarnings("unused")
        ActorBuilder printStreamWriter = wfb.createActorBuilder()
                .actorClass(PrintStreamWriter.class)
                .parameter("stream", outputStream)
                .parameter("separator", separator)
                .listensTo(filter);
        
        filter.listensTo(mergeTwoThreeFive);
        
        wfb.build();
        wfb.run();
    }
}