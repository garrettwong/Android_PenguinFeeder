package com.core.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GWM on 6/5/17.
 */

public interface IRepository<T> {
    List<T> getAll();
    T getById(int id);
    void add(T t);
    void update(T t);
    void delete(int id);
}
