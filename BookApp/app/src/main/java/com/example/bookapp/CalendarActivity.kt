package com.example.bookapp

import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var addEventBtn: Button
    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        calendarView = findViewById(R.id.calendarView)
        addEventBtn = findViewById(R.id.addEventBtn)

        // Başlangıçta bugünün tarihini seçili olarak ayarla
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        selectedDate = dateFormat.format(calendar.time)

        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            // Seçilen tarihi formatla
            calendar.set(year, month, dayOfMonth)
            selectedDate = dateFormat.format(calendar.time)
            Toast.makeText(this, "Seçilen Tarih: $selectedDate", Toast.LENGTH_SHORT).show()
        }

        addEventBtn.setOnClickListener {
            // Burada seçilen tarihe etkinlik ekleme mantığı eklenecek
            // Örneğin, bir dialog açabilir veya başka bir aktiviteye geçebilirsiniz
            Toast.makeText(this, "$selectedDate tarihine etkinlik ekle!", Toast.LENGTH_LONG).show()
        }
    }
} 