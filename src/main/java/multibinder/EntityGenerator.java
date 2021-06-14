package multibinder;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static java.util.UUID.randomUUID;

public class EntityGenerator {
    private static final String pattern = "EEEEE dd MMMMM yyyy HH:mm:ss.SSSZ";
    private static final SimpleDateFormat simpleDateFormat =
            new SimpleDateFormat(pattern, new Locale("en", "GB"));


    public static LocationSupplierSource generateSourceEntity(String id){
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

    public static LocationSupplierSource generateSourceEntity(){
        return generateSourceEntity(randomUUID().toString());
    }
}
