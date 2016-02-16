package org.kurator.akka.actors;

import java.util.Map;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import org.kurator.akka.AkkaActor;
import org.kurator.akka.data.DQReport.*;
public class GeoValidatorToDQReport extends AkkaActor {
  @Override
  @SuppressWarnings("unchecked")
  public void onData(Object value) {
    HashMap<String, Object> data = (HashMap<String, Object>)value;
    Map<String, String> dataResource = (Map<String, String>)data.get("dataResource");
    JSONParser parser = new JSONParser();
    try{
      Object obj = parser.parse((String)data.get("rawResults"));
      JSONObject r = (JSONObject)obj;
      JSONObject rawResults = (JSONObject)r.get("flags");
      String result;

      // TODO: Improve the Specifications descriptions. Toward a more precise and accurate description.
      // TODO: Contact Otegui and Guralnick to contribuite with this task?

      // Formating the raw result
      result = rawResults.get("distanceToCountryInKm")!=null
                  ? rawResults.get("distanceToCountryInKm").toString()
                  : rawResults.get("coordinatesInsideCountry")!=null
                    ? (boolean)rawResults.get("coordinatesInsideCountry")
                      ? "0"
                      : null
                    : null;
      // MEASURE format:  (DataResource, Dimension, Specification, Mechanism, Result)
      Measure coordinatesConsistencyCountry = new Measure(
        dataResource,
        "Coordinates distance from the country",
        "Calculate the distance to the closest point of the country boundaries, in Km, using a function in CartoDB [ref]",
        "Kurator: VertNet - Geospatial Quality API",result);

      result = rawResults.get("distanceToRangeMapInKm")!=null
                  ? rawResults.get("distanceToRangeMapInKm").toString()
                  : rawResults.get("coordinatesInsideRangeMap")!=null
                    ? (boolean)rawResults.get("coordinatesInsideRangeMap")
                      ? "0"
                      : null
                    : null;
      Measure coordinatesConsistencyIUCN = new Measure(
        dataResource,
        "Coordinates distance from the IUCN range map for the species",
        "Calculate the distance to the closest point of the species range map, in Km, using a function in CartoDB [ref]",
        "Kurator: VertNet - Geospatial Quality API",result);

      result = (boolean)rawResults.get("hasCoordinates")
                  ? "Complete"
                  : "Not Complete";
      Measure coordinatesCompleteness = new Measure(
        dataResource,
        "Coordinates completeness",
        "Check if both latitude and longitude was supplied [ref]",
        "Kurator: VertNet - Geospatial Quality API", result);


      result = (boolean)rawResults.get("hasCountry")
                  ? "Complete"
                  : "Not Complete";
      Measure countryCompleteness = new Measure(
        dataResource,
        "Country completeness",
        "Check if country code was supplied [ref]",
        "Kurator: VertNet - Geospatial Quality API",result);

      result = (boolean)rawResults.get("hasScientificName")
                  ? "Complete"
                  : "Not Complete";
      Measure scientificNameCompleteness = new Measure(
        dataResource,
        "Scientifc Name completeness",
        "Check if scientific name was supplied [ref]",
        "Kurator: VertNet - Geospatial Quality API",result);

      // VALIDATION format:  (DataResource, Criterion, Specification, Mechanism, Result)
      result = (boolean)rawResults.get("hasCoordinates")
                  ? "Compliant"
                  : "Not Compliant";
      Validation coordinatesBeComplete = new Validation(
        dataResource,
        "Coordinates must be complete",
        "Check if both latitude and longitude was supplied [ref]",
        "Kurator: VertNet - Geospatial Quality API",result);

      result = (boolean)rawResults.get("hasCountry")
                  ? "Compliant"
                  : "Not Compliant";
      Validation countryBeComplete = new Validation(
        dataResource,
        "Country must be complete",
        "Check if country code was supplied [ref]",
        "Kurator: VertNet - Geospatial Quality API",result);

      result = (boolean)rawResults.get("hasScientificName")
                  ? "Compliant"
                  : "Not Compliant";
      Validation scientificNameBeComplete = new Validation(
        dataResource,
        "Scientifc Name must be complete",
        "Check if scientific name was supplied [ref]",
        "Kurator: VertNet - Geospatial Quality API",result);

      result = rawResults.get("validCoordinates")!=null
                  ? (boolean)rawResults.get("validCoordinates")
                      ? "Compliant"
                      : "Not Compliant"
                  : null;
      Validation coordinatesBeInRange = new Validation(
        dataResource,
        "Coordinates must be in the range",
        "Check if the supplied values conform to the natural limits of coordinates [ref]",
        "Kurator: VertNet - Geospatial Quality API",result);

      result = rawResults.get("validCountry")!=null
                  ? (boolean)rawResults.get("validCountry")
                      ? "Compliant"
                      : "Not Compliant"
                  : null;
      Validation countryBeValid = new Validation(
        dataResource,
        "Country must be valid",
        "Check if the supplied value corresponds to an existing 2-character code for a country [ref]",
        "Kurator: VertNet - Geospatial Quality API",result);

      result = rawResults.get("highPrecisionCoordinates")!=null
                  ? (boolean)rawResults.get("highPrecisionCoordinates")
                      ? "Compliant"
                      : "Not Compliant"
                  : null;
      Validation coordinatesBePrecise = new Validation(
        dataResource,
        "Coordinates numerical precision must be higher then 3",
        "Check if coordinates have at least 3 decimal figures [ref]",
        "Kurator: VertNet - Geospatial Quality API",result);

      result = rawResults.get("nonZeroCoordinates")!=null
                  ? (boolean)rawResults.get("nonZeroCoordinates")
                      ? "Compliant"
                      : "Not Compliant"
                  : null;
      Validation coordinatesBeNonZero = new Validation(
        dataResource,
        "Coordinates must be different of 0",
        "Check if both latitude and longitude are equal 0 [ref]",
        "Kurator: VertNet - Geospatial Quality API",result);

      result = rawResults.get("coordinatesInsideCountry")!=null
                  ? (boolean)rawResults.get("coordinatesInsideCountry")
                      ? "Compliant"
                      : "Not Compliant"
                  : null;
      Validation coordinatesBeInsideCountry = new Validation(
        dataResource,
        "Coordinates must fall inside the country",
        "Check if coordinates fall inside the country [ref]",
        "Kurator: VertNet - Geospatial Quality API",result);

      result = rawResults.get("coordinatesInsideRangeMap")!=null
                  ? (boolean)rawResults.get("coordinatesInsideRangeMap")
                      ? "Compliant"
                      : "Not Compliant"
                  : null;
      Validation coordinatesBeInsideRangeIUCN = new Validation(
        dataResource,
        "Coordinates must fall inside the IUCN range map for the species",
        "Check if coordinates fall inside the IUCN range map for the scientific name [ref]",
        "Kurator: VertNet - Geospatial Quality API",result);

      // IMPROVEMENT format:  (DataResource, Enhancement, Specification, Mechanism, Result)
      result = rawResults.get("transposedCoordinates")!=null
                  ? (boolean)rawResults.get("transposedCoordinates")
                      ? "decimalLatitude: "+dataResource.get("decimalLongitude")+", decimalLongitude: "+dataResource.get("decimalLatitude")
                      : "No recommendation"
                  : null;
      Improvement coordinatesTransposition = new Improvement(
        dataResource,
        "Recommendation of transposition of coordinates",
        "Recommend swapping latitude by longitude when this swapping results in a coordinates that fall inside the associated country [ref]",
        "Kurator: VertNet - Geospatial Quality API",result);

      result = rawResults.get("negatedLatitude")!=null
                  ? (boolean)rawResults.get("negatedLatitude")
                      ? dataResource.get("decimalLatitude").contains("-")
                        ? "decimalLatitude: "+dataResource.get("decimalLatitude").split("-")[1]
                        : "decimalLatitude: -"+dataResource.get("decimalLatitude")
                      : "No recommendation"
                  : null;
      Improvement latitudeInversion = new Improvement(
        dataResource,
        "Recommendation of invertion the sign of latitude",
        "Recommend an inverted sign for the latitude when this invertion results in a coordinates that fall inside the associated country [ref]",
        "Kurator: VertNet - Geospatial Quality API",result);

      result = rawResults.get("negatedLongitude")!=null
                  ? (boolean)rawResults.get("negatedLongitude")
                    ? dataResource.get("decimalLongitude").contains("-")
                      ? "decimalLongitude: "+dataResource.get("decimalLongitude").split("-")[1]
                      : "decimalLongitude: -"+dataResource.get("decimalLongitude")
                    : "No recommendation"
                  : null;
      Improvement longitudeLongitude = new Improvement(
        dataResource,
        "Recommendation of invertion the sign of longitude",
        "Recommend an inverted sign for the longitude when this invertion results in a coordinates that fall inside the associated country [ref]",
        "Kurator: VertNet - Geospatial Quality API",result);

      DQReport report = new DQReport();
      // Add Measures to DQReport
      report.pushMeasure(coordinatesConsistencyCountry);
      report.pushMeasure(coordinatesConsistencyIUCN);
      report.pushMeasure(coordinatesCompleteness);
      report.pushMeasure(countryCompleteness);
      report.pushMeasure(scientificNameCompleteness);
      // Add Validations to DQReport
      report.pushValidation(coordinatesBeComplete);
      report.pushValidation(countryBeComplete);
      report.pushValidation(scientificNameBeComplete);
      report.pushValidation(coordinatesBeInRange);
      report.pushValidation(countryBeValid);
      report.pushValidation(coordinatesBePrecise);
      report.pushValidation(coordinatesBeNonZero);
      report.pushValidation(coordinatesBeInsideCountry);
      report.pushValidation(coordinatesBeInsideRangeIUCN);
      // Add Improvements to DQReport
      report.pushImprovement(coordinatesTransposition);
      report.pushImprovement(latitudeInversion);
      report.pushImprovement(longitudeLongitude);

      broadcast(report);
    }catch(ParseException pe){
      System.out.println("position: " + pe.getPosition());
      System.out.println(pe);
    }
  }
}
