package my.assignment.androidhandler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final static int SET_PROGRESS_BAR_VISIBILITY = 0;
    private final static int SET_PROGRESS_BAR_INVISIBILITY = 3;
    private final static int PROGRESS_UPDATE = 1;
    private final static int SET_TEXT_VIEW=2;
    private final static int SET_TOAST=4;
    private int mDelay = 500;


    ProgressBar progressBar;
    EditText edtxt;
    TextView imageUrltxt;
    ByteArrayOutputStream byteArrayOutputStream;



    static class UIHandler extends Handler {
        WeakReference<MainActivity> mParent;

        public UIHandler(WeakReference<MainActivity> parent) {
            mParent = parent;
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity parent = mParent.get();
            if (null != parent) {
                switch (msg.what) {
                    case SET_PROGRESS_BAR_VISIBILITY: {
                        parent.getProgressBar().setVisibility(View.VISIBLE);
                        parent.getProgressBar().getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);

                        break;
                    }
                    case SET_PROGRESS_BAR_INVISIBILITY: {
                        parent.getProgressBar().setVisibility(View.INVISIBLE);
                        break;
                    }
                    case PROGRESS_UPDATE: {
                        parent.getProgressBar().setProgress((Integer) msg.obj);
                        break;
                    }
                    case SET_TEXT_VIEW:{
                        parent.getTextView().setText(msg.obj.toString());
                        break;
                    }
                    case SET_TOAST:{
                        Toast.makeText(parent,msg.obj.toString(),Toast.LENGTH_LONG).show();
                        break;
                    }

                    }
                }
            }
        }



    Handler handler = new UIHandler(new WeakReference<MainActivity>(
            this));


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar=(ProgressBar)findViewById(R.id.progressBar);
        edtxt=(EditText)findViewById(R.id.editText);
        imageUrltxt=(TextView)findViewById(R.id.textView);
        byteArrayOutputStream=new ByteArrayOutputStream();

    }
    public void downloadImage(View view){

       new Thread(new DownLoadImageTask(handler)).start();
        edtxt.setText("");


    }
    public String storeImage(){
        String imageFileName="";
        if(canWriteOnExternalStorage()){
            File sdcard = Environment.getExternalStorageDirectory();
            File dir = new File(sdcard.getAbsolutePath() + "/mydir/");// to this path add a new directory path
            if(!dir.exists())
            dir.mkdir();                                                    // create this directory if not already created

            Log.i("Storage Path:",dir.toString());
            Random generator = new Random();
            int n = 10000;
            n = generator.nextInt(n);
            String fname = "Image-" + n + ".jpg";
            File file = new File(dir,fname);
            try{
                file.createNewFile();
                FileOutputStream fos=new FileOutputStream(file);

                fos.write(byteArrayOutputStream.toByteArray());
                fos.close();
                imageFileName="Image "+fname+"  saved in "+dir.toString();

            }catch (Exception e){
                Log.e("Store Image:",e.getMessage());

            }

        }
        return imageFileName;
    }


    public  boolean canWriteOnExternalStorage() {

        String state = Environment.getExternalStorageState(); // get the state of your external storage
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Log.d("MainActivity:","YES External Storage is avialable");   // if storage is mounted return true
            return true;
        }
        return false;
    }

    private class DownLoadImageTask implements Runnable {

        private final Handler handler;

        DownLoadImageTask( Handler handler) {
            this.handler = handler;
        }
        public void run(){
            String text=edtxt.getText().toString();
            Message msg = handler.obtainMessage(SET_PROGRESS_BAR_VISIBILITY,
                    ProgressBar.VISIBLE);
            handler.sendMessage(msg);

            try {
                URL imageUrl = new URL(text);
                HttpURLConnection conn=(HttpURLConnection)imageUrl.openConnection();
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);
                conn.setInstanceFollowRedirects(true);
                InputStream is=conn.getInputStream();
                Bitmap bitmap= BitmapFactory.decodeStream(is);
                bitmap.compress(Bitmap.CompressFormat.PNG,60,byteArrayOutputStream);
                if(bitmap!=null) {
                    sleep();
                    String name = storeImage();
                    msg = handler.obtainMessage(SET_TOAST, name);
                    handler.sendMessage(msg);
                }else{
                    msg = handler.obtainMessage(SET_TOAST, "Image does not exist or Network Error");
                    handler.sendMessage(msg);
                }

            }catch (Exception e){
                if(e.getMessage()==null){
                    msg = handler.obtainMessage(SET_TOAST, "Image can not be downloaded");
                    handler.sendMessage(msg);
                }else {
                    Log.e("Download Image:", e.getMessage());
                    msg = handler.obtainMessage(SET_TOAST, e.getMessage());
                    handler.sendMessage(msg);
                }

            }

            for (int i = 1; i < 11; i++) {
                msg = handler.obtainMessage(PROGRESS_UPDATE, i * 10);
                handler.sendMessage(msg);
            }
            msg = handler.obtainMessage(SET_PROGRESS_BAR_INVISIBILITY,
                    ProgressBar.INVISIBLE);
            handler.sendMessage(msg);
            msg = handler.obtainMessage(SET_TEXT_VIEW,
                    text);

            handler.sendMessage(msg);


        }
        private void sleep() {
            try {
                Thread.sleep(mDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }



    public ProgressBar getProgressBar() {
        return progressBar;
    }
    public TextView getTextView(){
        return imageUrltxt;
    }


}
