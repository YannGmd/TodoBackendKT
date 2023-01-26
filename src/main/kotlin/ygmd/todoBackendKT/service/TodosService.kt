package ygmd.todoBackendKT.service

import org.springframework.stereotype.Service
import ygmd.todoBackendKT.service.exceptions.TodoNotFoundException
import ygmd.todoBackendKT.service.exceptions.TodoNotUpdatableException
import java.util.UUID

@Service
interface TodosService {
    fun getTodos(): List<TodoModel>

    @Throws(TodoNotFoundException::class)
    fun getTodo(id: UUID): TodoModel?

    fun createTodo(model: TodoModel)

    @Throws(TodoNotFoundException::class) fun deleteTodo(id: UUID)

    fun deleteCompleted()

    @Throws(TodoNotFoundException::class, TodoNotUpdatableException::class)
    fun updateTodo(request: TodoUpdateRequest)
}