package id.piuwcreative.mypreloaddata.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;

import id.piuwcreative.mypreloaddata.model.MahasiswaModel;

import static android.provider.BaseColumns._ID;
import static id.piuwcreative.mypreloaddata.database.DatabaseContract.MahasiswaColumns.NAMA;
import static id.piuwcreative.mypreloaddata.database.DatabaseContract.MahasiswaColumns.NIM;
import static id.piuwcreative.mypreloaddata.database.DatabaseContract.TABLE_NAME;

public class MahasiswaHelper {
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase database;

    private static MahasiswaHelper INSTANCE;

    public MahasiswaHelper(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    public static MahasiswaHelper getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SQLiteOpenHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MahasiswaHelper(context);
                }
            }
        }

        return INSTANCE;
    }

    public void open() throws SQLException {
        database = databaseHelper.getWritableDatabase();
    }

    public void close() {
        databaseHelper.close();
        if (database.isOpen()) {
            database.close();
        }
    }

    public ArrayList<MahasiswaModel> getAllData() {
        Cursor cursor = database.query(TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                _ID+" ASC",
                null);

        cursor.moveToFirst();
        ArrayList<MahasiswaModel> arrayList = new ArrayList<>();
        MahasiswaModel mahasiswaModel;

        if (cursor.getCount() > 0) {
            do {
                mahasiswaModel = new MahasiswaModel();
                mahasiswaModel.setId(cursor.getInt(cursor.getColumnIndexOrThrow(_ID)));
                mahasiswaModel.setName(cursor.getString(cursor.getColumnIndexOrThrow(NAMA)));
                mahasiswaModel.setNim(cursor.getString(cursor.getColumnIndexOrThrow(NIM)));

                arrayList.add(mahasiswaModel);
                cursor.moveToNext();

            } while (!cursor.isAfterLast());
        }
        cursor.close();

        return arrayList;
    }

    public ArrayList<MahasiswaModel> getDataByName(String name) {
        Cursor cursor = database.query(TABLE_NAME,
                null,
                NAMA + " LIKE ?",
                new String[]{name},
                null,
                null,
                _ID + " ASC",
                null);

        cursor.moveToFirst();
        ArrayList<MahasiswaModel> arrayList = new ArrayList<>();
        MahasiswaModel mahasiswaModel;
        if (cursor.getCount() > 0) {
            do {
                mahasiswaModel = new MahasiswaModel();
                mahasiswaModel.setId(cursor.getInt(cursor.getColumnIndexOrThrow(_ID)));
                mahasiswaModel.setName(cursor.getString(cursor.getColumnIndexOrThrow(NAMA)));
                mahasiswaModel.setNim(cursor.getString(cursor.getColumnIndexOrThrow(NIM)));

                arrayList.add(mahasiswaModel);
                cursor.moveToNext();

            } while (!cursor.isAfterLast());
        }
        cursor.close();
        return arrayList;
    }

    public long insert(MahasiswaModel mahasiswaModel) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NAMA, mahasiswaModel.getName());
        contentValues.put(NIM, mahasiswaModel.getNim());

        return database.insert(TABLE_NAME, null, contentValues);
    }

    public void beginTransaction() {
        database.beginTransaction();
    }

    public void setTransactionSuccess() {
        database.setTransactionSuccessful();
    }

    public void endTransaction() {
        database.endTransaction();
    }

    public void insertTransaction(MahasiswaModel mahasiswaModel) {
        String sql = "INSERT INTO "+ TABLE_NAME +"("+NAMA +", "+NIM +") VALUES (?, ?)";
        SQLiteStatement stmt = database.compileStatement(sql);
        stmt.bindString(1, mahasiswaModel.getName());
        stmt.bindString(2, mahasiswaModel.getNim());
        stmt.execute();
        stmt.clearBindings();
    }
}
