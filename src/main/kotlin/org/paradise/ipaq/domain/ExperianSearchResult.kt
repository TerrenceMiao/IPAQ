package org.paradise.ipaq.domain

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
class ExperianSearchResult {

    @JsonProperty("count")
    var count = 0

    @JsonProperty("results")
    var results: List<ExperianSearchMatchedItem> = ArrayList()

}
