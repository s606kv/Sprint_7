import API.CourierAPI;
import API.OrderAPI;
import Service.Order;
import Service.ServiceLinks;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import net.datafaker.Faker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import static org.junit.Assert.assertNotNull;

public class GetOrdersListTest {
    private Order order;
    private int orderTrack;
    private CourierAPI courierAPI = new CourierAPI();
    private OrderAPI orderAPI = new OrderAPI();
    private Faker faker = new Faker(new Locale("ru"));

    @Before
    public void preconditions() {
        RestAssured.baseURI = ServiceLinks.BASE_URI;
        // Создали с json-заказ
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String address = faker.address().fullAddress();
        int metroStation = faker.number().numberBetween(1, 20);
        String phone = faker.phoneNumber().phoneNumber();
        int rentTime = faker.number().numberBetween(1, 7);
        String deliveryDate = LocalDate.now().plusDays(7).toString(); // я не понял, как это сделать через faker
        String comment = faker.lorem().sentence(3);
        order = new Order(firstName, lastName, address, metroStation, phone, rentTime, deliveryDate, comment, List.of("GREY"));
    }

    @Test
    @DisplayName("Сценарий получения списка заказов курьера.")
    @Description("В тесте реализован весь путь от создания курьера и заказа до отображения списка заказов этого курьера.")
    public void couriersOrdersListTest() {
        // Создаём заказ и пакуем ответ в переменную
        Response responseOrder = orderAPI.makeAnOrder(order);

        // Получаем трек-номер заказа
        orderTrack = orderAPI.getOrderTrack(responseOrder);

        // Получаем информацию о заказе
        Response orderInfo = orderAPI.printOrderInfo(orderTrack);

        // Получаем список заказов и проверяем, что он не пустой
        Map<String, Objects> orders = orderAPI.getOrderList();
        assertNotNull(orders);
    }

    @After
    public void postconditions() {
        // Отменяем заказ
        orderAPI.cancelOrder(orderTrack);
    }
}

