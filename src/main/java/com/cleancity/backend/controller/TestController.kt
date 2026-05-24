package com.cleancity.backend.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {

    @GetMapping("/test")
    fun test(): Map<String, String> {
        return mapOf("message" to "working")
    }
}
