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

      Result<MeasurementState> measureResult;

      // Formating the raw result
      if (rawResults.get("distanceToCountryInKm")!=null) {
        measureResult = new Result<>(rawResults.get("distanceToCountryInKm").toString(), MeasurementState.COMPLETE);
      } else if (rawResults.get("coordinatesInsideCountry")!=null) {
        if ((boolean)rawResults.get("coordinatesInsideCountry")) {
          measureResult = new Result<>("0", MeasurementState.COMPLETE);
        } else {
          measureResult = new Result<>("Internal prerequisites not met.", MeasurementState.NOT_COMPLETE);
        }
      } else {
        measureResult = new Result<>("Internal prerequisites not met.", MeasurementState.NOT_COMPLETE);
      }

      // MEASURE format:  (DataResource, Dimension, Specification, Mechanism, Result)
      Measure coordinatesConsistencyCountry = new Measure(
              dataResource,
              "Coordinates distance outside the country",
              "Calculate the distance to the closest point of the country boundaries, in Km, using a function in CartoDB  to the supplied coordinate, zero if inside country. [ref]",
              "Kurator: VertNet - Geospatial Quality API",measureResult);

      if (rawResults.get("distanceToRangeMapInKm")!=null) {
        measureResult = new Result<>(rawResults.get("distanceToRangeMapInKm").toString(), MeasurementState.COMPLETE);
      } else if (rawResults.get("coordinatesInsideRangeMap")!=null) {
        if ((boolean)rawResults.get("coordinatesInsideRangeMap")) {
          measureResult = new Result<>("0", MeasurementState.COMPLETE);
        } else {
          measureResult = new Result<>("Internal prerequisites not met.", MeasurementState.NOT_COMPLETE);
        }
      } else {
      measureResult = new Result<>("Internal prerequisites not met.", MeasurementState.NOT_COMPLETE);
     }

      Measure coordinatesConsistencyIUCN = new Measure(
              dataResource,
              "Coordinates distance from the IUCN range map for the species",
              "Calculate the distance to the closest point of the species range map, in Km, using a function in CartoDB to the supplied coordinate, zero if inside range. [ref]",
              "Kurator: VertNet - Geospatial Quality API",measureResult);

      if ((boolean)rawResults.get("hasCoordinates")) {
        measureResult = new Result<>("Complete", MeasurementState.COMPLETE);
      } else {
        measureResult = new Result<>("Not Complete", MeasurementState.NOT_COMPLETE);
      }

      Measure coordinatesCompleteness = new Measure(
              dataResource,
              "Latitude/Longitude completeness",
              "Check if both latitude and longitude were supplied [ref]",
              "Kurator: VertNet - Geospatial Quality API", measureResult);


      if ((boolean)rawResults.get("hasCountry")) {
        measureResult = new Result<>("Complete", MeasurementState.COMPLETE);
      } else {
        measureResult = new Result<>("Not Complete", MeasurementState.NOT_COMPLETE);
      }

      Measure countryCompleteness = new Measure(
              dataResource,
              "Country code completeness",
              "Check if country code was supplied [ref]",
              "Kurator: VertNet - Geospatial Quality API",measureResult);

      if ((boolean)rawResults.get("hasScientificName")) {
        measureResult = new Result<>("Complete", MeasurementState.COMPLETE);
      } else {
        measureResult = new Result<>("Not Complete", MeasurementState.NOT_COMPLETE);
      }

      Measure scientificNameCompleteness = new Measure(
              dataResource,
              "Scientifc Name completeness",
              "Check if scientific name was supplied [ref]",
              "Kurator: VertNet - Geospatial Quality API",measureResult);

      Result<ValidationState> validationResult;
      // VALIDATION format:  (DataResource, Criterion, Specification, Mechanism, Result)
      if ((boolean)rawResults.get("hasCoordinates")) {
        validationResult = new Result<>("Compliant", ValidationState.COMPLIANT);
      } else {
        validationResult = new Result<>("Not Compliant", ValidationState.NOT_COMPLIANT);
      }

      Validation coordinatesAreComplete = new Validation(
              dataResource,
              "Coordinates must be complete",
              "Check if both latitude and longitude was supplied [ref]",
              "Kurator: VertNet - Geospatial Quality API",validationResult);

      if ((boolean)rawResults.get("hasCountry")) {
        validationResult = new Result<>("Compliant", ValidationState.COMPLIANT);
      } else {
        validationResult = new Result<>("Not Compliant", ValidationState.NOT_COMPLIANT);
      }

      Validation countryAreComplete = new Validation(
              dataResource,
              "Country must be complete",
              "Check if country code was supplied [ref]",
              "Kurator: VertNet - Geospatial Quality API",validationResult);

      if ((boolean)rawResults.get("hasScientificName")) {
        validationResult = new Result<>("Compliant", ValidationState.COMPLIANT);
      } else {
        validationResult = new Result<>("Not Compliant", ValidationState.NOT_COMPLIANT);
      }

      Validation scientificNameAreComplete = new Validation(
              dataResource,
              "Scientifc Name must be complete",
              "Check if scientific name was supplied [ref]",
              "Kurator: VertNet - Geospatial Quality API",validationResult);

      if (rawResults.get("validCoordinates")!=null) {
        if ((boolean)rawResults.get("validCoordinates")) {
          validationResult = new Result<>("Compliant", ValidationState.COMPLIANT);
        } else {
          validationResult = new Result<>("Not Compliant", ValidationState.NOT_COMPLIANT);
        }
      } else {
        validationResult = new Result<>("Internal prerequisites not met.", ValidationState.UNABLE_TO_VALIDATE);
      }

      Validation coordinatesAreInRange = new Validation(
              dataResource,
              "Latitude and Longitude must be in valid range",
              "Check if the supplied values conform to the natural limits of coordinates (Lat +/-90, Long +/-180) [ref]",
              "Kurator: VertNet - Geospatial Quality API",validationResult);

      if (rawResults.get("validCountry")!=null) {
        if ((boolean)rawResults.get("validCountry")) {
          validationResult = new Result<>("Compliant", ValidationState.COMPLIANT);
        } else {
          validationResult = new Result<>("Not Compliant", ValidationState.NOT_COMPLIANT);
        }
      } else {
        validationResult = new Result<>("Internal prerequisites not met.", ValidationState.UNABLE_TO_VALIDATE);
      }

      Validation countryAreValid = new Validation(
              dataResource,
              "Country code must be valid",
              "Check if the supplied value corresponds to an existing 2-character code for a country [ref]",
              "Kurator: VertNet - Geospatial Quality API",validationResult);

      if (rawResults.get("highPrecisionCoordinates")!=null) {
        if ((boolean)rawResults.get("highPrecisionCoordinates")) {
          validationResult = new Result<>("Compliant", ValidationState.COMPLIANT);
        } else {
          validationResult = new Result<>("Not Compliant", ValidationState.NOT_COMPLIANT);
        }
      } else {
        validationResult = new Result<>("Internal prerequisites not met.", ValidationState.UNABLE_TO_VALIDATE);
      }

      Validation coordinatesArePrecise = new Validation(
              dataResource,
              "Coordinates numerical precision must be higher then 3",
              "Check if coordinates have at least 3 decimal places [ref]",
              "Kurator: VertNet - Geospatial Quality API",validationResult);

      if (rawResults.get("nonZeroCoordinates")!=null) {
        if ((boolean)rawResults.get("nonZeroCoordinates")) {
          validationResult = new Result<>("Compliant", ValidationState.COMPLIANT);
        } else {
          validationResult = new Result<>("Not Compliant", ValidationState.NOT_COMPLIANT);
        }
      } else {
        validationResult = new Result<>("Internal prerequisites not met.", ValidationState.UNABLE_TO_VALIDATE);
      }

      Validation coordinatesAreNonZero = new Validation(
              dataResource,
              "Coordinates must be different from 0",
              "Check if both latitude and longitude are equal 0 [ref]",
              "Kurator: VertNet - Geospatial Quality API",validationResult);

      if (rawResults.get("coordinatesInsideCountry")!=null) {
        if ((boolean)rawResults.get("coordinatesInsideCountry")) {
          validationResult = new Result<>("Compliant", ValidationState.COMPLIANT);
        } else {
          validationResult = new Result<>("Not Compliant", ValidationState.NOT_COMPLIANT);
        }
      } else {
        validationResult = new Result<>("Internal prerequisites not met.", ValidationState.UNABLE_TO_VALIDATE);
      }

      Validation coordinatesAreInsideCountry = new Validation(
              dataResource,
              "Coordinates must fall inside the country",
              "Check if coordinates fall inside the country [ref]",
              "Kurator: VertNet - Geospatial Quality API",validationResult);

      if (rawResults.get("coordinatesInsideRangeMap")!=null) {
        if ((boolean)rawResults.get("coordinatesInsideRangeMap")) {
          validationResult = new Result<>("Compliant", ValidationState.COMPLIANT);
        } else {
          validationResult = new Result<>("Not Compliant", ValidationState.NOT_COMPLIANT);
        }
      } else {
        validationResult = new Result<>("Internal prerequisites not met.", ValidationState.UNABLE_TO_VALIDATE);
      }

      Validation coordinatesAreInsideRangeIUCN = new Validation(
              dataResource,
              "Coordinates must fall inside the IUCN range map for the species",
              "Check if coordinates fall inside the IUCN range map for the scientific name [ref]",
              "Kurator: VertNet - Geospatial Quality API",validationResult);

      // IMPROVEMENT format:  (DataResource, Enhancement, Specification, Mechanism, Result)
      
      // Check the Report only a single transformation, multiple assertions on latitude and longitude are 
      // unclear, and impose an ordering dependency for interpretation.  
      boolean transpose = false;
      boolean latInvertSign = false;
      boolean longInvertSign = false;

      Map<String, String> resultMap = new HashMap<String, String>();

      String transformation = null;

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
        resultMap.put("decimalLatitude", dataResource.get("decimalLatitude"));
        resultMap.put("decimalLongitude", dataResource.get("decimalLongitude"));
          transformation = "Propose swapping latitude and longitude, as this swap results in a coordinate that falls inside the associated country [ref]";
      } else if(transpose && latInvertSign && !longInvertSign) {
        resultMap.put("decimalLatitude", dataResource.get("decimalLatitude"));
        resultMap.put("decimalLongitude", dataResource.get("decimalLongitude"));
          transformation = "Propose swapping latitude and longitude, and changing the sign of latitude, as this transformation results in a coordinate that falls inside the associated country [ref]";
      } else if(transpose && !latInvertSign && longInvertSign) {
        resultMap.put("decimalLatitude", dataResource.get("decimalLatitude"));
        resultMap.put("decimalLongitude", dataResource.get("decimalLongitude"));
          transformation = "Propose swapping latitude and longitude, and changing the sign of longitude, as this transformation results in a coordinate that falls inside the associated country [ref]";
      } else if(transpose && latInvertSign && longInvertSign) {
        resultMap.put("decimalLatitude", dataResource.get("decimalLatitude"));
        resultMap.put("decimalLongitude", dataResource.get("decimalLongitude"));
          transformation = "Propose swapping and changing the sign of latitude and longitude, as this transformation results in a coordinate that falls inside the associated country [ref]";
      } else if(!transpose && latInvertSign && !longInvertSign) {
        resultMap.put("decimalLatitude", dataResource.get("decimalLatitude"));
        resultMap.put("decimalLongitude", dataResource.get("decimalLongitude"));
          transformation = "Propose changing the sign of latitude, as this transformation results in a coordinate that falls inside the associated country [ref]";
      } else if(!transpose && !latInvertSign && longInvertSign) {
        resultMap.put("decimalLatitude", dataResource.get("decimalLatitude"));
        resultMap.put("decimalLongitude", dataResource.get("decimalLongitude"));
          transformation = "Propose changing the sign of longitude, as this transformation results in a coordinate that falls inside the associated country [ref]";
      } else if(!transpose && latInvertSign && longInvertSign) {
        resultMap.put("decimalLatitude", dataResource.get("decimalLatitude"));
        resultMap.put("decimalLongitude", dataResource.get("decimalLongitude"));
          transformation = "Propose changing the sign of latitude and longitude, as this transformation results in a coordinate that falls inside the associated country [ref]";
      }
        Improvement coordinatesTransposition = new Improvement(
              dataResource,
              "Recommendation to transform decimal latitude and or decimal longitude",
              transformation,
              "Kurator: VertNet - Geospatial Quality API",resultMap);

        // Associate the improvement with the validation
        if (transformation != null) {
            coordinatesAreInsideCountry.setImprovement(coordinatesTransposition);
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
          report.pushImprovement(coordinatesTransposition);

      return report;
    }catch(ParseException pe){
      System.out.println("position: " + pe.getPosition());
      System.out.println(pe);
      throw new RuntimeException(pe);
    }
  }
}
