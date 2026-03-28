package com.grocerystore.grocery_store.config

import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    OpenAPI groceryStoreOpenAPI() {
        return new OpenAPI()
            .info(apiInfo())
            .externalDocs(externalDocumentation())
    }

    private Info apiInfo() {
        return new Info()
            .title('Grocery Store API')
            .description('''
                ## Grocery Store Management System
                
                This is a RESTful API for managing products in a grocery store.
                You can use this API to perform CRUD operations on products.
                
                ### Key Features:
                - List all products
                - Get product details
                - Add new products
                - Update existing products
                - Delete products
                
                ### Authentication
                Currently, the API is open and doesn't require authentication.
                '''.stripIndent())
            .version('1.0.0')
            .contact(apiContact())
            .license(apiLicense())
    }

    private Contact apiContact() {
        return new Contact()
            .name('Grocery Store Support')
            .email('support@grocerystore.com')
            .url('https://www.grocerystore.com/contact')
    }

    private License apiLicense() {
        return new License()
            .name('Apache 2.0')
            .url('https://www.apache.org/licenses/LICENSE-2.0.html')
    }

    private ExternalDocumentation externalDocumentation() {
        return new ExternalDocumentation()
            .description('Grocery Store API Documentation')
            .url('https://docs.grocerystore.com/api')
    }
}
