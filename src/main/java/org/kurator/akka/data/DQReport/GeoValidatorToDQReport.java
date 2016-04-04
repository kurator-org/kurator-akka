package org.kurator.akka.data.DQReport;

import java.util.Map;
import java.util.HashMap;

import akka.dispatch.Mapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import org.kurator.akka.data.DQReport.*;
public class GeoValidatorToDQReport extends Mapper<Map<String, Object>, DQReport> {
  @Override
  @SuppressWarnings("unchecked")
  public DQReport apply(Map<String, Object> data) {
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
              "Coordinates distance outside the country",
              "Calculate the distance to the closest point of the country boundaries, in Km, using a function in CartoDB  to the supplied coordinate, zero if inside country. [ref]",
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
              "Calculate the distance to the closest point of the species range map, in Km, using a function in CartoDB to the supplied coordinate, zero if inside range. [ref]",
              "Kurator: VertNet - Geospatial Quality API",result);

      result = (boolean)rawResults.get("hasCoordinates")
              ? "Complete"
              : "Not Complete";
      Measure coordinatesCompleteness = new Measure(
              dataResource,
              "Latitude/Longitude completeness",
              "Check if both latitude and longitude were supplied [ref]",
              "Kurator: VertNet - Geospatial Quality API", result);


      result = (boolean)rawResults.get("hasCountry")
              ? "Complete"
              : "Not Complete";
      Measure countryCompleteness = new Measure(
              dataResource,
              "Country code completeness",
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
      Validation coordinatesAreComplete = new Validation(
              dataResource,
              "Coordinates must be complete",
              "Check if both latitude and longitude was supplied [ref]",
              "Kurator: VertNet - Geospatial Quality API",result);

      result = (boolean)rawResults.get("hasCountry")
              ? "Compliant"
              : "Not Compliant";
      Validation countryAreComplete = new Validation(
              dataResource,
              "Country must be complete",
              "Check if country code was supplied [ref]",
              "Kurator: VertNet - Geospatial Quality API",result);

      result = (boolean)rawResults.get("hasScientificName")
              ? "Compliant"
              : "Not Compliant";
      Validation scientificNameAreComplete = new Validation(
              dataResource,
              "Scientifc Name must be complete",
              "Check if scientific name was supplied [ref]",
              "Kurator: VertNet - Geospatial Quality API",result);

      result = rawResults.get("validCoordinates")!=null
              ? (boolean)rawResults.get("validCoordinates")
              ? "Compliant"
              : "Not Compliant"
              : null;
      Validation coordinatesAreInRange = new Validation(
              dataResource,
              "Latitude and Longitude must be in valid range",
              "Check if the supplied values conform to the natural limits of coordinates (Lat +/-90, Long +/-180) [ref]",
              "Kurator: VertNet - Geospatial Quality API",result);

      result = rawResults.get("validCountry")!=null
              ? (boolean)rawResults.get("validCountry")
              ? "Compliant"
              : "Not Compliant"
              : null;
      Validation countryAreValid = new Validation(
              dataResource,
              "Country code must be valid",
              "Check if the supplied value corresponds to an existing 2-character code for a country [ref]",
              "Kurator: VertNet - Geospatial Quality API",result);

      result = rawResults.get("highPrecisionCoordinates")!=null
              ? (boolean)rawResults.get("highPrecisionCoordinates")
              ? "Compliant"
              : "Not Compliant"
              : null;
      Validation coordinatesArePrecise = new Validation(
              dataResource,
              "Coordinates numerical precision must be higher then 3",
              "Check if coordinates have at least 3 decimal places [ref]",
              "Kurator: VertNet - Geospatial Quality API",result);

      result = rawResults.get("nonZeroCoordinates")!=null
              ? (boolean)rawResults.get("nonZeroCoordinates")
              ? "Compliant"
              : "Not Compliant"
              : null;
      Validation coordinatesAreNonZero = new Validation(
              dataResource,
              "Coordinates must be different from 0",
              "Check if both latitude and longitude are equal 0 [ref]",
              "Kurator: VertNet - Geospatial Quality API",result);

      result = rawResults.get("coordinatesInsideCountry")!=null
              ? (boolean)rawResults.get("coordinatesInsideCountry")
              ? "Compliant"
              : "Not Compliant"
              : null;
      Validation coordinatesAreInsideCountry = new Validation(
              dataResource,
              "Coordinates must fall inside the country",
              "Check if coordinates fall inside the country [ref]",
              "Kurator: VertNet - Geospatial Quality API",result);

      result = rawResults.get("coordinatesInsideRangeMap")!=null
              ? (boolean)rawResults.get("coordinatesInsideRangeMap")
              ? "Compliant"
              : "Not Compliant"
              : null;
      Validation coordinatesAreInsideRangeIUCN = new Validation(
              dataResource,
              "Coordinates must fall inside the IUCN range map for the species",
              "Check if coordinates fall inside the IUCN range map for the scientific name [ref]",
              "Kurator: VertNet - Geospatial Quality API",result);

      // IMPROVEMENT format:  (DataResource, Enhancement, Specification, Mechanism, Result)
      
      // Check the Report only a single transformation, multiple assertions on latitude and longitude are 
      // unclear, and impose an ordering dependency for interpretation.  
      boolean transpose = false;
      boolean latInvertSign = false;
      boolean longInvertSign = false;
      result = null;
      String transformation = null;
      Improvement coordinatesTransposition = null;
      if (rawResults.get("transposedCoordinates")!=null && (boolean)rawResults.get("transposedCoordinates")) { 
          transpose = true;
      }
      if (rawResults.get("negatedLatitude")!=null && (boolean)rawResults.get("negatedLatitude")) { 
          latInvertSign = true;
      } 
      if (rawResults.get("negatedLongitude")!=null && (boolean)rawResults.get("negatedLongitude")) { 
          longInvertSign = true;
      }
      if (transpose && !latInvertSign && !longInvertSign) { 
          result = "decimalLatitude: " + dataResource.get("decimalLongitude") + "|decimalLongitude: " + dataResource.get("decimalLatitude");
          transformation = "Propose swapping latitude and longitude, as this swap results in a coordinate that falls inside the associated country [ref]";
      } else if(transpose && latInvertSign && !longInvertSign) { 
          result = "decimalLatitude: -" + dataResource.get("decimalLongitude").trim() + "|decimalLongitude: " + dataResource.get("decimalLatitude");
          transformation = "Propose swapping latitude and longitude, and changing the sign of latitude, as this transformation results in a coordinate that falls inside the associated country [ref]";
      } else if(transpose && !latInvertSign && longInvertSign) {
          result = "decimalLatitude: " + dataResource.get("decimalLongitude").trim() + "|decimalLongitude: -" + dataResource.get("decimalLatitude").trim();
          transformation = "Propose swapping latitude and longitude, and changing the sign of longitude, as this transformation results in a coordinate that falls inside the associated country [ref]";
      } else if(transpose && latInvertSign && longInvertSign) {
          result = "decimalLatitude: -" + dataResource.get("decimalLongitude").trim() + "|decimalLongitude: -" + dataResource.get("decimalLatitude").trim();
          transformation = "Propose swapping and changing the sign of latitude and longitude, as this transformation results in a coordinate that falls inside the associated country [ref]";
      } else if(!transpose && latInvertSign && !longInvertSign) { 
          result = "decimalLatitude: -" + dataResource.get("decimalLatitude").trim() + "|decimalLongitude: " + dataResource.get("decimalLongitude").trim();
          transformation = "Propose changing the sign of latitude, as this transformation results in a coordinate that falls inside the associated country [ref]";
      } else if(!transpose && !latInvertSign && longInvertSign) {
          result = "decimalLatitude: " + dataResource.get("decimalLatitude").trim() + "|decimalLongitude: -" + dataResource.get("decimalLongitude").trim();
          transformation = "Propose changing the sign of longitude, as this transformation results in a coordinate that falls inside the associated country [ref]";
      } else if(!transpose && latInvertSign && longInvertSign) {
          result = "decimalLatitude: -" + dataResource.get("decimalLatitude").trim() + "|decimalLongitude: -" + dataResource.get("decimalLongitude").trim();
          transformation = "Propose changing the sign of latitude and longitude, as this transformation results in a coordinate that falls inside the associated country [ref]";
      } 
      if (result!=null) { 
         coordinatesTransposition = new Improvement(
              dataResource,
              "Recommendation to transform decimal latitude and or decimal longitude",
              transformation,
              "Kurator: VertNet - Geospatial Quality API",result);
      }

      DQReport report = new DQReport();
      // Add Measures to DQReport
      report.pushMeasure(coordinatesConsistencyCountry);
      report.pushMeasure(coordinatesConsistencyIUCN);
      report.pushMeasure(coordinatesCompleteness);
      report.pushMeasure(countryCompleteness);
      report.pushMeasure(scientificNameCompleteness);
      // Add Validations to DQReport
      report.pushValidation(coordinatesAreComplete);
      report.pushValidation(countryAreComplete);
      report.pushValidation(scientificNameAreComplete);
      report.pushValidation(coordinatesAreInRange);
      report.pushValidation(countryAreValid);
      report.pushValidation(coordinatesArePrecise);
      report.pushValidation(coordinatesAreNonZero);
      report.pushValidation(coordinatesAreInsideCountry);
      report.pushValidation(coordinatesAreInsideRangeIUCN);
      // Add Improvements to DQReport, if any
      if (coordinatesTransposition!=null) { 
          report.pushImprovement(coordinatesTransposition);
      }

      return report;
    }catch(ParseException pe){
      System.out.println("position: " + pe.getPosition());
      System.out.println(pe);
      throw new RuntimeException(pe);
    }
  }
}
