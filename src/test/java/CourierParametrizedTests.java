import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pojo.Courier;
import pojo.CourierResponseFail;

import static io.restassured.RestAssured.given;

@RunWith(Parameterized.class)
public class CourierParametrizedTests {

    private final String login;
    private final String password;
    private final String firstName;

    public CourierParametrizedTests(String login, String password, String firstName) {
        this.login = login;
        this.password = password;
        this.firstName = firstName;
    }

    @Parameterized.Parameters
    public static Object[][] getCourierData() {
        return new Object[][] {
                { "", "12345", "Eugenia" },
                { null, "12345", "Eugenia" },
                { "sprhero03", "", "Eugenia" },
                { "sprhero03", null, "Eugenia" },
                { null, null, "" },
                { "", null, "" },
                { null, "", null },
                { "", "", null },

        };
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/";
    }

    @Test
    public void createNoRequiredFieldFail() {

        Courier courier = new Courier(login, password, firstName);

        Response response = given()
                .header("Content-type", "application/json")
                .body(courier)
                .post("/api/v1/Courier");

        CourierResponseFail courierResponse = response
                .body()
                .as(CourierResponseFail.class);

        response.then().assertThat()
                .statusCode(400);
        Assert.assertEquals("Недостаточно данных для создания учетной записи", courierResponse.getMessage());

    }
}
