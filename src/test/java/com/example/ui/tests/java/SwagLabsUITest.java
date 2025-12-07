package com.example.ui.tests.java;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.*;

@Epic("UI Tests")
@Feature("Swag Labs Login and Cart")
public class SwagLabsUITest {

    private WebDriver driver;

    // Константы для данных логина
    private static final String STANDARD_USER = "standard_user";
    private static final String PASSWORD = "secret_sauce";
    private static final String LOGIN_URL = "https://www.saucedemo.com/";
    private static final String PRODUCTS_PAGE_TITLE = "Swag Labs"; // Заголовок вкладки/страницы, где находятся продукты
    private static final String PRODUCTS_HEADER_TEXT = "Products"; // Текст заголовка на странице продуктов


    @BeforeAll
    static void setupClass() {
        // Используем WebDriverManager для настройки драйвера Chrome
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setUp() {
        // Создаем экземпляр ChromeOptions для настройки браузера (по желанию)
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless"); // Раскомментируйте, если хотите запускать без GUI
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        // Создаем экземпляр драйвера
        this.driver = new ChromeDriver(options);
        // Устанавливаем неявное ожидание (необязательно, но полезно)
        // driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @AfterEach
    void tearDown() {
        // Закрываем браузер и завершаем сессию драйвера после каждого теста
        if (this.driver != null) {
            this.driver.quit();
        }
    }

    /**
     * Тест 1: Успешный логин
     * Ввести логин standard_user, пароль secret_sauce. Нажать кнопку Login.
     * Проверить, что отображается заголовок "Products".
     */
    @Test
    @Description("Выполняет успешный вход в систему с корректными учетными данными и проверяет наличие заголовка 'Products'.")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Login")
    public void testSuccessfulLogin() {
        driver.get(LOGIN_URL);

        WebElement usernameInput = driver.findElement(By.id("user-name"));
        WebElement passwordInput = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameInput.sendKeys(STANDARD_USER);
        passwordInput.sendKeys(PASSWORD);
        loginButton.click();

        // Проверка, что мы на нужной странице - проверяем заголовок вкладки или текст на странице
        // Заголовок вкладки обычно "Swag Labs"
        assertEquals(PRODUCTS_PAGE_TITLE, driver.getTitle(), "Заголовок вкладки после логина не соответствует ожидаемому.");

        // Проверка текста заголовка "Products" на странице
        WebElement productsHeader = driver.findElement(By.className("title")); // Заголовок h3 с классом 'title'
        assertTrue(productsHeader.isDisplayed(), "Элемент заголовка 'Products' не отображается.");
        assertEquals(PRODUCTS_HEADER_TEXT, productsHeader.getText(), "Текст заголовка 'Products' не соответствует ожидаемому.");
    }

    /**
     * Тест 2: Добавление товара в корзину
     * После логина, нажать кнопку "Add to cart" у первого товара.
     * Проверить, что иконка корзины показывает "1", а кнопка у товара сменилась на "Remove".
     */
    @Test
    @Description("Добавляет первый товар в корзину и проверяет, что счетчик корзины увеличился и кнопка изменилась на 'Remove'.")
    @Severity(SeverityLevel.NORMAL)
    @Story("Add to Cart")
    public void testAddItemToCart() {
        driver.get(LOGIN_URL);

        // Логин
        WebElement usernameInput = driver.findElement(By.id("user-name"));
        WebElement passwordInput = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameInput.sendKeys(STANDARD_USER);
        passwordInput.sendKeys(PASSWORD);
        loginButton.click();

        // Находим первый элемент "Add to cart"
        // Кнопки имеют класс 'btn_inventory', могут быть разными (primary или secondary)
        // Ищем первую кнопку с текстом "Add to cart"
        WebElement addToCartButton = driver.findElement(By.xpath("//button[text()='Add to cart']"));

        // Нажимаем кнопку
        addToCartButton.click();

        // Проверка 1: Изменилась ли кнопка на "Remove"
        // Ожидаем, что элемент теперь будет иметь текст "Remove"
        WebElement removeButton = driver.findElement(By.xpath("//button[text()='Remove']")); // Новый элемент с текстом "Remove"
        assertTrue(removeButton.isDisplayed(), "Кнопка 'Remove' не отображается после добавления товара.");
        assertEquals("Remove", removeButton.getText(), "Текст кнопки не изменился на 'Remove'.");

        // Проверка 2: Изменился ли счетчик корзины
        WebElement cartBadge = driver.findElement(By.className("shopping_cart_badge")); // Элемент с классом 'shopping_cart_badge'
        assertTrue(cartBadge.isDisplayed(), "Элемент счетчика корзины (badge) не отображается.");
        assertEquals("1", cartBadge.getText(), "Счетчик корзины не равен 1.");
    }
}