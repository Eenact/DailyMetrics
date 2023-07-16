package com.eenact.dailymetrics.enums

enum class Units(private val unit: String) {

    Gram("Grams"),

    Sec("Seconds"),

    Min("Minutes"),

    Hour("Hours"),

    KG("KiloGrams"),

    Day("Days"),

    Week("Weeks"),

    Month("Months"),

    Year("Year"),

    Other("Other");

    override fun toString(): String {
        return unit;
    }

    companion object {
        fun fromString(unit: String): Units {
            return when (unit) {
                Gram.unit -> Gram
                Sec.unit -> Sec
                Min.unit -> Min
                Hour.unit -> Hour
                KG.unit -> KG
                Day.unit -> Day
                Week.unit -> Week
                Month.unit -> Month
                Year.unit -> Year
                else -> Other
            }
        }
    }

}