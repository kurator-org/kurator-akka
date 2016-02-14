package org.kurator.akka.data.DQReport;

import java.util.Map;
import java.util.HashMap;

import org.kurator.akka.data.DQReport.*;
public class Improvement extends Assertion{
  private String enhancement;
  public Improvement (Map<String,String> dataResource, String enhancement, String specification, String mechanism, String result){
    super.setDataResource(dataResource);
    this.enhancement = enhancement;
    super.setSpecification(specification);
    super.setMechanism(mechanism);
    super.setResult(result);
  }
  public String getEnhancement(){
    return this.enhancement;
  }
}
