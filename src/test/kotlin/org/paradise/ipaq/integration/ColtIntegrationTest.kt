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

        setupExperianSearchException()

        RestAssured.given()
                .accept(ContentType.JSON)
                .header(Constants.COUNTRY, country)
                .queryParam(PARAMETER_QUERY, query)
                .queryParam(PARAMETER_COUNTRY, country)
                .queryParam(PARAMETER_TAKE, take)
        .`when`()
                .get(SEARCH)
        .then()
                .log().all()
        .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .contentType(ContentType.JSON)
                .body("count", CoreMatchers.equalTo<Int>(take.toInt()))
    }

    // -----------------------------------------------------------------------------------------------------------------
    private fun setupExperianSearchException() {

        // mock Experian Search operation
        httpRequest = HttpRequest.request()
                .withMethod(HttpMethod.GET.name)
                .withPath(SEARCH)
                .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .withHeader(Constants.HTTP_HEADER_AUTH_TOKEN, AUTH_TOKEN)
                .withHeader(Constants.COUNTRY, country)
                .withQueryStringParameter(PARAMETER_QUERY, query)
                .withQueryStringParameter(PARAMETER_COUNTRY, country)
                .withQueryStringParameter(PARAMETER_TAKE, Constants.MAXIMUM_TAKE.toString())

        AbstractIntegrationTest.mockServerClient!!.`when`(httpRequest)
                .respond(HttpResponse.response()
                        .withStatusCode(HttpStatus.SC_OK)
                        .withHeader(Header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                        .withBody(toJsonBody(ClassPathResource("search-resp.json"))))
    }

    companion object {

        private val AUTH_TOKEN = "9b63a00a-a3f7-440f-996f-c07199303587"

        private val SEARCH = "/Search"

        private val PARAMETER_QUERY = "query"
        private val query = "1 Infinite Loop"

        private val PARAMETER_COUNTRY = "country"
        private val country = "USA"

        private val PARAMETER_TAKE = "take"
        private val take = "10"
    }

}