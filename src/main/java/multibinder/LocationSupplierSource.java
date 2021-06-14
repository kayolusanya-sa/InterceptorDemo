package multibinder;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@JsonDeserialize(builder = LocationSupplierSource.Builder.class)
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocationSupplierSource implements Serializable {


    public LocationSupplierSource(){
        group = new Group();
    }

    @NotBlank
    @JsonProperty
    public String sid;
    @NotBlank
    @JsonProperty
    public String name;
    @NotBlank
    @JsonProperty
    public String supplierCode;
    @NotBlank
    @JsonProperty
    public String supplierNumber;
    @JsonProperty
    public String supplierInvoiceCurrency;
    @JsonProperty
    public String supplierPaymentCurrency;
    @NotBlank
    @JsonProperty
    public String lastRevisionDate;
    @JsonProperty
    public String startDate;
    @JsonProperty
    public String endDate;
    @NotBlank
    @JsonProperty
    public String status;
    @NotBlank
    @JsonProperty
    public String vatNumber;
    @NotNull
    @Valid
    @JsonProperty
    public Group group;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
    }

    @lombok.Builder
    @Data
    @AllArgsConstructor
    public static class Group{
        public Group(){}
        @JsonProperty
        public String id;
        @JsonProperty
        public String name;

        @JsonPOJOBuilder(withPrefix = "")
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Builder {
        }
    }
}
