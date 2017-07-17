package org.paradise.ipaq.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by terrence on 17/7/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class AddressDetails {

    @JsonProperty("addressLine1")
    var addressLine1 = null

    @JsonProperty("addressLine2")
    var addressLine2 = null

    @JsonProperty("addressLine3")
    var addressLine3 = null

    @JsonProperty("city")
    var city = null

    @JsonProperty("state")
    var state = null

    @JsonProperty("postcode")
    var postcode = null

    @JsonProperty("country")
    var country = null

}
