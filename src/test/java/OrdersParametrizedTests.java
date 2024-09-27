import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pojo.*;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

@RunWith(Parameterized.class)
public class OrdersParametrizedTests {

    private final String firstName;
    private final String lastName;
    private final String address;
    private final int metroStation;
    private final String phone;
    private final int rentTime;
    private final String deliveryDate;
    private final String comment;
    private final String firstColour;
    private final String secondColour;

    public OrdersParametrizedTests(String firstName, String lastName, String address,
                                   int metroStation, String phone, int rentTime,
                                   String deliveryDate, String comment, String firstColour, String secondColour) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.metroStation = metroStation;
        this.phone = phone;
        this.rentTime = rentTime;
        this.deliveryDate = deliveryDate;
        this.comment = comment;
        this.firstColour = firstColour;
        this.secondColour = secondColour;
    }

    @Parameterized.Parameters
    public static Object[][] getOrderData() {
        return new Object[][] {
                { "Ivan", "Ivanov", "red square 1", 1, "+70000000000", 1, "2024-09-30", "aaaa", "BLACK", null },
                { "Ivan", "Ivanov", "red square 1", 2, "80000000000", 2, "2024-09-29", "aaa aaaaa aaa 6", "GREY", null },
                { "Ivan", "Ivanov", "red square 1", 3, "8000000", 3, "2024-09-29", "ffffffff", "BLACK", "GREY" },
                { "Ivan", "Ivanov", "red square 1", 3, "8000000", 3, "2024-09-29", "ffffffff", "GREY", "BLACK" },
                { "Ivan", "Ivanov", "red square 1", 1, "+70000000000", 1, "2024-09-30", "aaaa", null, null },
        };
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/";
    }

    @Test
    public void createOrderSuccess() {

        List<String> colours = new ArrayList<>();
        colours.add(firstColour);
        colours.add(secondColour);

        Order order = new Order(firstName, lastName, address,
        metroStation, phone, rentTime,
        deliveryDate, comment, colours);

        Response response = given()
                .header("Content-type", "application/json")
                .body(order)
                .post("/api/v1/orders");

        OrderResponse orderResponse = response
                .body()
                .as(OrderResponse.class);

        response.then().assertThat()
                .statusCode(201);
        Assert.assertNotNull("Курьер не создан", orderResponse.getTrack());

    }
}
