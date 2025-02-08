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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.apache.http.HttpStatus.*;

@RunWith(Parameterized.class)
public class OrderCreatingParameterizedTest {
    private Order order;
    private int orderTrack;
    private final int expectedStatusCode;
    private final List<String>color;
    private String testName; // Сюда будет записано название теста параметров
    private OrderAPI orderAPI = new OrderAPI();
    private Faker faker = new Faker(new Locale("ru"));

    // конструктор для параметризации
    public OrderCreatingParameterizedTest(List<String>color, int expectedStatusCode, String testName) {
        this.color=color;
        this.expectedStatusCode=expectedStatusCode;
        this.testName=testName;
    }

    // параметры
    @Parameterized.Parameters (name="{2}") // Используем 3-й (индекс 2) аргумент в качестве имени теста
    public static Object[][] data () {
        return new Object [][] {
                {List.of("GREY"), SC_CREATED, "Заказ с цветом GRAY"},
                {List.of("BLACK"), SC_CREATED, "Заказ с цветом BLACK"},
                {List.of(""), SC_CREATED, "Заказ без указания цвета"},
                {List.of("BLACK", "GREY"), SC_CREATED, "Заказ с двумя цветами"}
        };
    }

    @Before
    public void preconditions () {
        RestAssured.baseURI = ServiceLinks.BASE_URI;
        // Создали заказ с json
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String address = faker.address().fullAddress();
        int metroStation = faker.number().numberBetween(1, 20);
        String phone = faker.phoneNumber().phoneNumber();
        int rentTime = faker.number().numberBetween(1, 7);
        String deliveryDate = LocalDate.now().plusDays(7).toString(); // я не понял, как это сделать через faker
        String comment = faker.lorem().sentence(3);
        order = new Order(firstName, lastName, address, metroStation, phone, rentTime, deliveryDate, comment, color);
    }

    @Test
    @DisplayName ("Параметризованный тест создания заказа с разными вариантами цвета самоката.")
    @Description ("Позитивная проверка создания заказа со всеми заполненными полями и разными вариантами цвета.")
    public void makeAnOrderTest () {
        // Отправляем запрос и сохраняем его в переменную
        Response response = orderAPI.makeAnOrder(order);

        // Проверяем соответствие статус-кода
        int actualStatusCode = response.getStatusCode();
        assertEquals("Ошибка. Статус код в ответе не совпадает с ожидаемым.", expectedStatusCode, actualStatusCode);

        // Убеждаемся, что в ответе присутствует трек
        orderAPI.checkResponseBodyIncludesTrack(response);

        // Получаем трек-номер заказа
        orderTrack = orderAPI.getOrderTrack(response);

        // Выводим заказ на экран
        orderAPI.printOrderInfo(orderTrack);
    }

    @After
    public void postconditions () {
        // отменяем заказ;
        if (orderTrack !=0) {
            orderAPI.cancelOrder(orderTrack);
        }
    }
}
