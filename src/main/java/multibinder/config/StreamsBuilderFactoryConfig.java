package multibinder.config;

import lombok.extern.slf4j.Slf4j;
import multibinder.exception.DeDuplicatorExceptionHandler;
import multibinder.interceptors.ACLConsumerInterceptor;
import multibinder.interceptors.ACLProducerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.StreamsBuilderFactoryBeanCustomizer;

import static java.util.Collections.singletonList;
import static java.util.Map.of;
import static java.util.Objects.requireNonNull;


@Configuration
@Slf4j
public class StreamsBuilderFactoryConfig {

    @Bean
    public StreamsBuilderFactoryBeanCustomizer streamsBuilderFactoryBeanCustomizer() {
        return factoryBean -> {
            factoryBean.setKafkaStreamsCustomizer(kafkaStreams -> kafkaStreams.setUncaughtExceptionHandler((thread, throwable) -> {
                log.error("Uncaught exception in stream {} {}", thread, throwable.getMessage());
            }));

            requireNonNull(factoryBean.getStreamsConfiguration()).putAll(
                    of(StreamsConfig.DEFAULT_PRODUCTION_EXCEPTION_HANDLER_CLASS_CONFIG, DeDuplicatorExceptionHandler.class,
                            StreamsConfig.producerPrefix(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG), singletonList(ACLProducerInterceptor.class.getName()),
                            StreamsConfig.consumerPrefix(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG), singletonList(ACLConsumerInterceptor.class.getName())
                    ));
        };
    }
}
