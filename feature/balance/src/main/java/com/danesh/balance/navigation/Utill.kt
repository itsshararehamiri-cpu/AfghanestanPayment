package com.danesh.balance.navigation
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import saman.zamani.persiandate.PersianDate
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

fun String?.formatTime(): String {
    return if (this != null)
        with(this) { "${take(2)}:${substring(2, 4)}:${takeLast(2)}" }.dropLast(3)
    else ""
}
fun String?.timeToForm(): String {
    return if (this != null)
        with(this) { "${take(2)}:${substring(2, 4)}" }
    else ""
}

fun getPersianDate(date_MMdd: String): String {
    return with(date_MMdd) {
        val cal = PersianDate()
        cal.setGrgMonth(take(2).toInt() )
        cal.setGrgDay(takeLast(2).toInt())
        cal.grgYear = cal.grgYear
        "${cal.shYear}/${if (cal.shMonth < 10) 0 else ""}${cal.shMonth}/${if (cal.shDay < 10) 0 else ""}${cal.shDay}"
    }
}
fun getCurrentMinuteTime():String{
    val sdf = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
    return sdf.format(Date()).substring(10,12)
}
fun String.toFormattedDate():String{
    return java.text.SimpleDateFormat("yyyyMMddHHmmss", Locale.US).parse(
        this
    )?.time.toString()
}
fun getCurrentDate():String{
    val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    return sdf.format(Date())
}
@RequiresApi(Build.VERSION_CODES.O)
fun convertTo(date_: String): String{
    if(date_.isNullOrEmpty()) return  ""
    val inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    val outputFormatter = DateTimeFormatter.ofPattern(
        "MMMM d, yyyy",//"MMMM d, yyyy",
        Locale.ENGLISH
    )

    val date = LocalDate.parse(date_, inputFormatter)

    val result = date.format(outputFormatter)

  return result
}