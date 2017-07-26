package org.paradise.ipaq.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.thymeleaf.spring4.SpringTemplateEngine
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver
import org.thymeleaf.templatemode.TemplateMode

/**
 * Created by terrence on 25/7/17.
 */
@Configuration
class TemplateConfig {

    @Autowired
    private val applicationContext: ApplicationContext? = null

    @Value("\${app.template.plaintext.directory}")
    private val plaintextTemplateDirectory: String? = null

    @Bean
    fun springTemplateEngine(): SpringTemplateEngine {

        val springTemplateEngine = SpringTemplateEngine()

        springTemplateEngine.setTemplateResolver(plaintextTemplateResolver())

        return springTemplateEngine
    }

    private fun plaintextTemplateResolver(): SpringResourceTemplateResolver {

        val plaintextTemplateResolver = SpringResourceTemplateResolver()

        plaintextTemplateResolver.setApplicationContext(applicationContext)
        plaintextTemplateResolver.prefix = plaintextTemplateDirectory
        plaintextTemplateResolver.suffix = ".txt"
        plaintextTemplateResolver.isCacheable = true
        plaintextTemplateResolver.templateMode = TemplateMode.TEXT

        return plaintextTemplateResolver
    }

}
