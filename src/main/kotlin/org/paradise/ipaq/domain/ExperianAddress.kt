package org.paradise.ipaq.domain

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ExperianAddress(@JsonProperty("address") val address: List<Map<String, String>>,
                           @JsonProperty("components") val components: List<Map<String, String>>)