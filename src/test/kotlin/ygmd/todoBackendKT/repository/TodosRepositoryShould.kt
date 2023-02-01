package ygmd.todoBackendKT.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.repository.findByIdOrNull
import java.util.*


@DataJpaTest
open class TodosRepositoryShould @Autowired constructor(
    private val todosRepository: TodosRepository,
) {
    private val title: String = "Todo Title";
    private val completed: Boolean = false;
    private val order: Int = 1

    @Test
    @DisplayName("persist new todo")
    fun persistTodo() {
        val toPersist = TodoEntity(title, completed, order)
        todosRepository.save(toPersist)

        assertThat(todosRepository.findAll())
            .isNotEmpty
            .hasSize(1)
            .containsExactly(toPersist)
    }

    @Test
    @DisplayName("return todo with find by id")
    fun returnTodoWithFindById() {
        val toPersist = TodoEntity(title, completed, order)
        todosRepository.save(toPersist)

        assertThat(todosRepository.findByIdOrNull(toPersist.id!!))
            .isNotNull
            .isEqualTo(toPersist)
    }

    @Test
    @DisplayName("return empty if not existing")
    fun returnEmptyIfNotExisting() {
        assertThat(todosRepository.findByIdOrNull(UUID.randomUUID()))
            .isNull()
    }

    @Test
    fun returnEntityAccordingToItsOrder(){
        val toPersist = TodoEntity(title, completed, order)
        todosRepository.save(toPersist)
        assertThat(todosRepository.findFirstByOrder(order))
            .isNotNull
            .isEqualTo(toPersist)
    }

    @Test
    @DisplayName("update todo if already existing")
    fun updateTodoIfAlreadyExisting() {
        val persistedFirst: TodoEntity = TodoEntity(title, completed, order)
        todosRepository.save(persistedFirst);

        val updatedTodo: TodoEntity = TodoEntity("New Title", true, 2, persistedFirst.id)
        todosRepository.save(updatedTodo);

        assertThat(todosRepository.findByIdOrNull(persistedFirst.id!!))
            .isNotNull
            .isEqualTo(updatedTodo)
    }

    @Test
    @DisplayName("delete existing todo")
    fun deleteExisting() {
        val toPersist = TodoEntity(title, completed, order)
        todosRepository.save(toPersist)

        assertThat(todosRepository.findByIdOrNull(toPersist.id!!))
            .isNotNull
            .isEqualTo(toPersist)

        todosRepository.deleteById(toPersist.id!!)
        assertThat(todosRepository.findAll())
            .isEmpty()
    }

    @Test
    @DisplayName("delete all todos")
    fun deleteAllTodos() {
        val todoFactory = { i: Int -> TodoEntity("Todo_$i", i % 2 == 0, i) }

        val todos = mutableListOf<TodoEntity>()
        for (i in 0..10) todos.add(todoFactory(i))
        todosRepository.saveAll(todos)

        assertThat(todosRepository.findAll())
            .containsAll(todos)

        todosRepository.deleteCompleted()
        assertThat(todosRepository.findAll())
            .containsAll(todos.filter { !it.completed })
    }
}
