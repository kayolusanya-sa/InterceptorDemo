package multibinder.interceptors;

import lombok.extern.slf4j.Slf4j;
import multibinder.LocationSupplierSource;
import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.List;
import java.util.Map;

@Slf4j
public class ACLConsumerInterceptor <K, V> implements ConsumerInterceptor<String, LocationSupplierSource>{

    public void configure(final Map<String, ?> configs) {
        final Map<String, Object> copyConfigs = (Map<String, Object>) configs;
    }

    public ConsumerRecords<String, LocationSupplierSource> onConsume(ConsumerRecords<String, LocationSupplierSource> records) {
        for (TopicPartition partition : records.partitions()) {
            List<ConsumerRecord<String, LocationSupplierSource>> recordsInPartition = records.records(partition);
            for (ConsumerRecord<String, LocationSupplierSource> record : recordsInPartition) {
                    System.out.println("onConsume:");
                    System.out.println(record.value());
            }
        }
        return records;
    }

    public void onCommit(Map<TopicPartition, OffsetAndMetadata> offsets) {
        System.out.println("onCommit");
    }

    @Override
    public void close() {
        System.out.println("close");
        this.close();
    }
}


