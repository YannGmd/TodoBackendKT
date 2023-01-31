package ygmd.todoBackendKT.service

import java.util.*

data class TodoModel(
    var id          : UUID? = null,
    var title       : String,
    var completed   : Boolean,
    var order       : Int,
)