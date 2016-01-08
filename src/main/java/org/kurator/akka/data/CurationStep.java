/** 
 * CurationStep.java 
 * 
 * Copyright 2013 President and Fellows of Harvard College
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kurator.akka.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A report from a data validation method concerning a data record.  
 * 
 * @author mole
 *
 */
public class CurationStep {

	/**
	 * Specification of the method by which the input elements are validated.
	 */
	private String validationMethodSpecification;
	/**
	 * Key value pairs representing (a flat list of) data elements provided to the method as input.
	 * 
	 * @see org.filteredpush.kuration.services.BaseCurationService.inputValues
	 */
	private Map<String,String> initialElementValues;   
	/**
	 * Key value pairs representing (a flat list of) data elements after any modifications by
	 * application of the method. 
	 * 
	 * @see org.filteredpush.kuration.services.BaseCurationService.curatedValues
	 */
	private Map<String,String> finalElementValues;
	
	/**
	 * An ordered list of comments made by the curation step in evaluating the initial element 
	 * values.  Provides provenance for the curation states and any changes proposed in the
	 * final element values.
	 * 
	 * @see org.filteredpush.kuration.services.BaseCurationService.comments
	 */
	private List<String> curationComments;
	/**
	 * A list of data quality assertions made by the curation step about the initial element
	 * values and any changes proposed in the final element values.
	 * 
	 * @see org.filteredpush.kuration.services.BaseCurationService.curationStatus
	 */
	private List<String> curationStates;
	/**
	 * An ordered list of sources that were consulted by the curation step in evaluating the
	 * initial element values.  Provides provenance for the curation states and changes proposed 
	 * in the final element values.
	 * 
	 * @see org.filteredpush.kuration.services.BaseCurationService.services
	 */
	private List<String> sourcesConsulted;
	
	public CurationStep(String validationMethodSpecification, Map<String,String>initialElementValues) { 
		this.validationMethodSpecification = validationMethodSpecification;
		this.initialElementValues = initialElementValues;
		init();
		// With no action taken, the final state is expected to be the same as the initial state.
		finalElementValues.putAll(initialElementValues);
	}
	
	protected void init() { 
		curationComments = new ArrayList<String>();
		curationStates = new ArrayList<String>();
		sourcesConsulted = new ArrayList<String>();
		finalElementValues = new HashMap<String,String>();
	}

	public void addCurationComment(String curationComment) { 
		curationComments.add(curationComment);
	}
	
	public void addCurationState(String curationState) { 
		curationStates.add(curationState);
	}	
	
	public void addCurationSource(String curationSource) { 
		sourcesConsulted.add(curationSource);
	}
	
	/**
	 * @return the actorName
	 */
	public String getValidationMethodSpecification() {
		return validationMethodSpecification;
	}

	/**
	 * @param actorName the actorName to set
	 */
	public void setValidationMethodSpecification(String validationMethodSpecification) {
		this.validationMethodSpecification = validationMethodSpecification;
	}
	
	public Map<String,String> getInitialElementValues() { 
		return initialElementValues;
	}
	
	public Map<String,String> getFinalElementValues() { 
		return finalElementValues;
	}

}
