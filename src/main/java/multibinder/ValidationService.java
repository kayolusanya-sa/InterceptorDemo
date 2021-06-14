package multibinder;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import multibinder.config.ServiceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import javax.validation.Validator;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.time.Instant.ofEpochMilli;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static org.joda.time.format.ISODateTimeFormat.dateTimeParser;

@Slf4j
@AllArgsConstructor
@Service
public class ValidationService {
    public static final String START_DATE_IS_IN_WRONG_FORMAT = " Field: startDate is in wrong format";
    public static final String END_DATE_IS_IN_WRONG_FORMAT = " Field: endDate is in wrong format";
    public static final String LAST_REVISION_DATE_IS_IN_WRONG_FORMAT = " Field: lastRevisionDate is in wrong format";
    public static final String ERROR_PARSING_ISO_8601_TIME = "Error parsing Field lastRevisionDate value: %s to ISO8601 format: ";
    private final Validator validator;
    private static final String FIELD_NAME = "Field";
    private static final String NULL_MESSAGE = "Null value received";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private StreamBridge streamBridge;

    @Autowired
    private ServiceConfig serviceProperties;


    public boolean validate(LocationSupplierSource locationSupplierSource){
        final boolean[] valid = {true};
        ofNullable(locationSupplierSource)
                .ifPresentOrElse( src -> {
                    valid[0] = validateSpec(src);},() -> { valid[0] = false;
                    processValidationResponse(null,NULL_MESSAGE);});
        return valid[0];
    }

    private boolean validateSpec(LocationSupplierSource locationSupplierSource){
        var message = validator.validate(locationSupplierSource).stream()
                .map(violation -> join(" ", FIELD_NAME,violation.getPropertyPath().toString(), violation.getMessage()))
                .collect(joining(", "));
        message += validateDates(locationSupplierSource);
        return !processValidationResponse(locationSupplierSource,message);
    }

    public boolean processValidationResponse(LocationSupplierSource locationSupplierSource, String errorMessage){
        boolean validationErrorsExist = false;
        if(!isNull(errorMessage) && !errorMessage.isBlank()){
            streamBridge.send(serviceProperties.getErrorTopic(),locationSupplierSource);
            log.error(errorMessage);
            validationErrorsExist = true;
        }
        return validationErrorsExist;
    }

    private String validateDates(LocationSupplierSource locationSupplierSource){
        StringBuilder message = new StringBuilder();
        if(!isNull(locationSupplierSource.getStartDate()) && !isValid(locationSupplierSource.getStartDate())){
            message.append(START_DATE_IS_IN_WRONG_FORMAT);
        }
        if(!isNull(locationSupplierSource.getEndDate()) && !isValid(locationSupplierSource.getEndDate())){
            message.append(END_DATE_IS_IN_WRONG_FORMAT);
        }
        if(!isNull(locationSupplierSource.getEndDate()) && !isValid(locationSupplierSource.getLastRevisionDate())){
            message.append(LAST_REVISION_DATE_IS_IN_WRONG_FORMAT);
            var lastUpdateDateTime = locationSupplierSource.getLastRevisionDate();
            try {
                ofEpochMilli(dateTimeParser().parseDateTime(lastUpdateDateTime).getMillis());
            } catch (final RuntimeException e) {
                message.append(format(ERROR_PARSING_ISO_8601_TIME,lastUpdateDateTime));
            }
        }
        return message.toString();
    }

    private boolean isValid(String date) {
        DATE_FORMAT.setLenient(false);
        try {
            DATE_FORMAT.parse(date);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }
}

