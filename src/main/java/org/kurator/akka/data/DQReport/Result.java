package org.kurator.akka.data.DQReport;

/**
 * Created by lowery on 3/30/16.
 */
public class Result {
    private String comment;
    private String state;

    public Result() {} // default constructor for Jackson

    public Result(String comment, String state) {
        this.comment = comment;
        this.state = state;
    }
}
