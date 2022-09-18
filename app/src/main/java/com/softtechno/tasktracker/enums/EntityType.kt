package com.softtechno.tasktracker.enums

enum class EntityType(private val entity: String) {

    YesNo("Yes/No"),

    Number("Number"),

    StartEndDate("Start - End Date");

    override fun toString(): String {
        return entity
    }

    companion object {
        fun fromString(entity: String): EntityType {
            return when (entity) {
                YesNo.entity -> YesNo
                Number.entity -> Number
                else -> StartEndDate
            }
        }
    }
}