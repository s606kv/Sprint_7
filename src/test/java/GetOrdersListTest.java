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

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.restassured.RestAssured.given;

public class GetOrdersListTest {
    private CourierData courier;
    private int courierId;
    private OrderInfo orderInfo;

    @Before
    public void preconditions () {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru/";
        // Создали json-курьера
        courier = new CourierData("Luigi606", "159159", "Луиджи");
        // Создали с json-заказ
        orderInfo = new OrderInfo("Mario", "Luigi", "Mushroom kingdom",
                4, "81111111111", 5, "01.01.2111",
                "До звонка будильника осталось 4 часа...", List.of("GREY"));
    }

    @Step("Получение ответа на POST запрос создания курьера. Ручка api/v1/courier")
    public void postForCourierCreating (CourierData courier) {
        System.out.println("Создаётся новый курьер...");

        Response response =
                given()
                        .contentType(ContentType.JSON)
                        .and()
                        .body(courier)
                        .when()
                        .post("api/v1/courier");

        // вывод сообщения в зависимости от исхода запроса
        String responseBody = response.then().extract().body().asString();
        int statusCode = response.getStatusCode();
        String info = (statusCode == 201)
                ? String.format("Статус-код: %d. Создан новый курьер.%n", statusCode)
                : String.format("ВНИМАНИЕ. Тело ответа: %s.%nКурьер не создан. Проверьте запрос.%n", responseBody);
        System.out.println(info);
    }

    @Step ("Логин курьера в системе и получение его id. Ручка api/v1/courier/login")
    public int getCourierId (CourierData courier) {
        System.out.println("Курьер логинится в системе, чтобы получить свой айди...");

        Response response = given()
                .contentType(ContentType.JSON)
                .and()
                .body(courier)
                .when()
                .post("api/v1/courier/login");

        int courierId = response.then().extract().body().path("id");

        // вывод сообщения в зависимости от исхода запроса
        String responseBody = response.then().extract().body().asString();
        int statusCode = response.getStatusCode();
        String info = (statusCode == 200)
                ? String.format("Статус-код: %d. Выполнен вход в систему. Курьеру присвоен id: %d.%n", statusCode, courierId)
                : String.format("ВНИМАНИЕ. Тело ответа: %s.%nВход в систему курьером не выполнен.%n", responseBody);
        System.out.println(info);

        return courierId;
    }

    @Step ("Удаление курьера из системы. Ручка /api/v1/courier/:id")
    public void deleteCourier (CourierData courier, int courierId) {
        System.out.println("Удаляем курьера из БД...");

        Response response = given()
                .contentType(ContentType.JSON)
                .and()
                .body(courier)
                .when()
                .delete(String.format("api/v1/courier/%d", courierId));

        // вывод сообщения в зависимости от исхода запроса
        String responseBody = response.then().extract().body().asString();
        int statusCode = response.getStatusCode();
        String info = (statusCode == 200)
                ? String.format("Статус-код: %d. Курьер с id %d удалён.%n", statusCode, courierId)
                : String.format("ВНИМАНИЕ. Тело ответа: %s.%nКурьер с id %d не удалён.%n", responseBody, courierId);
        System.out.println(info);
    }

