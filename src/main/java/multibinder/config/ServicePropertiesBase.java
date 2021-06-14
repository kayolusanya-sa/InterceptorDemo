package multibinder.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;


@ConfigurationProperties
@Configuration
@EnableConfigurationProperties
@Data
public class ServicePropertiesBase implements ServiceConfig {

    @Value("${spring.cloud.stream.binders.kafka3.environment.spring.cloud.stream.kafka.streams.binder.configuration.schema.registry.url}")
    private String schemaRegistryUrl;

    //@Value("${spring.cloud.stream.bindings.process-out-0.destination}")
    private String outputTopic;

    @Value("${spring.cloud.stream.bindings.process-in-0.consumer.dlqName}")
    private String errorTopic;

    private Spring spring = new Spring();

    private Server server = new Server();

    public long getTimeWindow(){
        return getSpring().getApplication().getTimeWindow();
    }

    public String getStoreName() {
        return getSpring().getApplication().getStoreName();
    }

    @Override
    public String getAggregatorStoreName() {
        return getSpring().getApplication().getAggregatorStoreName();
    }

    public Map<String, String> getClassIdMethodMap() {
        return getServer().getApplication();
    }

    @Override
    public Integer getBatchSize() {
        return getSpring().getApplication().getBatchSize();
    }

    @Override
    public String getDeDuplicatorOutputTopic() {
        return getSpring().getApplication().getDeduplicatorOutputTopic();
    }

    @Override
    public long getBatchTimeLimit() {
        return getSpring().getApplication().getBatchTimeLimit();
    }

    @Override
    public String getAggregatorSchedule() {
        return getSpring().getApplication().getAggregatorCron();
    }

    @Data
    public class Spring {
        private Application application = new Application();
    }

    @Data
    public class Server {
        private Map<String, String> application;
    }

    @Data
    public class Application {
        private String name;
        private String envVariableName;
        private String storeName;
        private String aggregatorStoreName;
        private long timeWindow;
        private Integer batchSize;
        private String aggregatorCron;
        private String deduplicatorOutputTopic;
        private long batchTimeLimit;
    }
}

