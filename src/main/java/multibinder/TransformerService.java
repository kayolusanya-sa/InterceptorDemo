package multibinder;

import org.apache.avro.specific.SpecificRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransformerService {
    @Autowired
    private AvroEntityMapper avroEntityMapper;

    public SpecificRecord transform(LocationSupplierSource input){
        return avroEntityMapper.entityToTransformationObject(input);
    }
}
