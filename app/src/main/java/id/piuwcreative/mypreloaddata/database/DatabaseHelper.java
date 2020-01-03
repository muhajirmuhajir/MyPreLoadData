package id.piuwcreative.mypreloaddata.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static android.provider.BaseColumns._ID;
import static id.piuwcreative.mypreloaddata.database.DatabaseContract.MahasiswaColumns.NAMA;
import static id.piuwcreative.mypreloaddata.database.DatabaseContract.MahasiswaColumns.NIM;
import static id.piuwcreative.mypreloaddata.database.DatabaseContract.TABLE_NAME;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "dbmahasiswa";

    private static final int DATABASE_VERSION = 1;

    private static String CREATE_TABLE_MAHASISWA = "create table "+ TABLE_NAME+
            " ("+ _ID+ " integer primary key autoincrement, "+
            NAMA + " text not null, " +
            NIM + " text not null);";

    public DatabaseHelper(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_MAHASISWA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
