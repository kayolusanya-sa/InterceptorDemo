/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package multibinder;

import lombok.extern.slf4j.Slf4j;
import multibinder.interceptors.ACLConsumerInterceptor;
import multibinder.interceptors.ACLProducerInterceptor;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.StreamsBuilderFactoryBeanCustomizer;
import reactor.core.publisher.Flux;

import java.util.function.Function;
import java.util.function.Supplier;

import static java.time.Duration.ofSeconds;
import static java.util.Collections.singletonList;
import static java.util.Map.of;
import static java.util.Objects.requireNonNull;

import static java.util.UUID.randomUUID;
import static org.apache.kafka.clients.consumer.ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.consumerPrefix;
import static org.apache.kafka.streams.StreamsConfig.producerPrefix;


@SpringBootApplication
@Slf4j
public class MultiBinderApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiBinderApplication.class, args);
    }

    @Bean
    public Supplier<Flux<LocationSupplierSource>> sendTestData(){
        return () -> Flux.range(1, 100)
                .map(i -> generateSourceEntity())
                .delayElements(ofSeconds(1));
    };

    @Bean
    public Function<KStream<String, LocationSupplierSource>, KStream<String, LocationSupplierSource>> concatFn() {
        return input -> input
                .peek((k, v) -> log.info("pre processed json message. key: {}  value: {} %n", k, v))
                .peek((k, v) -> log.info("post processed json message. key: {}  value: {} %n", k, v));
    }

    @Bean
    public StreamsBuilderFactoryBeanCustomizer streamsBuilderFactoryBeanCustomizer() {
        return factoryBean -> {
            factoryBean.setKafkaStreamsCustomizer(kafkaStreams -> kafkaStreams.setUncaughtExceptionHandler((thread, throwable) -> {
                log.error("Uncaught exception in stream {} {}", thread, throwable.getMessage());
            }));

            requireNonNull(factoryBean.getStreamsConfiguration()).putAll(
                    of(
                            producerPrefix(INTERCEPTOR_CLASSES_CONFIG), singletonList(ACLProducerInterceptor.class.getName()),
                            consumerPrefix(INTERCEPTOR_CLASSES_CONFIG), singletonList(ACLConsumerInterceptor.class.getName())
                    ));
        };
    }

    public static LocationSupplierSource generateSourceEntity(){
        String id = randomUUID().toString();
        LocationSupplierSource.Group grp = LocationSupplierSource.Group
                .builder()
                .id(String.valueOf(id.length()))
                .name("Group_name")
                .build();

        return LocationSupplierSource.builder()
                .endDate("2018-07-14")
                .lastRevisionDate("2018-07-14")
                .name("name")
                .sid(id)
                .startDate("2018-07-14")
                .status("status")
                .supplierCode("supp code")
                .supplierNumber("100")
                .vatNumber("100")
                .group(grp)
                .build();
    }
}
