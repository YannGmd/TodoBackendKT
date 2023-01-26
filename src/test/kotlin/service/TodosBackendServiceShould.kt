package service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import ygmd.todoBackendKT.repository.TodoEntity
import ygmd.todoBackendKT.repository.TodosRepository
import ygmd.todoBackendKT.service.TodoModel
import ygmd.todoBackendKT.service.exceptions.TodoNotFoundException
import ygmd.todoBackendKT.service.TodosBackendService
import ygmd.todoBackendKT.service.TodosService
import ygmd.todoBackendKT.service.TodoUpdateRequest
import ygmd.todoBackendKT.service.exceptions.TodoNotUpdatableException
import java.util.*

class TodosBackendServiceShould {
    @Mock
    val repository: TodosRepository = Mockito.mock(TodosRepository::class.java);

    private val service: TodosService = TodosBackendService(repository)

    @Test
    fun returnAllTodos(){
        doReturn(emptyList<TodoEntity>()).`when`(repository).findAll()
        service.getTodos()

        verify(repository).findAll()
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun returnSpecificTodo(){
        val savedEntity = withIdModelRef.toEntity()

        doReturn(Optional.of(savedEntity)).`when`(repository).findById(any(UUID::class.java))
        service.getTodo(withIdModelRef.id!!)

        verify(repository).findById(withIdModelRef.id!!)
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun createTodo(){
        doReturn(withIdModelRef.toEntity()).`when`(repository).findFirstByOrderByOrderDesc()
        doReturn(null).`when`(repository).save(any())

        service.createTodo(withoutIdModelRef)

        val verifier = inOrder(repository)
        verifier.verify(repository).findFirstByOrderByOrderDesc()
        verifier.verify(repository).save(TodoEntity(withoutIdModelRef.title, withoutIdModelRef.completed, withoutIdModelRef.order + 1))
        verifier.verifyNoMoreInteractions()
    }

    @Test
    fun deleteTodo(){
        doReturn(Optional.of(withIdModelRef.toEntity())).`when`(repository).findById(any(UUID::class.java))
        doNothing().`when`(repository).deleteById(any(UUID::class.java))

        service.deleteTodo(withIdModelRef.id!!)

        val verifier = inOrder(repository)
        verifier.verify(repository).findById(withIdModelRef.id!!)
        verifier.verify(repository).delete(withIdModelRef.toEntity())
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun throwOnDeleteNonExisting(){
        doReturn(Optional.ofNullable(null)).`when`(repository).findById(any(UUID::class.java))

        val toDeleteId = UUID.randomUUID()
        assertThrows<TodoNotFoundException> { service.deleteTodo(toDeleteId) }

        verify(repository).findById(toDeleteId)
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun deleteCompleted(){
        doNothing().`when`(repository).deleteCompleted()
        service.deleteCompleted()

        verify(repository).deleteCompleted()
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun updateTodo(){
        doReturn(Optional.of(withIdModelRef.toEntity())).`when`(repository).findById(any(UUID::class.java))
        doReturn(null).`when`(repository).save(any())

        val updateRequest = TodoUpdateRequest(
            withIdModelRef.id!!,
            "Custom title",
            false,
            withIdModelRef.order
        );
        service.updateTodo(updateRequest);

        val verifier = inOrder(repository)
        verifier.verify(repository).findById(updateRequest.id)
        verifier.verify(repository).save(
            withIdModelRef.copy(
                title = updateRequest.title,
                completed = updateRequest.completed
            ).toEntity()
        )
        verifier.verifyNoMoreInteractions()
    }

    @Test
    fun throwOnUpdateWhenNonExisting(){
        doReturn(Optional.ofNullable(null)).`when`(repository).findById(any(UUID::class.java))

        val updateRequest = TodoUpdateRequest(
            withIdModelRef.id!!,
            withIdModelRef.title,
            withIdModelRef.completed,
            withIdModelRef.order
        );

        assertThrows<TodoNotFoundException> { service.updateTodo(updateRequest) }
        verify(repository).existsByOrder(updateRequest.order)
        verify(repository).findById(updateRequest.id)
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun throwOnUpdateWithSameOrder(){
        doReturn(true).`when`(repository).existsByOrder(anyInt())

        val updateRequest = TodoUpdateRequest(
            withIdModelRef.id!!,
            withIdModelRef.title,
            withIdModelRef.completed,
            withIdModelRef.order
        );

        assertThrows<TodoNotUpdatableException> { service.updateTodo(updateRequest) }
        verify(repository).existsByOrder(updateRequest.order)
        verifyNoMoreInteractions(repository)
    }

    private fun TodoModel.toEntity(): TodoEntity {
        return TodoEntity(this.title, this.completed, this.order, this.id)
    }

    private fun TodoEntity.toModel(): TodoModel {
        return TodoModel(this.id, this.title, this.completed, this.order)
    }

    companion object {
        val withoutIdModelRef = TodoModel(null, "Without id model title", false, 1)
        val withIdModelRef = TodoModel(UUID.randomUUID(), "With id model title", false, 1)
    }
}