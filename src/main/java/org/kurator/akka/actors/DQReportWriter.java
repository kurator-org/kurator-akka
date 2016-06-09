package org.kurator.akka.actors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;

import java.io.*;
import java.util.*;

import org.kurator.akka.KuratorActor;
import org.kurator.akka.data.DQReport.*;
import org.kurator.postproccess.PostProcessor;
import org.kurator.postproccess.ffdq.FullReportPostProcessor;

public class DQReportWriter extends KuratorActor {
  public boolean jsonOutput = true;
  public boolean consoleOutput = true;
  //public String filePath = "output.json";
  public String filePath;
  private FileWriter fileWriter;
  private DQReport report;
    private List<DQReport> reports;
  private StringWriter reportWriter;

    private HSSFWorkbook wb;

    private List<String> measureKeys;
    private List<String> validationKeys;
    private List<String> improvementKeys;
    private HSSFRow measuresHeader;
    private HSSFRow validationsHeader;
    private HSSFRow improvementsHeader;

    private HSSFSheet measures;
    private HSSFSheet validations;
    private HSSFSheet improvements;

    private int measuresRowNum = 1;
    private int validationsRowNum = 1;
    private int improvementsRowNum = 1;

  private boolean firstReport = false; // true if first report has been written

  @Override
  public void onStart() throws Exception {
    if(this.jsonOutput){
        reportWriter = new StringWriter();
        File file = File.createTempFile("output", ".json");

        fileWriter = new FileWriter(file, false);
        fileWriter.write("[");

        filePath = file.getAbsolutePath();

        wb = new HSSFWorkbook();
        measures = wb.createSheet("Measures");
        validations = wb.createSheet("Validations");
        improvements = wb.createSheet("Improvements");
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void onData(Object value) {
    this.report = (DQReport)value;

      List<Measure> measuresList = report.getMeasures();
      List<Validation> validationList = report.getValidations();
      List<Improvement> improvementList = report.getImprovements();

          if (measureKeys == null && report.getMeasures().size() > 0) {
              measuresHeader = measures.createRow(0);

              measureKeys = new ArrayList<String>();
              measureKeys.addAll(measuresList.get(0).getDataResource().keySet());

              for (int i = 0; i < measuresList.size(); i++) {
                  measuresHeader.createCell(i).setCellValue(measuresList.get(i).getDimension());
              }

              for (int i = 0, offset = measuresList.size(); i < measureKeys.size(); i++) {
                  measuresHeader.createCell(i+offset).setCellValue(measureKeys.get(i));
              }
          }

        if (validationKeys == null && report.getValidations().size() > 0) {
            validationsHeader = validations.createRow(0);

            validationKeys = new ArrayList<String>(); // list instead of set to preserve ordering
            validationKeys.addAll(report.getValidations().get(0).getDataResource().keySet());

            HSSFRow validationsHeader = validations.createRow(0);

            for (int i = 0; i < validationList.size(); i++) {
                validationsHeader.createCell(i).setCellValue(validationList.get(i).getCriterion());
            }

            for (int i = 0, offset = validationList.size(); i < validationKeys.size(); i++) {
                validationsHeader.createCell(i + offset).setCellValue(validationKeys.get(i));
            }
        }

      if (improvementKeys == null && report.getImprovements().size() > 0) {
          improvementsHeader = improvements.createRow(0);

          improvementKeys = new ArrayList<String>();
          improvementKeys.addAll(improvementList.get(0).getDataResource().keySet());

          for (int i = 0; i < improvementList.size(); i++) {
              improvementsHeader.createCell(i).setCellValue(improvementList.get(i).getEnhancement());
          }

          for (int i = 0, offset = improvementList.size(); i < improvementKeys.size(); i++) {
              improvementsHeader.createCell(i+offset).setCellValue(improvementKeys.get(i));
          }

      }

    if(this.consoleOutput)
        consoleDQReport();
    if(this.jsonOutput)
        jsonDQReport();

      measuresList = report.getMeasures();
      if (measuresList.size() > 0) {
          HSSFRow measuresRow = measures.createRow(measuresRowNum);

          for (int i = 0; i < measuresList.size(); i++) {
              measuresRow.createCell(i).setCellValue(measuresList.get(i).getResult().getComment());
              measures.autoSizeColumn(i);

              Map<String, String> dataResource = measuresList.get(0).getDataResource();
              for (int j = 0, offset = measuresList.size(); j < measureKeys.size(); j++) {
                  String key = measureKeys.get(j);
                  measuresRow.createCell(j + offset).setCellValue(dataResource.get(key));
                  measures.autoSizeColumn(j);
              }
          }

        measuresRowNum++;
      }

      validationList = report.getValidations();

      if (validationList.size() > 0) {
          HSSFRow validationsRow = validations.createRow(validationsRowNum);

          for (int i = 0; i < validationList.size(); i++) {
              validationsRow.createCell(i).setCellValue(validationList.get(i).getResult().getComment());
              validations.autoSizeColumn(i);

              Map<String, String> dataResource = validationList.get(0).getDataResource();
              for (int j = 0, offset = validationList.size(); j < validationKeys.size(); j++) {
                  String key = validationKeys.get(j);
                  validationsRow.createCell(j + offset).setCellValue(dataResource.get(key));
                  validations.autoSizeColumn(j);
              }
          }

          validationsRowNum++;
      }

      improvementList = report.getImprovements();

      if (improvementList.size() > 0) {
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
                  improvementsRow.createCell(j + offset).setCellValue(dataResource.get(key));
                  improvements.autoSizeColumn(j);
              }
          }

          improvementsRowNum++;
      }

        broadcast(value);
  }

