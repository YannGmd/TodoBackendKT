package ygmd.todoBackendKT.service

import ygmd.todoBackendKT.repository.TodoEntity
import java.util.UUID

data class TodoUpdateRequest(val id: UUID, val title: String, val completed: Boolean, val order: Int)
