import io.qameta.allure.Epic;
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

public class CourierLoginTest {
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

        CourierTest.postCourier(courier);

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
    @DisplayName("Успешный логин")
    public void courierLoginSuccess() {
        CourierLogin courier = new CourierLogin(login, password);

        Response response = postCourierLogin(courier);

        CourierLoginResponse courierResponse = response
                .body()
                .as(CourierLoginResponse.class);

        checkResponseCode(response, 200);

        Assert.assertNotNull("Не получен id курьера", courierResponse.getId());

    }

    @Test
    @Epic(value = "/api/v1/courier/login")
    @DisplayName("Не указан логин")
    public void courierLoginEmptyNameFail() {
        CourierLogin courier = new CourierLogin("", password);

        Response response = postCourierLogin(courier);

        checkResponseCode(response, 400);
        checkResponseMessage(response, "Недостаточно данных для входа");

    }

    @Test
    @Epic(value = "/api/v1/courier/login")
    @DisplayName("Не указан пароль")
    public void courierLoginEmptyPassFail() {
        CourierLogin courier = new CourierLogin(login, "");

        Response response = postCourierLogin(courier);

        checkResponseCode(response, 400);
        checkResponseMessage(response, "Недостаточно данных для входа");

    }

    @Test
    @Epic(value = "/api/v1/courier/login")
    @DisplayName("Нет поля Логин")
    public void courierLoginNoNameFail() {

        String json = "{\"password\": \"" + password + "\"}";

        Response response = given()
                .header("Content-type", "application/json")
                .and()
                .body(json)
                .post("/api/v1/courier/login");

        checkResponseCode(response, 400);
        checkResponseMessage(response, "Недостаточно данных для входа");

    }

    @Test
    @Epic(value = "/api/v1/courier/login")
    @DisplayName("Нет поля Пароль")
    public void courierLoginNoPassFail() {

        String json = "{\"login\": \"" + login + "\"}";

        Response response = given()
                .header("Content-type", "application/json")
                .and()
                .body(json)
                .post("/api/v1/courier/login");

        checkResponseCode(response, 504);

    }

    @Test
    @Epic(value = "/api/v1/courier/login")
    @DisplayName("Несуществующие Логин и Пароль")
    public void courierLoginWrongLoginFail() {
        CourierLogin courier = new CourierLogin(login + "0000", password);

        Response response = postCourierLogin(courier);

        checkResponseCode(response, 404);
        checkResponseMessage(response, "Учетная запись не найдена");

    }

    @Test
    @Epic(value = "/api/v1/courier/login")
    @DisplayName("Неправильный пароль")
    public void courierLoginWrongPassFail() {
        CourierLogin courier = new CourierLogin(login, password + "0000");

        Response response = postCourierLogin(courier);

        checkResponseCode(response, 404);
        checkResponseMessage(response, "Учетная запись не найдена");
    }

    @Test
    @Epic(value = "/api/v1/courier/login")
    @DisplayName("Неправильные логин и пароль")
    public void courierLoginWrongDataFail() {
        CourierLogin courier = new CourierLogin(login + "00", password + "00");

        Response response = postCourierLogin(courier);

        checkResponseCode(response, 404);
        checkResponseMessage(response, "Учетная запись не найдена");

    }

}
