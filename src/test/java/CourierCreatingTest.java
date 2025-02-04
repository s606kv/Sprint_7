import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

public class CourierCreatingTest {

    private CourierData courier;
    private int courierId;

    @Before
    public void preconditions () {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru/";
        // Создали json с курьером
        courier = new CourierData("Luigi", "159159", "Луиджи");
    }

    @Step ("Получение ответа на POST запрос создания курьера. Ручка api/v1/courier")
    public Response postForCourierCreating (CourierData courier) {
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
                : String.format("ВНИМАНИЕ. Тело ответа: %s.%nКурьер не создан. Проверьте тело запроса.%n", responseBody);
        System.out.println(info);

        return response;
    }

    @Step ("Логин курьера в системе и получение его ID. Ручка api/v1/courier/login")
    public int getCourierId (CourierData courier) {
        System.out.println("Попытка входа курьера в систему...");

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
                ? String.format("Статус-код: %d. Выполнен вход в систему курьера c ID: %d.%n", statusCode, courierId)
                : String.format("ВНИМАНИЕ. Тело ответа: %s.%nВход в систему курьером не выполнен.%n", responseBody);
        System.out.println(info);

        return courierId;
    }

    @Step ("Проверка статус-кода на POST запрос создания курьера.")
    public void checkStatusCode (Response response, int expectedStatusCode) {
        System.out.println("Проверяется статус-код запроса на создание курьера...");
        int actualStatusCode = response.getStatusCode();
        System.out.println(String.format("ОР: %d%nФР: %d", expectedStatusCode, actualStatusCode));

        if (actualStatusCode==expectedStatusCode) {
            System.out.println("Статус-коды совпали.\n");
        } else {
            System.out.println("ВНИМАНИЕ. Статус-коды не совпали.\n");
        }

        assertEquals("Ошибка. Статус-коды не совпали.", expectedStatusCode, actualStatusCode);
    }

    @Step ("Проверка тела ответа на POST запрос создания курьера.")
    public void checkPostResponseBody (Response response, String expectedResponse) {
        System.out.println("Проверяется тело ответа...");
        String actualResponse = response.then().extract().body().asString();
        System.out.println(String.format("ОР: %s%nФР: %s", expectedResponse, actualResponse));

        if (actualResponse.equals(expectedResponse)) {
            System.out.println("Тела ответов совпадали.\n");
        } else {
            System.out.println("ВНИМАНИЕ. Тела ответов не совпали.\n");
        }

        assertEquals("Ошибка. Тела ответов не совпали.", expectedResponse, actualResponse);
    }

    @Step ("Удаление курьера из системы. Ручка /api/v1/courier/:id")
    public void deleteCourier (CourierData courier, int courierId) {
        System.out.println("Удаляем курьера из БД...");

        Response response = given()
                .contentType(ContentType.JSON)
                .and()
                .body(courier)
                .when()
                .delete(String.format("/api/v1/courier/%d", courierId));

        // вывод сообщения в зависимости от исхода запроса
        String responseBody = response.then().extract().body().asString();
        int statusCode = response.getStatusCode();
        String info = (statusCode == 200)
                ? String.format("Статус-код: %d. Курьер с id %d удалён.%n", statusCode, courierId)
                : String.format("ВНИМАНИЕ. Тело ответа: %s.%nКурьер с id %d не удалён.%n", responseBody, courierId);
        System.out.println(info);
    }

    @Test
    @DisplayName ("Успешное создание курьера со всеми заполненными полями")
    @Description ("Основная позитивная проверка возможности создания нового курьера." +
            "В теле передаются все обязательные поля." +
            "Проверяется статус-код и тело ответа.")
    public void courierCreatingWithAllFieldsTest () {
        // Отправляем запрос и сохраняем его в переменную
        Response response = postForCourierCreating (courier);

        // Проверили статус-код ответа
        checkStatusCode (response, 201);

        // Получили ID клиента для последующего удаления
        courierId = getCourierId(courier);

        // Проверили тело ответа на соответствие документации
        String expectedResponse = "{\"ok\": true}";
        checkPostResponseBody (response, expectedResponse);
    }

    @Test
    @DisplayName ("Проверка возможности создания курьера с ранее использованным логином.")
    @Description ("Убеждаемся, что невозможно создать курьера с логином, который уже зарегистрирован в БД.")
    public void impossibleToUseTheSameLoginTest () {
        // Отправляем запрос и сохраняем его в переменную
        Response firstResponse = postForCourierCreating (courier);

        // Убедились, что курьер создан
        checkStatusCode (firstResponse, 201);

        // Получили айди курьера для последующего удаления
        courierId = getCourierId(courier);

        System.out.println("Пробуем создать курьера с тем же логином...");

        // Создали новый объект с json с аналогичным логином
        CourierData sameLoginCourier = new CourierData("Luigi", "159159159159", "ЛуиджиЛуиджи");

        // Сохранили ответ в переменную
        Response secondResponse = postForCourierCreating (sameLoginCourier);

        // Проверили статус-код ответа на создание дубля курьера
        checkStatusCode (secondResponse, 409);

        // Проверили тело ответа на соответствие документации
        String expectedResponse = "{\"message\": \"Этот логин уже используется\"}";
        checkPostResponseBody (secondResponse, expectedResponse);
    }

    @Test
    @DisplayName ("Создание курьера без логина.")
    @Description ("Убеждаемся, что невозможно создать курьера с пустым логином.")
    public void requiredLoginFieldTest () {
        System.out.println("Пробуем создать курьера с пустым логином...");

        // Меняем логин на пустой
        courier.setLogin("");

        // Отправляем запрос и сохраняем его в переменную
        Response response = postForCourierCreating (courier);

        // Если курьер будет создан, то ему присвоится айди, по которому произойдет удаление
        if (response.getStatusCode()==201) {
            courierId = getCourierId(courier);
        }
        // Проверили статус-код ответа на создание курьера без обязательного поля
        checkStatusCode (response, 400);

        // Проверили тело ответа на соответствие документации
        String expectedResponse = "{\"message\": \"Недостаточно данных для создания учетной записи\"}";
        checkPostResponseBody (response, expectedResponse);
    }

    @Test
    @DisplayName ("Создание курьера без пароля.")
    @Description ("Убеждаемся, что невозможно создать курьера с пустым паролем.")
    public void requiredPasswordFieldTest () {
        System.out.println("Пробуем создать курьера с пустым паролем...");

        // Меняем пароль на пустой
        courier.setPassword("");

        // Отправляем запрос и сохраняем его в переменную
        Response response = postForCourierCreating (courier);

        // Если курьер вдруг будет создан, то ему присвоится айди, по которому произойдет удаление
        if (response.getStatusCode()==201) {
            courierId = getCourierId(courier);
        }

        // Проверили статус-код ответа на создание курьера без обязательного поля
        checkStatusCode (response, 400);

        // Проверили тело ответа на соответствие документации
        String expectedResponse = "{\"message\": \"Недостаточно данных для создания учетной записи\"}";
        checkPostResponseBody (response, expectedResponse);
    }

    @After
    public void postconditions () {
        // удаляем курьера
        if (courierId != 0) {
            deleteCourier(courier, courierId);
        }
    }
}
