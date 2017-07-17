package org.paradise.ipaq.domain

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.commons.lang3.tuple.ImmutablePair

@JsonInclude(JsonInclude.Include.NON_NULL)
class ExperianAddress {

    @JsonProperty("address")
    private var address: List<ImmutablePair<String, String>> = ArrayList()

    @JsonProperty("components")
    private var components: List<ImmutablePair<String, String>> = ArrayList()

}
