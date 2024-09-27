import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pojo.Courier;
import pojo.CourierResponse;
import pojo.CourierResponseFail;

import static io.restassured.RestAssured.*;

public class CourierTests {

    @Before
    public void setUp() {
        // повторяющуюся для разных ручек часть URL лучше записать в переменную в методе Before
        // если в классе будет несколько тестов, указывать её придётся только один раз
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/";
    }

    public Response postCourier(Courier courier) {
        return given()
                .header("Content-type", "application/json")
                .body(courier)
                .post("/api/v1/Courier");
    }

    @Test
    public void createAllParamsSuccess() {
        Courier courier = new Courier("sprhero12", "12345", "Eugenia");

        Response response = postCourier(courier);

        CourierResponse courierResponse = response
                .body()
                .as(CourierResponse.class);

        response.then().assertThat()
                .statusCode(201);
        Assert.assertTrue("Курьер не создан", courierResponse.getOk());

    }

    @Test
    public void createNoNameSuccess() {
        String json = "{\"login\": \"sprhero13\",\n" +
                "\"password\": \"saske\"}";

        Response response = given()
                .header("Content-type", "application/json")
                .and()
                .body(json)
                .post("/api/v1/Courier");

        response.then().assertThat()
                .statusCode(201);
        Assert.assertTrue("Курьер не создан", response
                .body()
                .as(CourierResponse.class).getOk());

    }

    @Test
    public void createTwoCouriersFail() {
        Courier courier = new Courier("sprhero14", "12345", "Eugenia");

        //вызываем первый раз
        Response response = postCourier(courier);

        //вызываем второй раз
        Response responseRepeat = postCourier(courier);

        CourierResponseFail courierResponse = responseRepeat
                .body()
                .as(CourierResponseFail.class);

        responseRepeat.then().assertThat()
                .statusCode(409);
        Assert.assertEquals("Этот логин уже используется. Попробуйте другой.", courierResponse.getMessage());

    }

    @Test
    public void createTwoLoginsFail() {
        Courier courierOne = new Courier("sprhero15", "12345", "Eugenia");
        Courier courierTwo = new Courier("sprhero15", "1234567", "Ivan");

        //вызываем первый раз
        Response response = postCourier(courierOne);

        //вызываем второй раз
        Response responseRepeat = postCourier(courierTwo);

        CourierResponseFail courierResponse = responseRepeat
                .body()
                .as(CourierResponseFail.class);

        responseRepeat.then().assertThat()
                .statusCode(409);
        Assert.assertEquals("Этот логин уже используется. Попробуйте другой.", courierResponse.getMessage());

    }

    @Test
    public void createNoLoginFail() {

        String json = "{\"password\": \"1234\",\n" +
                "\"firstName\": \"saske\"}";

        Response response = given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(json)
                        .post("/api/v1/Courier");

        response.then().assertThat()
                .statusCode(400);
        Assert.assertEquals("Недостаточно данных для создания учетной записи", response
                .body()
                .as(CourierResponseFail.class).getMessage());

    }

    @Test
    public void createNoPasswordFail() {

        String json = "{\"login\": \"sprhero16\",\n" +
                "\"firstName\": \"saske\"}";

        Response response = given()
                .header("Content-type", "application/json")
                .and()
                .body(json)
                .post("/api/v1/Courier");

        response.then().assertThat()
                .statusCode(400);
        Assert.assertEquals("Недостаточно данных для создания учетной записи", response
                .body()
                .as(CourierResponseFail.class).getMessage());

    }

}
