package org.paradise.ipaq.integration

import com.jayway.restassured.RestAssured
import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc
import org.apache.commons.io.IOUtils
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.mockserver.client.server.MockServerClient
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.JsonBody
import org.paradise.ipaq.ColtApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.core.io.Resource
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.io.IOException
import java.nio.charset.Charset

/**
 * @author terrence
 */
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = arrayOf(ColtApplication::class, IntegrationConfig::class))
@TestPropertySource("/test.properties")
abstract class AbstractIntegrationTest {

    @Autowired
    protected var webApplicationContext: WebApplicationContext? = null

    protected var mock: MockMvc? = null

    @Value("\${local.server.port}")
    protected val port: Int = 0


    @Before
    fun setup() {

        mock = MockMvcBuilders.webAppContextSetup(webApplicationContext!!).build()

        RestAssured.port = port

        RestAssuredMockMvc.mockMvc(mock)
    }

    @After
    fun tearDown() {
        mockServerClient!!.reset()
    }

    protected fun <T> getBean(id: String, classOfBean: Class<T>): T {
        return webApplicationContext!!.getBean(id, classOfBean)
    }

    protected fun <T> getBean(classOfBean: Class<T>): T {
        return webApplicationContext!!.getBean(classOfBean)
    }

    companion object {

        val MOCK_SERVER_PORT = 8000

        var mockServerClient: MockServerClient? = null

        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            mockServerClient = ClientAndServer.startClientAndServer(MOCK_SERVER_PORT)
        }

        @AfterClass
        @JvmStatic
        fun afterClass() {
            mockServerClient!!.stop()
        }

        fun toJsonBody(resource: Resource): JsonBody {

            try {
                return JsonBody(IOUtils.toString(resource.inputStream, Charset.defaultCharset()))
            } catch (e: IOException) {
                throw RuntimeException(e)
            }

        }

        protected fun asJson(endpoint: String): String {
            return endpoint + ".json"
        }

        protected fun asXml(endpoint: String): String {
            return endpoint + ".xml"
        }

        protected fun sizeOf(path: String): String {
            return path + ".size()"
        }
    }

}