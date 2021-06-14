package multibinder.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.BrokerNotAvailableException;
import org.apache.kafka.common.errors.RecordTooLargeException;
import org.apache.kafka.streams.errors.ProductionExceptionHandler;

import java.util.Map;


@Slf4j
public class DeDuplicatorExceptionHandler implements ProductionExceptionHandler {
    public ProductionExceptionHandlerResponse handle(final ProducerRecord<byte[], byte[]> record,
                                                     final Exception exception) {
        if (exception instanceof RecordTooLargeException) {
            return ProductionExceptionHandlerResponse.CONTINUE;
        } else if (exception instanceof BrokerNotAvailableException) {
            log.error("broker not available '{}'",exception.getMessage());
            return ProductionExceptionHandlerResponse.FAIL;
        }
        else {
            log.error("Error occurred '{}' ",exception.getMessage());
            return ProductionExceptionHandlerResponse.FAIL;
        }
    }

    @Override
    public void configure(Map<String, ?> map) {

    }
}