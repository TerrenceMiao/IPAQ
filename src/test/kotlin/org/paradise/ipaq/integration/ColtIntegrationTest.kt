package org.paradise.ipaq.integration

import com.jayway.restassured.RestAssured
import com.jayway.restassured.http.ContentType
import org.apache.http.HttpStatus
import org.hamcrest.CoreMatchers
import org.junit.Test
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.paradise.ipaq.Constants
import org.paradise.ipaq.TestData
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType

/**
 * Created by terrence on 30/7/17.
 */
class ColtIntegrationTest : AbstractIntegrationTest() {

    private var httpRequest: HttpRequest? = null

    @Test
    @Throws(Exception::class)
    fun testSearch() {

        val take = 10

        setupExperianSearchException()

        RestAssured.given()
                .accept(ContentType.JSON)
                .header(Constants.COUNTRY, TestData.QUERY_COUNTRY)
                .queryParam(PARAMETER_QUERY, TestData.QUERY_ADDRESS)
                .queryParam(PARAMETER_COUNTRY, TestData.QUERY_COUNTRY)
                .queryParam(PARAMETER_TAKE, take)
        .`when`()
                .get(SEARCH)
        .then()
                .log().all()
        .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .contentType(ContentType.JSON)
                .body("count", CoreMatchers.equalTo<Int>(take))
    }

    @Test
    @Throws(Exception::class)
    fun testFormat() {

        setupExperianFormatException()

        RestAssured.given()
                .accept(ContentType.JSON)
                .header(Constants.COUNTRY, TestData.QUERY_COUNTRY)
                .queryParam(PARAMETER_COUNTRY, TestData.QUERY_COUNTRY)
                .queryParam(PARAMETER_ID, TestData.QUERY_ID)
        .`when`()
                .get(FORMAT)
        .then()
                .log().all()
        .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .contentType(ContentType.JSON)
    }

    @Test
    @Throws(Exception::class)
    fun testHealthCheck() {

        setupHealthCheckExpectation()

        RestAssured.given()
                .accept(ContentType.JSON)
        .`when`()
                .get("/healthCheck")
        .then()
                .log().all()
        .assertThat()
                .statusCode(HttpStatus.SC_OK)
    }

    // -----------------------------------------------------------------------------------------------------------------
    private fun setupExperianSearchException() {

        // mock Experian Search operation
        httpRequest = HttpRequest.request()
                .withMethod(HttpMethod.GET.name)
                .withPath(SEARCH)
                .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .withHeader(Constants.HTTP_HEADER_AUTH_TOKEN, AUTH_TOKEN)
                // Spring Sleuth doesn't invoke on CustomHttpSpanInjector, and doesn't inject HTTP headers
//                .withHeader(Constants.COUNTRY, TestData.QUERY_COUNTRY)
                .withQueryStringParameter(PARAMETER_QUERY, TestData.QUERY_ADDRESS)
                .withQueryStringParameter(PARAMETER_COUNTRY, TestData.QUERY_COUNTRY)
                .withQueryStringParameter(PARAMETER_TAKE, Constants.MAXIMUM_TAKE.toString())

        AbstractIntegrationTest.mockServerClient!!.`when`(httpRequest)
                .respond(HttpResponse.response()
                        .withStatusCode(HttpStatus.SC_OK)
                        .withHeader(Header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                        .withBody(toJsonBody(ClassPathResource("search-resp.json"))))
    }

    private fun setupExperianFormatException() {

        // mock Experian Search operation
        httpRequest = HttpRequest.request()
                .withMethod(HttpMethod.GET.name)
                .withPath(FORMAT)
                .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .withHeader(Constants.HTTP_HEADER_AUTH_TOKEN, AUTH_TOKEN)
                // Spring Sleuth doesn't invoke on CustomHttpSpanInjector, and doesn't inject HTTP headers
//                .withHeader(Constants.COUNTRY, TestData.QUERY_COUNTRY)
                .withQueryStringParameter(PARAMETER_COUNTRY, TestData.QUERY_COUNTRY)
                .withQueryStringParameter(PARAMETER_ID, TestData.QUERY_ID)

        AbstractIntegrationTest.mockServerClient!!.`when`(httpRequest)
                .respond(HttpResponse.response()
                        .withStatusCode(HttpStatus.SC_OK)
                        .withHeader(Header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                        .withBody(toJsonBody(ClassPathResource("format-resp.json"))))
    }

    private fun setupHealthCheckExpectation() {

        httpRequest = HttpRequest.request()
                .withMethod(HttpMethod.GET.name)
                .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .withHeader(Constants.HTTP_HEADER_AUTH_TOKEN, AUTH_TOKEN)
                // Spring Sleuth doesn't invoke on CustomHttpSpanInjector, and doesn't inject HTTP headers
//                .withHeader(Constants.COUNTRY, TestData.QUERY_COUNTRY)
                .withPath("/_admin/health")

        AbstractIntegrationTest.mockServerClient!!.`when`(httpRequest)
                .respond(HttpResponse.response()
                        .withStatusCode(HttpStatus.SC_OK))
    }

    companion object {

        // same Auth Token as defined in "test.properties" file
        private val AUTH_TOKEN = "9b63a00a-a3f7-440f-996f-c07199303587"

        // operations
        private val SEARCH = "/Search"
        private val FORMAT = "/format"

        // parameters
        private val PARAMETER_QUERY = "query"
        private val PARAMETER_COUNTRY = "country"
        private val PARAMETER_TAKE = "take"
        private val PARAMETER_ID = "id"
    }

}