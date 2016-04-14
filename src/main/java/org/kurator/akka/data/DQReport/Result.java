package org.kurator.akka.data.DQReport;

/**
 * Created by lowery on 3/30/16.
 */
public class Result<T extends State> {
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
