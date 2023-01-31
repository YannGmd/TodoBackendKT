package ygmd.todoBackendKT.controller.dto

import org.springframework.web.util.UriComponentsBuilder
import ygmd.todoBackendKT.service.TodoModel
import java.util.UUID

data class TodoResponse(val id: UUID, val title: String, val completed: Boolean, val order: Int, val url: String)

fun TodoModel.asResponse(builder: UriComponentsBuilder): TodoResponse {
    return TodoResponse(this.id!!, this.title, this.completed, this.order, buildURI(this, builder))
}

private fun buildURI(model: TodoModel, builder: UriComponentsBuilder): String {
    return builder.replacePath("/todos").path("/{id}")
        .buildAndExpand(mapOf("id" to model.id))
        .toUriString()
}