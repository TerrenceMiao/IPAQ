package org.paradise.ipaq.domain

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
class ExperianAddress {

    @JsonProperty("address")
    private var address: List<Map<String, String>>? = null

    @JsonProperty("components")
    private var components: List<Map<String, String>>? = null

}
