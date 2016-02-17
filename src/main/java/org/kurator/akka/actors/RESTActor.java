package org.kurator.akka.actors;

import java.util.Map;
import java.util.HashMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import org.kurator.akka.AkkaActor;
public class RESTActor extends AkkaActor {
  public String url = "";
  public String method = "GET";
  public String paramsInputMapping = "";
  public String format = "application/json";
  private Map<String, String> params = new HashMap();
  @Override
  @SuppressWarnings("unchecked")
  
  public void onData(Object value) {
    Map<String,String> line = (Map<String, String>)value;
    /*
    * paramsInputMapping is a list ("," separeted) of key-value (":" separeted)
    * key: name of the parameter in the URL
    * value: name of column in the CSV
    */
    for(String map : this.paramsInputMapping.split(",")){
      String[] keyValue = map.split(":");
      params.put(keyValue[0].trim(),keyValue[1].trim());
    }
    boolean isFirst = true;
    String p = "";
    HttpURLConnection conn = null;
    try {
      for ( String key : params.keySet() ) {
        if(isFirst){
          p += "?"+key+"="+line.get(params.get(key)).replace(" ","%20");
          isFirst = false;
        }else{
          p += "&"+key+"="+line.get(params.get(key)).replace(" ","%20");
        }
      }
      URL url = new URL(this.url+p);
      conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod(method);
      conn.setRequestProperty("Accept", this.format);
      if (conn.getResponseCode() != 200) {
        throw new RuntimeException("Failed : HTTP error code : "
        + conn.getResponseCode());
      }
      BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
      String output;
      while ((output = br.readLine()) != null) {
          Map data = new HashMap();
          data.put("dataResource",value);
          data.put("rawResults",output);
          broadcast(data);
      }
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    try { 
      if (conn!=null) { 
    	  conn.disconnect();
      }
    } catch (Exception e) { 
    	// exception thrown trying to disconnect
    }
  }
}
