package ygmd.todoBackendKT.controller

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import ygmd.todoBackendKT.controller.dto.*
import ygmd.todoBackendKT.service.TodosService
import ygmd.todoBackendKT.service.exceptions.TodoNotFoundException
import ygmd.todoBackendKT.service.exceptions.TodoNotUpdatableException
import java.util.*

@RestController
@RequestMapping("/todos")
class TodosController @Autowired constructor(@Qualifier("todosBackendService") private val service: TodosService) {

    @GetMapping(produces = ["application/json"])
    fun getTodos(builder: UriComponentsBuilder): ResponseEntity<List<TodoResponse>> {
        return ResponseEntity.ok(
            service.getTodos()
                .map{ it.asResponse(builder) }
        )
    }

    @GetMapping("/{id}", produces = ["application/json"])
    fun getTodo(
        @PathVariable id: String,
        builder: UriComponentsBuilder
    ): ResponseEntity<TodoResponse> {
        return try {
            ResponseEntity.ok(service.getTodo(UUID.fromString(id)).asResponse(builder))
        } catch (e: TodoNotFoundException){
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping(consumes = ["application/json"], produces = ["application/json"])
    fun createTodo(
        @RequestBody @Valid request: TodoCreationRequest,
        builder: UriComponentsBuilder
    ): ResponseEntity<TodoResponse> {
        val serviceResponse = service.createTodo(request.asTodoUpdate()).asResponse(builder)
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceResponse)
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteAll(
        @RequestParam(name = "completed", required = false) onlyCompleted: Boolean
    ){
        if(onlyCompleted) service.deleteCompleted()
        else service.deleteAll()
    }

    @DeleteMapping("/{id}")
    fun deleteTodo(
        @PathVariable id: String
    ): ResponseEntity<Nothing> {
        return ResponseEntity.status(
            try {
                service.deleteTodo(UUID.fromString(id))
                HttpStatus.NO_CONTENT
            } catch (e: TodoNotFoundException){
                HttpStatus.NOT_FOUND
            }
        ).build()
    }

    @PutMapping("/{id}")
    fun updateTodo(
        @PathVariable id: String,
        @RequestBody @Valid updateRequest: TodoUpdateRequest,
        builder: UriComponentsBuilder
    ): ResponseEntity<TodoResponse> {
        return try {
            val updated = service.updateTodo(updateRequest.asTodoUpdate(UUID.fromString(id)))
            ResponseEntity.ok(updated!!.asResponse(builder))
        } catch (e: TodoNotFoundException){
            ResponseEntity.notFound().build()
        } catch (e: TodoNotUpdatableException){
            ResponseEntity.status(409).build()
        }
    }

    @PatchMapping("/{id}")
    fun updatePartialTodo(
        @PathVariable id: String,
        @RequestBody @Valid updatePartialRequest: TodoUpdatePartialRequest,
        builder: UriComponentsBuilder
    ): ResponseEntity<TodoResponse> = try {
        val updated = service.updateTodo(updatePartialRequest.asTodoUpdate(UUID.fromString(id)))
        ResponseEntity.ok(updated!!.asResponse(builder))
    } catch (e: TodoNotFoundException){
        ResponseEntity.notFound().build()
    } catch (e: TodoNotUpdatableException){
        ResponseEntity.status(409).build()
    }
}