package org.kurator.akka.actors;

import akka.japi.Procedure;
import akka.persistence.UntypedPersistentActor;
import org.kurator.akka.KuratorActor;
import org.kurator.akka.data.DQReport.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lowery on 4/20/2016.
 */
public class DateValidator extends UntypedPersistentActor {
    private DQReport report = new DQReport();

    @Override
    public String persistenceId() { return "sample-id-4"; }

    @Override
    public void onReceiveRecover(Object o) throws Exception {

    }

    @Override
    public void onReceiveCommand(Object value) {
        List<Assertion> assertions = new ArrayList<Assertion>();
        Map<String, String> dataResource = (Map<String, String>) value;

        String eventDate = dataResource.get("eventDate");

        Result result;

        // measure: eventDate is non null
        if (eventDate == null) {
            result = new Result<MeasurementState>("Event date field contains empty string", MeasurementState.NOT_COMPLETE);
        } else {
            result = new Result<MeasurementState>("Event date is not null", MeasurementState.COMPLETE);
        }

        assertions.add(new Measure(dataResource, "check eventDate completeness",
                "eventDate is non null", "Kurator - Date Validator", result));

        // measure: eventDate specifies a specific date.

        // validation: eventDate format conforms to ISO date format.
        if (eventDate != null) {
            try {
                LocalDate parsedDate = LocalDate.parse(eventDate, DateTimeFormatter.ISO_DATE);
                result = new Result<ValidationState>("Event date is a valid ISO date", ValidationState.COMPLIANT);
            } catch (DateTimeParseException e) {
                result = new Result<ValidationState>("Event date is not a valid ISO date", ValidationState.NOT_COMPLIANT);
            }
        }

        assertions.add(new Measure(dataResource, "Event date format", "eventDate format conforms to ISO date format",
                "Kurator - Date Validator", result));

        // validation: eventDate contains a valid date or date range (e.g. not 1990-Feb-31).

        // validation: eventDate is consistent with day, month, year, startDayOfYear, endDatOfYear.

        // validation: eventDate is consistent with verbatimEventDate

        // enhancement: populate an empty eventDate from day, month, year, and/or verbatimEventDate
        Map<String, String> resultMap = new HashMap<>();
        if (eventDate == null) {
            LocalDate date = LocalDate.of(Integer.parseInt(dataResource.get("year")),
                    Integer.parseInt(dataResource.get("month")),
                    Integer.parseInt(dataResource.get("day")));

            resultMap.put("eventDate", date.format(DateTimeFormatter.ISO_DATE));
            assertions.add(new Improvement(dataResource, "Fill in empty event date",
                    "populate an empty eventDate from day, month, year, and/or verbatimEventDate",
                    "Kurator - Date Validator", resultMap));
        }

        persistAll(assertions, new Procedure<Assertion>() {
            @Override
            public void apply(Assertion assertion) throws Exception {
                addAssertion(assertion);
            }
        });
    }

    public void addAssertion(Assertion assertion) {
        if (assertion instanceof Measure) {
            report.pushMeasure((Measure) assertion);
        } else if (assertion instanceof Improvement) {
            report.pushImprovement((Improvement) assertion);
        } else if (assertion instanceof Validation) {
            report.pushValidation((Validation) assertion);
        }
    }
}
