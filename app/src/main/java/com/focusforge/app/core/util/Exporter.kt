package com.focusforge.app.core.util

import com.google.gson.GsonBuilder
import java.io.File

object Exporter {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun toJson(data: Any): String {
        return gson.toJson(data)
    }

    fun toCsv(headers: List<String>, rows: List<List<String>>): String {
        val sb = StringBuilder()
        sb.append(headers.joinToString(","))
        sb.append("\n")
        for (row in rows) {
            val escapedRow = row.map { cell ->
                if (cell.contains(",") || cell.contains("\"") || cell.contains("\n")) {
                    "\"${cell.replace("\"", "\"\"")}\""
                } else {
                    cell
                }
            }
            sb.append(escapedRow.joinToString(","))
            sb.append("\n")
        }
        return sb.toString()
    }
}
