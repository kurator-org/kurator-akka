package org.kurator.akka.data.DQReport;

import java.util.Map;
import java.util.HashMap;

import org.kurator.akka.data.DQReport.*;
public class Validation extends Assertion{
  private String criterion;
  private String result;

  public Validation() {} // default constructor for Jackson

  public Validation (Map<String,String> dataResource, String criterion, String specification, String mechanism, String result){
    super.setDataResource(dataResource);
    this.criterion = criterion;
    super.setSpecification(specification);
    super.setMechanism(mechanism);
    this.result = result;
  }
  public String getCriterion(){
    return this.criterion;
  }

  public String getResult() {
    return result;
  }
}
