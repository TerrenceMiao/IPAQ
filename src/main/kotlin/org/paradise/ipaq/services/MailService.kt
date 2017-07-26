package org.paradise.ipaq.services

import org.paradise.ipaq.Constants
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring4.SpringTemplateEngine

/**
 * Created by terrence on 26/7/17.
 */
@Service
class MailService(val springTemplateEngine: SpringTemplateEngine) {

    @Value("\${app.template.plaintext.file.email}")
    private val emailTemplate: String? = null

    fun sendMail(): Boolean {

        val context = Context()
        context.setVariable(Constants.CUSTOMER_NAME_VARIABLE, "John Smith")

        val emailBody = springTemplateEngine.process(emailTemplate, context)

        return true
    }

}