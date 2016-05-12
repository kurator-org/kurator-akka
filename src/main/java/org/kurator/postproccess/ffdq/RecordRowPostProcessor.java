package org.kurator.postproccess.ffdq;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Created by lowery on 4/7/16.
 */
public class RecordRowPostProcessor implements PostProcessor {

    @Override
    public void postprocess(InputStream in, String format, OutputStream out) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<DQReport> reports = mapper.readValue(in, new TypeReference<List<DQReport>>(){});

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet measures = wb.createSheet("Measures");
        HSSFSheet validations = wb.createSheet("Validations");
        HSSFSheet improvements = wb.createSheet("Improvements");

        List<String> measureKeys = new ArrayList<String>();

        List<Measure> measuresList = reports.get(0).getMeasures();
        measureKeys.addAll(measuresList.get(0).getDataResource().keySet());

        HSSFRow measuresHeader = measures.createRow(0);

        for (int i = 0; i < measuresList.size(); i++) {
            measuresHeader.createCell(i).setCellValue(measuresList.get(i).getDimension());
        }

        for (int i = 0, offset = measuresList.size(); i < measureKeys.size(); i++) {
            measuresHeader.createCell(i+offset).setCellValue(measureKeys.get(i));
        }

        List<String> validationKeys = new ArrayList<String>(); // list instead of set to preserve ordering

        List<Validation> validationList = reports.get(0).getValidations();
        validationKeys.addAll(reports.get(0).getValidations().get(0).getDataResource().keySet());

        HSSFRow validationsHeader = validations.createRow(0);

        for (int i = 0; i < validationList.size(); i++) {
            validationsHeader.createCell(i).setCellValue(validationList.get(i).getCriterion());
        }

        for (int i = 0, offset = validationList.size(); i < validationKeys.size(); i++) {
            validationsHeader.createCell(i+offset).setCellValue(validationKeys.get(i));
        }

        List<String> improvementKeys = new ArrayList<String>();

        List<Improvement> improvementList = reports.get(0).getImprovements();
        improvementKeys.addAll(improvementList.get(0).getDataResource().keySet());

        HSSFRow improvementsHeader = improvements.createRow(0);

        for (int i = 0; i < improvementList.size(); i++) {
            improvementsHeader.createCell(i).setCellValue(improvementList.get(i).getEnhancement());
        }

        for (int i = 0, offset = improvementList.size(); i < improvementKeys.size(); i++) {
            improvementsHeader.createCell(i+offset).setCellValue(improvementKeys.get(i));
        }


        int measuresRowNum = 1;
        int validationsRowNum = 1;
        int improvementsRowNum = 1;


        for (DQReport report : reports) {
            measuresList = report.getMeasures();
            HSSFRow measuresRow = measures.createRow(measuresRowNum);

            for (int i = 0; i < measuresList.size(); i++) {
                measuresRow.createCell(i).setCellValue(measuresList.get(i).getResult().getComment());
                measures.autoSizeColumn(i);

                Map<String, String> dataResource = measuresList.get(0).getDataResource();
                for (int j = 0, offset = measuresList.size(); j < measureKeys.size(); j++) {
                    String key = measureKeys.get(j);
                    measuresRow.createCell(j+offset).setCellValue(dataResource.get(key));
                    measures.autoSizeColumn(j);
                }
            }

            measuresRowNum++;

            validationList = report.getValidations();
            HSSFRow validationsRow = validations.createRow(validationsRowNum);

            for (int i = 0; i < validationList.size(); i++) {
                validationsRow.createCell(i).setCellValue(validationList.get(i).getResult().getComment());
                validations.autoSizeColumn(i);

                Map<String, String> dataResource = validationList.get(0).getDataResource();
                for (int j = 0, offset = validationList.size(); j < validationKeys.size(); j++) {
                    String key = validationKeys.get(j);
                    validationsRow.createCell(j+offset).setCellValue(dataResource.get(key));
                    validations.autoSizeColumn(j);
                }
            }

            validationsRowNum++;

            improvementList = report.getImprovements();
            HSSFRow improvementsRow = improvements.createRow(improvementsRowNum);

            for (int i = 0; i < improvementList.size(); i++) {
                StringBuilder sb = new StringBuilder();

                Map<String, String> result = improvementList.get(i).getResult();
                for (String key : result.keySet()) {
                    sb.append(key + ": " + result.get(key) + " ");
                }

                improvementsRow.createCell(i).setCellValue(sb.toString());
                improvements.autoSizeColumn(i);

                Map<String, String> dataResource = improvementList.get(0).getDataResource();
                for (int j = 0, offset = improvementList.size(); j < improvementKeys.size(); j++) {
                    String key = improvementKeys.get(j);
                    improvementsRow.createCell(j+offset).setCellValue(dataResource.get(key));
                    improvements.autoSizeColumn(j);
                }
            }

            improvementsRowNum++;
        }

        wb.write(out);
    }
}
