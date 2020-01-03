package id.piuwcreative.mypreloaddata;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import id.piuwcreative.mypreloaddata.services.DataManagerService;

import static id.piuwcreative.mypreloaddata.services.DataManagerService.CANCEL_MESSAGE;
import static id.piuwcreative.mypreloaddata.services.DataManagerService.FAILED_MESSAGE;
import static id.piuwcreative.mypreloaddata.services.DataManagerService.PREPARATION_MESSAGE;
import static id.piuwcreative.mypreloaddata.services.DataManagerService.SUCCESS_MESSAGE;
import static id.piuwcreative.mypreloaddata.services.DataManagerService.UPDATE_MESSAGE;

public class MainActivity extends AppCompatActivity implements HandlerCallback{
    Messenger mBoundService;
    boolean mServiceBound;

    private ProgressBar progressBar;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBoundService = new Messenger(iBinder);
            mServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mServiceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progress_bar);

        Intent mBoundServiceIntent = new Intent(MainActivity.this, DataManagerService.class);
        Messenger mActivityMessenger = new Messenger(new IncomingHandler(this));
        mBoundServiceIntent.putExtra(DataManagerService.ACTIVITY_HANDLER, mActivityMessenger);

        bindService(mBoundServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    @Override
    public void preparation() {
        Toast.makeText(this, "MEMULAI MEMUAT DATA", Toast.LENGTH_LONG).show();
    }

    @Override
    public void updateProgress(long progress) {
        progressBar.setProgress((int) progress);
    }

    @Override
    public void loadSuccess() {
        Toast.makeText(this, "BERHASIL", Toast.LENGTH_LONG).show();
        startActivity(new Intent(MainActivity.this, MahasiswaActivity.class));
        finish();
    }

    @Override
    public void loadFailed() {
        Toast.makeText(this, "GAGAL", Toast.LENGTH_LONG).show();
    }

    @Override
    public void loadCancel() {
        Toast.makeText(this, "CANCEL", Toast.LENGTH_LONG).show();
    }

    private static class IncomingHandler extends Handler {
        WeakReference<HandlerCallback> weakCallback;

        IncomingHandler(HandlerCallback callback) {
            weakCallback = new WeakReference<>(callback);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case PREPARATION_MESSAGE:
                    weakCallback.get().preparation();
                    break;
                case UPDATE_MESSAGE :
                    Bundle bundle = msg.getData();
                    long progress = bundle.getLong("KEY_PROGRESS");
                    weakCallback.get().updateProgress(progress);
                    break;
                case SUCCESS_MESSAGE:
                    weakCallback.get().loadSuccess();
                    break;
                case FAILED_MESSAGE:
                    weakCallback.get().loadFailed();
                    break;
                case CANCEL_MESSAGE :
                    weakCallback.get().loadCancel();
            }
        }
    }

}

interface HandlerCallback{
    void preparation();

    void updateProgress(long progress);

    void loadSuccess();

    void loadFailed();

    void loadCancel();

}
