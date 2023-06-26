package com.pixeldev.pdfbook.helper

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE favorite_table (_id INTEGER PRIMARY KEY AUTOINCREMENT, book_title TEXT, book_author TEXT, book_thumb TEXT, book_url TEXT, book_desc TEXT, book_featured TEXT, book_category TEXT);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS favorite_table")
        onCreate(db)
    }

    fun addBook(
        title: String?,
        author: String?,
        thumb: String?,
        url: String?,
        desc: String?,
        feature: Boolean?,
        category: String?
    ) {
        //SQLiteDatabase db = getWritableDatabase();
        val db = getInstance(context)!!.writableDatabase
        val cv = ContentValues()
        cv.put(COLUMN_TITLE, title)
        cv.put(COLUMN_AUTHOR, author)
        cv.put(COLUMN_THUMB, thumb)
        cv.put(COLUMN_URL, url)
        cv.put(COLUMN_DESC, desc)
        cv.put(COLUMN_FEATURED, feature)
        cv.put(COLUMN_CATEGORY, category)
        if (db.insert(TABLE_NAME, null as String?, cv) == -1L) {
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Book added to favorite", Toast.LENGTH_SHORT).show()
        }
    }

    fun readAllData(): Cursor? {
        val db = readableDatabase
        return db?.rawQuery("SELECT * FROM favorite_table", null as Array<String?>?)
    }

    fun deleteOneRowbyName(row_id: String) {
        if (writableDatabase.delete(TABLE_NAME, "book_title=?", arrayOf(row_id)).toLong() == -1L) {
            Toast.makeText(context, "Failed to Delete.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Book removed from favorite.", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(Throwable::class)
    protected fun finalize() {
        close()
    }

    companion object {
        private const val COLUMN_AUTHOR = "book_author"
        private const val COLUMN_CATEGORY = "book_category"
        private const val COLUMN_DESC = "book_desc"
        private const val COLUMN_FEATURED = "book_featured"
        private const val COLUMN_ID = "_id"
        private const val COLUMN_THUMB = "book_thumb"
        private const val COLUMN_TITLE = "book_title"
        private const val COLUMN_URL = "book_url"
        private const val DATABASE_NAME = "EBook.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "favorite_table"
        private var sInstance: DatabaseHelper? = null
        fun getInstance(context: Context): DatabaseHelper? {

            // Use the application context, which will ensure that you
            // don't accidentally leak an Activity's context.
            if (sInstance == null) {
                sInstance = DatabaseHelper(context.applicationContext)
            }
            return sInstance
        }
    }
}