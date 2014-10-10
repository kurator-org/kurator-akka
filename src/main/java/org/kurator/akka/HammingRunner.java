package org.kurator.akka;

import java.util.concurrent.TimeoutException;

public class HammingRunner {

    public static void main(String[] args) throws TimeoutException,
            InterruptedException {

        int maxHammingValue = Integer.parseInt(args[0]);
        Hamming wf = new Hamming(maxHammingValue);
        wf.run();
    }

}
