import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.example.courier.Courier;
import org.example.courier.CourierClient;
import org.example.courier.CourierCredentials;
import org.example.courier.CourierGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.example.courier.Constants.BASE_URL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;


public class CreateCourierTest {
    private Courier courier;
    private Courier similarCourier;
    private CourierClient courierClient;
    private Courier someCourier;
    private Courier courierWithoutLogin;
    private Courier courierWithoutPassword;
    private int idCourier;


    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URL;
        courier = CourierGenerator.getDefault();
        similarCourier = CourierGenerator.getRegisteredCourier();
        someCourier = CourierGenerator.getRandomCourier();
        courierWithoutLogin = CourierGenerator.getWithoutLogin();
        courierWithoutPassword = CourierGenerator.getWithoutPassword();
        courierClient = new CourierClient();
    }

    @After
    public void tearDown() {
        courierClient.deleteCourier(idCourier);
    }

    @Test
    @DisplayName("Создать курьера")
    @Description("успешный запрос возвращает ok: true")
    public void createCourierIsPossibleTest() {
        // создаем курьера
        ValidatableResponse response = courierClient.create(courier);
        int statusCode = response.extract().statusCode();
        assertThat("Курьер авторизовался", statusCode, equalTo(201));
        //успешный запрос возвращает ok: true
        response.assertThat().body("ok", equalTo(true));
        //получаем id курьера, который потом удаляется
        int  idCourier = courierClient.loginUserGetID(CourierCredentials.from(courier));
        courierClient.deleteCourier(idCourier);
    }

    @Test
    @DisplayName("Создать одинаковых курьеров")
    public void createCourierAlreadyUsedTest() {
        // создаем курьера
        courierClient.create(similarCourier);
        ValidatableResponse response = courierClient.create(similarCourier);
        //запрос возвращает код ответа
        int codeResponse = response.extract().statusCode();
        // проверяем статус код
        assertEquals("Статус код  соответсвует 409", SC_CONFLICT, codeResponse);
        //"нельзя создать двух одинаковых курьеров"
        response.assertThat().body("message", notNullValue());
        //удаляем id
        int  idCourier = courierClient.loginUserGetID(CourierCredentials.from(similarCourier));
        courierClient.deleteCourier(idCourier);


    }
    @Test
    @DisplayName("Создать курьера с одинаковым логином")
    @Description("Eсли создать пользователя с логином, который уже есть, возвращается ошибка")
    public void createCourierWithDuplicatedLoginTest(){
        // создаем курьера
        courierClient.create(similarCourier);
        String firstLogin = similarCourier.getLogin();
        //создаем курьера с зарегистрированным логином
        someCourier.setLogin(firstLogin);
        ValidatableResponse response = courierClient.create(someCourier);
        //запрос возвращает код ответа
        int codeResponse = response.extract().statusCode();
        // проверяем статус код
        assertEquals("Статус код  соответсвует 409", SC_CONFLICT, codeResponse);
        // ответ соответствует Body Message
        String message = response.extract().path("message");
        assertEquals("Нельзя создать курьера с одинаковым логином",message,"Этот логин уже используется. Попробуйте другой.");
    }

    @Test
    @DisplayName("Создать курьера без логина")
    @Description("Чтобы создать курьера, нужно передать в ручку все обязательные поля; 
                 если одного из полей нет, запрос возвращает ошибку;")
    public void createCourierWithoutLoginTest(){
        // создаем курьера без логина
        ValidatableResponse response = courierClient.create(courierWithoutLogin);
        //запрос возвращает код ответа
        int codeResponse = response.extract().statusCode();
        // проверяем статус код
        assertEquals("Статус код  соответсвует 400", SC_BAD_REQUEST,codeResponse);
        // значение не равно null
        response.assertThat().body("message", notNullValue());
    }

    @Test
    @DisplayName("Создать курьера без пароля")
    @Description("если одного из полей нет, запрос возвращает ошибку")
    public void createCourierWithoutPasswordTest(){
        // создаем курьера без пароля
        ValidatableResponse response = courierClient.create(courierWithoutPassword);
        //запрос возвращает код ответа
        int codeResponse = response.extract().statusCode();
        // проверяем статус код
        assertEquals("Статус код  соответсвует 400", SC_BAD_REQUEST,codeResponse);
        // ответ соответствует Body Message
        String message = response.extract().path("message");
        assertEquals("Не все поля заполнены",message,"Недостаточно данных для создания учетной записи");
    }


}
