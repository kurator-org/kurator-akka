package org.kurator.akka.data.DQReport;

import java.util.List;

/**
 * Created by lowery on 3/30/16.
 */
public class Context {
    private String name;
    private List<String> dataElements;

    public Context() {} // default constructor for Jackson

    public Context(String name, List<String> dataElements) {
        this.name = name;
        this.dataElements = dataElements;
    }
}
