package API;

import Service.Courier;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import static Service.ServiceLinks.*;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.assertEquals;

public class CourierAPI {

    @Step("Получение ответа на POST запрос создания курьера. Ручка api/v1/courier")
    public Response postForCourierCreating(Courier courier) {
        System.out.println("Создаётся новый курьер...");

        Response response =
                given()
                        .contentType(ContentType.JSON)
                        .and()
                        .body(courier)
                        .when()
                        .post(COURIER_CREATE_ENDPOINT);

        // вывод сообщения в зависимости от исхода запроса
        String responseBody = response.then().extract().body().asString();
        int statusCode = response.getStatusCode();
        String info = (statusCode == SC_CREATED)
                ? String.format("Статус-код: %d. Создан новый курьер.%n", statusCode)
                : String.format("--> ВНИМАНИЕ. Тело ответа: %s.%nКурьер не создан. Проверьте тело запроса.%n", responseBody);
        System.out.println(info);

        return response;
    }

    @Step("Получение ответа на POST запрос логина курьера в систему. Ручка api/v1/courier/login")
    public Response postForLogin (Courier courier) {
        System.out.println("Выполняется логин курьера в систему...");
        Response response = given()
                .contentType(ContentType.JSON)
                .and()
                .body(courier)
                .when()
                .post(COURIER_LOGIN_ENDPOINT);

        // вывод сообщения в зависимости от исхода запроса
        String responseBody = response.then().extract().body().asString();
        int statusCode = response.getStatusCode();
        String info = (statusCode == SC_OK)
                ? String.format("Статус-код: %d. Успешный вход в систему.%n", statusCode)
                : String.format("--> ВНИМАНИЕ. Тело ответа: %s.%nВход не выполнен. Проверьте тело запроса.%n", responseBody);
        System.out.println(info);

        return response;
    }

    @Step("Получение ID курьера. Ручка api/v1/courier/login")
    public int getCourierId(Response response) {
        System.out.println("Попытка входа курьера в систему...");

        int courierId = response.then().extract().body().path("id");

        // вывод сообщения в зависимости от исхода запроса
        String responseBody = response.then().extract().body().asString();
        int statusCode = response.getStatusCode();
        String info = (statusCode == SC_OK)
                ? String.format("Статус-код: %d. Выполнен вход в систему курьера c ID: %d.%n", statusCode, courierId)
                : String.format("--> ВНИМАНИЕ. Тело ответа: %s.%nВход в систему курьером не выполнен.%n", responseBody);
        System.out.println(info);

        return courierId;
    }

    @Step ("Проверка статус-кода на POST запрос.")
    public void assertStatusCode(Response response, int expectedStatusCode) {
        System.out.println("Проверяется статус-код ответа...");
        int actualStatusCode = response.getStatusCode();
        System.out.println(String.format("ОР: %d%nФР: %d", expectedStatusCode, actualStatusCode));

        if (actualStatusCode==expectedStatusCode) {
            System.out.println("Статус-коды совпали.\n");
        } else {
            System.out.println("--> ВНИМАНИЕ. Статус-коды не совпали.\n");
        }

        assertEquals("Ошибка. Статус-коды не совпали.", expectedStatusCode, actualStatusCode);
    }

    @Step ("Проверка тела ответа на POST запрос.")
    public void assertResponseBody(Response response, String expectedResponse) {
        System.out.println("Проверяется тело ответа...");
        String actualResponse = response.then().extract().body().asString();
        System.out.println(String.format("ОР: %s%nФР: %s", expectedResponse, actualResponse));

        if (actualResponse.equals(expectedResponse)) {
            System.out.println("Тела ответов совпадали.\n");
        } else {
            System.out.println("--> ВНИМАНИЕ. Тела ответов не совпали.\n");
        }

        assertEquals("Ошибка. Тела ответов не совпали.", expectedResponse, actualResponse);
    }

    @Step ("Удаление курьера из системы. Ручка /api/v1/courier/:id")
    public void deleteCourier(Courier courier, int courierId) {
        System.out.println("Удаляем курьера из БД...");

        Response response = given()
                .contentType(ContentType.JSON)
                .and()
                .body(courier)
                .when()
                .delete(String.format(COURIER_DELETE_ENDPOINT, courierId));

        // вывод сообщения в зависимости от исхода запроса
        String responseBody = response.then().extract().body().asString();
        int statusCode = response.getStatusCode();
        String info = (statusCode == SC_OK)
                ? String.format("Статус-код: %d. Курьер с id %d удалён.%n", statusCode, courierId)
                : String.format("--> ВНИМАНИЕ. Тело ответа: %s.%nКурьер с id %d не удалён.%n", responseBody, courierId);
        System.out.println(info);
    }

}
