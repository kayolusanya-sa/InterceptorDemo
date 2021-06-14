package multibinder;

import lombok.extern.slf4j.Slf4j;
import multibinder.config.ServiceConfig;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.metrics.Sensor;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsMetrics;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.Cancellable;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.Punctuator;
import org.apache.kafka.streams.processor.TaskId;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import uk.co.sainsburys.supplychain.integrationservices.model.generated.location.batch.LocationSupplierBatch;
import uk.co.sainsburys.supplychain.integrationservices.model.generated.location.supplier.LocationSupplier;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.time.Duration.ofSeconds;
import static java.time.Instant.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.UUID.randomUUID;
import static org.apache.kafka.streams.KeyValue.pair;
import static org.apache.kafka.streams.processor.PunctuationType.WALL_CLOCK_TIME;
import static uk.co.sainsburys.supplychain.integrationservices.model.generated.location.batch.LocationSupplierBatch.newBuilder;

@Slf4j
@Service
@ConfigurationProperties
public class AggregatorService implements Transformer<String, SpecificRecord, KeyValue<String, SpecificRecord>> {


    private final ServiceConfig serviceProperties;
    private KeyValueStore<String, SpecificRecord> store;
    private ProcessorContext context;
    public static final String DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm:ss SSS";
    private Sensor metricsSensor;
    private Sensor rateMetricsSensor;
    private static AtomicInteger count = new AtomicInteger(1);
    private StreamsMetrics metrics;
    private BatchScheduler scheduler;

    public AggregatorService(ServiceConfig serviceProperties){
        this.serviceProperties = serviceProperties;
        log.info("***** constructor fired at  : {} *******", ofPattern(DATE_TIME_FORMAT).format(LocalDateTime.now()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void init(final ProcessorContext context) {
        TaskId taskId = context.taskId();
        log.info("***** init with taskId : {} on partition {} at  : {} *******",taskId, taskId.partition,ofPattern(DATE_TIME_FORMAT).format(LocalDateTime.now()));
        this.context = context;
        store = (KeyValueStore<String, SpecificRecord>) this.context.getStateStore(serviceProperties.getAggregatorStoreName());
        metrics = context.metrics();
        scheduler = new BatchScheduler(context);

        final String tagKey = "task-id";
        final String tagValue = context.taskId().toString();
        final String nodeName = "AggregatorServiceProcessor_" + count.getAndIncrement();
        metricsSensor = context.metrics().addLatencyRateTotalSensor("transformer-node",
                nodeName, "aggregator-processor-calculation",
                Sensor.RecordingLevel.DEBUG,
                tagKey,
                tagValue);

        rateMetricsSensor = context.metrics().addRateTotalSensor("transformer-node",
                nodeName, "aggregator-processor-calculation",
                Sensor.RecordingLevel.DEBUG,
                tagKey,
                tagValue);
    }

    @Override
    public KeyValue<String, SpecificRecord> transform(final String key, final SpecificRecord specificRecord) {
        long start = System.nanoTime();
        KeyValue<String, SpecificRecord> output = null;
        store.put(key,specificRecord);
        if (getStoreContents().size() == serviceProperties.getBatchSize()) {// TODO fix multiple calls to retrieve store contents
            var batch = (LocationSupplierBatch)createBatch();
            clearStore();
            output = pair(batch.getBatchUuid(), batch);
            //scheduler.resetPunctuator();
        }
        long end = System.nanoTime();
        rateMetricsSensor.record();
        metricsSensor.record(end - start);
        return output;
    }

    @Override
    public void close() {
        log.info("Shutting down the AggregatorService KStream/Process API Analysis Metrics  App now");
        for (Map.Entry<MetricName, ? extends Metric> metricNameEntry : metrics.metrics().entrySet()) {
            Metric metric = metricNameEntry.getValue();
            MetricName metricName = metricNameEntry.getKey();
            if(!metric.metricValue().equals(0.0) && !metric.metricValue().equals(Double.NEGATIVE_INFINITY)) {
                log.info("MetricName {}", metricName.name());
                log.info(" = {}", metric.metricValue());
                log.info(" = {}", metric.value());

            }
        }
    }

    private SpecificRecord createBatch(){
        List<LocationSupplier> suppliers = getStoreContents();
        return newBuilder()
                .setBatchCount(suppliers.size())
                .setBatchUuid(randomUUID().toString())
                .setCreateDateTime(now())
                .setLocationSuppliers(new ArrayList<>(suppliers))
                .build();
    }

    private void clearStore(){
        store.all().forEachRemaining(item -> store.delete(item.key));
    }

    private List<LocationSupplier> getStoreContents(){
        final List<LocationSupplier> recordList = new ArrayList<>();
        store.all().forEachRemaining(record -> recordList.add((LocationSupplier)record.value));
        return recordList;
    }

    class BatchScheduler implements Punctuator {
        private Cancellable schedule;
        private final ProcessorContext context;

        public BatchScheduler(ProcessorContext context){
            this.context = context;
            this.schedule = getSchedulerHandle();
        }

        @Override
        public void punctuate(final long timestamp) {
            var batch = (LocationSupplierBatch)createBatch();
            if(batch.getLocationSuppliers().size() > 0){
                log.info("** BatchScheduler assigned to partition {} batched {} messages at : {} **",
                        context.taskId().partition, batch.getBatchCount(),
                        new SimpleDateFormat(DATE_TIME_FORMAT).format(new Date(timestamp)));
                context.forward(batch.getBatchUuid(), batch);
                context.commit();
                clearStore();
            }
        }

        private Cancellable getSchedulerHandle(){
            return this.context.schedule(ofSeconds(
                    serviceProperties.getBatchTimeLimit()),
                    WALL_CLOCK_TIME,
                    this);
        }

        public void resetPunctuator() {
            log.info("** resetting Punctuator assigned to task {}  **",
                        context.taskId());
            this.schedule.cancel();
            this.schedule = getSchedulerHandle();
        }
    }
}
