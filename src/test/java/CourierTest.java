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

import static io.restassured.RestAssured.*;

public class CourierTest {
    private String login;
    private String password;
    private String name;
    private boolean isSuccess;

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
        if(isSuccess) {
            CourierLogin courierLogin = new CourierLogin(login, password);
            Integer id = CourierLoginTest.postCourierLogin(courierLogin)
                    .body()
                    .as(CourierLoginResponse.class)
                    .getId();

            given()
                    .delete("/api/v1/courier/{id}", id);
        }
    }

    @Step("Вызов /api/v1/Courier")
    public static Response postCourier(Courier courier) {
        return given()
                .header("Content-type", "application/json")
                .body(courier)
                .post("/api/v1/Courier");
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
    @Epic(value = "/api/v1/Courier")
    @DisplayName("Запрос со всеми параметрами")
    public void createAllParamsSuccess() {
        isSuccess = true;
        Courier courier = new Courier(login, password, name);

        Response response = postCourier(courier);

        CourierResponse courierResponse = response
                .body()
                .as(CourierResponse.class);

        checkResponseCode(response, 201);
        Assert.assertTrue("Курьер не создан", courierResponse.getOk());

    }

    @Test
    @Epic(value = "/api/v1/Courier")
    @DisplayName("Нет поля Имя")
    public void createNoNameSuccess() {
        isSuccess = true;

        String json = "{\"login\": \"" + login + "\",\n" +
                "\"password\": \"" + password + "\"}";

        Response response = given()
                .header("Content-type", "application/json")
                .and()
                .body(json)
                .post("/api/v1/Courier");

        checkResponseCode(response, 201);
        Assert.assertTrue("Курьер не создан", response
                .body()
                .as(CourierResponse.class).getOk());

    }

    @Test
    @Epic(value = "/api/v1/Courier")
    @DisplayName("Нельзя создать двух одинаковых курьеров")
    public void createTwoCouriersFail() {
        isSuccess = true;

        Courier courier = new Courier(login, password, name);

        //вызываем первый раз
        Response response = postCourier(courier);

        //вызываем второй раз
        Response responseRepeat = postCourier(courier);

        checkResponseCode(responseRepeat, 409);
        checkResponseMessage(responseRepeat, "Этот логин уже используется. Попробуйте другой.");

    }

    @Test
    @Epic(value = "/api/v1/Courier")
    @DisplayName("Нельзя создать двух курьеров с одинаковым логином")
    public void createTwoLoginsFail() {
        isSuccess = true;

        Courier courierOne = new Courier(login, password, name);
        Courier courierTwo = new Courier(login, "1234567", "Ivan");

        //вызываем первый раз
        Response response = postCourier(courierOne);

        //вызываем второй раз
        Response responseRepeat = postCourier(courierTwo);

        checkResponseCode(responseRepeat, 409);
        checkResponseMessage(responseRepeat, "Этот логин уже используется. Попробуйте другой.");

    }

    @Test
    @Epic(value = "/api/v1/Courier")
    @DisplayName("Нет поля Логин")
    public void createNoLoginFail() {

        String json = "{\"password\": \"" + password + "\",\n" +
                "\"firstName\": \"" + name + "\"}";

        Response response = given()
                        .header("Content-type", "application/json")
                        .and()
                        .body(json)
                        .post("/api/v1/Courier");

        checkResponseCode(response, 400);
        checkResponseMessage(response, "Недостаточно данных для создания учетной записи");

    }

    @Test
    @Epic(value = "/api/v1/Courier")
    @DisplayName("Нет поля Пароль")
    public void createNoPasswordFail() {

        String json = "{\"login\": \"" + login + "\",\n" +
                "\"firstName\": \"" + name + "\"}";

        Response response = given()
                .header("Content-type", "application/json")
                .and()
                .body(json)
                .post("/api/v1/Courier");

        checkResponseCode(response, 400);
        checkResponseMessage(response, "Недостаточно данных для создания учетной записи");

    }

}
