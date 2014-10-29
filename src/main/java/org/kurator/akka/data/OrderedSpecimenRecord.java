package org.kurator.akka.data;

import java.util.LinkedList;
import java.util.List;

import fp.util.SpecimenRecord;

public class OrderedSpecimenRecord extends SpecimenRecord {

    private List<String> header;
    
    public OrderedSpecimenRecord() {
        super();
        header = new LinkedList<String>();
    }
    
    @Override
    public String put(String key, String value) {
        header.add(key);
        return super.put(key, value);
    }
    
    public List<String> getKeyList() {
        return new LinkedList<String>(header);
    }
    
    @Override
    public String toString() {
        
        boolean isFirst = true;
        
        StringBuffer buffer = new StringBuffer("{");
        
        for (String item : header){
            if (isFirst) {
                isFirst = false;
            } else {
                buffer.append(", ");
            }
            buffer.append(item)
                  .append("=")
                  .append(get(item));
        }
        
        buffer.append("}");
        
        return buffer.toString();  
    }
    private static final long serialVersionUID = 1L;

}
