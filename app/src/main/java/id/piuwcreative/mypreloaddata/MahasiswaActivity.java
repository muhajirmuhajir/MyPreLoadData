package id.piuwcreative.mypreloaddata;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

import id.piuwcreative.mypreloaddata.adapter.MahasiswaAdapter;
import id.piuwcreative.mypreloaddata.database.MahasiswaHelper;
import id.piuwcreative.mypreloaddata.model.MahasiswaModel;

public class MahasiswaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mahasiswa);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        MahasiswaAdapter adapter = new MahasiswaAdapter();
        recyclerView.setAdapter(adapter);

        MahasiswaHelper mahasiswaHelper = new MahasiswaHelper(this);
        mahasiswaHelper.open();

        ArrayList<MahasiswaModel> mahasiswaModels = mahasiswaHelper.getAllData();
        mahasiswaHelper.close();

        adapter.setData(mahasiswaModels);
    }
}
