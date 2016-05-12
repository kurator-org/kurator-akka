package org.kurator.postproccess;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.kurator.akka.data.DQReport.DQReport;
import org.kurator.akka.data.DQReport.Improvement;
import org.kurator.akka.data.DQReport.Validation;
import play.libs.Json;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by lowery on 4/5/16.
 */
public class ReportSummary {
    private List<DQReport> reports;
    private Map<String, List<Validation>> compliant = new HashMap<>();
    private Map<String, List<Validation>> nonCompliant = new HashMap<>();

    private Map<String, List<Improvement>> improvements = new HashMap<>();

    public ReportSummary(InputStream in) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        this.reports = mapper.readValue(in, new TypeReference<List<DQReport>>(){});

        initSummary();
    }

    public ReportSummary(List<DQReport> reports) {
        this.reports = reports;

        initSummary();
    }

    private void initSummary() {
        for (DQReport report : reports) {
            for (Validation validation : report.getValidations()) {
                String criterion = validation.getCriterion();

                if (validation.getResult() != null && validation.getResult().equals("Compliant")) {
                    if (!compliant.containsKey(criterion)) {
                        compliant.put(criterion, new ArrayList<>());
                    }

                    List<Validation> compliantValidations = compliant.get(criterion);

                    compliantValidations.add(validation);
                } else if (validation.getResult() == null || validation.getResult().equals("Not Compliant")) {
                    if (!nonCompliant.containsKey(criterion)) {
                        nonCompliant.put(criterion, new ArrayList<>());
                    }

                    List<Validation> nonCompliantValidations = nonCompliant.get(criterion);

                    nonCompliantValidations.add(validation);
                }
            }

            for (Improvement improvement : report.getImprovements()) {
                String enhancement = improvement.getEnhancement();

                if (!improvements.containsKey(enhancement)) {
                    improvements.put(enhancement, new ArrayList<>());
                }

                List<Improvement> improvementsList = improvements.get(enhancement);

                //improvementsList.add(improvement);

                if (improvement.getResult() != null && !improvement.getResult().isEmpty()) {
                    improvementsList.add(improvement);
                }
            }
        }
    }

    public int getCompliantCount(String criterion) {
        return compliant.get(criterion).size();
    }

    public int getNonCompliantCount(String criterion) {
        return nonCompliant.get(criterion).size();
    }

    public int getImprovementsCount(String criterion) {
        List<Improvement> improvementsForValidation = new ArrayList<>();


        for (Validation validation : nonCompliant.get(criterion)) {
            if (validation.getImprovement() != null) {
                improvementsForValidation.add(validation.getImprovement());
            }
        }
        return improvementsForValidation.size();
    }

    public int getNonCompliantAfterImprovementsCount(String criterion) {
        return nonCompliant.get(criterion).size() - getImprovementsCount(criterion);
    }

    public Set<String> getEnhancements() {
        return improvements.keySet();
    }

    public Set<String> getCompliantCriterion() {
        return compliant.keySet();
    }

    public Set<String> getNonCompliantCriterion() {
        return nonCompliant.keySet();
    }

    public Set<String> getAllCriterion() {
        Set<String> criterion = new HashSet<>();

        criterion.addAll(getCompliantCriterion());
        criterion.addAll(getNonCompliantCriterion());

        return criterion;
    }

    public String asJson() {
        System.out.println("Compliant");
        for (String criterion : getCompliantCriterion()) {
            System.out.println("    " + criterion + ": " + getCompliantCount(criterion));
        }

        System.out.println("\nNon Compliant");
        for (String criterion : getNonCompliantCriterion()) {
            System.out.println("    " + criterion + ": " + getNonCompliantCount(criterion));
        }

        System.out.println("\nImprovements");
        for (String enhancement : getEnhancements()) {
            System.out.println("    " + enhancement + ": " + getImprovementsCount(enhancement));
        }

        ArrayNode data = Json.newArray();

        int compliantCount = getCompliantCount("Coordinates must fall inside the country");
        ObjectNode compliant = Json.newObject();

        compliant.put("value", compliantCount);
        compliant.put("color", "#66cdaa");
        compliant.put("highlight", "#a3e1cc");
        compliant.put("label", "Compliant");

        data.add(compliant);

        int improvementsCount = getImprovementsCount(
                "Coordinates must fall inside the country");
        ObjectNode improvements = Json.newObject();

        improvements.put("value", improvementsCount);
        improvements.put("color", "#6897bb");
        improvements.put("highlight", "#a4c0d6");
        improvements.put("label", "Improvements");

        data.add(improvements);


        int nonCompliantCount = getNonCompliantAfterImprovementsCount(
                "Coordinates must fall inside the country");
        ObjectNode nonCompliant = Json.newObject();

        nonCompliant.put("value", nonCompliantCount);
        nonCompliant.put("color", "#fa8072");
        nonCompliant.put("highlight", "#fcb2aa");
        nonCompliant.put("label", "Non Compliant");

        data.add(nonCompliant);
        return data.toString();
    }
}
