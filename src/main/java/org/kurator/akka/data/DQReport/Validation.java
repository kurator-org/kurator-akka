package org.kurator.akka.data.DQReport;

import java.util.Map;
import java.util.HashMap;

import org.kurator.akka.data.DQReport.*;
public class Validation extends Assertion{
  private String criterion;
  public Validation (Map<String,String> dataResource, String criterion, String specification, String mechanism, String result){
    super.setDataResource(dataResource);
    this.criterion = criterion;
    super.setSpecification(specification);
    super.setMechanism(mechanism);
    super.setResult(result);
  }
  public String getCriterion(){
    return this.criterion;
  }
}
