package org.paradise.ipaq.domain

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ExperianSearchResult(@JsonProperty("count") val count: Int = 0,
                                @JsonProperty("results") val results: List<ExperianSearchMatchedItem>)