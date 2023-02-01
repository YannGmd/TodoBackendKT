package ygmd.todoBackendKT.service

import java.util.UUID

data class TodoUpdate(
    val id: UUID,
    val title: String?,
    val completed: Boolean?,
    val order: Int?
)