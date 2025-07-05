package com.example.design.Observer;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;



public class DatabaseEngine {
    // 使用 SQLiteOpenHelper 获取数据库实例
    private final SQLiteDatabase db;
    private final DatabaseHelper dbHelper; // 自定义的 SQLiteOpenHelper

    public DatabaseEngine(Context context) {
        // 正确初始化：通过 DatabaseHelper 获取数据库实例
        this.dbHelper = new DatabaseHelper(context);
        this.db = dbHelper.getWritableDatabase();
    }

    public void logVoiceCommand(String command) {
        ContentValues values = new ContentValues();
        values.put("command", command);
        values.put("timestamp", System.currentTimeMillis());

        try {
            // 插入数据到 voice_logs 表
            db.insert("voice_logs", null, values);
        } catch (Exception e) {
            Log.e("DatabaseEngine", "保存失败: " + e.getMessage());
        }
    }

    // 关闭数据库连接（可选）
    public void close() {
        if (db != null && db.isOpen()) {
            db.close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}

