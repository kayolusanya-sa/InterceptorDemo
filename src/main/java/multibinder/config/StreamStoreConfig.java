package multibinder.config;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.state.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Map;
import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static java.time.Duration.ofSeconds;
import static java.util.Map.of;
import static org.apache.kafka.streams.state.Stores.persistentTimestampedWindowStore;
import static org.apache.kafka.streams.state.Stores.windowStoreBuilder;


@Configuration
public class StreamStoreConfig {

    private  final Serde<SpecificRecord> specificAvroSerde = new SpecificAvroSerde<>() ;

    @Autowired
    private ServiceConfig serviceProperties;

    @Autowired
    private Environment environment;

    @Bean
    public StoreBuilder<WindowStore<String, SpecificRecord>> streamStore() {
        specificAvroSerde.configure(getSerdeConfig(),false);
        WindowBytesStoreSupplier windowBytesStoreSupplier = persistentTimestampedWindowStore(
                serviceProperties.getStoreName(),
                ofSeconds(serviceProperties.getTimeWindow()),
                ofSeconds(serviceProperties.getTimeWindow()),
                false);
        return windowStoreBuilder(windowBytesStoreSupplier, Serdes.String(), specificAvroSerde);
    }

    @Bean
    public StoreBuilder<KeyValueStore<String, SpecificRecord>> keyValueStreamStore() {
        specificAvroSerde.configure(getSerdeConfig(),false);
        return Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(serviceProperties.getAggregatorStoreName()),
                Serdes.String(),specificAvroSerde)
                .withCachingEnabled();
    }

    private Map<String, String> getSerdeConfig(){
        return of(SCHEMA_REGISTRY_URL_CONFIG, serviceProperties.getSchemaRegistryUrl());
    }
}
