import API.CourierAPI;
import Service.Courier;
import Service.ServiceLinks;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import net.datafaker.Faker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.assertNotNull;

public class CourierLoginTest {
    private Courier courier;
    private int courierId;
    private CourierAPI courierAPI = new CourierAPI();
    private Faker faker = new Faker();

    @Before
    public void preconditions() {
        RestAssured.baseURI = ServiceLinks.BASE_URI;

        // Создали json с рандомными данными курьера
        String login = faker.name().username();
        String password = faker.internet().password(3,10);
        courier = new Courier (login, password);

        // Создали курьера
        courierAPI.postForCourierCreating(courier);
    }

    @Test
    @DisplayName("Проверка ответа на успешный вход курьера в систему.")
    @Description("Основная позитивная проверка возможности входа в систему. " +
            "В теле передаются все обязательные поля. " +
            "Проверяется статус-код и тело ответа.")
    public void loginTest() {
        // Отправляем запрос и сохраняем его в переменную
        Response response = courierAPI.postForLogin(courier);

        // Проверили статус-код на соответствие ожиданиям
        courierAPI.assertStatusCode(response, SC_OK);

        // Убедились, что в ответе содержится айди курьера
        courierId = courierAPI.getCourierId(response);
        assertNotNull("Ошибка. В ответе отсутствует айди", courierId);

        // Проверили структуру ответа
        String expectedResponseBody = String.format("{id: %d}", courierId);
        courierAPI.assertResponseBody(response, expectedResponseBody);
    }

    @Test
    @DisplayName ("Попытка входа без логина.")
    @Description ("Убеждаемся, что невозможно войти в систему с пустым логином.")
    public void requiredLoginFieldTest () {
        // Отправляем запрос и сохраняем его в переменную
        Response response = courierAPI.postForLogin(courier);

        // Получили айди курьера для последующего удаления
        courierId = courierAPI.getCourierId(response);

        System.out.println("Пытаемся войти в систему без логина...");

        // Очистили логин курьера
        courier.setLogin("");

        // Снова попытались войти
        Response negativeResponse = courierAPI.postForLogin(courier);

        // Проверили статус-код на соответствие ожиданиям
        courierAPI.assertStatusCode(negativeResponse, SC_BAD_REQUEST);

        // Проверили структуру ответа
        String expectedResponseBody = "{\"message\":  \"Недостаточно данных для входа\"}";
        courierAPI.assertResponseBody(negativeResponse, expectedResponseBody);
    }

    @Test
    @DisplayName ("Попытка входа без пароля.")
    @Description ("Убеждаемся, что невозможно войти в систему с пустым паролем.")
    public void requiredPasswordFieldTest () {
        // Отправляем запрос и сохраняем его в переменную
        Response response = courierAPI.postForLogin(courier);

        // Получили айди курьера для последующего удаления
        courierId = courierAPI.getCourierId(response);

        System.out.println("Пытаемся войти в систему без пароля...");

        // Очистили пароль курьера
        courier.setPassword("");

        // Снова попытались войти
        Response negativeResponse = courierAPI.postForLogin(courier);

        // Проверили статус-код на соответствие ожиданиям
        courierAPI.assertStatusCode(negativeResponse, SC_BAD_REQUEST);

        // Проверили структуру ответа
        String expectedResponseBody = "{\"message\":  \"Недостаточно данных для входа\"}";
        courierAPI.assertResponseBody(negativeResponse, expectedResponseBody);
    }

    @Test
    @DisplayName ("Попытка входа с неверным логином.")
    @Description ("Убеждаемся, что невозможно войти в систему с неверным логином.")
    public void wrongLoginFieldTest () {
        // Отправляем запрос и сохраняем его в переменную
        Response response = courierAPI.postForLogin(courier);

        // Получили айди курьера для последующего удаления
        courierId = courierAPI.getCourierId(response);

        System.out.println("Пытаемся снова войти в систему, но с неверным логином...");

        // Поменяли логин курьера
        courier.setLogin("LuigiLuigi");

        // Снова попытались войти
        Response negativeResponse = courierAPI.postForLogin(courier);

        // Проверили статус-код на соответствие ожиданиям
        courierAPI.assertStatusCode(negativeResponse, SC_NOT_FOUND);

        // Проверили структуру ответа
        String expectedResponseBody = "{\"message\": \"Учетная запись не найдена\"}";
        courierAPI.assertResponseBody(negativeResponse, expectedResponseBody);
    }

    @Test
    @DisplayName ("Попытка входа с неверным паролем.")
    @Description ("Убеждаемся, что невозможно войти в систему с неверным паролем.")
    public void wrongPasswordFieldTest () {
        // Отправляем запрос и сохраняем его в переменную
        Response response = courierAPI.postForLogin(courier);

        // Получили айди курьера для последующего удаления
        courierId = courierAPI.getCourierId(response);

        System.out.println("Пытаемся снова войти в систему, но с неверным паролем...");

        // Поменяли пароль
        courier.setPassword("159159159159");

        // Снова попытались войти
        Response negativeResponse = courierAPI.postForLogin(courier);

        // Проверили статус-код на соответствие ожиданиям
        courierAPI.assertStatusCode(negativeResponse, SC_NOT_FOUND);

        // Проверили структуру ответа
        String expectedResponseBody = "{\"message\": \"Учетная запись не найдена\"}";
        courierAPI.assertResponseBody(negativeResponse, expectedResponseBody);
    }

    @After
    public void postconditions() {
        // удаляем курьера
        if (courierId != 0) {
            courierAPI.deleteCourier(courier, courierId);
        }
    }
}
