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
import static org.junit.Assert.assertNotNull;

public class CourierLoginTest {
    private CourierData courier;
    private int courierId;

    @Before
    public void preconditions() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru/";
        // Создали json с курьером
        courier = new CourierData("Luigi", "159159");
        // Создали курьера в системе
        postForCourierCreating(courier);
    }

    @Step("Создание нового курьера. Ручка api/v1/courier")
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

    @Step("Получение ответа на POST запрос входа курьера в систему. Ручка api/v1/courier/login")
    public Response postForLoginToTheSystem(CourierData courier) {
        System.out.println("Выполняется логин курьера в систему...");
        Response response = given()
                .contentType(ContentType.JSON)
                .and()
                .body(courier)
                .when()
                .post("api/v1/courier/login");

        // вывод сообщения в зависимости от исхода запроса
        String responseBody = response.then().extract().body().asString();
        int statusCode = response.getStatusCode();
        String info = (statusCode == 200)
                ? String.format("Статус-код: %d. Успешный вход в систему.%n", statusCode)
                : String.format("ВНИМАНИЕ. Тело ответа: %s.%nВход не выполнен. Проверьте тело запроса.%n", responseBody);
        System.out.println(info);

        return response;
    }

    @Step("Получение ID курьера. Ручка api/v1/courier/login")
    public int getCourierId(Response response) {
        System.out.println("Получаем Id курьера...");

        int courierId = response.then().extract().body().path("id");

        // вывод сообщения в зависимости от исхода запроса
        String responseBody = response.then().extract().body().asString();
        int statusCode = response.getStatusCode();
        String info = (statusCode == 200)
                ? String.format("Статус-код: %d. Курьеру присвоен Id: %d.%n", statusCode, courierId)
                : String.format("ВНИМАНИЕ. Тело ответа: %s.%nId курьеру не присвоен, проверьте входные параметры.%n", responseBody);
        System.out.println(info);

        return courierId;
    }

    @Step("Проверка статус-кода на POST запрос входа в систему.")
    public void checkStatusCode(Response response, int expectedStatusCode) {
        System.out.println("Проверяется статус-код ответа на логин в системе...");
        int actualStatusCode = response.getStatusCode();
        System.out.println(String.format("ОР: %d%nФР: %d", expectedStatusCode, actualStatusCode));

        if (actualStatusCode==expectedStatusCode) {
            System.out.println("Статус-коды совпали.\n");
        } else {
            System.out.println("ВНИМАНИЕ. Статус-коды не совпали.\n");
        }

        assertEquals("Ошибка. Статус-коды не совпали.", expectedStatusCode, actualStatusCode);
    }

    @Step("Проверка тела ответа на POST запрос входа в систему.")
    public void checkPostResponseBody(Response response, String expectedResponse) {
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

    @Step("Удаление курьера из системы. Ручка /api/v1/courier/:id")
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
    @DisplayName("Проверка ответа на успешный вход курьера в систему.")
    @Description("Основная позитивная проверка возможности входа в систему. " +
            "В теле передаются все обязательные поля. " +
            "Проверяется статус-код и тело ответа.")
    public void loginTest() {
        // Отправляем запрос и сохраняем его в переменную
        Response response = postForLoginToTheSystem(courier);

        // Проверили статус-код на соответствие ожиданиям
        checkStatusCode(response, 200);

        // Убедились, что в ответе содержится айди курьера
        courierId = getCourierId(response);
        assertNotNull("Ошибка. В ответе отсутствует айди", courierId);

        // Проверили структуру ответа
        String expectedResponseBody = String.format("{id: %d}", courierId);
        checkPostResponseBody(response, expectedResponseBody);
    }

    @Test
    @DisplayName ("Попытка входа без логина.")
    @Description ("Убеждаемся, что невозможно войти в систему с пустым логином.")
    public void requiredLoginFieldTest () {
        // Отправляем запрос и сохраняем его в переменную
        Response response = postForLoginToTheSystem(courier);

        // Получили айди курьера для последующего удаления
        courierId = getCourierId(response);

        System.out.println("Пытаемся войти в систему без логина...");

        // Очистили логин курьера
        courier.setLogin("");

        // Снова попытались войти
        Response negativeResponse = postForLoginToTheSystem(courier);

        // Проверили статус-код на соответствие ожиданиям
        checkStatusCode(negativeResponse, 400);

        // Проверили структуру ответа
        String expectedResponseBody = "{\"message\":  \"Недостаточно данных для входа\"}";
        checkPostResponseBody(negativeResponse, expectedResponseBody);
    }

    @Test
    @DisplayName ("Попытка входа без пароля.")
    @Description ("Убеждаемся, что невозможно войти в систему с пустым паролем.")
    public void requiredPasswordFieldTest () {
        // Отправляем запрос и сохраняем его в переменную
        Response response = postForLoginToTheSystem(courier);

        // Получили айди курьера для последующего удаления
        courierId = getCourierId(response);

        System.out.println("Пытаемся войти в систему без пароля...");

        // Очистили пароль курьера
        courier.setPassword("");

        // Снова попытались войти
        Response negativeResponse = postForLoginToTheSystem(courier);

        // Проверили статус-код на соответствие ожиданиям
        checkStatusCode(negativeResponse, 400);

        // Проверили структуру ответа
        String expectedResponseBody = "{\"message\":  \"Недостаточно данных для входа\"}";
        checkPostResponseBody(negativeResponse, expectedResponseBody);
    }

    @Test
    @DisplayName ("Попытка входа с неверным логином.")
    @Description ("Убеждаемся, что невозможно войти в систему с неверным логином.")
    public void wrongLoginFieldTest () {
        // Отправляем запрос и сохраняем его в переменную
        Response response = postForLoginToTheSystem(courier);

        // Получили айди курьера для последующего удаления
        courierId = getCourierId(response);

        System.out.println("Пытаемся снова войти в систему, но с неверным логином...");

        // Поменяли логин курьера
        courier.setLogin("LuigiLuigi");

        // Снова попытались войти
        Response negativeResponse = postForLoginToTheSystem(courier);

        // Проверили статус-код на соответствие ожиданиям
        checkStatusCode(negativeResponse, 404);

        // Проверили структуру ответа
        String expectedResponseBody = "{\"message\": \"Учетная запись не найдена\"}";
        checkPostResponseBody(negativeResponse, expectedResponseBody);
    }

    @Test
    @DisplayName ("Попытка входа с неверным паролем.")
    @Description ("Убеждаемся, что невозможно войти в систему с неверным паролем.")
    public void wrongPasswordFieldTest () {
        // Отправляем запрос и сохраняем его в переменную
        Response response = postForLoginToTheSystem(courier);

        // Получили айди курьера для последующего удаления
        courierId = getCourierId(response);

        System.out.println("Пытаемся снова войти в систему, но с неверным паролем...");

        // Поменяли пароль
        courier.setPassword("159159159159");

        // Снова попытались войти
        Response negativeResponse = postForLoginToTheSystem(courier);

        // Проверили статус-код на соответствие ожиданиям
        checkStatusCode(negativeResponse, 404);

        // Проверили структуру ответа
        String expectedResponseBody = "{\"message\": \"Учетная запись не найдена\"}";
        checkPostResponseBody(negativeResponse, expectedResponseBody);
    }

    @After
    public void postconditions() {
        // удаляем курьера
        if (courierId != 0) {
            deleteCourier(courier, courierId);
        }
    }
}
