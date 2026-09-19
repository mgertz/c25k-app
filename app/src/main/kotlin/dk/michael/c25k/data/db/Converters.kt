package dk.michael.c25k.data.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromOutcome(value: RunOutcome): String = value.name

    @TypeConverter
    fun toOutcome(value: String): RunOutcome = RunOutcome.valueOf(value)
}
