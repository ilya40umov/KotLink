package org.kotlink

import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.RequestBuilder
import org.springframework.test.web.servlet.ResultActions

fun MockMvc.perform(requestBuilder: RequestBuilder, body: ResultActions.() -> Unit) {
    body(perform(requestBuilder))
}