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

import static io.restassured.RestAssured.given;

public class CourierLoginTests {
    private String login;
    private String password;
    private Integer id;

    @Before
    @Step("Подготовка данных и создание курьера")
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/";

        login = "sprhero" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        password = RandomStringUtils.randomNumeric(5);

        Courier courier = new Courier(login, password, RandomStringUtils.randomAlphabetic(6));

        Response response = CourierTests.postCourier(courier);

        CourierLogin courierLogin = new CourierLogin(login, password);
        id = postCourierLogin(courierLogin)
                .body()
                .as(CourierLoginResponse.class)
                .getId();
    }

    @After
    @Step("Удаление курьера")
    public void clear() {

        given()
                .delete("/api/v1/courier/{id}", id);

    }

    @Step("Вызов /api/v1/courier/login")
    public static Response postCourierLogin(CourierLogin courier) {
        return given()
                .header("Content-type", "application/json")
                .body(courier)
                .post("/api/v1/courier/login");

    }

    @Test
    @DisplayName("Успешный логин")
    public void courierLoginSuccess() {
        CourierLogin courier = new CourierLogin(login, password);

        Response response = postCourierLogin(courier);

        CourierLoginResponse courierResponse = response
                .body()
                .as(CourierLoginResponse.class);

        response.then().assertThat()
                .statusCode(200);
        Assert.assertNotNull("Не получен id курьера", courierResponse.getId());

    }

    @Test
    @DisplayName("Не указан логин")
    public void courierLoginEmptyNameFail() {
        CourierLogin courier = new CourierLogin(null, password);

        Response response = postCourierLogin(courier);

        CourierResponseFail courierResponse = response
                .body()
                .as(CourierResponseFail.class);

        response.then().assertThat()
                .statusCode(400);
        Assert.assertEquals("Недостаточно данных для входа", courierResponse.getMessage());

    }

    @Test
    @DisplayName("Не указан пароль")
    public void courierLoginEmptyPassFail() {
        CourierLogin courier = new CourierLogin(login, null);

        Response response = postCourierLogin(courier);

        CourierResponseFail courierResponse = response
                .body()
                .as(CourierResponseFail.class);

        response.then().assertThat()
                .statusCode(400);
        Assert.assertEquals("Недостаточно данных для входа", courierResponse.getMessage());

    }

    @Test
    @DisplayName("Нет поля Логин")
    public void courierLoginNoNameFail() {

        String json = "{\"password\": \"" + password + "\"}";

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
    @DisplayName("Нет поля Пароль")
    public void courierLoginNoPassFail() {

        String json = "{\"login\": \"" + login + "\"}";

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
    @DisplayName("Несуществующие Логин и Пароль")
    public void courierLoginWrongLoginFail() {
        CourierLogin courier = new CourierLogin(login + "0000", password);

        Response response = postCourierLogin(courier);

        CourierResponseFail courierResponse = response
                .body()
                .as(CourierResponseFail.class);

        response.then().assertThat()
                .statusCode(404);
        Assert.assertEquals("Учетная запись не найдена", courierResponse.getMessage());

    }

    @Test
    @DisplayName("Неправильный пароль")
    public void courierLoginWrongPassFail() {
        CourierLogin courier = new CourierLogin(login, password + "0000");

        Response response = postCourierLogin(courier);

        CourierResponseFail courierResponse = response
                .body()
                .as(CourierResponseFail.class);

        response.then().assertThat()
                .statusCode(404);
        Assert.assertEquals("Учетная запись не найдена", courierResponse.getMessage());

    }

    @Test
    @DisplayName("Неправильные логин и пароль")
    public void courierLoginWrongDataFail() {
        CourierLogin courier = new CourierLogin(login + "00", password + "00");

        Response response = postCourierLogin(courier);

        CourierResponseFail courierResponse = response
                .body()
                .as(CourierResponseFail.class);

        response.then().assertThat()
                .statusCode(404);
        Assert.assertEquals("Учетная запись не найдена", courierResponse.getMessage());

    }

}
