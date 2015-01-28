package org.kurator.akka.samples;

import java.io.PrintStream;

import org.kurator.akka.ActorBuilder;
import org.kurator.akka.WorkflowRunner;
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

        WorkflowRunner wr = new WorkflowRunner()
                .outputStream(outputStream);
        
        ActorBuilder oneShot = wr.createActorBuilder()
                .name("oneShot")
                .actorClass(ConstantSource.class)
                .parameter("value", 1)
                .parameter("sendEos", false);
        
        ActorBuilder filter = wr.createActorBuilder()
                .name("filter")
                .actorClass(Filter.class)
                .parameter("max", maxHammingNumber)
                .parameter("sendEosOnExceed", true)
                .listensTo(oneShot);
        
        ActorBuilder multiplyByTwo = wr.createActorBuilder()
                .name("multiplyByTwo")
                .actorClass(Multiplier.class)
                .parameter("factor", 2)
                .listensTo(filter);

        ActorBuilder multiplyByThree = wr.createActorBuilder()
                .name("multiplyByThree")
                .actorClass(Multiplier.class)
                .parameter("factor", 3)
                .listensTo(filter);
        
        ActorBuilder multiplyByFive = wr.createActorBuilder()
                .name("multiplyByFive")
                .actorClass(Multiplier.class)
                .parameter("factor", 5)
                .listensTo(filter);
        
        ActorBuilder mergeTwoThree = wr.createActorBuilder()
                .name("mergeTwoThree")
                .actorClass(IntegerStreamMerger.class)
                .parameter("streamCount", 2)
                .listensTo(multiplyByTwo)
                .listensTo(multiplyByThree);
           
        ActorBuilder mergeTwoThreeFive = wr.createActorBuilder()
                .name("mergeTwoThreeFive")
                .actorClass(IntegerStreamMerger.class)
                .parameter("streamCount", 2)
                .listensTo(multiplyByFive)
                .listensTo(mergeTwoThree);
        
        @SuppressWarnings("unused")
        ActorBuilder printStreamWriter = wr.createActorBuilder()
                .name("printStreamWriter")
                .actorClass(PrintStreamWriter.class)
                .parameter("separator", separator)
                .listensTo(filter);
        
        filter.listensTo(mergeTwoThreeFive);
        
        wr.build();
        wr.run();
    }
}