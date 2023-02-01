package ygmd.todoBackendKT.controller.dto

import ygmd.todoBackendKT.service.TodoUpdate
import java.util.UUID

data class TodoUpdateRequest(val title: String, val completed: Boolean, val order: Int)

fun TodoUpdateRequest.asTodoUpdate(id: UUID): TodoUpdate {
    return TodoUpdate(id, title, completed, order)
}

data class TodoUpdatePartialRequest(val title: String?, val completed: Boolean?, val order: Int?)

fun TodoUpdatePartialRequest.asTodoUpdate(id: UUID): TodoUpdate {
    return TodoUpdate(id, title, completed, order)
}
