package ygmd.todoBackendKT.controller.dto

import jakarta.validation.constraints.NotBlank
import ygmd.todoBackendKT.service.TodoModel

data class TodoCreationRequest(@field:NotBlank val title: String)
fun TodoCreationRequest.asTodoUpdate(): TodoModel {
    return TodoModel(null, title, false, 0)
}
