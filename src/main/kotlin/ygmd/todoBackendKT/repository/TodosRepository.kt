package ygmd.todoBackendKT.repository

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
@Transactional(readOnly = true)
interface TodosRepository: CrudRepository<TodoEntity, UUID> {
    @Transactional
    @Modifying
    @Query(value = "delete from TodoEntity t where t.completed=true")
    fun deleteCompleted();

    fun findFirstByOrderByOrderDesc(): TodoEntity?

    fun existsByOrder(order: Int): Boolean
}
