package org.kurator.akka.messages;

import org.kurator.akka.data.WorkflowProduct;

public class ProductPublication {

    public final WorkflowProduct product;
        	
    public ProductPublication(WorkflowProduct product) {
        this.product = product;
	}
}
