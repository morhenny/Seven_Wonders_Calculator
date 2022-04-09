package de.morhenn.seven_wonders_calculator

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

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
}