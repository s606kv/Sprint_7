package Service;

public class ServiceLinks {
    public static final String BASE_URI = "http://qa-scooter.praktikum-services.ru/";

    public static final String COURIER_CREATE_ENDPOINT = "api/v1/courier";
    public static final String COURIER_LOGIN_ENDPOINT = "api/v1/courier/login";
    public static final String COURIER_DELETE_ENDPOINT = "/api/v1/courier/%d";

    public static final String ORDER_CREATE_ENDPOINT = "api/v1/orders";
    public static final String ORDER_INFO_ENDPOINT = "api/v1/orders/track";
    public static final String ORDER_LIST_ENDPOINT = "api/v1/orders";
    public static final String ORDER_CANCEL_ENDPOINT = "api/v1/orders/cancel";
}
