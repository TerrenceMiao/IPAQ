package org.paradise.ipaq.config

import org.apache.commons.lang3.StringUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.regex.Pattern

/**
 * Created by terrence on 29/7/17.
 */
@Configuration
class TraceConfig {

    @Bean
    fun customerHttpSpanExtractor(): CustomHttpSpanExtractor {

        return CustomHttpSpanExtractor(Pattern.compile(StringUtils.EMPTY))
    }

    @Bean
    fun customerHttpSpanInjector(): CustomHttpSpanInjector {

        return CustomHttpSpanInjector()
    }

}