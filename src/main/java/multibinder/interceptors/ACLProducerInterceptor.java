package multibinder.interceptors;


import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;

import java.util.Map;

@Slf4j
public class ACLProducerInterceptor<K,V> implements ProducerInterceptor<String, SpecificRecord> {

    @Autowired
    private StreamBridge streamBridge;

    @Override
    public ProducerRecord<String, SpecificRecord> onSend(ProducerRecord<String, SpecificRecord> record) {
        log.info("---<< ProducerRecord being sent out {} >>---", record);
        return record;
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
        if (exception != null) {
            log.error("Stream error occurred",exception);
            //streamBridge.send();
            //send to DLQ
        }else {
            log.info("---<< record has been acknowledged {} >>---", metadata);
        }
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> configs) {

    }
}
