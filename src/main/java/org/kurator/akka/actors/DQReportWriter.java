package org.kurator.akka.actors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.kurator.akka.KuratorActor;
import org.kurator.akka.data.DQReport.*;
public class DQReportWriter extends KuratorActor {
  public boolean jsonOutput = true;
  public boolean consoleOutput = true;
  public String filePath = "output.json";
  private FileWriter file;
  private DQReport report;

  private boolean firstReport = false; // true if first report has been written
  @Override
  public void onStart() throws Exception {
    if(this.jsonOutput){
          if (filePath != null) {
              file = new FileWriter(filePath, false);
              file.write("[");
          }
    }
  }
  @Override
  @SuppressWarnings("unchecked")
  public void onData(Object value) {
    this.report = (DQReport)value;

    if(this.consoleOutput)
        consoleDQReport();
    if(this.jsonOutput)
        jsonDQReport();

    broadcast(value);
  }
  @Override
  public void onEnd() throws Exception {
      if (file != null) {
          file.write("]");
          file.flush();
          file.close();
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
                file.write(","); // prepend a comma in the list for all reports except the first one
            } else {
                firstReport = true;
            }

            Writer writer = new StringWriter();
            mapper.writeValue(writer, report);
            System.out.print("\n DQ Report wrote in: "+filePath+" \n ");

            file.write(writer.toString());
            //file = new FileWriter(filePath);
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
          System.out.println("\033[0;1m"+item.getDimension()+": "+item.getResult().toUpperCase()+" \033[0;0m");
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
            System.out.println("\033[0;1m"+item.getCriterion()+": "+item.getResult().toUpperCase()+" \033[0;0m");
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
