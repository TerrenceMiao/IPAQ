package org.paradise.ipaq.integration;

import au.com.auspost.microservice.App;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Body;
import org.mockserver.model.JsonBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author terrence
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = { App.class, IntegrationConfig.class })
@TestPropertySource("/test.properties")
public abstract class AbstractIntegrationTest {

    public static final int MOCK_SERVER_PORT = 8000;

    public static final String SHIPPING_API_PATH = "/shipping/v1";
    public static final String CSSO_API_PATH = "/cssoapi/v2";
    public static final String USERPREFERENCES_API_PATH = "/userpreferences/v1";
    public static final String FEATURE_TOGGLE_PATH = "/featuretoggles/lodgement";
    public static final String PRESENTATION_METADATA_PATH = "/accessone/v1/metadata/presentation";
    public static final String PICKUPS_METADATA_PATH = "/accessone/v1/metadata/pickups";

    public static final String TOKEN = "ObSSOCookie-token-in-Session-API";
    public static final String BEARER = "I'm a Bearer, not a Token";

    protected static MockServerClient mockServerClient;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    protected MockMvc mock;

    @Value("${local.server.port}")
    private int port;

    @BeforeClass
    public static void beforeClass() {
        mockServerClient = ClientAndServer.startClientAndServer(MOCK_SERVER_PORT);
    }

    @Before
    public void setup() {

        mock = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        RestAssured.port = port;

        RestAssuredMockMvc.mockMvc(mock);
    }

    @After
    public void tearDown() {
        mockServerClient.reset();
    }

    @AfterClass
    public static void afterClass() {
        mockServerClient.stop();
    }

    public static Body toJsonBody(Resource resource) {

        try {
            return new JsonBody(IOUtils.toString(resource.getInputStream(), Charset.defaultCharset()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> T getBean(String id, Class<T> classOfBean) {
        return  webApplicationContext.getBean(id, classOfBean);
    }

    protected <T> T getBean(Class<T> classOfBean) {
        return  webApplicationContext.getBean(classOfBean);
    }

    protected static String asJson(String endpoint) {
        return endpoint.concat(".json");
    }

    protected static String asXml(String endpoint) {
        return endpoint.concat(".xml");
    }

    protected static String sizeOf(String path) {
        return path.concat(".size()");
    }

}