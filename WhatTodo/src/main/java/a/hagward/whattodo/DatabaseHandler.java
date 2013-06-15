package a.hagward.whattodo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anders on 2013-06-08.
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "todos.db";

    private static final String TABLE_TODOS = "todos";

    private static final String KEY_ID = "id";
    private static final String KEY_UTIME = "utime";
    private static final String KEY_COMPLETED = "completed"; // 0 = not completed, 1 = completed
    private static final String KEY_TITLE = "title";

    private static final String DATABASE_CREATE = "create table "
            + TABLE_TODOS + "(" + KEY_ID
            + " integer primary key autoincrement, " + KEY_UTIME
            + " integer, " + KEY_COMPLETED
            + " integer, " + KEY_TITLE
            + " text);";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        Log.w(DatabaseHandler.class.getName(),
                "Upgrading database from version " + i + " to " + i2
                        + ". This will destroy all data.");
        sqLiteDatabase.execSQL("drop table if exists " + TABLE_TODOS);
        onCreate(sqLiteDatabase);
    }

    public void addTodo(Todo todo) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_UTIME, todo.getUtime());
        values.put(KEY_COMPLETED, todo.getCompleted());
        values.put(KEY_TITLE, todo.getTitle());

        db.insert(TABLE_TODOS, null, values);
        db.close();
    }

    public Todo getTodo(int id) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_TODOS, new String[] {KEY_ID,
                KEY_UTIME, KEY_COMPLETED, KEY_TITLE}, KEY_ID + "=?",
                new String[] {String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Todo todo = new Todo(cursor.getLong(0), cursor.getLong(1), cursor.getInt(2),
                cursor.getString(3));
        return todo;
    }

    public List<Todo> getAllTodos(int completed) {
        List<Todo> todoList = new ArrayList<Todo>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_TODOS, new String[] {KEY_ID,
                KEY_UTIME, KEY_COMPLETED, KEY_TITLE}, KEY_COMPLETED + "=?",
                new String[] {String.valueOf(completed)}, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Todo todo = new Todo();
                todo.setId(cursor.getLong(0));
                todo.setUtime(cursor.getLong(1));
                todo.setCompleted(cursor.getInt(2));
                todo.setTitle(cursor.getString(3));
                todoList.add(todo);
            } while (cursor.moveToNext());
        }

        return todoList;
    }

    public int getTodoCount() {
        String countQuery = "select " + KEY_ID + " from " + TABLE_TODOS;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        return cursor.getCount();
    }

    public long getNextId() {
        String query = "select last_insert_rowid() from " + TABLE_TODOS;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst())
            return cursor.getLong(0) + 1L;
        else
            return 1L;
    }

    public int updateTodo(Todo todo) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_UTIME, todo.getUtime());
        values.put(KEY_COMPLETED, todo.getCompleted());
        values.put(KEY_TITLE, todo.getTitle());

        return db.update(TABLE_TODOS, values, KEY_ID + "=?",
                new String[] {String.valueOf(todo.getId())});
    }

    public void deleteTodo(Todo todo) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_TODOS, KEY_ID + " = ?",
                new String[] {String.valueOf(todo.getId())});
        db.close();
    }
}
