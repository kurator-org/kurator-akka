package org.kurator.akka.actors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kurator.akka.AkkaActor;
import org.kurator.akka.data.GenericMapRecord;

public class MapRecordProjector extends AkkaActor {

    public List<String> fields = new ArrayList<String>();
    public String recordClass = null;
    
    private Class<? extends Map<String, String>> _recordClass = GenericMapRecord.class;
    
    @SuppressWarnings("unchecked")
    @Override
    public void onInitialize() throws Exception {
        if (recordClass != null) {
            _recordClass = (Class<? extends Map<String, String>>) Class.forName(recordClass);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void onData(Object value) throws Exception {
        if (value instanceof Map<?,?>) {
            Map<String,String> projection = project((Map<String,String>)value);
            if (projection != null) {
                broadcast(projection);
            }
        }
    }

    protected Map<String,String> project(Map<? extends String,? extends String> record) throws Exception {
        Map<String, String> projection = _recordClass.newInstance();        
        for (String fieldName : fields) {
            projection.put(fieldName, record.get(fieldName));
        }
        return projection;
    }
}
