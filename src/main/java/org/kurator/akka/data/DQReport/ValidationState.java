package org.kurator.akka.data.DQReport;

/**
 * Created by lowery on 4/14/16.
 */
public enum ValidationState implements State {
    COMPLIANT, NOT_COMPLIANT, UNABLE_TO_VALIDATE
}
