package ygmd.todoBackendKT

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import ygmd.todoBackendKT.repository.TodosRepository
import ygmd.todoBackendKT.service.TodosBackendService
import ygmd.todoBackendKT.service.TodosService

@Configuration
@ComponentScan(basePackageClasses = [TodosRepository::class])
open class TodoBackendKtApplicationConfiguration @Autowired constructor(private val repository: TodosRepository) {
    @Bean
    open fun corsConfigurer(): WebMvcConfigurer? {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/todos")
                    .allowedOrigins("https://www.todobackend.com")
                    .allowedMethods("GET", "POST", "DELETE", "OPTIONS")
                registry.addMapping("/todos/*")
                    .allowedOrigins("https://www.todobackend.com")
                    .allowedMethods("GET", "PUT", "PATCH", "DELETE", "OPTIONS")
            }
        }
    }
}