    @Step ("Создаём заказ и получаем ответ. Ручка /api/v1/orders")
    public Response makeAnOrder (OrderInfo order) {
        System.out.println("Создаём заказ...");

        Response response = given()
                .contentType(ContentType.JSON)
                .and()
                .body(order)
                .when()
                .post("api/v1/orders");

        // вывод сообщения в зависимости от исхода запроса
        String responseBody = response.then().extract().body().asString();
        int statusCode = response.getStatusCode();
        String info = (statusCode == 201)
                ? String.format("Статус-код: %d. Создан новый заказ.%n", statusCode)
                : String.format("ВНИМАНИЕ. Тело ответа: %s.%nЗаказ не создан. Проверьте тело запроса.%n", responseBody);
        System.out.println(info);

        return response;
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

    @Step ("Получение id заказа по его трек-номеру. Ручка /api/v1/orders/track")
    public int getOrderId (Response response, int orderTrack) {
        System.out.println("Получаем id заказа по его трек-номеру...");

        int orderId = response.then().extract().body().path("order.id");
        int statusCode = response.getStatusCode();

        String info = (orderId!=0)
                ? String.format("Статус-код: %d. Заказу присвоен id: %d.%n", statusCode, orderId)
                : String.format("ВНИМАНИЕ. Статус-код: %d. Не удалось получить id заказа. Проверьте запрос.%n", statusCode);
        System.out.println(info);

        return orderId;
    }

    @Step ("Курьер принимает заказ по его id. Ручка /api/v1/orders/accept/:id")
    public void acceptTheOrder (int courierId, int orderId) {
        System.out.println(String.format("Курьер с id %d принимает заказ с id %d...", courierId, orderId));

        Response response = given()
                .contentType(ContentType.JSON)
                .and()
                .queryParam("courierId", courierId)
                .put(String.format("api/v1/orders/accept/%d", orderId));

        int statusCode = response.getStatusCode();
        String responseBody = response.then().extract().body().asString();

        String info = (statusCode==200)
                ? String.format("Статус-код: %d. Курьер принял заказ.%n", statusCode)
                : String.format("ВНИМАНИЕ. Тело ответа: %s. Курьер не принял заказ.%n", responseBody);
        System.out.println(info);
    }

    @Step ("Завершаем заказ. Ручка /api/v1/orders/finish/:id")
    public void finishOrder (int orderId) {
        System.out.println("Завершаем заказ...");

        Response response = given().
                contentType(ContentType.JSON)
                .put(String.format("api/v1/orders/finish/%d", orderId));

        // вывод сообщения в зависимости от исхода запроса
        String responseBody = response.then().extract().body().asString();
        int statusCode = response.getStatusCode();
        String info = (statusCode == 200)
                ? String.format("Статус-код: %d. Заказ с id %d завершен.%n", statusCode, orderId)
                : String.format("ВНИМАНИЕ. Тело ответа: %s.%nЗаказ с id %d не завершен. Проверьте запрос.%n", responseBody, orderId);
        System.out.println(info);
    }

    @Step ("Получаем список заказов курьера. Ручка /api/v1/orders")
    public void getCouriersOrderList (int courierId) {
        System.out.println(String.format("Получаем список заказов курьера с id %d...", courierId));

        Response response = given()
                .contentType(ContentType.JSON)
                .queryParam("courierId", courierId)
                .get("api/v1/orders");

        int statusCode = response.getStatusCode();
        if (statusCode!=200) {
            System.out.println(String.format("ВНИМАНИЕ. Статус-код: %d", statusCode));
        }

        // Получаем тело-ответа со списком объектов
        Map<String, Objects> responseBody = response.then().extract().body().as(Map.class);
        // Создаём объект gson для читаемого вида
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        // И упаковываем его в читаемый вид
        String orderList = gson.toJson(responseBody);
        // Печатаем и ложимся спатеньки (нет)
        System.out.println(String.format("Статус код: %d. Список заказов:%n%s%n", statusCode, orderList));
    }

    @Test
    @DisplayName("Сценарий получения списка заказов курьера.")
    @Description("В тесте реализован весь путь от создания курьера и заказа до отображения списка заказов этого курьера.")
    public void couriersOrdersListTest() {
        // Создаём курьера
        postForCourierCreating(courier);

        // Получаем айди курьера для удаления
        courierId = getCourierId(courier);

        // Создаём заказ и пакуем ответ в переменную
        Response responseOrder = makeAnOrder(orderInfo);

        // Получаем трек-номер заказа
        int orderTrack = getOrderTrack(responseOrder);

        // Получаем инфу о заказе
        Response orderInfo = printOrderInfo(orderTrack);

        // Получаем id заказа
        int orderId = getOrderId(orderInfo, orderTrack);

        // Курьер с айди принимает заказ с айди
        acceptTheOrder(courierId, orderId);

        // Заканчиваем заказ, дабы не висели открытыми в БД
        finishOrder(orderId);

        // Получаем список заказов курьера
        getCouriersOrderList(courierId);
    }

    @After
    public void postconditions () {
        // удаляем курьера
        if (courierId != 0) {
            deleteCourier(courier, courierId);
        }
    }
}

