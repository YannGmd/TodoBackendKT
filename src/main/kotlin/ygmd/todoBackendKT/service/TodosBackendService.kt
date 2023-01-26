package ygmd.todoBackendKT.service

import ygmd.todoBackendKT.repository.TodoEntity
import ygmd.todoBackendKT.repository.TodosRepository
import ygmd.todoBackendKT.service.exceptions.TodoNotFoundException
import ygmd.todoBackendKT.service.exceptions.TodoNotUpdatableException
import java.util.*

class TodosBackendService(private val todosRepository: TodosRepository) : TodosService{
    override fun getTodos(): List<TodoModel> {
        return todosRepository.findAll()
            .map { it.toModel() }
            .toList()
    }

    override fun getTodo(id: UUID): TodoModel? {
        return todosRepository.findById(id)
            .map { it.toModel() }
            .orElse(null) ?: throw TodoNotFoundException()
    }

    override fun createTodo(model: TodoModel) {
        val toSave = TodoEntity(model.title, model.completed, maxOrder())
        todosRepository.save(toSave)
    }

    private fun maxOrder(): Int {
        return (todosRepository.findFirstByOrderByOrderDesc()?.order?.inc()) ?: 1
    }

    override fun deleteTodo(id: UUID) {
        todosRepository.delete(
            todosRepository.findById(id).orElseThrow { TodoNotFoundException() }
        )
    }

    override fun deleteCompleted() {
        todosRepository.deleteCompleted()
    }

    override fun updateTodo(request: TodoUpdateRequest) {
        if(todosRepository.existsByOrder(request.order)) throw TodoNotUpdatableException()

        todosRepository.save(
            todosRepository.findById(request.id)
                .orElseThrow{ TodoNotFoundException() }
                .copy(title = request.title, completed = request.completed, order = request.order)
        )
    }

    private fun TodoModel.toEntity(): TodoEntity {
        return TodoEntity(this.title, this.completed, this.order, this.id)
    }

    private fun TodoEntity.toModel(): TodoModel {
        return TodoModel(this.id, this.title, this.completed, this.order)
    }
}