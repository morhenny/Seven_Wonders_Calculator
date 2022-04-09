package de.morhenn.seven_wonders_calculator

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.pow

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button = findViewById<Button>(R.id.button)
        val reset = findViewById<Button>(R.id.reset)
        val table = findViewById<TableLayout>(R.id.table)

        val nameRow = table.getChildAt(0) as TableRow
        for (i in 1..7) {
            for (j in 0..10) {
                val row = table.getChildAt(j) as TableRow
                val field = row.getChildAt(i) as EditText
                if (j == 4) { //Science Row
                    field.setOnLongClickListener {
                        startScienceDialog(field)
                        true
                    }
                }

                field.setOnEditorActionListener { v, actionId, event ->
                    if (j == 0) {
                        if (i < 7) {
                            val newField = row.getChildAt(i + 1) as EditText
                            newField.requestFocus()
                        } else {
                            val newRow = table.getChildAt(j + 1) as TableRow
                            val newField = newRow.getChildAt(1) as EditText
                            newField.requestFocus()
                        }
                    } else if (i < 7 && (nameRow.getChildAt(i + 1) as EditText).text.isNotBlank()) {
                        val newField = row.getChildAt(i + 1) as EditText
                        newField.requestFocus()
                    } else {
                        if (j == 10) {
                            (v as EditText).clearFocus()
                            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(v.windowToken, 0)
                            false
                        } else {
                            val newRow = table.getChildAt(j + 1) as TableRow
                            val newField = newRow.getChildAt(1) as EditText
                            newField.requestFocus()
                        }
                    }
                    true
                }
            }
        }
        button.setOnClickListener {
            val resultRow = table.getChildAt(11) as TableRow
            for (i in 1..7) {
                var result = 0
                for (j in 1..10) {
                    val row = table.getChildAt(j) as TableRow
                    val field = row.getChildAt(i) as EditText
                    if (field.text.isNotBlank()) {
                        result += field.text.toString().toInt()
                    }
                }
                val resultView = resultRow.getChildAt(i) as TextView
                resultView.text = result.toString()
            }
        }
        reset.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Are you sure?")
                .setMessage("Everything will be deleted!")
                .setPositiveButton("Yes") { _, _ ->
                    for (i in 1..7) {
                        for (j in 0..10) {
                            val row = table.getChildAt(j) as TableRow
                            val field = row.getChildAt(i) as EditText
                            field.setText("")
                        }
                        val row = table.getChildAt(11) as TableRow
                        val field = row.getChildAt(i) as TextView
                        field.text = "0"
                    }
                }
                .setNeutralButton("Cancel") { _, _ -> }
                .show()
        }
    }

    private fun startScienceDialog(field: EditText) {
        val dialogLayout = layoutInflater.inflate(R.layout.science_dialog, null)
        val scienceResult = dialogLayout.findViewById<TextView>(R.id.science_result)
        val circleAmount = dialogLayout.findViewById<TextView>(R.id.circle_amount)
        val gearAmount = dialogLayout.findViewById<TextView>(R.id.gear_amount)
        val bookAmount = dialogLayout.findViewById<TextView>(R.id.book_amount)
        var circles = 0
        var gears = 0
        var books = 0

        dialogLayout.findViewById<ImageButton>(R.id.cicle_add).setOnClickListener {
            circles = circleAmount.text.toString().toInt() + 1
            circles.toString().also { circleAmount.text = it }
            refreshScienceResult(circles, gears, books, scienceResult)
        }
        dialogLayout.findViewById<ImageButton>(R.id.circle_sub).setOnClickListener {
            if (circleAmount.text.toString().toInt() > 0) {
                circles = circleAmount.text.toString().toInt() - 1
                circles.toString().also { circleAmount.text = it }
                refreshScienceResult(circles, gears, books, scienceResult)
            }
        }
        dialogLayout.findViewById<ImageButton>(R.id.gear_add).setOnClickListener {
            gears = gearAmount.text.toString().toInt() + 1
            gears.toString().also { gearAmount.text = it }
            refreshScienceResult(circles, gears, books, scienceResult)
        }
        dialogLayout.findViewById<ImageButton>(R.id.gear_sub).setOnClickListener {
            if (gearAmount.text.toString().toInt() > 0) {
                gears = gearAmount.text.toString().toInt() - 1
                gears.toString().also { gearAmount.text = it }
                refreshScienceResult(circles, gears, books, scienceResult)
            }
        }
        dialogLayout.findViewById<ImageButton>(R.id.book_add).setOnClickListener {
            books = bookAmount.text.toString().toInt() + 1
            books.toString().also { bookAmount.text = it }
            refreshScienceResult(circles, gears, books, scienceResult)
        }
        dialogLayout.findViewById<ImageButton>(R.id.book_sub).setOnClickListener {
            if (bookAmount.text.toString().toInt() > 0) {
                books = bookAmount.text.toString().toInt() - 1
                books.toString().also { bookAmount.text = it }
                refreshScienceResult(circles, gears, books, scienceResult)
            }
        }
        AlertDialog.Builder(this)
            .setView(dialogLayout)
            .setPositiveButton("Confirm") { _, _ ->
                field.setText(scienceResult.text.toString())
            }
            .setNeutralButton("Cancel") { _, _ -> }
            .show()
    }

    private fun refreshScienceResult(circles: Int, gears: Int, books: Int, resultView: TextView) {
        var result = 0
        var setCount = circles
        if (gears < setCount) {
            setCount = gears
        }
        if (books < setCount) {
            setCount = books
        }
        result = setCount * 7 + circles.toDouble().pow(2.0).toInt() + gears.toDouble().pow(2.0).toInt() + books.toDouble().pow(2.0).toInt()
        resultView.text = result.toString()
    }
}