package org.paradise.ipaq.integration

import au.com.auspost.microservice.Constants
import au.com.auspost.microservice.service.redis.RedisSessionStore
import au.com.auspost.signing.aws.KmsEncryption
import com.jayway.restassured.http.ContentType
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.http.HttpStatus
import org.junit.Test
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest
import org.mockserver.model.StringBody
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType

import java.nio.charset.Charset
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

import com.jayway.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.isEmptyOrNullString
import org.hamcrest.Matchers.isEmptyString
import org.mockito.Matchers.eq
import org.mockito.Mockito.`when`
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response

/**
 * An integration test that collects all the units of the country api to ensure that they collaborate correctly.
 */
class SessionIntegrationTest : AbstractIntegrationTest() {

    private val cNumber = "C00004854247"

    private val obSSOCookieString = "ObSSOCookie=\"%s\"; Version=1; Domain=.auspost.com.au; Path=/; Secure; HttpOnly"

    private val obSSOCookieKey = Constants.HTTP_HEADERS_OBSSOCOOKIE_KEY + "=" + cNumber

    private val body = "{\"username\": \"jazz2.jarbarker@spamgourmet.com\", \"password\": \"Welcome123\"}"

    private val status = "{\"status\":\"UNAUTHENTICATED\"}"

    private val environment = "4"

    private var httpRequest: HttpRequest? = null

    private val clock = Clock.systemDefaultZone()

    @Autowired
    private val mockKmsEncryption: KmsEncryption? = null

