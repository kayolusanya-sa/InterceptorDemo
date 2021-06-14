package multibinder.config;

import java.util.Map;

public interface ServiceConfig {
    long getTimeWindow();

    String getStoreName();

    String getAggregatorStoreName();

    Map<String, String> getClassIdMethodMap();

    String getSchemaRegistryUrl();

    Integer getBatchSize();

    String getOutputTopic();

    String getAggregatorSchedule();

    String getErrorTopic();

    String getDeDuplicatorOutputTopic();

    default String getBasicAuthCredentialsSource(){
        return null;
    }

    default String getBasicAuthUserInfo(){
        return null;
    }

    long getBatchTimeLimit();
}
