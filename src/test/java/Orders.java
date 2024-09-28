import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class Orders {

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/";
    }

    @Test
    @DisplayName("Получение списка заказов без параметров")
    public void getOrdersSuccess() {

        given()
                .get("/api/v1/orders")
                .then().statusCode(200).assertThat().body("orders", notNullValue());

    }
}
