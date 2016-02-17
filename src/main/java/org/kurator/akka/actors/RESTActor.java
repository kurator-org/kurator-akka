package org.kurator.akka.actors;

import static akka.dispatch.Futures.future;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Callable;
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

import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

/**
 * General purpose actor for invoking a REST service and putting the response into the 
 * data stream for processing by another actor.  By default, uses GET and requests 
 * application/json as the response format.
 * 
 * @author Allan Viegas
 *
 */
public class RESTActor extends AkkaActor {
  public String url = "";
  public String method = "GET";
  /**
   * paramsInputMapping is a list ("," separated) of key-value (":" separated)
   * key: name of the parameter in the URL
   * value: name of column in the CSV
   */
  public String paramsInputMapping = "";
  public String format = "application/json";
  private Map<String, String> params = new HashMap();
  @Override
  @SuppressWarnings("unchecked")
  
  public void onData(Object value) {
    Map<String,String> line = (Map<String, String>)value;
    for(String map : this.paramsInputMapping.split(",")){
      String[] keyValue = map.split(":");
      params.put(keyValue[0].trim(),keyValue[1].trim());
    }
    boolean isFirst = true;
    String p = "";
    try {
      for ( String key : params.keySet() ) {
        if(isFirst){
          p += "?"+key+"="+line.get(params.get(key)).replace(" ","%20");
          isFirst = false;
        }else{
          p += "&"+key+"="+line.get(params.get(key)).replace(" ","%20");
        }
      }
      URL iri = new URL(this.url+p);
	  final ExecutionContext ec = this.getContext().dispatcher();
      Future<String> f = future(new DoLookup<String>(iri, this.format, value), ec);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
  }
 
  private class DoLookup<T> implements Callable {
	    
	    URL url;
	    String format;
	    Object value;
	  
	    public DoLookup(URL url, String format, Object inputValue) {
	    	this.url = url;
	    	this.format = format;
	    	this.value = inputValue;
	    }
	  
		@Override
		public String call() throws Exception {
			HttpURLConnection conn = null;
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(method);
			conn.setRequestProperty("Accept", format);
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			String output;
			while ((output = br.readLine()) != null) {
				Map data = new HashMap();
				data.put("dataResource",value);
				data.put("rawResults",output);
				broadcast(data);
			}
		    try { 
		        if (conn!=null) { 
		      	  conn.disconnect();
		        }
		    } catch (Exception e) { 
		      	// exception thrown trying to disconnect, consume.
		    }
		    return output;
		}
  }
  
}