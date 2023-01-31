package ygmd.todoBackendKT.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.Mockito.*
import ygmd.todoBackendKT.controller.dto.TodoUpdateRequest
import ygmd.todoBackendKT.controller.dto.asTodoUpdate
import ygmd.todoBackendKT.repository.TodoEntity
import ygmd.todoBackendKT.repository.TodosRepository
import ygmd.todoBackendKT.service.exceptions.TodoNotFoundException
import ygmd.todoBackendKT.service.exceptions.TodoNotUpdatableException
import java.util.*

class TodosBackendServiceShould {
    @Mock
    val repository: TodosRepository = mock(TodosRepository::class.java)

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
        val savedEntity = withIdModelRef.asEntity()

        doReturn(Optional.of(savedEntity)).`when`(repository).findById(any(UUID::class.java))
        service.getTodo(withIdModelRef.id!!)

        verify(repository).findById(withIdModelRef.id!!)
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun createTodo(){
        doReturn(withoutIdModelRef.asEntity()).`when`(repository).findFirstByOrderByOrderDesc()
        doReturn(withIdModelRef.asEntity()).`when`(repository).save(any(TodoEntity::class.java))

        service.createTodo(withoutIdModelRef)

        val verifier = inOrder(repository)
        verifier.verify(repository).findFirstByOrderByOrderDesc()
        verifier.verify(repository).save(TodoEntity(withoutIdModelRef.title, withoutIdModelRef.completed, withoutIdModelRef.order + 1))
        verifier.verifyNoMoreInteractions()
    }

    @Test
    fun deleteTodo(){
        doReturn(Optional.of(withIdModelRef.asEntity())).`when`(repository).findById(any(UUID::class.java))
        doNothing().`when`(repository).deleteById(any(UUID::class.java))

        service.deleteTodo(withIdModelRef.id!!)

        val verifier = inOrder(repository)
        verifier.verify(repository).findById(withIdModelRef.id!!)
        verifier.verify(repository).delete(withIdModelRef.asEntity())
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
    fun deleteAll(){
        doNothing().`when`(repository).deleteAll()
        service.deleteAll()

        verify(repository).deleteAll()
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun updateTodo(){
        fun TodoUpdate.asEntity(): TodoEntity {
            return TodoEntity(this.title!!, this.completed!!, this.order!!, this.id)
        }

        val updateRequest = TodoUpdate(
            withIdModelRef.id!!,
            withIdModelRef.title.plus(" reloaded"),
            withIdModelRef.completed.not(),
            withIdModelRef.order.inc()
        )

        doReturn(Optional.of(withIdModelRef.asEntity())).`when`(repository).findById(any(UUID::class.java))
        doReturn(withIdModelRef.asEntity()).`when`(repository).findFirstByOrder(anyInt())
        doReturn(
            TodoEntity(
                title = updateRequest.title!!,
                completed = updateRequest.completed!!,
                order = updateRequest.order!!,
                id = updateRequest.id
            )
        ).`when`(repository).save(any(TodoEntity::class.java))

        service.updateTodo(updateRequest)

        inOrder(repository).let {
            it.verify(repository).findById(updateRequest.id)
            it.verify(repository).findFirstByOrder(withIdModelRef.order)
            it.verify(repository).save(updateRequest.asEntity())
            it.verifyNoMoreInteractions()
        }
    }

    @Test
    fun updateCompleted(){
        val existingEntity = TodoEntity("Todo title", false, 1, UUID.randomUUID())

        val partialUpdateRequest = TodoUpdate(
            existingEntity.id!!,
            null,
            true,
            null
        )

        val returnedModel = TodoModel(
            partialUpdateRequest.id,
            existingEntity.title,
            partialUpdateRequest.completed!!,
            existingEntity.order
        )

        doReturn(null).`when`(repository).findFirstByOrder(anyInt())
        doReturn(Optional.of(existingEntity)).`when`(repository).findById(any(UUID::class.java))
        doReturn(returnedModel.asEntity()).`when`(repository).save(any(TodoEntity::class.java))

        val saved = service.updateTodo(partialUpdateRequest)

        assertThat(saved)
            .isNotNull
            .isEqualTo(returnedModel)

        val verifier = inOrder(repository)
        verifier.verify(repository).findById(partialUpdateRequest.id)
        verifier.verify(repository).save(returnedModel.asEntity())
        verifier.verifyNoMoreInteractions()
    }

    @Test
    fun throwOnUpdateWhenNonExisting(){
        doReturn(Optional.ofNullable(null)).`when`(repository).findById(any(UUID::class.java))

        val updateRequest = TodoUpdateRequest(
            withIdModelRef.title,
            withIdModelRef.completed,
            withIdModelRef.order
        )

        val id = UUID.randomUUID()
        assertThrows<TodoNotFoundException> { service.updateTodo(updateRequest.asTodoUpdate(id)) }
        verify(repository).findById(id)
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun throwOnUpdateWithSameOrder(){
        val firstEntity = TodoEntity("Random title", false, 1, UUID.randomUUID())
        doReturn(Optional.of(firstEntity)).`when`(repository).findById(any(UUID::class.java))
        doReturn(firstEntity.copy(id = UUID.randomUUID())).`when`(repository).findFirstByOrder(anyInt())

        val updateRequest = TodoUpdate(
            firstEntity.id!!,
            withIdModelRef.title,
            withIdModelRef.completed,
            withIdModelRef.order
        )

        assertThrows<TodoNotUpdatableException> { service.updateTodo(updateRequest) }

        inOrder(repository).also {
            it.verify(repository).findById(firstEntity.id!!)
            it.verify(repository).findFirstByOrder(updateRequest.order!!)
            it.verifyNoMoreInteractions()
        }
    }

    private fun TodoModel.asEntity(): TodoEntity {
        return TodoEntity(this.title, this.completed, this.order, this.id)
    }

    companion object {
        val withoutIdModelRef = TodoModel(null, "Without id model title", false, 1)
        val withIdModelRef = TodoModel(UUID.randomUUID(), "With id model title", false, 1)
    }
}