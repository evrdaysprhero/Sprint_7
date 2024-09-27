import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pojo.*;

import static io.restassured.RestAssured.given;

public class CourierLoginTests {
    private final String LOGIN = "sprhero100";
    private final String PASSWORD = "12345";
    private Integer id;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/";

        Courier courier = new Courier(LOGIN, PASSWORD, "Eugenia");

        Response response = CourierTests.postCourier(courier);

        CourierLogin courierLogin = new CourierLogin(LOGIN, PASSWORD);
        id = postCourierLogin(courierLogin)
                .body()
                .as(CourierLoginResponse.class)
                .getId();
    }

    @After
    public void clear() {

        given()
                .delete("/api/v1/courier/{id}", id);

    }

    public static Response postCourierLogin(CourierLogin courier) {
        return given()
                .header("Content-type", "application/json")
                .body(courier)
                .post("/api/v1/courier/login");

    }

    @Test
    public void courierLoginSuccess() {
        CourierLogin courier = new CourierLogin(LOGIN, PASSWORD);

        Response response = postCourierLogin(courier);

        CourierLoginResponse courierResponse = response
                .body()
                .as(CourierLoginResponse.class);

        response.then().assertThat()
                .statusCode(200);
        Assert.assertNotNull("Не получен id курьера", courierResponse.getId());

    }

    @Test
    public void courierLoginEmptyNameFail() {
        CourierLogin courier = new CourierLogin(null, PASSWORD);

        Response response = postCourierLogin(courier);

        CourierResponseFail courierResponse = response
                .body()
                .as(CourierResponseFail.class);

        response.then().assertThat()
                .statusCode(400);
        Assert.assertEquals("Недостаточно данных для входа", courierResponse.getMessage());

    }

    @Test
    public void courierLoginEmptyPassFail() {
        CourierLogin courier = new CourierLogin(LOGIN, null);

        Response response = postCourierLogin(courier);

        CourierResponseFail courierResponse = response
                .body()
                .as(CourierResponseFail.class);

        response.then().assertThat()
                .statusCode(400);
        Assert.assertEquals("Недостаточно данных для входа", courierResponse.getMessage());

    }

    @Test
    public void courierLoginNoNameFail() {

        String json = "{\"password\": \"" + PASSWORD + "\"}";

        Response response = given()
                .header("Content-type", "application/json")
                .and()
                .body(json)
                .post("/api/v1/courier/login");

        response.then().assertThat()
                .statusCode(400);
        Assert.assertEquals("Недостаточно данных для входа", response
                .body()
                .as(CourierResponseFail.class)
                .getMessage());

    }

    @Test
    public void courierLoginNoPassFail() {

        String json = "{\"login\": \"" + LOGIN + "\"}";

        Response response = given()
                .header("Content-type", "application/json")
                .and()
                .body(json)
                .post("/api/v1/courier/login");

        response.then().assertThat()
                .statusCode(400);
        Assert.assertEquals("Недостаточно данных для входа", response
                .body()
                .as(CourierResponseFail.class)
                .getMessage());

    }

    @Test
    public void courierLoginWrongLoginFail() {
        CourierLogin courier = new CourierLogin("sprhero0000", PASSWORD);

        Response response = postCourierLogin(courier);

        CourierResponseFail courierResponse = response
                .body()
                .as(CourierResponseFail.class);

        response.then().assertThat()
                .statusCode(404);
        Assert.assertEquals("Учетная запись не найдена", courierResponse.getMessage());

    }

    @Test
    public void courierLoginWrongPassFail() {
        CourierLogin courier = new CourierLogin(LOGIN, "0000");

        Response response = postCourierLogin(courier);

        CourierResponseFail courierResponse = response
                .body()
                .as(CourierResponseFail.class);

        response.then().assertThat()
                .statusCode(404);
        Assert.assertEquals("Учетная запись не найдена", courierResponse.getMessage());

    }

    @Test
    public void courierLoginWrongDataFail() {
        CourierLogin courier = new CourierLogin("sprhero00000", "0000");

        Response response = postCourierLogin(courier);

        CourierResponseFail courierResponse = response
                .body()
                .as(CourierResponseFail.class);

        response.then().assertThat()
                .statusCode(404);
        Assert.assertEquals("Учетная запись не найдена", courierResponse.getMessage());

    }

}
