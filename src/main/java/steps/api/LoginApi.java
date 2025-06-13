package steps.api;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import static data.Constants.*;

public class LoginApi {
    public static final RequestSpecification BASE_SPEC = new RequestSpecBuilder()
            .setBaseUri(URL_ESCUELAJS)
            .setContentType(APPLICATION)
            .build();
}
