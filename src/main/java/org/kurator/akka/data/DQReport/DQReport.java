package org.kurator.akka.data.DQReport;

import java.util.List;
import java.util.ArrayList;

import org.kurator.akka.data.DQReport.*;
public class DQReport {
  private List<Measure> measures;
  private List<Validation> validations;
  private List<Improvement> improvements;

  public DQReport(){
    this.measures = new ArrayList<Measure>();
    this.validations = new ArrayList<Validation>();
    this.improvements = new ArrayList<Improvement>();
  }
  public void pushMeasure(Measure measure){
    this.measures.add(measure);
  }
  public void pushValidation(Validation validation){
    this.validations.add(validation);
  }
  public void pushImprovement(Improvement improvement){
    this.improvements.add(improvement);
  }
  public List<Measure> getMeasures(){
    return this.measures;
  }
  public List<Validation> getValidations(){
    return this.validations;
  }
  public List<Improvement> getImprovements(){
    return this.improvements;
  }
}
