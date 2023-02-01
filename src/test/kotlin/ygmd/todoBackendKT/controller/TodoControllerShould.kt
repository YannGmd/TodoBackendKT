package ygmd.todoBackendKT.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.test.web.servlet.*
import org.springframework.web.util.UriComponentsBuilder
import ygmd.todoBackendKT.TodoBackendKtApplication
import ygmd.todoBackendKT.controller.dto.*
import ygmd.todoBackendKT.service.TodoModel
import ygmd.todoBackendKT.service.TodoUpdate
import ygmd.todoBackendKT.service.TodosService
import ygmd.todoBackendKT.service.exceptions.TodoNotFoundException
import ygmd.todoBackendKT.service.exceptions.TodoNotUpdatableException
import java.util.*
import java.util.function.Supplier

@SpringBootTest(classes = [TodoBackendKtApplication::class])
@AutoConfigureMockMvc
open class TodoControllerShould {
    @Autowired private lateinit var mvc: MockMvc
    @MockBean private lateinit var service: TodosService

    private val mapper: ObjectMapper = Jackson2ObjectMapperBuilder.json().build()
    private val builder: UriComponentsBuilder = UriComponentsBuilder
        .newInstance()
        .scheme("http")
        .host("localhost")


    @Test
    fun returnAllTodos(){
        val models = (0..10)
            .map { TodoModel(UUID.randomUUID(), "Todo model $it", it % 2 == 0, it) }
            .toList()
        doReturn(models).`when`(service).getTodos()

        mvc.get("/todos")
            .andExpectAll {
                status { isOk() }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json(mapper.writeValueAsString(models.map { it.asResponse(builder) }))
                }
            }

