package org.kurator.akka.actors;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import akka.dispatch.ExecutionContexts;
import akka.dispatch.Futures;
import akka.dispatch.OnComplete;
import akka.dispatch.OnSuccess;
import akka.dispatch.sysmsg.Failed;
import akka.util.Timeout;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
import org.kurator.akka.AkkaActor;

import org.kurator.akka.FutureActor;
import org.kurator.akka.data.DQReport.DQReport;
import org.kurator.akka.data.DQReport.GeoValidatorToDQReport;
import org.kurator.akka.messages.EndOfStream;
import scala.concurrent.Await;
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
public class RestActor extends FutureActor {
	public int numThreads = 10;

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
	private ExecutionContext ec;


	@Override
	protected void onStart() throws Exception {
		super.onStart();
		ec = ExecutionContexts.fromExecutor(Executors.newFixedThreadPool(numThreads));
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onData(Object value) {
			Map<String, String> line = (Map<String, String>) value;
			for (String map : this.paramsInputMapping.split(",")) {
				String[] keyValue = map.split(":");
				params.put(keyValue[0].trim(), keyValue[1].trim());
			}
			boolean isFirst = true;
			String p = "";
			try {
				for (String key : params.keySet()) {
					if (isFirst) {
						p += "?" + key + "=" + URLEncoder.encode(line.get(params.get(key)), "UTF-8");
						isFirst = false;
					} else {
						p += "&" + key + "=" + URLEncoder.encode(line.get(params.get(key)), "UTF-8");
					}
				}

				URL iri = new URL(this.url + p);

				final ExecutionContext ec = this.getContext().dispatcher();
				future(new DoLookup(iri, this.format, value), ec, new GeoValidatorToDQReport());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	private class DoLookup implements Callable {

		URL url;
		String format;
		Object value;

		public DoLookup(URL url, String format, Object inputValue) {
			this.url = url;
			this.format = format;
			this.value = inputValue;
		}

		@Override
		public Map call() {
			HttpURLConnection conn = null;

			try {
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod(method);
				conn.setRequestProperty("Accept", format);
				if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
					throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
				}
				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

				Map data = new HashMap();
				String output;

				while ((output = br.readLine()) != null) {
					data.put("dataResource",value);
					data.put("rawResults",output);
				}

				return data;
			} catch (IOException e) {
				throw new RuntimeException("Failed: Could not connect to " + url.toExternalForm());
			} finally {
				if (conn!=null) { conn.disconnect(); }
			}
		}
	}
}