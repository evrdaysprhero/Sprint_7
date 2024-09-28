import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pojo.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.*;

public class CourierTests {
    private String login;
    private String password;
    private String name;

    @Before
    @Step("Подготовка данных")
    public void setUp() {
        // повторяющуюся для разных ручек часть URL лучше записать в переменную в методе Before
        // если в классе будет несколько тестов, указывать её придётся только один раз
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/";
        login = "sprhero" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        password = RandomStringUtils.randomNumeric(6);
        name = RandomStringUtils.randomAlphabetic(5);
    }

    @After
    @Step("Удаление созданного курьера")
    public void clear() {
        CourierLogin courierLogin = new CourierLogin(login, password);
        Integer id = CourierLoginTests.postCourierLogin(courierLogin)
                .body()
                .as(CourierLoginResponse.class)
                .getId();

        given()
                .delete("/api/v1/courier/{id}", id);
    }

    @Step("Вызов /api/v1/Courier")
    public static Response postCourier(Courier courier) {
        return given()
                .header("Content-type", "application/json")
                .body(courier)
                .post("/api/v1/Courier");
    }

    @Test
    @DisplayName("Запрос со всеми параметрами")
    public void createAllParamsSuccess() {
        Courier courier = new Courier(login, password, name);

        Response response = postCourier(courier);

        CourierResponse courierResponse = response
                .body()
                .as(CourierResponse.class);

        response.then().assertThat()
                .statusCode(201);
        Assert.assertTrue("Курьер не создан", courierResponse.getOk());

    }

    @Test
    @DisplayName("Нет поля Имя")
    public void createNoNameSuccess() {
        String json = "{\"login\": \"" + login + "\",\n" +
                "\"password\": \"" + password + "\"}";

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
    @DisplayName("Нельзя создать двух одинаковых курьеров")
    public void createTwoCouriersFail() {
        Courier courier = new Courier(login, password, name);

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
    @DisplayName("Нельзя создать двух курьеров с одинаковым логином")
    public void createTwoLoginsFail() {
        Courier courierOne = new Courier(login, password, name);
        Courier courierTwo = new Courier(login, "1234567", "Ivan");

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
    @DisplayName("Нет поля Логин")
    public void createNoLoginFail() {

        String json = "{\"password\": \"" + password + "\",\n" +
                "\"firstName\": \"" + name + "\"}";

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
    @DisplayName("Нет поля Пароль")
    public void createNoPasswordFail() {

        String json = "{\"login\": \"" + login + "\",\n" +
                "\"firstName\": \"" + name + "\"}";

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
