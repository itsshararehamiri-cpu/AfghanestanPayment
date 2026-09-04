package com.danesh.balance.navigation
import android.util.Log
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
fun String.formatAmount(): String {
    if(this.isEmpty()) return this
    val t = this.replace(",", "")
    return String.format("%,d", t.toEnglishNumber().toLong())
}

fun String?.mask(): String {
    return if (this != null)
        "${take(6)}****${takeLast(4)}" else ""
}


fun String.toTimeFormat():String {
    val temp = StringBuilder()
    temp.append(this.substring(0, 2))
    temp.append(":")
    temp.append(this.substring(2, 4))
    return temp.toString()
}
fun String.isNotNumber():Boolean{
    return this.contains("-") ||
            this.contains(".") ||
            this.contains(",")
}
fun String.isNumber():Boolean{
    return !this.contains("-")&&
            !this.contains(".") &&
            !this.contains(",")

}
fun String.toEnglishNumber(): String {
    val persianNumbers = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
    val englishNumber = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
    if (this.length == 0) {
        return ""
    }
    var out = ""
    val length = this.length
    for (i in 0 until length) {
        val c = this[i]
        if ('۰' <= c && c <= '۹') {
            for (j in 0 until persianNumbers.size) {
                if (persianNumbers.get(j).equals(c.toString(), ignoreCase = true)) {
                    out += englishNumber.get(j)
                    break
                }
            }
        } else if (c == '،') {
            out += '٫'
        } else {
            out += c
        }
    }
    return out
}


fun getDate(){
    val date = LocalDate.of(2026, 6, 14)

    val formatter = DateTimeFormatter.ofPattern(
        "MMMM dd, yyyy",
        Locale.ENGLISH
    )

    println(date.format(formatter))
}