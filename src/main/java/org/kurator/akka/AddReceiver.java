package org.kurator.akka;

public class AddReceiver {

    private final String receiverName;

    public AddReceiver(String receiverName) {
        this.receiverName = receiverName;
    }

    public String get() {
        return receiverName;
    }
}
