package org.paradise.ipaq.domain


import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
class ExperianSearchMatchedItem {

    @JsonProperty("suggestion")
    var suggestion: String? = null

    @JsonProperty("matched")
    var matched: List<List<Int>>? = null

    @JsonProperty("format")
    var format: String? = null

}