    public void initHeaders() {
        measuresHeader = measures.createRow(0);

        measuresHeader.createCell(0).setCellValue("Dimension");
        measuresHeader.createCell(1).setCellValue("Mechanism");
        measuresHeader.createCell(2).setCellValue("Result");
        measuresHeader.createCell(3).setCellValue("Specification");

        validationsHeader = validations.createRow(0);

        validationsHeader.createCell(0).setCellValue("Criterion");
        validationsHeader.createCell(1).setCellValue("Mechanism");
        validationsHeader.createCell(2).setCellValue("Result");
        validationsHeader.createCell(3).setCellValue("Specification");

        improvementsHeader = improvements.createRow(0);

        improvementsHeader.createCell(0).setCellValue("Enhancement");
        improvementsHeader.createCell(1).setCellValue("Mechanism");
        improvementsHeader.createCell(2).setCellValue("Result");
        improvementsHeader.createCell(3).setCellValue("Specification");
    }

    private void writeMeasures() {
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
    }

    private void writeValidations() {
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
    }

    private void writeImprovements() {
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

  @Override
  public void onEnd() throws Exception {
      if (fileWriter != null) {
          fileWriter.write(reportWriter.toString());
          fileWriter.write("]");
          fileWriter.flush();
          fileWriter.close();

          File xlsFile = File.createTempFile("output", ".xls");
          wb.write(new FileOutputStream(xlsFile));
          wb.close();

          publishArtifact("dq_report_json", filePath);
          publishArtifact("dq_report_xls", xlsFile.getAbsolutePath());
          //publishArtifact("dq_report_xls", xlsFile.getAbsolutePath());

          System.out.print("\n DQ Report written to: "+filePath+" \n ");
      }
  }

  // TODO: Serialize to json with a framework instead of this custom serialization.
  /*
  Backed out 684209476dae00f4a119222641369efc7163d990 
  as this use of ObjectWrapper results in only the report on the last record in 
  a data set being preserved, as mapper.writeValue() is overwriting instead of appending.
  Moving to class property File file = new File(filePath) doesn't solve this. 

  public void jsonDQReport(){
    ObjectMapper mapper = new ObjectMapper();
    try {
      mapper.writeValue(new File(filePath), report);
      System.out.print("\n DQ Report wrote in: "+filePath+" \n ");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  */

    public void jsonDQReport(){
        ObjectMapper mapper = new ObjectMapper();
        try {
            if (firstReport) {
                reportWriter.write(","); // prepend a comma in the list for all reports except the first one
            } else {
                firstReport = true;
            }

            mapper.writeValue(reportWriter, report);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

  public void consoleDQReport(){
    System.out.println("\033[0;1m ================= DQ REPORT ================= \033[0;0m \n ");
    if(report!=null) {  
      System.out.println("\t ::: DATA ::: \n");
      System.out.println("\033[0;1m ID: \033[0;0m "+report.getMeasures().get(0).getDataResource().get("Id"));
      System.out.println("\033[0;1m Latitude: \033[0;0m "+report.getMeasures().get(0).getDataResource().get("decimalLatitude"));
      System.out.println("\033[0;1m Longitude: \033[0;0m"+report.getMeasures().get(0).getDataResource().get("decimalLongitude"));
      System.out.println("\033[0;1m Scientific Name: \033[0;0m"+report.getMeasures().get(0).getDataResource().get("scientificName"));
      System.out.println("\033[0;1m Country Code: \033[0;0m "+report.getMeasures().get(0).getDataResource().get("countryCode"));
      if (report.getMeasures().size()>0){
      System.out.println("\n \t ::: MEASURES ::: \n");
      for(Measure item : report.getMeasures()){
        if(item.getResult()!=null){
          System.out.println("\033[0;1m"+item.getDimension()+": "+item.getResult().getComment().toUpperCase()+" \033[0;0m");
          System.out.println("\t \033[0;1m Specification: \033[0;0m"+item.getSpecification());
          System.out.println("\t \033[0;1m Mechanism: \033[0;0m"+item.getMechanism());
          System.out.println("\n");
        }
      }
      }
      if(report.getValidations().size()>0){
        System.out.println("\t ::: VALIDATIONS ::: \n");
        for(Validation item : report.getValidations()){
          if(item.getResult()!=null){
            System.out.println("\033[0;1m"+item.getCriterion()+": "+item.getResult().getComment().toUpperCase()+" \033[0;0m");
            System.out.println("\t \033[0;1m Specification: \033[0;0m"+item.getSpecification());
            System.out.println("\t \033[0;1m Mechanism: \033[0;0m"+item.getMechanism());
            System.out.println("\n");
          }
        }
      }
      if(report.getImprovements().size()>0){
        System.out.println("\t ::: IMPROVEMENTS ::: \n");
        for(Improvement item : report.getImprovements()){
          if(item.getResult()!=null){
            System.out.println("\033[0;1m"+item.getEnhancement()+": "+item.getResult()+" \033[0;0m");
            System.out.println("\t \033[0;1m Specification: \033[0;0m"+item.getSpecification());
            System.out.println("\t \033[0;1m Mechanism: \033[0;0m"+item.getMechanism());
            System.out.println("\n");
          }
        }
      }
    }
  }
}
