package org.paradise.ipaq.controllers

import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component

/**
 * Created by terrence on 2/6/17.
 */
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
class EnvironmentLocal {

    var query: String? = null
    var id: String? = null
    var country: String? = null
}