    // createSession ---------------------------------------------------------------------------------------------------
    @Test
    @Throws(Exception::class)
    fun shouldCreateSession() {

        setupCSSOAPICreateSessionExpectation(TOKEN)
        setupCSSOAPIGetCustomerBasicExpectation(TOKEN)
        setupFeatureTogglesExpectation(environment)
        setupUserPreferencesAPIExpectation(TOKEN, environment)
        setupAccessOneAPIPresentationExpectation(environment)
        setupAccessOneAPIPickupsExpectation(environment)
        setupShippingAPIOrganisationsExpectation(TOKEN, environment)

        val currentTime = LocalDateTime.now(clock).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header(Constants.HTTP_HEADERS_X_ENV, environment)
                .body(body)
                .`when`()
                .post("/")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.SET_COOKIE, String.format(obSSOCookieString, TOKEN))
                .header(HttpHeaders.AUTHORIZATION, String.format(Constants.HTTP_HEADERS_SESSION_TOKEN_FORMAT, TOKEN))
                .body("username", equalTo<T>("Courtney Tarttelin"))
                .body("expiry", greaterThanOrEqualTo<T>(currentTime))
                .body("token", isEmptyOrNullString())
                .body("customer.first_name", equalTo<T>("Courtney"))
                .body("customer.surname", equalTo<T>("Tarttelin"))
                .body("customer.email", equalTo<T>("jazz2.jarbarker@spamgourmet.com"))
                .body("customer.customer_number", equalTo<T>("C00004854247"))
                .body("customer.apcn", equalTo<T>("1004826888"))
                .body("feature_toggles[0].id", equalTo<T>("addressBookImport"))
                .body("feature_toggles[0].active", equalTo<T>(java.lang.Boolean.TRUE))
                .body("user_preferences[0].preferenceType", equalTo<T>("EBAYIMPORT"))
                .body("user_preferences[1].preferenceType", equalTo<T>("TESTPREF"))
                .body("domestic_presentation_metadata.packaging.packaging_groups[1].id", equalTo<T>("BOX"))
                .body("domestic_presentation_metadata.packaging.packaging_groups[1].display_name", equalTo<T>("Australia Post flat rate boxes"))
                .body("domestic_presentation_metadata.packaging.packaging_types[0].id", equalTo<T>("PKOWN"))
                .body("domestic_presentation_metadata.packaging.packaging_types[0].display_name", equalTo<T>("My own packaging"))
                .body("domestic_presentation_metadata.services[0].id", equalTo<T>("DOMREG"))
                .body("domestic_presentation_metadata.services[0].display_name", equalTo<T>("Parcel Post"))
                .body("domestic_presentation_metadata.services[0].products[0].id", equalTo<T>("B30"))
                .body("domestic_presentation_metadata.services[0].products[0].display_name", equalTo<T>("Postage - Own packaging"))
                .body("domestic_presentation_metadata.services[0].features[1].@type", equalTo<T>("feature"))
                .body("domestic_presentation_metadata.services[0].features[1].id", equalTo<T>("TRANSIT_COVER"))
                .body("domestic_presentation_metadata.features[0].@type", equalTo<T>("threshold-mandatory"))
                .body("domestic_presentation_metadata.features[0].id", equalTo<T>("SIGNATURE_ON_DELIVERY"))
                .body("domestic_presentation_metadata.features[0].display_name", equalTo<T>("Signature on delivery"))
                .body("domestic_presentation_metadata.features[0].trigger_name", equalTo<T>("TRANSIT_COVER"))
                .body("domestic_presentation_metadata.features[1].@type", equalTo<T>("bounded-numeric"))
                .body("domestic_presentation_metadata.features[1].id", equalTo<T>("TRANSIT_COVER"))
                .body("domestic_presentation_metadata.features[1].maximum", equalTo<T>(5000))
                .body("domestic_presentation_metadata.features[1].type", equalTo<T>("currency"))
                .body("pickups[0].id", equalTo<T>("SBD"))
                .body("pickups[0].display_name", equalTo<T>("Same business day"))
                .body("pickups[0].product_ids[0]", equalTo<T>("PU1"))
                .body("organisations[0].organisation_id", equalTo<T>("1004826888"))
                .body("organisations[0].type", equalTo<T>("NCC"))
                .body("organisations[0].status", equalTo<T>("ACTIVE"))
                .body("organisations[0].email", equalTo<T>("jazz2.jarbarker@spamgourmet.com"))
                .body("organisations[0].accounts[0].account_number", equalTo<T>("a4769e096eae4ba2b852a3faf249eb6d"))
                .body("organisations[0].accounts[0].expired", equalTo<T>(java.lang.Boolean.FALSE))
                .body("organisations[0].accounts[0].credit_blocked", equalTo<T>(java.lang.Boolean.FALSE))
                .body("organisations[0].accounts[0].addresses[0].type", equalTo<T>("MERCHANT_LOCATION"))
                .body("organisations[0].accounts[0].details.lodgement_postcode", equalTo<T>(StringUtils.EMPTY))
                .body("authorisation.actions[0].id", equalTo<T>("1"))
                .body("authorisation.actions[0].predicate", equalTo<T>("view"))
                .body("authorisation.actions[0].target", equalTo<T>("/dispatch-apps$environment/shipments/"))
                .body("authorisation.actions[0].label", equalTo<T>("Shipments"))
                .body("authorisation.actions[4].id", equalTo<T>("5"))
                .body("authorisation.actions[4].predicate", equalTo<T>("create"))
                .body("authorisation.actions[4].target", equalTo<T>("/dispatch-apps$environment/upsertshipments/"))
                .body("authorisation.actions[4].label", equalTo<T>("Add shipment"))
                .body("authorisation.permissions[0].context", equalTo<T>("348294733939"))
                .body("authorisation.permissions[0].actions[0]", equalTo<T>("1"))
                .body("authorisation.permissions[0].actions[4]", equalTo<T>("5"))
    }

    @Test
    @Throws(Exception::class)
    fun shouldCreateSessionWithoutEnvironmentSetting() {

        setupCSSOAPICreateSessionExpectation(TOKEN)
        setupCSSOAPIGetCustomerBasicExpectation(TOKEN)
        setupFeatureTogglesExpectation(StringUtils.EMPTY)
        setupUserPreferencesAPIExpectation(TOKEN, StringUtils.EMPTY)
        setupAccessOneAPIPresentationExpectation(StringUtils.EMPTY)
        setupAccessOneAPIPickupsExpectation(StringUtils.EMPTY)
        setupShippingAPIOrganisationsExpectation(TOKEN, StringUtils.EMPTY)

        val currentTime = LocalDateTime.now(clock).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(body)
                .`when`()
                .post("/")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.SET_COOKIE, String.format(obSSOCookieString, TOKEN))
                .header(HttpHeaders.AUTHORIZATION, String.format(Constants.HTTP_HEADERS_SESSION_TOKEN_FORMAT, TOKEN))
                .body("username", equalTo<T>("Courtney Tarttelin"))
                .body("expiry", greaterThanOrEqualTo<T>(currentTime))
                .body("token", isEmptyOrNullString())
                .body("customer.first_name", equalTo<T>("Courtney"))
                .body("customer.surname", equalTo<T>("Tarttelin"))
                .body("customer.email", equalTo<T>("jazz2.jarbarker@spamgourmet.com"))
                .body("customer.customer_number", equalTo<T>("C00004854247"))
                .body("customer.apcn", equalTo<T>("1004826888"))
                .body("feature_toggles[0].id", equalTo<T>("addressBookImport"))
                .body("feature_toggles[0].active", equalTo<T>(java.lang.Boolean.TRUE))
                .body("user_preferences[0].preferenceType", equalTo<T>("EBAYIMPORT"))
                .body("user_preferences[1].preferenceType", equalTo<T>("TESTPREF"))
                .body("domestic_presentation_metadata.packaging.packaging_groups[1].id", equalTo<T>("BOX"))
                .body("domestic_presentation_metadata.packaging.packaging_groups[1].display_name", equalTo<T>("Australia Post flat rate boxes"))
                .body("domestic_presentation_metadata.packaging.packaging_types[0].id", equalTo<T>("PKOWN"))
                .body("domestic_presentation_metadata.packaging.packaging_types[0].display_name", equalTo<T>("My own packaging"))
                .body("domestic_presentation_metadata.services[0].id", equalTo<T>("DOMREG"))
                .body("domestic_presentation_metadata.services[0].display_name", equalTo<T>("Parcel Post"))
                .body("domestic_presentation_metadata.services[0].products[0].id", equalTo<T>("B30"))
                .body("domestic_presentation_metadata.services[0].products[0].display_name", equalTo<T>("Postage - Own packaging"))
                .body("domestic_presentation_metadata.services[0].features[1].@type", equalTo<T>("feature"))
                .body("domestic_presentation_metadata.services[0].features[1].id", equalTo<T>("TRANSIT_COVER"))
                .body("domestic_presentation_metadata.features[0].@type", equalTo<T>("threshold-mandatory"))
                .body("domestic_presentation_metadata.features[0].id", equalTo<T>("SIGNATURE_ON_DELIVERY"))
                .body("domestic_presentation_metadata.features[0].display_name", equalTo<T>("Signature on delivery"))
                .body("domestic_presentation_metadata.features[0].trigger_name", equalTo<T>("TRANSIT_COVER"))
                .body("domestic_presentation_metadata.features[1].@type", equalTo<T>("bounded-numeric"))
                .body("domestic_presentation_metadata.features[1].id", equalTo<T>("TRANSIT_COVER"))
                .body("domestic_presentation_metadata.features[1].maximum", equalTo<T>(5000))
                .body("domestic_presentation_metadata.features[1].type", equalTo<T>("currency"))
                .body("pickups[0].id", equalTo<T>("SBD"))
                .body("pickups[0].display_name", equalTo<T>("Same business day"))
                .body("pickups[0].product_ids[0]", equalTo<T>("PU1"))
                .body("organisations[0].organisation_id", equalTo<T>("1004826888"))
                .body("organisations[0].type", equalTo<T>("NCC"))
                .body("organisations[0].status", equalTo<T>("ACTIVE"))
                .body("organisations[0].email", equalTo<T>("jazz2.jarbarker@spamgourmet.com"))
                .body("organisations[0].accounts[0].account_number", equalTo<T>("a4769e096eae4ba2b852a3faf249eb6d"))
                .body("organisations[0].accounts[0].expired", equalTo<T>(java.lang.Boolean.FALSE))
                .body("organisations[0].accounts[0].credit_blocked", equalTo<T>(java.lang.Boolean.FALSE))
                .body("organisations[0].accounts[0].addresses[0].type", equalTo<T>("MERCHANT_LOCATION"))
                .body("organisations[0].accounts[0].details.lodgement_postcode", equalTo<T>(StringUtils.EMPTY))
                .body("authorisation.actions[0].id", equalTo<T>("1"))
                .body("authorisation.actions[0].predicate", equalTo<T>("view"))
                .body("authorisation.actions[0].target", equalTo<T>("/dispatch-apps/shipments/"))
                .body("authorisation.actions[0].label", equalTo<T>("Shipments"))
                .body("authorisation.actions[4].id", equalTo<T>("5"))
                .body("authorisation.actions[4].predicate", equalTo<T>("create"))
                .body("authorisation.actions[4].target", equalTo<T>("/dispatch-apps/upsertshipments/"))
                .body("authorisation.actions[4].label", equalTo<T>("Add shipment"))
                .body("authorisation.permissions[0].context", equalTo<T>("348294733939"))
                .body("authorisation.permissions[0].actions[0]", equalTo<T>("1"))
                .body("authorisation.permissions[0].actions[4]", equalTo<T>("5"))
    }

    @Test
    @Throws(Exception::class)
    fun shouldFailedToCreateSessionIfBadRequest() {

        // mock AccessOne API
        httpRequest = request()
                .withMethod(HttpMethod.POST.name)
                .withPath(CSSO_API_PATH + "/session")
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .withBody(toJsonBody(ClassPathResource("create-session-req.json")))

        AbstractIntegrationTest.mockServerClient.`when`(httpRequest)
                .respond(response()
                        .withStatusCode(HttpStatus.SC_BAD_REQUEST)
                        .withHeader(Header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                        .withBody(StringBody(status)))

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(body)
                .`when`()
                .post("/")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .header(HttpHeaders.SET_COOKIE, isEmptyOrNullString())
                .header(HttpHeaders.AUTHORIZATION, isEmptyOrNullString())
                .body("[0].code", equalTo<T>(org.springframework.http.HttpStatus.BAD_REQUEST.reasonPhrase))
                .body("[0].detail", equalTo<T>(status))
    }

    @Test
    @Throws(Exception::class)
    fun shouldFailedToCreateSessionIfUnauthorized() {

        // mock AccessOne API
        httpRequest = request()
                .withMethod(HttpMethod.POST.name)
                .withPath(CSSO_API_PATH + "/session")
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .withBody(toJsonBody(ClassPathResource("create-session-req.json")))

        AbstractIntegrationTest.mockServerClient.`when`(httpRequest)
                .respond(response()
                        .withStatusCode(HttpStatus.SC_UNAUTHORIZED)
                        .withHeader(Header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                        .withBody(StringBody(status)))

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(body)
                .`when`()
                .post("/")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .header(HttpHeaders.SET_COOKIE, isEmptyOrNullString())
                .header(HttpHeaders.AUTHORIZATION, isEmptyOrNullString())
                .body("[0].code", equalTo<T>(org.springframework.http.HttpStatus.UNAUTHORIZED.reasonPhrase))
                .body("[0].detail", equalTo<T>(status))
    }

    @Test
    @Throws(Exception::class)
    fun shouldFailedToCreateSessionIfNotFound() {

        // mock AccessOne API
        httpRequest = request()
                .withMethod(HttpMethod.POST.name)
                .withPath(CSSO_API_PATH + "/session")
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .withBody(toJsonBody(ClassPathResource("create-session-req.json")))

        AbstractIntegrationTest.mockServerClient.`when`(httpRequest)
                .respond(response()
                        .withStatusCode(HttpStatus.SC_NOT_FOUND)
                        .withHeader(Header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                        .withBody(StringBody(status)))

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(body)
                .`when`()
                .post("/")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .header(HttpHeaders.SET_COOKIE, isEmptyOrNullString())
                .header(HttpHeaders.AUTHORIZATION, isEmptyOrNullString())
                .body("[0].code", equalTo<T>(org.springframework.http.HttpStatus.NOT_FOUND.reasonPhrase))
                .body("[0].detail", equalTo<T>(status))
    }

    @Test
    @Throws(Exception::class)
    fun shouldFailedToCreateSessionIfInternalServiceError() {

        setupCSSOAPICreateSessionExpectation(TOKEN)

        httpRequest = request()
                .withMethod(HttpMethod.GET.name)
                .withPath(CSSO_API_PATH + "/customer/basic")
                .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .withHeader(Constants.HTTP_HEADERS_AP_APP_ID, Constants.APP_CSSO_NAME)
                .withHeader(HttpHeaders.COOKIE, Constants.HTTP_HEADERS_OBSSOCOOKIE + "=" + "\"" + TOKEN + "\";")

        AbstractIntegrationTest.mockServerClient.`when`(httpRequest)
                .respond(response()
                        .withStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                        .withHeader(Header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                        .withBody(StringBody(status)))

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header(Constants.HTTP_HEADERS_X_ENV, environment)
                .body(body)
                .`when`()
                .post("/")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.SC_SERVICE_UNAVAILABLE)
                .header(HttpHeaders.SET_COOKIE, isEmptyOrNullString())
                .header(HttpHeaders.AUTHORIZATION, isEmptyOrNullString())
                .body("[0].code", equalTo<T>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase))
                .body("[0].detail", equalTo<T>(status))
    }

    @Test
    @Throws(Exception::class)
    fun shouldThrowValidationErrorsWhileMissingPassword() {

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body("{\"username\": \"jazz2.jarbarker@spamgourmet.com\"}")
                .`when`()
                .post("/")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.SET_COOKIE, isEmptyOrNullString())
                .header(HttpHeaders.AUTHORIZATION, isEmptyOrNullString())
                .body("errors[0].code", equalTo<T>("NOT_NULL_CONSTRAINT_VIOLATION"))
                .body("errors[0].detail", equalTo<T>("The field password must be provided"))
                .body("errors[0].source.parameter", equalTo<T>("password"))
    }

    @Test
    @Throws(Exception::class)
    fun shouldThrowValidationErrorsWhileMissingUsername() {

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body("{\"password\": \"Welcome123\"}")
                .`when`()
                .post("/")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.SET_COOKIE, isEmptyOrNullString())
                .header(HttpHeaders.AUTHORIZATION, isEmptyOrNullString())
                .body("errors[0].code", equalTo<T>("NOT_NULL_CONSTRAINT_VIOLATION"))
                .body("errors[0].detail", equalTo<T>("The field username must be provided"))
                .body("errors[0].source.parameter", equalTo<T>("username"))
    }

    @Test
    @Throws(Exception::class)
    fun shouldFailedIfPersistSessionFailed() {

        val failedToken = "Token causes failure"

        setupCSSOAPICreateSessionExpectation(failedToken)
        setupCSSOAPIGetCustomerBasicExpectation(failedToken)
        setupFeatureTogglesExpectation(environment)
        setupUserPreferencesAPIExpectation(failedToken, environment)
        setupAccessOneAPIPresentationExpectation(environment)
        setupAccessOneAPIPickupsExpectation(environment)
        setupShippingAPIOrganisationsExpectation(failedToken, environment)

        given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header(Constants.HTTP_HEADERS_X_ENV, environment)
                .body(body)
                .`when`()
                .post("/")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.SC_SERVICE_UNAVAILABLE)
                .contentType(ContentType.JSON)
                .body("[0].code", equalTo<T>(Constants.APP_SESSION_API_NAME))
                .body("[0].detail", equalTo<T>(Constants.REDIS_SERVER_PERSISTENCE_ERROR))
    }

    // refreshSession --------------------------------------------------------------------------------------------------
    @Test
    @Throws(Exception::class)
    fun shouldRefreshSession() {

        setupCSSOAPIValidateSessionExpectation(TOKEN)

        val currentTime = LocalDateTime.now(clock).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        `when`(mockKmsEncryption!!.decrypt(eq<T>(IntegrationConfig.ENCRYPTED_PERSISTED_VALUE)))
                .thenReturn(IOUtils.toString(ClassPathResource("session.json").inputStream, Charset.defaultCharset()))

        given()
                .accept(ContentType.JSON)
                .header(Constants.HTTP_HEADERS_X_ENV, environment)
                .header(HttpHeaders.AUTHORIZATION, String.format(Constants.HTTP_HEADERS_SESSION_TOKEN_FORMAT, TOKEN))
                .cookie(Constants.HTTP_HEADERS_OBSSOCOOKIE, TOKEN)
                .`when`()
                .put("/")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, String.format(Constants.HTTP_HEADERS_SESSION_TOKEN_FORMAT, TOKEN))
                .header(HttpHeaders.SET_COOKIE, String.format(obSSOCookieString, TOKEN))
                .body("username", equalTo<T>("Courtney Tarttelin"))
                .body("expiry", greaterThanOrEqualTo<T>(currentTime))
                .body("token", isEmptyOrNullString())
                .body("customer.first_name", equalTo<T>("Courtney"))
                .body("customer.surname", equalTo<T>("Tarttelin"))
                .body("customer.email", equalTo<T>("jazz2.jarbarker@spamgourmet.com"))
                .body("customer.customer_number", equalTo<T>("C00004854247"))
                .body("customer.apcn", equalTo<T>("1004826888"))
                .body("feature_toggles[0].id", equalTo<T>("addressBookImport"))
                .body("feature_toggles[0].active", equalTo<T>(java.lang.Boolean.TRUE))
                .body("user_preferences[0].preferenceType", equalTo<T>("EBAYIMPORT"))
                .body("user_preferences[1].preferenceType", equalTo<T>("TESTPREF"))
                .body("domestic_presentation_metadata.packaging.packaging_groups[1].id", equalTo<T>("BOX"))
                .body("domestic_presentation_metadata.packaging.packaging_groups[1].display_name", equalTo<T>("Australia Post flat rate boxes"))
                .body("domestic_presentation_metadata.packaging.packaging_types[0].id", equalTo<T>("PKOWN"))
                .body("domestic_presentation_metadata.packaging.packaging_types[0].display_name", equalTo<T>("My own packaging"))
                .body("domestic_presentation_metadata.services[0].id", equalTo<T>("DOMREG"))
                .body("domestic_presentation_metadata.services[0].display_name", equalTo<T>("Parcel Post"))
                .body("domestic_presentation_metadata.services[0].products[0].id", equalTo<T>("B30"))
                .body("domestic_presentation_metadata.services[0].products[0].display_name", equalTo<T>("Postage - Own packaging"))
                .body("domestic_presentation_metadata.services[0].features[1].@type", equalTo<T>("feature"))
                .body("domestic_presentation_metadata.services[0].features[1].id", equalTo<T>("TRANSIT_COVER"))
                .body("domestic_presentation_metadata.features[0].@type", equalTo<T>("threshold-mandatory"))
                .body("domestic_presentation_metadata.features[0].id", equalTo<T>("SIGNATURE_ON_DELIVERY"))
                .body("domestic_presentation_metadata.features[0].display_name", equalTo<T>("Signature on delivery"))
                .body("domestic_presentation_metadata.features[0].trigger_name", equalTo<T>("TRANSIT_COVER"))
                .body("domestic_presentation_metadata.features[1].@type", equalTo<T>("bounded-numeric"))
                .body("domestic_presentation_metadata.features[1].id", equalTo<T>("TRANSIT_COVER"))
                .body("domestic_presentation_metadata.features[1].maximum", equalTo<T>(5000))
                .body("domestic_presentation_metadata.features[1].type", equalTo<T>("currency"))
                .body("pickups[0].id", equalTo<T>("SBD"))
                .body("pickups[0].display_name", equalTo<T>("Same business day"))
                .body("pickups[0].product_ids[0]", equalTo<T>("PU1"))
                .body("organisations[0].organisation_id", equalTo<T>("1004826888"))
                .body("organisations[0].type", equalTo<T>("NCC"))
                .body("organisations[0].status", equalTo<T>("ACTIVE"))
                .body("organisations[0].email", equalTo<T>("jazz2.jarbarker@spamgourmet.com"))
                .body("organisations[0].accounts[0].account_number", equalTo<T>("a4769e096eae4ba2b852a3faf249eb6d"))
                .body("organisations[0].accounts[0].expired", equalTo<T>(java.lang.Boolean.FALSE))
                .body("organisations[0].accounts[0].credit_blocked", equalTo<T>(java.lang.Boolean.FALSE))
                .body("organisations[0].accounts[0].addresses[0].type", equalTo<T>("MERCHANT_LOCATION"))
                .body("organisations[0].accounts[0].details.lodgement_postcode", equalTo<T>(StringUtils.EMPTY))
                .body("authorisation.actions[0].id", equalTo<T>("1"))
                .body("authorisation.actions[0].predicate", equalTo<T>("view"))
                .body("authorisation.actions[0].target", equalTo<T>("/dispatch-apps$environment/shipments/"))
                .body("authorisation.actions[0].label", equalTo<T>("Shipments"))
                .body("authorisation.actions[4].id", equalTo<T>("5"))
                .body("authorisation.actions[4].predicate", equalTo<T>("create"))
                .body("authorisation.actions[4].target", equalTo<T>("/dispatch-apps$environment/upsertshipments/"))
                .body("authorisation.actions[4].label", equalTo<T>("Add shipment"))
                .body("authorisation.permissions[0].context", equalTo<T>("348294733939"))
                .body("authorisation.permissions[0].actions[0]", equalTo<T>("1"))
                .body("authorisation.permissions[0].actions[4]", equalTo<T>("5"))
    }

    @Test
    @Throws(Exception::class)
    fun shouldRefreshSessionOnlyObSSOCookie() {

        setupCSSOAPIValidateSessionExpectation(TOKEN)

        setupCSSOAPICreateSessionExpectation(TOKEN)
        setupCSSOAPIGetCustomerBasicExpectation(TOKEN)
        setupFeatureTogglesExpectation(environment)
        setupUserPreferencesAPIExpectation(TOKEN, environment)
        setupAccessOneAPIPresentationExpectation(environment)
        setupAccessOneAPIPickupsExpectation(environment)
        setupShippingAPIOrganisationsExpectation(TOKEN, environment)

        val currentTime = LocalDateTime.now(clock).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        given()
                .accept(ContentType.JSON)
                .header(Constants.HTTP_HEADERS_X_ENV, environment)
                .cookie(Constants.HTTP_HEADERS_OBSSOCOOKIE, TOKEN)
                .`when`()
                .put("/")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, String.format(Constants.HTTP_HEADERS_SESSION_TOKEN_FORMAT, TOKEN))
                .header(HttpHeaders.SET_COOKIE, String.format(obSSOCookieString, TOKEN))
                .body("username", equalTo<T>("Courtney Tarttelin"))
                .body("expiry", greaterThanOrEqualTo<T>(currentTime))
                .body("token", isEmptyOrNullString())
                .body("customer.first_name", equalTo<T>("Courtney"))
                .body("customer.surname", equalTo<T>("Tarttelin"))
                .body("customer.email", equalTo<T>("jazz2.jarbarker@spamgourmet.com"))
                .body("customer.customer_number", equalTo<T>("C00004854247"))
                .body("customer.apcn", equalTo<T>("1004826888"))
                .body("feature_toggles[0].id", equalTo<T>("addressBookImport"))
                .body("feature_toggles[0].active", equalTo<T>(java.lang.Boolean.TRUE))
                .body("user_preferences[0].preferenceType", equalTo<T>("EBAYIMPORT"))
                .body("user_preferences[1].preferenceType", equalTo<T>("TESTPREF"))
                .body("domestic_presentation_metadata.packaging.packaging_groups[1].id", equalTo<T>("BOX"))
                .body("domestic_presentation_metadata.packaging.packaging_groups[1].display_name", equalTo<T>("Australia Post flat rate boxes"))
                .body("domestic_presentation_metadata.packaging.packaging_types[0].id", equalTo<T>("PKOWN"))
                .body("domestic_presentation_metadata.packaging.packaging_types[0].display_name", equalTo<T>("My own packaging"))
                .body("domestic_presentation_metadata.services[0].id", equalTo<T>("DOMREG"))
                .body("domestic_presentation_metadata.services[0].display_name", equalTo<T>("Parcel Post"))
                .body("domestic_presentation_metadata.services[0].products[0].id", equalTo<T>("B30"))
                .body("domestic_presentation_metadata.services[0].products[0].display_name", equalTo<T>("Postage - Own packaging"))
                .body("domestic_presentation_metadata.services[0].features[1].@type", equalTo<T>("feature"))
                .body("domestic_presentation_metadata.services[0].features[1].id", equalTo<T>("TRANSIT_COVER"))
                .body("domestic_presentation_metadata.features[0].@type", equalTo<T>("threshold-mandatory"))
                .body("domestic_presentation_metadata.features[0].id", equalTo<T>("SIGNATURE_ON_DELIVERY"))
                .body("domestic_presentation_metadata.features[0].display_name", equalTo<T>("Signature on delivery"))
                .body("domestic_presentation_metadata.features[0].trigger_name", equalTo<T>("TRANSIT_COVER"))
                .body("domestic_presentation_metadata.features[1].@type", equalTo<T>("bounded-numeric"))
                .body("domestic_presentation_metadata.features[1].id", equalTo<T>("TRANSIT_COVER"))
                .body("domestic_presentation_metadata.features[1].maximum", equalTo<T>(5000))
                .body("domestic_presentation_metadata.features[1].type", equalTo<T>("currency"))
                .body("pickups[0].id", equalTo<T>("SBD"))
                .body("pickups[0].display_name", equalTo<T>("Same business day"))
                .body("pickups[0].product_ids[0]", equalTo<T>("PU1"))
                .body("organisations[0].organisation_id", equalTo<T>("1004826888"))
                .body("organisations[0].type", equalTo<T>("NCC"))
                .body("organisations[0].status", equalTo<T>("ACTIVE"))
                .body("organisations[0].email", equalTo<T>("jazz2.jarbarker@spamgourmet.com"))
                .body("organisations[0].accounts[0].account_number", equalTo<T>("a4769e096eae4ba2b852a3faf249eb6d"))
                .body("organisations[0].accounts[0].expired", equalTo<T>(java.lang.Boolean.FALSE))
                .body("organisations[0].accounts[0].credit_blocked", equalTo<T>(java.lang.Boolean.FALSE))
                .body("organisations[0].accounts[0].addresses[0].type", equalTo<T>("MERCHANT_LOCATION"))
                .body("organisations[0].accounts[0].details.lodgement_postcode", equalTo<T>(StringUtils.EMPTY))
                .body("authorisation.actions[0].id", equalTo<T>("1"))
                .body("authorisation.actions[0].predicate", equalTo<T>("view"))
                .body("authorisation.actions[0].target", equalTo<T>("/dispatch-apps$environment/shipments/"))
                .body("authorisation.actions[0].label", equalTo<T>("Shipments"))
                .body("authorisation.actions[4].id", equalTo<T>("5"))
                .body("authorisation.actions[4].predicate", equalTo<T>("create"))
                .body("authorisation.actions[4].target", equalTo<T>("/dispatch-apps$environment/upsertshipments/"))
                .body("authorisation.actions[4].label", equalTo<T>("Add shipment"))
                .body("authorisation.permissions[0].context", equalTo<T>("348294733939"))
                .body("authorisation.permissions[0].actions[0]", equalTo<T>("1"))
                .body("authorisation.permissions[0].actions[4]", equalTo<T>("5"))
    }

    @Test
    @Throws(Exception::class)
    fun shouldRefreshSessionOnlyBearerExpired() {

        setupCSSOAPIValidateSessionExpectation(TOKEN)

        `when`(mockKmsEncryption!!.decrypt(eq<T>(IntegrationConfig.ENCRYPTED_PERSISTED_VALUE)))
                .thenReturn(IOUtils.toString(ClassPathResource("session-expired.json").inputStream, Charset.defaultCharset()))

        given()
                .accept(ContentType.JSON)
                .header(Constants.HTTP_HEADERS_X_ENV, environment)
                .header(HttpHeaders.AUTHORIZATION, String.format(Constants.HTTP_HEADERS_SESSION_TOKEN_FORMAT, TOKEN))
                .`when`()
                .put("/")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
    }

    @Test
    @Throws(Exception::class)
    fun shouldRefreshSessionDifferentObSSOCookieAndBearer() {

        setupCSSOAPIValidateSessionExpectation(TOKEN)

        `when`(mockKmsEncryption!!.decrypt(eq<T>(IntegrationConfig.ENCRYPTED_PERSISTED_VALUE)))
                .thenReturn(IOUtils.toString(ClassPathResource("session-bearer.json").inputStream, Charset.defaultCharset()))

        given()
                .accept(ContentType.JSON)
                .header(Constants.HTTP_HEADERS_X_ENV, environment)
                .header(HttpHeaders.AUTHORIZATION, String.format(Constants.HTTP_HEADERS_SESSION_TOKEN_FORMAT, BEARER))
                .cookie(Constants.HTTP_HEADERS_OBSSOCOOKIE, TOKEN)
                .`when`()
                .put("/")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, String.format(Constants.HTTP_HEADERS_SESSION_TOKEN_FORMAT, TOKEN))
                .header(HttpHeaders.SET_COOKIE, String.format(obSSOCookieString, TOKEN))
                .body("token", isEmptyOrNullString())
    }

    @Test
    @Throws(Exception::class)
    fun shouldRefreshSessionInvalidObSSOCookie() {

        setupCSSOAPIValidateInvalidSessionExpectation(TOKEN)

        `when`(mockKmsEncryption!!.decrypt(eq<T>(IntegrationConfig.ENCRYPTED_PERSISTED_VALUE)))
                .thenReturn(IOUtils.toString(ClassPathResource("session.json").inputStream, Charset.defaultCharset()))

        given()
                .accept(ContentType.JSON)
                .header(Constants.HTTP_HEADERS_X_ENV, environment)
                .header(HttpHeaders.AUTHORIZATION, String.format(Constants.HTTP_HEADERS_SESSION_TOKEN_FORMAT, TOKEN))
                .cookie(Constants.HTTP_HEADERS_OBSSOCOOKIE, TOKEN)
                .`when`()
                .put("/")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
    }

    // deleteSession ---------------------------------------------------------------------------------------------------
    @Test
    @Throws(Exception::class)
    fun shouldDeleteSession() {

        `when`(mockKmsEncryption!!.decrypt(eq<T>(IntegrationConfig.ENCRYPTED_PERSISTED_VALUE)))
                .thenReturn(IOUtils.toString(ClassPathResource("session.json").inputStream, Charset.defaultCharset()))

        given()
                .accept(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, String.format(Constants.HTTP_HEADERS_SESSION_TOKEN_FORMAT, TOKEN))
                .`when`()
                .delete("/")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.SET_COOKIE, isEmptyOrNullString())
                .header(HttpHeaders.AUTHORIZATION, isEmptyOrNullString())
                .body(isEmptyString())
    }

    @Test
    @Throws(Exception::class)
    fun shouldDeleteSessionWhenSessionExpired() {

        `when`(mockKmsEncryption!!.decrypt(eq<T>(IntegrationConfig.ENCRYPTED_PERSISTED_VALUE)))
                .thenReturn(IOUtils.toString(ClassPathResource("session-expired.json").inputStream, Charset.defaultCharset()))

        given()
                .accept(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, String.format(Constants.HTTP_HEADERS_SESSION_TOKEN_FORMAT, TOKEN))
                .`when`()
                .delete("/")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.SET_COOKIE, isEmptyOrNullString())
                .header(HttpHeaders.AUTHORIZATION, isEmptyOrNullString())
                .body(isEmptyString())
    }

    @Test
    @Throws(Exception::class)
    fun shouldDeleteSessionWhenSessionNotExisting() {

        given()
                .accept(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, String.format(Constants.HTTP_HEADERS_SESSION_TOKEN_FORMAT, "Token doesn't exist"))
                .`when`()
                .delete("/")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .contentType(ContentType.JSON)
                .header(HttpHeaders.SET_COOKIE, isEmptyOrNullString())
                .header(HttpHeaders.AUTHORIZATION, isEmptyOrNullString())
                .body("[0].code", equalTo<T>(org.springframework.http.HttpStatus.NOT_FOUND.reasonPhrase))
                .body("[0].detail", equalTo<T>(RedisSessionStore.CANT_FIND_SESSION_IN_CACHE))
    }

    // -----------------------------------------------------------------------------------------------------------------
    private fun setupCSSOAPICreateSessionExpectation(token: String) {

        // mock CSSO API to create session and receive CSSO token
        httpRequest = request()
                .withMethod(HttpMethod.POST.name)
                .withPath(CSSO_API_PATH + "/session")
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .withHeader(Constants.HTTP_HEADERS_AP_APP_ID, Constants.APP_CSSO_NAME)
                .withBody(toJsonBody(ClassPathResource("create-session-req.json")))

        AbstractIntegrationTest.mockServerClient.`when`(httpRequest)
                .respond(response()
                        .withStatusCode(HttpStatus.SC_OK)
                        .withHeader(Header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                        .withHeader(Header(HttpHeaders.SET_COOKIE, String.format(obSSOCookieString, token)))
                        .withBody(toJsonBody(ClassPathResource("create-session-resp.json"))))
    }

    private fun setupCSSOAPIGetCustomerBasicExpectation(token: String) {

        // mock CSSO API to get customer basic
        httpRequest = request()
                .withMethod(HttpMethod.GET.name)
                .withPath(CSSO_API_PATH + "/customer/basic")
                .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .withHeader(Constants.HTTP_HEADERS_AP_APP_ID, Constants.APP_CSSO_NAME)
                .withCookie(Constants.HTTP_HEADERS_OBSSOCOOKIE, token)

        AbstractIntegrationTest.mockServerClient.`when`(httpRequest)
                .respond(response()
                        .withStatusCode(HttpStatus.SC_OK)
                        .withHeader(Header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                        .withHeader(Header(HttpHeaders.SET_COOKIE, String.format(obSSOCookieString, token)))
                        .withBody(toJsonBody(ClassPathResource("get-customer-basic-resp.json"))))
    }

    private fun setupCSSOAPIValidateSessionExpectation(token: String) {

        // mock CSSO API to create session and receive CSSO token
        httpRequest = request()
                .withMethod(HttpMethod.GET.name)
                .withPath(CSSO_API_PATH + "/session")
                .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .withHeader(Constants.HTTP_HEADERS_AP_APP_ID, Constants.APP_CSSO_NAME)
                .withCookie(Constants.HTTP_HEADERS_OBSSOCOOKIE, token)

        AbstractIntegrationTest.mockServerClient.`when`(httpRequest)
                .respond(response()
                        .withStatusCode(HttpStatus.SC_OK)
                        .withHeader(Header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                        .withHeader(Header(HttpHeaders.SET_COOKIE, String.format(obSSOCookieString, token)))
                        .withBody(toJsonBody(ClassPathResource("validate-session-resp.json"))))
    }

    private fun setupCSSOAPIValidateInvalidSessionExpectation(token: String) {

        // mock CSSO API to create session and receive CSSO token
        httpRequest = request()
                .withMethod(HttpMethod.GET.name)
                .withPath(CSSO_API_PATH + "/session")
                .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .withHeader(Constants.HTTP_HEADERS_AP_APP_ID, Constants.APP_CSSO_NAME)
                .withCookie(Constants.HTTP_HEADERS_OBSSOCOOKIE, token)

        AbstractIntegrationTest.mockServerClient.`when`(httpRequest)
                .respond(response()
                        .withStatusCode(HttpStatus.SC_UNAUTHORIZED)
                        .withHeader(Header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                        .withHeader(Header(HttpHeaders.SET_COOKIE, String.format(obSSOCookieString, token)))
                        .withBody(toJsonBody(ClassPathResource("validate-session-unauthorized-resp.json"))))
    }

    private fun setupFeatureTogglesExpectation(env: String) {

        val toggleEnvironment = if (StringUtils.isNotEmpty(env)) "0" + env else StringUtils.EMPTY

        // mock Feature Toggles service
        httpRequest = request()
                .withMethod(HttpMethod.GET.name)
                .withPath(FEATURE_TOGGLE_PATH + "/pdev" + toggleEnvironment + ".json")
                .withHeader(HttpHeaders.ACCEPT, "application/json, application/json, application/*+json, application/*+json")

        AbstractIntegrationTest.mockServerClient.`when`(httpRequest)
                .respond(response()
                        .withStatusCode(HttpStatus.SC_OK)
                        .withHeader(Header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                        .withBody(toJsonBody(ClassPathResource("get-feature-toggles-resp.json"))))
    }

    private fun setupUserPreferencesAPIExpectation(token: String, env: String) {

        // mock User Preferences API to get all user preferences
        if (StringUtils.isNotEmpty(env)) {
            httpRequest = request()
                    .withMethod(HttpMethod.GET.name)
                    .withPath(USERPREFERENCES_API_PATH + "/")
                    .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .withHeader(Constants.HTTP_HEADERS_AP_CNUMBER, cNumber)
                    .withHeader(Constants.HTTP_HEADERS_X_ENV, env)
                    .withCookie(Constants.HTTP_HEADERS_OBSSOCOOKIE, token)
        } else {
            httpRequest = request()
                    .withMethod(HttpMethod.GET.name)
                    .withPath(USERPREFERENCES_API_PATH + "/")
                    .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .withHeader(Constants.HTTP_HEADERS_AP_CNUMBER, cNumber)
                    .withCookie(Constants.HTTP_HEADERS_OBSSOCOOKIE, token)
        }

        AbstractIntegrationTest.mockServerClient.`when`(httpRequest)
                .respond(response()
                        .withStatusCode(HttpStatus.SC_OK)
                        .withHeader(Header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                        .withHeader(Header(HttpHeaders.SET_COOKIE, obSSOCookieKey))
                        .withBody(toJsonBody(ClassPathResource("get-user-preferences-resp.json"))))
    }

    private fun setupAccessOneAPIPresentationExpectation(env: String) {

        // mock AccessOne API to get domestic product presentation metadata
        if (StringUtils.isNotEmpty(env)) {
            httpRequest = request()
                    .withMethod(HttpMethod.GET.name)
                    .withPath(PRESENTATION_METADATA_PATH)
                    .withQueryStringParameter("country", "AU")
                    .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .withHeader(Constants.HTTP_HEADERS_X_ENV, env)
        } else {
            httpRequest = request()
                    .withMethod(HttpMethod.GET.name)
                    .withPath(PRESENTATION_METADATA_PATH)
                    .withQueryStringParameter("country", "AU")
                    .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        }

        AbstractIntegrationTest.mockServerClient.`when`(httpRequest)
                .respond(response()
                        .withStatusCode(HttpStatus.SC_OK)
                        .withHeader(Header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                        .withBody(toJsonBody(ClassPathResource("get-domestic-presentation-metadata-resp.json"))))
    }

    private fun setupAccessOneAPIPickupsExpectation(env: String) {

        // mock AccessOne API to get Pickups product metadata
        if (StringUtils.isNotEmpty(env)) {
            httpRequest = request()
                    .withMethod(HttpMethod.GET.name)
                    .withPath(PICKUPS_METADATA_PATH)
                    .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .withHeader(Constants.HTTP_HEADERS_X_ENV, env)
        } else {
            httpRequest = request()
                    .withMethod(HttpMethod.GET.name)
                    .withPath(PICKUPS_METADATA_PATH)
                    .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        }

        AbstractIntegrationTest.mockServerClient.`when`(httpRequest)
                .respond(response()
                        .withStatusCode(HttpStatus.SC_OK)
                        .withHeader(Header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                        .withBody(toJsonBody(ClassPathResource("get-pickups-metadata-resp.json"))))
    }

    private fun setupShippingAPIOrganisationsExpectation(token: String, env: String) {

        // mock Shipping API to get all organisations
        if (StringUtils.isNotEmpty(env)) {
            httpRequest = request()
                    .withMethod(HttpMethod.GET.name)
                    .withPath(SHIPPING_API_PATH + "/organisations")
                    .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .withHeader(Constants.HTTP_HEADERS_X_ENV, env)
                    .withCookie(Constants.HTTP_HEADERS_OBSSOCOOKIE, token)
        } else {
            httpRequest = request()
                    .withMethod(HttpMethod.GET.name)
                    .withPath(SHIPPING_API_PATH + "/organisations")
                    .withHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .withCookie(Constants.HTTP_HEADERS_OBSSOCOOKIE, token)
        }

        AbstractIntegrationTest.mockServerClient.`when`(httpRequest)
                .respond(response()
                        .withStatusCode(HttpStatus.SC_OK)
                        .withHeader(Header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                        .withHeader(Header(HttpHeaders.SET_COOKIE, obSSOCookieKey))
                        .withBody(toJsonBody(ClassPathResource("get-organisations-resp.json"))))
    }

}
