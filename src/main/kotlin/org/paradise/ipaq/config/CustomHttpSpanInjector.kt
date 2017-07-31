package org.paradise.ipaq.config

import org.apache.commons.lang3.StringUtils
import org.paradise.ipaq.Constants
import org.slf4j.LoggerFactory
import org.springframework.cloud.sleuth.Span
import org.springframework.cloud.sleuth.SpanTextMap
import org.springframework.cloud.sleuth.instrument.web.ZipkinHttpSpanInjector

/**
 * Created by terrence on 28/7/17.
 */
class CustomHttpSpanInjector : ZipkinHttpSpanInjector() {

    override fun inject(span: Span, carrier: SpanTextMap) {

        super.inject(span, carrier)

        val country = span.getBaggageItem(Constants.COUNTRY)

        LOG.debug("Inject country [{}] into trace", country)

        if (StringUtils.isNotEmpty(country)) {
            carrier.put(Constants.COUNTRY, country)
        }
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(CustomHttpSpanInjector::class.java)
    }

}
