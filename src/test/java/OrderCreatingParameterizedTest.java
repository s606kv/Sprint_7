import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class OrderCreatingParameterizedTest {
    private OrderInfo orderInfo;
    private int orderTrack;
    private final int expectedStatusCode;
    private final List<String>color;
    private String testName; // Сюда будет записано название теста параметров

    // конструктор для параметризации
    public OrderCreatingParameterizedTest(List<String>color, int expectedStatusCode, String testName) {
        this.color=color;
        this.expectedStatusCode=expectedStatusCode;
        this.testName=testName;
    }

    @Parameterized.Parameters (name="{2}") // Используем 3-й (индекс 2) аргумент в качестве имени теста
    public static Object[][] data () {
        return new Object [][] {
                {List.of("GREY"), 201, "Заказ с одним цветом"},
                {List.of(""), 201, "Заказ без указания цвета"},
                {List.of("BLACK", "GREY"), 201, "Заказ с двумя цветами"}
        };
    }

    @Before
    public void preconditions () {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru";
        // Создали заказ с json
        orderInfo = new OrderInfo("Mario", "Luigi", "Mushroom kingdom",
                4, "81111111111", 5, "01.01.2111",
                "Поиграть хочется, а времени нет, эх...", color);
    }

    @Step ("Создаём заказ и получаем ответ. Ручка /api/v1/orders")
    public Response makeAnOrder (OrderInfo order) {
        System.out.println("Создаём заказ...");

        Response response = given()
                .contentType(ContentType.JSON)
                .and()
                .body(order)
                .when()
                .post("/api/v1/orders");

        // вывод сообщения в зависимости от исхода запроса
        String responseBody = response.then().extract().body().asString();
        int statusCode = response.getStatusCode();
        String info = (statusCode == 201)
                ? String.format("Статус-код: %d. Создан новый заказ.%n", statusCode)
                : String.format("ВНИМАНИЕ. Тело ответа: %s.%nЗаказ не создан. Проверьте тело запроса.%n", responseBody);
        System.out.println(info);

        return response;
    }

    @Step ("Проверяем, что тело ответа содержит трек-номер")
    public void checkResponseBodyIncludesTrack (Response response) {
        System.out.println("Проверяется наличие трек номера в теле ответа...");

        boolean isTrackExist = response.then().extract().body().path("track")!=null;
        if(isTrackExist) {
            System.out.println("В теле ответа содержится трек-номер заказа.\n");
        } else {
            System.out.println("ВНИМАНИЕ. Трек-номер в теле ответа отсутствует.\n");
        }
    }

    @Step ("Получаем трек-номер заказа. Ручка /api/v1/orders")
    public int getOrderTrack (Response response) {
        System.out.println("Получаем трек-номер заказа...");

        int orderTrack = response.then().extract().body().path("track");

        // вывод сообщения в зависимости от исхода запроса
        String responseBody = response.then().extract().body().asString();
        int statusCode = response.getStatusCode();
        String info = (statusCode == 201)
                ? String.format("Статус-код: %d. Заказу присвоен трек-номер: %d.%n", statusCode, orderTrack)
                : String.format("ВНИМАНИЕ. Тело ответа: %s.%nТрек-номер не получен. Проверьте входные параметры.%n", responseBody);
        System.out.println(info);

        return orderTrack;
    }

    @Step ("Вывод на экран информации о заказе. Ручка /api/v1/orders/track")
    public Response printOrderInfo (int orderTrack) {
        System.out.println("Выводится информация о заказе...");

        Response response = given()
                .queryParam("t", orderTrack)
                .get("api/v1/orders/track");

        int statusCode = response.getStatusCode();

        // Создаём объект gson для читаемого вида
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        // Ответ представляет собой мэп, где ключ=строка, а значение может быть любым типом
        Map<String, Object> responseBody = response.then().extract().body().as(Map.class);
        // Упаковываем его в читаемый вид
        String prettyBody = gson.toJson(responseBody);
        // Выводим на экран
        System.out.println(String.format("Статус-код: %d. Информация о заказе:%n%s%n", statusCode, prettyBody));

        return response;
    }

    @Step ("Отмена заказа по его треку. Ручка /api/v1/orders/cancel")
    public void cancelOrder (int orderTrack) {
        System.out.println("Отменяем заказ...");

        Response response = given().
                contentType(ContentType.JSON)
                .queryParam("track", orderTrack)
                .put("/api/v1/orders/cancel");

        // вывод сообщения в зависимости от исхода запроса
        String responseBody = response.then().extract().body().asString();
        int statusCode = response.getStatusCode();
        String info = (statusCode == 200)
                ? String.format("Статус-код: %d. Заказ с трек-номером %d отменён.%n", statusCode, orderTrack)
                : String.format("ВНИМАНИЕ. Тело ответа: %s.%nЗаказ с трек-номером id %d не отменён. Проверьте запрос.%n", responseBody, orderTrack);
        System.out.println(info);
    }

    @Test
    @DisplayName ("Параметризованный тест создания заказа с разными вариантами цвета самоката.")
    @Description ("Позитивная проверка создания заказа со всеми заполненными полями и разными вариантами цвета.")
    public void makeAnOrderTest () {
        // Отправляем запрос и сохраняем его в переменную
        Response response = makeAnOrder(orderInfo);

        // Проверяем соответствие статус-кода
        int actualStatusCode = response.getStatusCode();
        assertEquals("Ошибка. Статус код в ответе не совпадает с ожидаемым.", expectedStatusCode, actualStatusCode);

        // Убеждаемся, что в ответе присутствует трек
        checkResponseBodyIncludesTrack(response);

        // Получаем трек-номер заказа
        orderTrack = getOrderTrack(response);

        // Выводим заказ на экран
        printOrderInfo(orderTrack);
    }

    @After
    public void postconditions () {
        // отменяем заказ;
        if (orderTrack !=0) {
            cancelOrder(orderTrack);
        }
    }
}
