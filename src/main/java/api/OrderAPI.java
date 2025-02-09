package api;

import service.Order;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Map;
import java.util.Objects;
import static service.ServiceLinks.*;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.assertTrue;

public class OrderAPI {

    @Step("Создаём заказ и получаем ответ. Ручка /api/v1/orders")
    public Response makeAnOrder (Order order) {
        System.out.println("Создаём заказ...");

        Response response = given()
                .contentType(ContentType.JSON)
                .and()
                .body(order)
                .when()
                .post(ORDER_CREATE_ENDPOINT);

        // вывод сообщения в зависимости от исхода запроса
        String responseBody = response.getBody().asString();
        int statusCode = response.getStatusCode();
        String info = (statusCode == SC_CREATED)
                ? String.format("Статус-код: %d. Создан новый заказ.%n", statusCode)
                : String.format("⚠\uFE0F ВНИМАНИЕ. Тело ответа: %s.%nЗаказ не создан. Проверьте тело запроса.%n", responseBody);
        System.out.println(info);

        return response;
    }

    @Step ("Проверяем, что тело ответа содержит трек-номер")
    public void checkResponseBodyIncludesTrack (Response response) {
        System.out.println("Проверяется наличие трек номера в теле ответа...");

        boolean isTrackExist = response.then().extract().body().path("track")!=null;

        // Вывод информации о наличии содержимого в поле "track"
        if(isTrackExist) {
            System.out.println("В теле ответа содержится трек-номер заказа.\n");
        } else {
            System.out.println("⚠\uFE0F ВНИМАНИЕ. Трек-номер в теле ответа отсутствует.\n");
        }

        // Проверка
        assertTrue(isTrackExist);

    }

    @Step ("Получаем трек-номер заказа. Ручка /api/v1/orders")
    public int getOrderTrack (Response response) {
        System.out.println("Получаем трек-номер заказа...");

        int orderTrack = response.then().extract().body().path("track");

        // вывод сообщения в зависимости от исхода запроса
        String responseBody = response.getBody().asString();
        int statusCode = response.getStatusCode();
        String info = (statusCode == SC_CREATED)
                ? String.format("Статус-код: %d. Заказу присвоен трек-номер: %d.%n", statusCode, orderTrack)
                : String.format("⚠\uFE0F ВНИМАНИЕ. Тело ответа: %s.%nТрек-номер не получен. Проверьте входные параметры.%n", responseBody);
        System.out.println(info);

        return orderTrack;
    }

    @Step ("Вывод на экран информации о заказе. Ручка /api/v1/orders/track")
    public Response printOrderInfo (int orderTrack) {
        System.out.println("Выводится информация о заказе...");

        Response response = given()
                .queryParam("t", orderTrack)
                .get(ORDER_INFO_ENDPOINT);

        int statusCode = response.getStatusCode();

        // Создаём объект gson для читаемого вида
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        // Ответ представляет собой мэп, где ключ=строка, а значение может быть любым типом
        Map<String, Object> responseBody = response.getBody().as(Map.class);
        // Упаковываем его в читаемый вид
        String prettyBody = gson.toJson(responseBody);
        // Выводим на экран
        System.out.println(String.format("Статус-код: %d. Создан заказ:%n%s%n", statusCode, prettyBody));

        return response;
    }

    @Step ("Получаем список заказов. Ручка /api/v1/orders")
    public Map<String, Objects> getOrderList () {
        System.out.println("Запрашиваем список заказов...");

        Response response = given()
                .contentType(ContentType.JSON)
                .queryParam("limit", 2)
                .get(ORDER_LIST_ENDPOINT);

        int statusCode = response.getStatusCode();
        if (statusCode == SC_OK) {
            System.out.println(String.format("Статус код: %d", statusCode));
        } else {
            System.out.println(String.format("⚠\uFE0F ВНИМАНИЕ. Статус-код: %d", statusCode));
        }

        // Получаем ответ со списком объектов
        Map<String, Objects> orderList = response.getBody().as(Map.class);

        // Вывод информации о наличии непустого тела в ответе
        boolean noEmptyBody = orderList != null;
        if (noEmptyBody) {
            System.out.println("В теле ответа содержится информация.");
        } else {
            System.out.println("Тело ответа пустое.");
        }
        return orderList;
    }

    @Step ("Отмена заказа по его треку. Ручка /api/v1/orders/cancel")
    public void cancelOrder (int orderTrack) {
        System.out.println("Отменяем заказ...");

        Response response = given().
                contentType(ContentType.JSON)
                .queryParam("track", orderTrack)
                .put(ORDER_CANCEL_ENDPOINT);

        // вывод сообщения в зависимости от исхода запроса
        String responseBody = response.getBody().asString();
        int statusCode = response.getStatusCode();
        String info = (statusCode == 200)
                ? String.format("Статус-код: %d. Заказ с трек-номером %d отменён.%n", statusCode, orderTrack)
                : String.format("⚠\uFE0F ВНИМАНИЕ. Тело ответа: %s.%nЗаказ с трек-номером %d не отменён. Проверьте запрос.%n", responseBody, orderTrack);
        System.out.println(info);
    }


}
