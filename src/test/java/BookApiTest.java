import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.response.Response;
import org.testng.annotations.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;


public class BookApiTest {
    private static int booksize;
    private static List<Integer> createIDs = new ArrayList<>();
    private static int index = 0;
    private static List<Book> books = new ArrayList<>();

    @BeforeClass
    public static void setUp() {
        RestAssured.baseURI = "http://localhost:8081";
        RestAssured.basePath = "/api/books";

        //配置日志，在验证失败时打印
        RestAssured.config = RestAssured.config()
                .logConfig(LogConfig.logConfig()
                        .enableLoggingOfRequestAndResponseIfValidationFails());

        //清空服务器数据
        given().delete().then().statusCode(204);

        System.out.println("=========== Book-API接口测试开始！ =========");
    }

    @AfterClass
    public static void tearDown() {
        System.out.println("========== Book-API接口测试结束！ ==========");
    }

    @BeforeMethod
    public void beforeMethod() {
        index = 0;
    }

    @DataProvider(name = "validbookdata")
    public Object[][] getvalidData() throws Exception {
        List<Object[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("testdata.csv"))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] books = line.split(",");
                if (Integer.parseInt(books[2]) == 200) {
                    data.add(new Object[]{books[0], books[1], Integer.parseInt(books[2])});
                }
            }
        }
        return data.toArray(new Object[0][]);
    }

    @DataProvider(name = "bookdata")
    public Object[][] getinvalidData() throws Exception {
        List<Object[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("testdata.csv"))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] books = line.split(",");
                data.add(new Object[]{books[0], books[1], Integer.parseInt(books[2])});
            }
        }
        return data.toArray(new Object[0][]);
    }

    @DataProvider(name = "update-data")
    public Object[][] getUpdateData() throws Exception {
        List<Object[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("update-books.csv"))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] books = line.split(",");
                data.add(new Object[]{Integer.parseInt(books[0]), books[1], books[2], Integer.parseInt(books[3])});
            }
        }
        return data.toArray(new Object[0][]);
    }

    @DataProvider(name = "delete-data")
    public Object[][] getDeleteData() throws Exception {
        List<Object[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("delete-data.csv"))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] books = line.split(",");
                data.add(new Object[]{books[0], Integer.parseInt(books[1]), books[2]});
            }
        }
        return data.toArray(new Object[0][]);
    }


    @Test(dataProvider = "bookdata", priority = 1)
    public void testCreateBook(String title, String author, int expectedStatusCode) {
        // RestAssured 发送 POST 请求
        Response response = given()
                .log().all()
                .contentType("application/json")
                .body("{\"title\":\"" + title + "\",\"author\":\"" + author + "\"}")
                .when()
                .post()
                .then()
                .statusCode(expectedStatusCode)
                .extract().response();
        if (expectedStatusCode == 200) {
            books.add(new Book(response.jsonPath().getInt("id"), title, author));
            createIDs.add(response.jsonPath().getInt("id"));
            booksize++;
        }

    }

    @Test(priority = 3)
    public void testGetBook() {
        for (Book book : books) {
            given()
                    .log().all()
                    .when()
                    .get("/" + book.getId())
                    .then()
                    .statusCode(200)
                    .body("id", equalTo(book.getId()))
                    .body("title", equalTo(book.getTitle()))
                    .body("author", equalTo(book.getAuthor()));
        }
    }

    @Test(priority = 4, dataProvider = "update-data")
    public void testUpdateBook(int id, String title, String author, int expectedStatusCode) {
        Map<String, String> update = new HashMap<>();
        update.put("title", title);
        update.put("author", author);

        given()
                .log().all()
                .contentType("application/json")
                .body(update)
                .when()
                .put("/" + id)
                .then()
                .statusCode(expectedStatusCode);
        if(expectedStatusCode == 204) {
            given()
                    .log().all()
                    .when()
                    .get("/" + id)
                    .then()
                    .statusCode(200)
                    .body("id", equalTo(id))
                    .body("title", equalTo(title))
                    .body("author", equalTo(author));
        }
    }

    @Test(priority = 5, dataProvider = "delete-data")
    public void testDeleteBook(String id, int expectedStatusCode, String description) {
        given()
                .log().all()
                .when()
                .delete("/" + id)
                .then()
                .statusCode(expectedStatusCode);
        System.out.println(description);
    }

    @Test(priority = 2)
    public void testGetAllBooks() {
        given()
                .log().all()
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("size()", equalTo(booksize));
    }
}
