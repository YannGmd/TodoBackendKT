package ygmd.todoBackendKT.repository

import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.util.*

@Entity
data class TodoEntity (
    var title       : String,
    var completed   : Boolean,
    @Column(name = "todo_order")
    var order       : Int,

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    var id          : UUID? = null,
)