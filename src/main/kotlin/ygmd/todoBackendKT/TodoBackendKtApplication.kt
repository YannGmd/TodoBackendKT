package ygmd.todoBackendKT

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import ygmd.todoBackendKT.repository.TodosRepository

@SpringBootApplication
@EnableJpaRepositories(basePackageClasses = [TodosRepository::class])
open class TodoBackendKtApplication
	fun main(args: Array<String>) = runApplication<TodoBackendKtApplication>(*args)

