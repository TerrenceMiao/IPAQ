package org.paradise.ipaq.domain


import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ExperianSearchMatchedItem(@JsonProperty("suggestion") val suggestion: String,
                                     @JsonProperty("matched") val matched: List<List<Int>>,
                                     @JsonProperty("format") val format: String)