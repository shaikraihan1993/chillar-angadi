package com.grocerystore.grocery_store.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
@Tag(name = "Hello World", description = "Simple greeting endpoints")
class HelloWorldController {

    @Operation(
        summary = "Get Hello World",
        description = "Returns a simple greeting message"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successfully returned greeting"
    )
    @GetMapping("/hello")
    String sayHello() {
        return "Hello, World!"
    }
}
