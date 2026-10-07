package de.morhenn.seven_wonders_calculator.ui

import java.text.DateFormat
import java.util.Date

fun formatDate(millis: Long): String = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(millis))

fun formatDateTime(millis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(millis))
