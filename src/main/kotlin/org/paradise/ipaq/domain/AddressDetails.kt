package org.paradise.ipaq.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by terrence on 17/7/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class AddressDetails {

    @JsonProperty("addressLine1")
    var addressLine1: String? = null

    @JsonProperty("addressLine2")
    var addressLine2: String? = null

    @JsonProperty("addressLine3")
    var addressLine3: String? = null

    @JsonProperty("city")
    var city: String? = null

    @JsonProperty("state")
    var state: String? = null

    @JsonProperty("postcode")
    var postcode: String? = null

    @JsonProperty("country")
    var country: String? = null

}
