package org.kurator.postproccess.ffdq;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.kurator.akka.data.DQReport.DQReport;
import org.kurator.akka.data.DQReport.Improvement;
import org.kurator.akka.data.DQReport.Measure;
import org.kurator.akka.data.DQReport.Validation;
import org.kurator.postproccess.PostProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by lowery on 3/29/16.
 */
public class FullReportPostProcessor implements PostProcessor {

    public void postprocess(InputStream in, String format, OutputStream out) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<DQReport> reports = mapper.readValue(in, new TypeReference<List<DQReport>>(){});

        if ("xls".equalsIgnoreCase(format)) {
            writeXls(reports, out);
        } if ("json".equalsIgnoreCase(format)) {
            mapper.writeValue(out, reports);
        } else {
            throw new IllegalArgumentException("Invalid output format for postprocessor: " + format);
        }
    }

    private void writeXls(List<DQReport> reports, OutputStream out) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet measures = wb.createSheet("Measures");
        HSSFSheet validations = wb.createSheet("Validations");
        HSSFSheet improvements = wb.createSheet("Improvements");

        List<String> measureKeys = new ArrayList<String>();
        measureKeys.addAll(reports.get(0).getMeasures().get(0).getDataResource().keySet());

        HSSFRow measuresHeader = measures.createRow(0);

        measuresHeader.createCell(0).setCellValue("Dimension");
        measuresHeader.createCell(1).setCellValue("Mechanism");
        measuresHeader.createCell(2).setCellValue("Result");
        measuresHeader.createCell(3).setCellValue("Specification");

        for (int i = 0, offset = 4; i < measureKeys.size(); i++) {
            measuresHeader.createCell(i+offset).setCellValue(measureKeys.get(i));
        }

        List<String> validationKeys = new ArrayList<String>(); // list instead of set to preserve ordering
        validationKeys.addAll(reports.get(0).getValidations().get(0).getDataResource().keySet());

        HSSFRow validationsHeader = validations.createRow(0);

        validationsHeader.createCell(0).setCellValue("Criterion");
        validationsHeader.createCell(1).setCellValue("Mechanism");
        validationsHeader.createCell(2).setCellValue("Result");
        validationsHeader.createCell(3).setCellValue("Specification");

        for (int i = 0, offset = 4; i < measureKeys.size(); i++) {
            validationsHeader.createCell(i+offset).setCellValue(validationKeys.get(i));
        }

        List<String> improvementKeys = new ArrayList<String>();
        improvementKeys.addAll(reports.get(0).getImprovements().get(0).getDataResource().keySet());

        HSSFRow improvementsHeader = improvements.createRow(0);

        improvementsHeader.createCell(0).setCellValue("Enhancement");
        improvementsHeader.createCell(1).setCellValue("Mechanism");
        improvementsHeader.createCell(2).setCellValue("Result");
        improvementsHeader.createCell(3).setCellValue("Specification");

        for (int i = 0, offset = 4; i < improvementKeys.size(); i++) {
            improvementsHeader.createCell(i+offset).setCellValue(improvementKeys.get(i));
        }

        int measuresRowNum = 1;
        int validationsRowNum = 1;
        int improvementsRowNum = 1;

        for (DQReport report : reports) {
            measures.createRow(measuresRowNum++);
            validations.createRow(validationsRowNum++);
            improvements.createRow(improvementsRowNum++);

            for (Measure measure : report.getMeasures()) {
                HSSFRow row = measures.createRow(measuresRowNum);

                row.createCell(0).setCellValue(measure.getDimension());
                row.createCell(1).setCellValue(measure.getMechanism());
                row.createCell(2).setCellValue(measure.getResult().getComment());
                row.createCell(3).setCellValue(measure.getSpecification());

                int offset = 4;

                for (int i = 0; i < offset; i++) {
                    measures.autoSizeColumn(i);
                }

                Map<String, String> dataResource = measure.getDataResource();
                for (int i = 0; i < measureKeys.size(); i++) {
                    String key = measureKeys.get(i);
                    row.createCell(i+offset).setCellValue(dataResource.get(key));
                    measures.autoSizeColumn(i);
                }

                measuresRowNum++;
            }

            for (Validation validation : report.getValidations()) {
                HSSFRow row = validations.createRow(validationsRowNum);

                row.createCell(0).setCellValue(validation.getCriterion());
                row.createCell(1).setCellValue(validation.getMechanism());
                row.createCell(2).setCellValue(validation.getResult().getComment());
                row.createCell(3).setCellValue(validation.getSpecification());

                int offset = 4;
                for (int i = 0; i < offset; i++) {
                    validations.autoSizeColumn(i);
                }

                Map<String, String> dataResource = validation.getDataResource();
                for (int i = 0; i < validationKeys.size(); i++) {
                    String key = validationKeys.get(i);
                    row.createCell(i+offset).setCellValue(dataResource.get(key));
                    validations.autoSizeColumn(i);
                }

                validationsRowNum++;
            }

            for (Improvement improvement : report.getImprovements()) {
                StringBuilder resultText = new StringBuilder();
                Map<String, String> result = improvement.getResult();
                for (String key : result.keySet()) {
                    resultText.append(": " + result.get(key) + " ");
                }

                HSSFRow row = improvements.createRow(improvementsRowNum);

                row.createCell(0).setCellValue(improvement.getEnhancement());
                row.createCell(1).setCellValue(improvement.getMechanism());
                row.createCell(2).setCellValue(resultText.toString());
                row.createCell(3).setCellValue(improvement.getSpecification());

                int offset = 4;

                for (int i = 0; i < offset; i++) {
                    improvements.autoSizeColumn(i);
                }

                Map<String, String> dataResource = improvement.getDataResource();
                for (int i = 0; i < improvementKeys.size(); i++) {
                    String key = improvementKeys.get(i);
                    row.createCell(i+offset).setCellValue(dataResource.get(key));
                    improvements.autoSizeColumn(i);
                }

                improvementsRowNum++;
            }
        }

        wb.write(out);
    }
}
