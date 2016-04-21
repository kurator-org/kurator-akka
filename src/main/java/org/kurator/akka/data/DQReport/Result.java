package org.kurator.akka.data.DQReport;

import java.io.Serializable;

/**
 * Created by lowery on 3/30/16.
 */
public class Result<T extends State> implements Serializable {
    private static final long serialVersionUID = 1L;
    private String comment;
    private T state;

    public Result() {} // default constructor for Jackson

    public Result(String comment, T state) {
        this.comment = comment;
        this.state = state;
    }

    public String getComment() {
        return comment;
    }

    public T getState() {
        return state;
    }
}
