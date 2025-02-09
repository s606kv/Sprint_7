import api.CourierAPI;
import service.Courier;
import service.ServiceLinks;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.apache.http.HttpStatus.*;
import net.datafaker.Faker;

public class CourierCreatingTest {

    private Courier courier;
    private int courierId;
    private CourierAPI courierAPI= new CourierAPI();
    private Faker faker = new Faker();

    @Before
    public void preconditions () {
        RestAssured.baseURI = ServiceLinks.BASE_URI;

        // Создали json с курьером с рандомными данными
        String login = faker.name().username();
        String password = faker.internet().password(3,10);
        String firstName = faker.name().firstName();
        courier = new Courier (login, password, firstName);
    }

    @Test
    @DisplayName ("Успешное создание курьера со всеми заполненными полями")
    @Description ("Основная позитивная проверка возможности создания нового курьера." +
            "В теле передаются все обязательные поля." +
            "Проверяется статус-код и тело ответа.")
    public void courierCreatingWithAllFieldsTest () {
        // Отправляем запрос и сохраняем его в переменную
        Response responseCreate = courierAPI.postForCourierCreating (courier);

        // Проверили статус-код ответа
        courierAPI.assertStatusCode (responseCreate, SC_CREATED);

        // Получили ID клиента для последующего удаления
        Response responseLogin = courierAPI.postForLogin(courier);
        courierId = courierAPI.getCourierId(responseLogin);

        // Проверили тело ответа на соответствие документации
        String responseBodyKey = "ok";
        String expectedKeyValue = "true";
        courierAPI.assertResponseBody (responseCreate, responseBodyKey, expectedKeyValue);
    }

    @Test
    @DisplayName ("Проверка возможности создания курьера с ранее использованным логином.")
    @Description ("Убеждаемся, что невозможно создать курьера с логином, который уже зарегистрирован в БД.")
    public void impossibleToUseTheSameLoginTest () {
        // Отправляем запрос и сохраняем его в переменную
        Response firstResponse = courierAPI.postForCourierCreating (courier);

        // Убедились, что курьер создан
        courierAPI.assertStatusCode (firstResponse, SC_CREATED);

        // Получили айди курьера для последующего удаления
        Response responseLogin = courierAPI.postForLogin(courier);
        courierId = courierAPI.getCourierId(responseLogin);

        System.out.println("Пробуем создать курьера с тем же логином...");

        // Создали новый объект с json с аналогичным логином
        Courier sameLoginCourier = new Courier("Luigi", "159159159159", "ЛуиджиЛуиджи");

        // Сохранили ответ в переменную
        Response secondResponse = courierAPI.postForCourierCreating (sameLoginCourier);

        // Проверили статус-код ответа на создание дубля курьера
        courierAPI.assertStatusCode (secondResponse, SC_CONFLICT);

        // Проверили тело ответа на соответствие документации
        String responseBodyKey = "message";
        String expectedKeyValue = "Этот логин уже используется";
        courierAPI.assertResponseBody (secondResponse, responseBodyKey, expectedKeyValue);
    }

    @Test
    @DisplayName ("Создание курьера без логина.")
    @Description ("Убеждаемся, что невозможно создать курьера с пустым логином.")
    public void requiredLoginFieldTest () {
        System.out.println("Пробуем создать курьера с пустым логином...");

        // Меняем логин на пустой
        courier.setLogin("");

        // Отправляем запрос и сохраняем его в переменную
        Response responseCreate = courierAPI.postForCourierCreating (courier);

        // Если курьер будет создан, то ему присвоится айди, по которому произойдет удаление
        if (responseCreate.getStatusCode() == SC_CREATED) {
            Response responseLogin = courierAPI.postForLogin(courier);
            courierId = courierAPI.getCourierId(responseLogin);
        }
        // Проверили статус-код ответа на создание курьера без обязательного поля
        courierAPI.assertStatusCode (responseCreate, SC_BAD_REQUEST);

        // Проверили тело ответа на соответствие документации
        String responseBodyKey = "message";
        String expectedKeyValue = "Недостаточно данных для создания учетной записи";
        courierAPI.assertResponseBody (responseCreate, responseBodyKey, expectedKeyValue);
    }

    @Test
    @DisplayName ("Создание курьера без пароля.")
    @Description ("Убеждаемся, что невозможно создать курьера с пустым паролем.")
    public void requiredPasswordFieldTest () {
        System.out.println("Пробуем создать курьера с пустым паролем...");

        // Меняем пароль на пустой
        courier.setPassword("");

        // Отправляем запрос и сохраняем его в переменную
        Response responseCreate = courierAPI.postForCourierCreating (courier);

        // Если курьер вдруг будет создан, то ему присвоится айди, по которому произойдет удаление
        if (responseCreate.getStatusCode() == SC_CREATED) {
            Response responseLogin = courierAPI.postForLogin(courier);
            courierId = courierAPI.getCourierId(responseLogin);
        }

        // Проверили статус-код ответа на создание курьера без обязательного поля
        courierAPI.assertStatusCode (responseCreate, SC_BAD_REQUEST);

        // Проверили тело ответа на соответствие документации
        String responseBodyKey = "message";
        String expectedKeyValue = "Недостаточно данных для создания учетной записи";
        courierAPI.assertResponseBody (responseCreate, responseBodyKey, expectedKeyValue);
    }

    @After
    public void postconditions () {
        // удаляем курьера
        if (courierId != 0) {
            courierAPI.deleteCourier(courier, courierId);
        }
    }
}
