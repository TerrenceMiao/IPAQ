package org.paradise.ipaq.services.trace

import org.apache.commons.lang3.StringUtils
import org.paradise.ipaq.Constants
import org.slf4j.LoggerFactory
import org.springframework.cloud.sleuth.Span
import org.springframework.cloud.sleuth.SpanTextMap
import org.springframework.cloud.sleuth.instrument.web.ZipkinHttpSpanExtractor
import org.springframework.cloud.sleuth.util.TextMapUtil
import java.util.regex.Pattern

/**
 * Created by terrence on 28/7/17.
 */
class CustomHttpSpanExtractor(skipPattern: Pattern) : ZipkinHttpSpanExtractor(skipPattern) {

    override fun joinTrace(carrier: SpanTextMap): Span {

        val map = TextMapUtil.asMap(carrier)

        val country = map[Constants.COUNTRY]

        LOG.debug("Extracting trace data with country [{}]", country)

        // Enable SPAN explorable
        carrier.put(Span.SPAN_FLAGS, "1")

        val span = super.joinTrace(carrier)

        if (StringUtils.isNotEmpty(country)) {
            span.setBaggageItem(Constants.COUNTRY, country)
        }

        return span
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(CustomHttpSpanExtractor::class.java)
    }

}
