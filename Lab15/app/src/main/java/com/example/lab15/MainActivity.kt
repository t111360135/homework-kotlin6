package com.example.lab15

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private val items = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var dbrw: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        dbrw = MyDBHelper(this).writableDatabase
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        findViewById<ListView>(R.id.listView).adapter = adapter
        initializeListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbrw.close()
    }

    private fun initializeListeners() {
        val edBook = findViewById<EditText>(R.id.edBook)
        val edPrice = findViewById<EditText>(R.id.edPrice)
        val btnInsert = findViewById<Button>(R.id.btnInsert)
        val btnUpdate = findViewById<Button>(R.id.btnUpdate)
        val btnDelete = findViewById<Button>(R.id.btnDelete)
        val btnQuery = findViewById<Button>(R.id.btnQuery)

        btnInsert.setOnClickListener {
            executeSQL(
                edBook.text.toString(),
                edPrice.text.toString(),
                "INSERT INTO myTable(book, price) VALUES(?,?)",
                "新增:${edBook.text},價格:${edPrice.text}"
            )
        }

        btnUpdate.setOnClickListener {
            executeSQL(
                edBook.text.toString(),
                edPrice.text.toString(),
                "UPDATE myTable SET price = ? WHERE book LIKE ?",
                "更新:${edBook.text},價格:${edPrice.text}"
            )
        }

        btnDelete.setOnClickListener {
            executeSQL(
                edBook.text.toString(),
                null,
                "DELETE FROM myTable WHERE book LIKE ?",
                "刪除:${edBook.text}"
            )
        }

        btnQuery.setOnClickListener {
            val query = if (edBook.text.isEmpty()) "SELECT * FROM myTable" else "SELECT * FROM myTable WHERE book LIKE ?"
            val args = if (edBook.text.isEmpty()) null else arrayOf(edBook.text.toString())
            val cursor = dbrw.rawQuery(query, args)
            items.clear()
            showToast("共有${cursor.count}筆資料")
            if (cursor.moveToFirst()) {
                do {
                    items.add("書名:${cursor.getString(0)}\t\t\t\t價格:${cursor.getInt(1)}")
                } while (cursor.moveToNext())
            }
            adapter.notifyDataSetChanged()
            cursor.close()
        }
    }

    private fun executeSQL(book: String, price: String?, sql: String, successMessage: String) {
        if (book.isEmpty() || (price == null && sql.contains("VALUES"))) {
            showToast("欄位請勿留空")
            return
        }
        try {
            val args = if (price == null) arrayOf(book) else arrayOf(price, book)
            dbrw.execSQL(sql, args)
            showToast(successMessage)
            clearFields()
        } catch (e: Exception) {
            showToast("操作失敗:$e")
        }
    }

    private fun showToast(text: String) = Toast.makeText(this, text, Toast.LENGTH_LONG).show()

    private fun clearFields() {
        findViewById<EditText>(R.id.edBook).text.clear()
        findViewById<EditText>(R.id.edPrice).text.clear()
    }
}
