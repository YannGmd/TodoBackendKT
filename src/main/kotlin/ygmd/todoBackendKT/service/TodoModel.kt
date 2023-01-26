package ygmd.todoBackendKT.service

import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.util.*

data class TodoModel(
    var id          : UUID? = null,
    var title       : String,
    var completed   : Boolean,
    var order       : Int,
)