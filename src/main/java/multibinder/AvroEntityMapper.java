package multibinder;

import org.joda.time.format.ISODateTimeFormat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import uk.co.sainsburys.supplychain.integrationservices.model.generated.location.supplier.LocationSupplier;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static java.time.Instant.ofEpochMilli;
import static java.time.LocalDate.parse;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Objects.isNull;
import static org.joda.time.format.ISODateTimeFormat.dateTimeParser;

@Mapper(componentModel = "spring", imports = {ISODateTimeFormat.class,Instant.class, BigDecimal.class,
        BigInteger.class, UUID.class, MathContext.class,LocalDate.class, DateTimeFormatter.class})
public interface AvroEntityMapper {
    AvroEntityMapper INSTANCE = Mappers.getMapper(AvroEntityMapper.class);
    String DATE_PATTERN = "yyyy-MM-dd";
    String SUPPLIER = "SUPPLIER";

    @Mappings({
            @Mapping(target = "sourceUuid", expression = "java(locationSupplierSource.sid + '-' + getInstant(locationSupplierSource.lastRevisionDate).toString())"),
            @Mapping(target = "locationId", source = "sid"),
            @Mapping(target = "lastUpdateDateTime", expression = "java(getInstant(locationSupplierSource.lastRevisionDate))"),
            @Mapping(target = "creationDateTime", expression = "java(Instant.now())"),
            @Mapping(target = "supplyChainUuid", source = "sid"),  // TODO: review this

            @Mapping(target = "additionalFields.supplierNumber", source = "supplierNumber"),
            @Mapping(target = "additionalFields.supplierCode", source = "supplierCode"),
            @Mapping(target = "additionalFields.startDate", expression = "java(getDate(locationSupplierSource.startDate))"),
            @Mapping(target = "additionalFields.endDate", expression = "java(getDate(locationSupplierSource.endDate))"),
            @Mapping(target = "additionalFields.supplierStatus", source = "status"),
            @Mapping(target = "additionalFields.VATNumber", source = "vatNumber"),
            @Mapping(target = "additionalFields.cust", source = "status"),
            @Mapping(target = "additionalFields.SMDGroupName", expression = "java(locationSupplierSource.getGroup().getName())"),
            @Mapping(target = "additionalFields.SMDGroupId", expression = "java(Integer.parseInt(locationSupplierSource.getGroup().getId()))"),
            @Mapping(target = "additionalFields.vendorId", source = "status"),

            @Mapping(target = "basicLocation.locationName", source = "name"),
            @Mapping(target = "basicLocation.description", source = "name"),
            @Mapping(target = "basicLocation.locationTypeCode", constant = SUPPLIER),
            @Mapping(target = "basicLocation.address.name", source = "name"),
            @Mapping(target = "basicLocation.address.streetAddressOne", source = "name"),
            @Mapping(target = "basicLocation.address.streetAddressTwo", source = "name"),
            @Mapping(target = "basicLocation.address.streetAddressThree", source = "name"),
            @Mapping(target = "basicLocation.address.crossStreet", source = "name"),
            @Mapping(target = "basicLocation.address.city", source = "name"),
            @Mapping(target = "basicLocation.address.cityCode", source = "name"),
            @Mapping(target = "basicLocation.address.POBoxNumber", source = "name"),
            @Mapping(target = "basicLocation.address.postalCode", source = "name"),
            @Mapping(target = "basicLocation.address.countyCode", source = "name"),
            @Mapping(target = "basicLocation.address.provinceCode", source = "name"),
            @Mapping(target = "basicLocation.address.state", source = "name"),
            @Mapping(target = "basicLocation.address.countryName", source = "name"),
            @Mapping(target = "basicLocation.address.countryCode", source = "name"),
            @Mapping(target = "basicLocation.address.currencyCode", source = "name"),
            @Mapping(target = "basicLocation.address.languageCode", source = "name")

    })
    LocationSupplier entityToTransformationObject(LocationSupplierSource locationSupplierSource);

    MathContext BIG_DECIMAL_CONTEXT = new MathContext(18);
    default BigDecimal intToBigDecimal(int i) {
        return new BigDecimal(BigInteger.valueOf(100 * i), 2, BIG_DECIMAL_CONTEXT);
    }

    default Instant getInstant(final String lastUpdateDateTime){
        if(!isNull(lastUpdateDateTime)){
            return ofEpochMilli(dateTimeParser().parseDateTime(lastUpdateDateTime).getMillis());
        }else{ return null;}
    }

    default LocalDate getDate(String dateString){
        if(!isNull(dateString)) {
            return parse(dateString,ofPattern(DATE_PATTERN));
        }else{ return null;}
    }
}
