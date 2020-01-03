package id.piuwcreative.mypreloaddata.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import id.piuwcreative.mypreloaddata.R;
import id.piuwcreative.mypreloaddata.database.MahasiswaHelper;
import id.piuwcreative.mypreloaddata.model.MahasiswaModel;
import id.piuwcreative.mypreloaddata.pref.AppPreference;

public class DataManagerService extends Service {
    private Messenger mActivityMessenger;

    private LoadDataAsync loadData;


    public static final int PREPARATION_MESSAGE = 0;
    public static final int UPDATE_MESSAGE = 1;
    public static final int SUCCESS_MESSAGE = 2;
    public static final int FAILED_MESSAGE = 3;
    public static final int CANCEL_MESSAGE = 4;
    public static final String ACTIVITY_HANDLER = "activity_handler";


    public DataManagerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        loadData = new LoadDataAsync(this, myCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        loadData.cancel(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        mActivityMessenger = intent.getParcelableExtra(ACTIVITY_HANDLER);
        loadData.execute();
        return mActivityMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("test", "tes");
        loadData.cancel(true);
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    private final LoadDataCallback myCallback = new LoadDataCallback() {
        @Override
        public void onPreLoad() {
            sendMessage(PREPARATION_MESSAGE);
        }

        @Override
        public void onProgressUpdate(long progress) {
            try {
                Message message = Message.obtain(null, UPDATE_MESSAGE);
                Bundle bundle = new Bundle();
                bundle.putLong("KEY_PROGRESS", progress);
                message.setData(bundle);
                mActivityMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onLoadSuccess() {
            sendMessage(SUCCESS_MESSAGE);
        }

        @Override
        public void onLoadFailed() {
            sendMessage(FAILED_MESSAGE);
        }

        @Override
        public void onLoadCancel() {
            sendMessage(CANCEL_MESSAGE);
        }
    };

    public void sendMessage(int messageStatus) {
        Message message = Message.obtain(null, messageStatus);
        try {
            mActivityMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static class LoadDataAsync extends AsyncTask<Void, Integer, Boolean> {
        private final WeakReference<Context> context;
        private final WeakReference<LoadDataCallback> weakCallback;

        private static final double MAX_PROGRESS = 100;


        public LoadDataAsync(Context context, LoadDataCallback callback) {
            this.context = new WeakReference<>(context);
            this.weakCallback = new WeakReference<>(callback);

        }

        @Override
        protected void onPreExecute() {
            weakCallback.get().onPreLoad();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            MahasiswaHelper mahasiswaHelper = MahasiswaHelper.getInstance(context.get());
            AppPreference appPreference = new AppPreference(context.get());
            Boolean firstRun = appPreference.getFirstRun();

            if (firstRun) {
                ArrayList<MahasiswaModel> mahasiswaModels = preLoadRaw();

                mahasiswaHelper.open();
                double progress = 30;
                publishProgress((int) progress);
                double progressMaxInsert = 80.0;

                double progressDiff = (progressMaxInsert - progress) / mahasiswaModels.size();

                boolean isInsertSuccess;

                //Gunakan ini untuk insert query dengan menggunakan standar query

                try {
                    mahasiswaHelper.beginTransaction();
                    for (MahasiswaModel model : mahasiswaModels) {
                        if (isCancelled()) {
                            break;
                        } else {
                            mahasiswaHelper.insertTransaction(model);
                            progress += progressDiff;
                            publishProgress((int) progress);
                        }

                    }
                    if (isCancelled()) {
                        isInsertSuccess = false;
                        appPreference.setFirstRun(true);
                        weakCallback.get().onLoadCancel();
                    } else {
                        mahasiswaHelper.setTransactionSuccess();
                        isInsertSuccess = true;
                        appPreference.setFirstRun(false);
                    }


                } catch (Exception e) {
                    isInsertSuccess = false;
                }finally {
                    mahasiswaHelper.endTransaction();
                }

                //akhir dari query
                mahasiswaHelper.close();

                publishProgress((int) MAX_PROGRESS);
                return isInsertSuccess;
            } else {
                try {
                    synchronized (this) {
                        this.wait(2000);

                        publishProgress(50);

                        this.wait(2000);
                        publishProgress((int) MAX_PROGRESS);

                        return true;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            weakCallback.get().onProgressUpdate(values[0]);

        }

        @Override
        protected void onCancelled() {
            weakCallback.get().onLoadCancel();
        }


        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                weakCallback.get().onLoadSuccess();
            } else {
                weakCallback.get().onLoadFailed();
            }
        }

        private ArrayList<MahasiswaModel> preLoadRaw() {
            ArrayList<MahasiswaModel> mahasiswaModels = new ArrayList<>();
            String line;
            BufferedReader reader;
            try {
                InputStream raw_dict = context.get().getResources().openRawResource((R.raw.data_mahasiswa));
                reader = new BufferedReader(new InputStreamReader(raw_dict));
                do {
                    line = reader.readLine();
                    String[] splitstr = line.split("\t");
                    MahasiswaModel mahasiswaModel;
                    mahasiswaModel = new MahasiswaModel();
                    mahasiswaModel.setName(splitstr[0]);
                    mahasiswaModel.setNim(splitstr[1]);
                    mahasiswaModels.add(mahasiswaModel);
                } while (line != null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mahasiswaModels;
        }
    }

}

interface LoadDataCallback {
    void onPreLoad();

    void onProgressUpdate(long progress);

    void onLoadSuccess();

    void onLoadFailed();

    void onLoadCancel();
}
