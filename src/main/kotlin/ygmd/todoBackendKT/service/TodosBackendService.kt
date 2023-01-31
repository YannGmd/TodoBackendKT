package ygmd.todoBackendKT.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ygmd.todoBackendKT.repository.TodoEntity
import ygmd.todoBackendKT.repository.TodosRepository
import ygmd.todoBackendKT.service.exceptions.TodoNotFoundException
import ygmd.todoBackendKT.service.exceptions.TodoNotUpdatableException
import java.util.*

@Service
class TodosBackendService(private val todosRepository: TodosRepository) : TodosService{
    override fun getTodos(): List<TodoModel> {
        return todosRepository.findAll()
            .map { it.asModel() }
            .toList()
    }

    override fun getTodo(id: UUID): TodoModel {
        return todosRepository
            .findById(id)
            .map{it.asModel()}
            .orElseThrow{ TodoNotFoundException() }
    }

    override fun createTodo(request: TodoModel): TodoModel {
        println("Creating: $request")
        val toSave = TodoEntity(request.title, request.completed, maxOrder())
        return todosRepository.save(toSave).asModel()
    }

    override fun deleteAll() {
        todosRepository.deleteAll()
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

    override fun updateTodo(request: TodoUpdate): TodoModel {
        val existingTodo = getTodo(request.id)
        checkOrder(existingTodo)
        val toSave = existingTodo.copy(
            id = request.id,
            title = request.title ?: existingTodo.title,
            completed = request.completed ?: existingTodo.completed,
            order = request.order ?: existingTodo.order
        ).asEntity()
        val savedModel = todosRepository.save(toSave)
        return savedModel.asModel()
    }

    private fun checkOrder(ref: TodoModel){
        todosRepository.findFirstByOrder(ref.order)?.let { if (it.id != ref.id) throw TodoNotUpdatableException() }
    }

    private fun TodoModel.asEntity(): TodoEntity {
        return TodoEntity(this.title, this.completed, this.order, this.id)
    }

    private fun TodoEntity.asModel(): TodoModel {
        return TodoModel(this.id, this.title, this.completed, this.order)
    }
}