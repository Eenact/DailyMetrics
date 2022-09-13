package com.softtechno.tasktracker.enums

enum class EntityType(private val entity: String) {

    YesNo("Yes/No"),

    Number("Number"),

    StartEndDate("Start - End Date");

    override fun toString(): String {
        return entity
    }
}