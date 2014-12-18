package org.kurator.akka.samples;

import java.io.PrintStream;

import org.kurator.akka.ActorBuilder;
import org.kurator.akka.WorkflowBuilder;
import org.kurator.akka.actors.ConstantSource;
import org.kurator.akka.actors.Filter;
import org.kurator.akka.actors.IntegerStreamMerger;
import org.kurator.akka.actors.Multiplier;
import org.kurator.akka.actors.PrintStreamWriter;

public class Hamming {

    public static void main(String[] args) throws Exception {
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

    public void run() throws Exception {

        WorkflowBuilder wfb = new WorkflowBuilder()
                .outputStream(outputStream);
        
        ActorBuilder oneShot = wfb.createActorBuilder()
                .name("oneShot")
                .actorClass(ConstantSource.class)
                .parameter("value", 1)
                .parameter("sendEos", false);
        
        ActorBuilder filter = wfb.createActorBuilder()
                .name("filter")
                .actorClass(Filter.class)
                .parameter("max", maxHammingNumber)
                .parameter("sendEosOnExceed", true)
                .listensTo(oneShot);
        
        ActorBuilder multiplyByTwo = wfb.createActorBuilder()
                .name("multiplyByTwo")
                .actorClass(Multiplier.class)
                .parameter("factor", 2)
                .listensTo(filter);

        ActorBuilder multiplyByThree = wfb.createActorBuilder()
                .name("multiplyByThree")
                .actorClass(Multiplier.class)
                .parameter("factor", 3)
                .listensTo(filter);
        
        ActorBuilder multiplyByFive = wfb.createActorBuilder()
                .name("multiplyByFive")
                .actorClass(Multiplier.class)
                .parameter("factor", 5)
                .listensTo(filter);
        
        ActorBuilder mergeTwoThree = wfb.createActorBuilder()
                .name("mergeTwoThree")
                .actorClass(IntegerStreamMerger.class)
                .parameter("streamCount", 2)
                .listensTo(multiplyByTwo)
                .listensTo(multiplyByThree);
           
        ActorBuilder mergeTwoThreeFive = wfb.createActorBuilder()
                .name("mergeTwoThreeFive")
                .actorClass(IntegerStreamMerger.class)
                .parameter("streamCount", 2)
                .listensTo(multiplyByFive)
                .listensTo(mergeTwoThree);
        
        @SuppressWarnings("unused")
        ActorBuilder printStreamWriter = wfb.createActorBuilder()
                .name("printStreamWriter")
                .actorClass(PrintStreamWriter.class)
                .parameter("separator", separator)
                .listensTo(filter);
        
        filter.listensTo(mergeTwoThreeFive);
        
        wfb.build();
        wfb.run();
    }
}