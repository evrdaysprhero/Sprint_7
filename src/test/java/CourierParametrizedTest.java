import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
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
@Epic(value = "/api/v1/Courier")
@Feature(value = "Недостаточно данных для создания учетной записи")
public class CourierParametrizedTest {

    private final String login;
    private final String password;
    private final String firstName;

    public CourierParametrizedTest(String login, String password, String firstName) {
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

    @Step("Проверка кода ответа")
    public static void checkResponseCode(Response response, Integer expCode) {
        response.then().assertThat()
                .statusCode(expCode);
    }

    @Step("Проверка сообщения об ошибке")
    public static void checkResponseMessage(Response response, String expMsg) {
        CourierResponseFail courierResponse = response
                .body()
                .as(CourierResponseFail.class);
        Assert.assertEquals(expMsg, courierResponse.getMessage());
    }

    @Test
    public void createNoRequiredFieldFail() {

        Courier courier = new Courier(login, password, firstName);

        Response response = given()
                .header("Content-type", "application/json")
                .body(courier)
                .post("/api/v1/Courier");

        checkResponseCode(response, 400);
        checkResponseMessage(response, "Недостаточно данных для создания учетной записи");

    }
}
