package org.kurator.postproccess;

import org.kurator.akka.data.DQReport.Validation;

/**
 * Created by lowery on 4/5/16.
 */
public class ValidationSummary {
    private int numCompliant;
    private int numNotCompliant;

    public ValidationSummary(Validation validation) {
        if (validation.getResult().equals("Compliant")) {
            numCompliant++;
        } else if (validation.getResult().equals("Not Compliant")) {
            numNotCompliant++;
        }
    }

}
