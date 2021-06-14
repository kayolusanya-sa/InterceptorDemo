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
import multibinder.config.ServiceConfig;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.time.Duration.ofSeconds;
import static multibinder.EntityGenerator.generateSourceEntity;


@SpringBootApplication
@Slf4j
public class MultibinderApplication {
    @Autowired
    private ServiceConfig serviceProperties;

    private final EmitterProcessor<Message<?>> processor = EmitterProcessor.create();

    //processor.onNext(message);

    @Autowired
    private TransformerService transformerService;

    @Autowired
    private ValidationService validationService;

    private final AtomicBoolean semaphore = new AtomicBoolean(true);

    public static void main(String[] args) {
        SpringApplication.run(MultibinderApplication.class, args);
    }

    @Bean
    public Supplier<Flux<LocationSupplierSource>> sendTestData(){
        return () -> Flux.range(1, 1000)
                .map(i -> generateSourceEntity())
                .delayElements(ofSeconds(1));
    };

    @Bean
    public Supplier<Flux<Message<?>>> supplier() {
        return () -> processor;
    }


    @Bean
    public Function<LocationSupplierSource, LocationSupplierSource> process() {
        return payload -> payload;
    }

    @Bean
    public Function<KStream<String, LocationSupplierSource>, KStream<String, SpecificRecord>> concatFn() {
        return input -> input
                .peek((k, v) -> log.info("pre processed json message. key: {}  value: {} %n", k, v))
                .filter((key, value) -> validationService.validate(value))
                .map((key, value) -> new KeyValue<>(value.getSid(), transformerService.transform(value)))
                .transform(this::getAggregatorService,serviceProperties.getAggregatorStoreName())
                .peek((k, v) -> log.info("post processed avro message. key: {}  value: {} %n", k, v));
    }

    private AggregatorService getAggregatorService(){
        return new AggregatorService(serviceProperties);
    }

}
