package org.kurator.akka.samples;

import java.io.PrintStream;

import org.kurator.akka.ActorConfig;
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
        
        ActorConfig oneShot = wr.actor(ConstantSource.class)
            .parameter("value", 1)
            .parameter("sendEosOnEnd", false);
        
        ActorConfig filter = wr.actor(Filter.class)
            .parameter("max", maxHammingNumber)
            .parameter("sendEosOnExceed", true)
            .listensTo(oneShot);
        
        ActorConfig multiplyByTwo = wr.actor(Multiplier.class)
            .parameter("factor", 2)
            .listensTo(filter);

        ActorConfig multiplyByThree = wr.actor(Multiplier.class)
            .parameter("factor", 3)
            .listensTo(filter);
        
        ActorConfig multiplyByFive = wr.actor(Multiplier.class)
            .parameter("factor", 5)
            .listensTo(filter);
        
        ActorConfig mergeTwoThree = wr.actor(IntegerStreamMerger.class)
            .parameter("streamCount", 2)
            .listensTo(multiplyByTwo)
            .listensTo(multiplyByThree);
           
        ActorConfig mergeTwoThreeFive = wr.actor(IntegerStreamMerger.class)
            .parameter("streamCount", 2)
            .listensTo(multiplyByFive)
            .listensTo(mergeTwoThree);
        
        @SuppressWarnings("unused")
        ActorConfig printStreamWriter = wr.actor(PrintStreamWriter.class)
            .parameter("separator", separator)
            .listensTo(filter);
        
        filter.listensTo(mergeTwoThreeFive);
        
        wr.build();
        wr.run();
    }
}