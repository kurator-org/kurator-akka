package org.kurator.akka.data.DQReport;

import java.util.Map;
import java.util.HashMap;

import org.kurator.akka.data.DQReport.*;
public class Measure extends Assertion{
  private String dimension;
  public Measure (Map<String,String> dataResource, String dimension, String specification, String mechanism, String result){
    super.setDataResource(dataResource);
    this.dimension = dimension;
    super.setSpecification(specification);
    super.setMechanism(mechanism);
    super.setResult(result);
  }
  public String getDimension(){
    return this.dimension;
  }
}
