package org.kurator.akka.messages;

import java.util.Objects;

/**
 * Created by lowery on 2/17/2016.
 */
public class FutureComplete implements ControlMessage {
    private final Object value;

    public FutureComplete(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