        verify(service).getTodos()
        verifyNoMoreInteractions(service)
    }

    @Test
    fun returnSpecificTodo(){
        val responseModel = TodoModel(UUID.randomUUID(), "Todo model", true, 2)
        doReturn(responseModel).`when`(service).getTodo(any())

        mvc.get("/todos/${responseModel.id!!}")
            .andExpectAll {
                status { isOk() }
                content {
                    contentType(MediaType.APPLICATION_JSON)
                    json(mapper.writeValueAsString(responseModel.asResponse(builder)))
                }
            }

        verify(service).getTodo(responseModel.id!!)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun returnNotFound(){
        val id = UUID.randomUUID()
        doThrow(TodoNotFoundException::class.java).`when`(service).getTodo(any())

        mvc.get("/todos/$id")
            .andExpect { status { isNotFound() } }

        verify(service).getTodo(id)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun returnCreatedStatusAndCreatedTodoOnCreate(){
        val creationRequest = TodoCreationRequest("Create Todo")
        val createdModel: TodoModel = TodoModel(UUID.randomUUID(), creationRequest.title, false, 1)
        `when`(service.createTodo(any())).thenReturn(createdModel)

        mvc.post("/todos"){
            content = mapper.writeValueAsString(creationRequest)
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isCreated() }
            content {
                contentType(MediaType.APPLICATION_JSON)
                json(mapper.writeValueAsString(createdModel.asResponse(builder)))
            }
        }

        verify(service).createTodo(creationRequest.asTodoUpdate())
        verifyNoMoreInteractions(service)
    }

    @Test
    fun returnNoContentOnDeleteAllTodos(){
        mvc.delete("/todos")
            .andExpect {
                status { isNoContent() }
            }

        verify(service).deleteAll()
        verifyNoMoreInteractions(service)
    }

    @Test
    fun returnNoContentOnDeleteCompleted(){
        mvc.delete("/todos"){
            param("completed", "true")
        }.andExpect {
            status { isNoContent() }
        }

        verify(service).deleteCompleted()
        verifyNoMoreInteractions(service)
    }

    @Test
    fun returnNoContentOnDeleteSpecificTodo(){
        val idToDelete = UUID.randomUUID()
        doNothing().`when`(service).deleteTodo(any())

        mvc.delete("/todos/$idToDelete")
            .andExpect { status { isNoContent() } }

        verify(service).deleteTodo(idToDelete)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun returnNotFoundOnDeletingNonExisting(){
        val idToDelete = UUID.randomUUID()
        doThrow(TodoNotFoundException::class.java).`when`(service).deleteTodo(any())

        mvc.delete("/todos/$idToDelete")
            .andExpect {
                status { isNotFound() }
            }

        verify(service).deleteTodo(idToDelete)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun returnOkAndUpdatedTodoOnUpdateTodo(){
        val updateRequest = TodoUpdateRequest( "New title", true, 1)
        val updatedModel = TodoModel(UUID.randomUUID(), updateRequest.title, updateRequest.completed, updateRequest.order)

        doReturn(updatedModel).`when`(service).updateTodo(any())

        mvc.put("/todos/${updatedModel.id}"){
            accept = MediaType.APPLICATION_JSON
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(updateRequest)
        }.andExpect {
            status { isOk() }
            content {
                contentType(MediaType.APPLICATION_JSON)
                json(mapper.writeValueAsString(updatedModel.asResponse(builder)))
            }
        }

        verify(service).updateTodo(updateRequest.asTodoUpdate(updatedModel.id!!))
        verifyNoMoreInteractions(service)
    }

    @Test
    fun returnNotFoundOnUpdateNonExisting(){
        doThrow(TodoNotFoundException::class.java).`when`(service).updateTodo(any())

        val id = UUID.randomUUID()
        val updateRequest = TodoUpdateRequest( "New title", true, 1)

        mvc.put("/todos/$id"){
            accept = MediaType.APPLICATION_JSON
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(updateRequest)
        }.andExpect {
            status { isNotFound() }
        }

        verify(service).updateTodo(updateRequest.asTodoUpdate(id))
        verifyNoMoreInteractions(service)
    }

    @Test
    fun returnConflictResponseOnUpdateWithSameOrder(){
        doThrow(TodoNotUpdatableException::class.java).`when`(service).updateTodo(any())

        val id = UUID.randomUUID()
        val updateRequest = TodoUpdateRequest("New title", true, 1)

        mvc.put("/todos/$id"){
            accept = MediaType.APPLICATION_JSON
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(updateRequest)
        }.andExpect {
            status { isConflict() }
        }

        verify(service).updateTodo(updateRequest.asTodoUpdate(id))
        verifyNoMoreInteractions(service)
    }

    @Test
    fun returnOkWithUpdatedModelOnUpdatePartial() {
        val id = UUID.randomUUID()
        val updateRequest = TodoUpdatePartialRequest("Random title", null, null)
        val updateModel = TodoModel(id, updateRequest.title!!, false, 1)

        doReturn(updateModel).`when`(service).getTodo(any())
        doReturn(updateModel).`when`(service).updateTodo(any())

        mvc.patch("/todos/$id") {
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(updateRequest)
        }.andExpect {
            status { isOk() }
            content { mapper.writeValueAsString(updateModel.asResponse(builder)) }
        }

        inOrder(service).let {
            it.verify(service).updateTodo(TodoUpdate(
                updateModel.id!!,
                updateRequest.title,
                updateRequest.completed,
                updateRequest.order,
            ))
            it.verifyNoMoreInteractions()
        }
    }

    @Test
    fun returnNotFoundOnUpdatePartialNonExisting(){
        val id = UUID.randomUUID()
        val updateRequest = TodoUpdatePartialRequest("Random title", null, null)
        doThrow(TodoNotFoundException::class.java).`when`(service).updateTodo(any())

        mvc.patch("/todos/$id"){
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(updateRequest)
        }.andExpect {
            status { isNotFound() }
        }

        verify(service).updateTodo(updateRequest.asTodoUpdate(id))
        verifyNoMoreInteractions(service)
    }

    @Test
    fun returnConflictOnUpdatePartial(){
        val id = UUID.randomUUID()
        val updateRequest = TodoUpdatePartialRequest("Random title", null, null)
        val updateModel = TodoModel(id, updateRequest.title!!, false, 1)

        doReturn(updateModel).`when`(service).getTodo(any())
        doThrow(TodoNotUpdatableException::class.java).`when`(service).updateTodo(any())

        mvc.patch("/todos/$id"){
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(updateRequest)
        }.andExpect {
            status { isConflict() }
        }

        verify(service).updateTodo(TodoUpdate(id, updateRequest.title, updateRequest.completed, updateRequest.order))
        verifyNoMoreInteractions(service)
    }

    /**
     * Returns Mockito.any() as nullable type to avoid java.lang.IllegalStateException when
     * null is returned.
     */
    private fun <T> any(): T = Mockito.any<T>()
}