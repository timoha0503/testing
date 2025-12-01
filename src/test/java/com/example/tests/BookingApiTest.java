package com.example.tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Позволяет использовать @BeforeAll без static
public class BookingApiTest {

    private String bookingId;
    private String authToken;

    // --- Данные для теста ---
    // Хранение данных в переменных
    private final String testFirstname = "Никита";
    private final String testLastname = "Гоман";
    private final int testTotalprice = 42;
    private final boolean testDepositpaid = true;
    private final String testCheckin = "2025-10-29";
    private final String testCheckout = "2025-10-31";
    private final String testAdditionalneeds = "Скакалка и музыка Короля и шута";

    // Данные для аутентификации (берутся из переменных окружения)
    // Переменные окружения перед запуском теста:
    // RESTFUL_BOOKER_USERNAME=admin
    // RESTFUL_BOOKER_PASSWORD=password123
    private final String username = System.getenv("RESTFUL_BOOKER_USERNAME");
    private final String password = System.getenv("RESTFUL_BOOKER_PASSWORD");

    // --- Вспомогательные методы ---

    // Метод для получения токена
    private String getAuthToken(String user, String pass) {
        if (user == inull || pass == null) {
            throw new IllegalStateException("Username or Password environment variable is not set.");
        }
        String authPayload = String.format("""
            {
                "username": "%s",
                "password": "%s"
            }
            """, user, pass);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(authPayload)
                .when()
                .post("/auth");

        return response
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    // Метод для формирования тела запроса на создание бронирования
    private String createBookingPayload(String firstname, String lastname, int totalprice, boolean depositpaid,
                                        String checkin, String checkout, String additionalneeds) {
        return String.format("""
            {
                "firstname" : "%s",
                "lastname" : "%s",
                "totalprice" : %d,
                "depositpaid" : %b,
                "bookingdates" : {
                    "checkin" : "%s",
                    "checkout" : "%s"
                },
                "additionalneeds" : "%s"
            }
            """, firstname, lastname, totalprice, depositpaid, checkin, checkout, additionalneeds);
    }

    // --- Методы теста ---

    @BeforeAll
    public void setUp() {
        // Устанавливаем базовый URL для всех запросов
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";

        // Получаем токен аутентификации
        this.authToken = getAuthToken(this.username, this.password);
    }

    // Тест 1: Создание бронирования (POST /booking)
    @Test
    public void testCreateBooking() {
        String bookingPayload = createBookingPayload(
                this.testFirstname,
                this.testLastname,
                this.testTotalprice,
                this.testDepositpaid,
                this.testCheckin,
                this.testCheckout,
                this.testAdditionalneeds
        );

        this.bookingId = given()
                .contentType(ContentType.JSON)
                .body(bookingPayload)
                .when()
                .post("/booking")
                .then()
                .statusCode(200)
                .body("bookingid", notNullValue()) // Проверяем, что bookingid есть в ответе
                .extract()
                .path("bookingid").toString(); // Сохраняем ID для дальнейшего использования

        System.out.println("Created Booking ID: " + this.bookingId);
    }

    // Тест 2: Полное обновление бронирования (PUT /booking/{id})
    // Зависит от testCreateBooking
    @Test
    public void testUpdateBooking() {
        // Убедимся, что бронирование создано
        if (this.bookingId == null) {
            testCreateBooking(); // Создаем бронь, если она не создана в рамках этого запуска
        }

        // Новые данные для обновления
        String updatedFirstname = "ОбновленныйНикита";
        String updatedLastname = "ОбновленныйГоман";
        int updatedTotalprice = 500;
        boolean updatedDepositpaid = false;
        String updatedCheckin = "2025-11-01";
        String updatedCheckout = "2025-11-10";
        String updatedAdditionalneeds = "Ужин";

        String updatedBookingPayload = createBookingPayload(
                updatedFirstname,
                updatedLastname,
                updatedTotalprice,
                updatedDepositpaid,
                updatedCheckin,
                updatedCheckout,
                updatedAdditionalneeds
        );

        given()
                .contentType(ContentType.JSON)
                .header("Cookie", "token=" + this.authToken) // Передаем токен в заголовке Cookie
                .body(updatedBookingPayload)
                .when()
                .put("/booking/" + this.bookingId) // Используем полученный ID
                .then()
                .statusCode(200) // Проверяем, что статус-код 200
                .body("firstname", equalTo(updatedFirstname))
                .body("lastname", equalTo(updatedLastname));
    }

    // Тест 3: Частичное обновление бронирования (PATCH /booking/{id})
    // Зависит от testCreateBooking
    @Test
    public void testPartialUpdateBooking() {
        // Убедимся, что бронирование создано
        if (this.bookingId == null) {
            testCreateBooking(); // Создаем бронь, если она не создана в рамках этого запуска
        }

        // Данные для частичного обновления (только имя и цена)
        String partialUpdatePayload = """
            {
                "firstname" : "PatchedНикита",
                "totalprice" : 999
            }
            """;

        given()
                .contentType(ContentType.JSON)
                .header("Cookie", "token=" + this.authToken)
                .body(partialUpdatePayload)
                .when()
                .patch("/booking/" + this.bookingId)
                .then()
                .statusCode(200)
                .body("firstname", equalTo("PatchedНикита")) // Проверяем, что имя изменилось
                .body("lastname", equalTo(this.testLastname)); // Проверяем, что фамилия осталась старой из начального создания
    }
}