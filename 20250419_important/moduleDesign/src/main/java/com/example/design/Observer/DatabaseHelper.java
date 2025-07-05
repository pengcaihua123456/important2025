package com.example.design.Observer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "voice_commands.db";
    private static final int DATABASE_VERSION = 1;

    // 表结构定义
    private static final String CREATE_TABLE_VOICE_LOGS =
            "CREATE TABLE voice_logs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "command TEXT NOT NULL," +
                    "timestamp INTEGER NOT NULL)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建表
        db.execSQL(CREATE_TABLE_VOICE_LOGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 简单处理：删除旧表，创建新表
        db.execSQL("DROP TABLE IF EXISTS voice_logs");
        onCreate(db);
    }
}
