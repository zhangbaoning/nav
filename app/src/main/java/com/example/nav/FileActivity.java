package com.example.nav;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.*;
import android.webkit.MimeTypeMap;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import okhttp3.*;

import java.io.File;

import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE_MODAL;

public class FileActivity extends AppCompatActivity {
    boolean isLongClick = false;
    File file;
    File selectFile;
    OkHttpClient client = new OkHttpClient();
    ArrayAdapter arrayAdapter;

    private void checkPermission() {

        if (Build.VERSION.SDK_INT >= 23) {
            int write = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (write != PackageManager.PERMISSION_GRANTED || read != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 300);
            } else {
                String name = "CrashDirectory";
                File file1 = new File(Environment.getExternalStorageDirectory(), name);
                if (file1.mkdirs()) {
                    Log.i("wytings", "permission -------------> " + file1.getAbsolutePath());
                } else {
                    Log.i("wytings", "permission -------------fail to make file ");
                }
            }
        } else {
            Log.i("wytings", "------------- Build.VERSION.SDK_INT < 23 ------------");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 300) {
            Log.i("wytings", "--------------requestCode == 300->" + requestCode + "," + permissions.length + "," + grantResults.length);
        } else {
            Log.i("wytings", "--------------requestCode != 300->" + requestCode + "," + permissions + "," + grantResults);
        }
    }

    public final String[] EXTERNAL_PERMS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public final int EXTERNAL_REQUEST = 138;


    public boolean requestForPermission() {

        boolean isPermissionOn = true;
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            if (!canAccessExternalSd()) {
                isPermissionOn = false;
                requestPermissions(EXTERNAL_PERMS, EXTERNAL_REQUEST);
            }
        }

        return isPermissionOn;
    }

    public boolean canAccessExternalSd() {
        return (hasPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE));
    }

    private boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, perm));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        checkPermission();
        // TextView textView = findViewById(R.id.textView);
        File file = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DCIM);
        File file1 = file.getParentFile();
        StringBuffer stringBuffer = new StringBuffer();
        dirList(Environment.getExternalStorageDirectory());
        // textView.setText(stringBuffer.toString());

    }

    void dirList(final File file) {
        this.file = file;

        arrayAdapter = new ArrayAdapter(FileActivity.this, android.R.layout.simple_list_item_1, file.list());
        final ListView listView = findViewById(R.id.fileList);
        listView.setChoiceMode(CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                String fileName = (String) arrayAdapter.getItem(position);
                selectFile = new File(file, fileName);
                if (selectFile.isFile()) {
                    sendFile(selectFile);
                } else {


                    for (File listFile : selectFile.listFiles()) {
                        if (listFile.isFile()) {
                            sendFile(listFile);
                        }
                    }
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                menu.add("同步");
                arrayAdapter = new ArrayAdapter(FileActivity.this, android.R.layout.simple_list_item_multiple_choice, file.list());
                listView.setAdapter(arrayAdapter);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getTitle().equals("同步")) {
                    CharSequence charSequence = "开始同步";
                    Toast toast = Toast.makeText(getApplicationContext(), charSequence, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    sendFile(selectFile);
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                isLongClick = true;
                return true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (!isLongClick) {


                    if (file.isDirectory()) {


                        String parentFile = (String) arrayAdapter.getItem(position);
                        File listFile = new File(file.getAbsolutePath(), parentFile);
                        if (listFile.isDirectory()) {
                            dirList(listFile);
                        } else {
                            openFile(listFile);
                        }

                    } else {
                        openFile(file);
                    }
                }
            }
        });
//        for (File listFile : file.listFiles()) {
//            System.out.println(listFile.getName());
//        }
        listView.setAdapter(arrayAdapter);
    }

    void openFile(File file) {
        sendFile(file);
        if (true) {

            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            Uri url = FileProvider.getUriForFile(this, "com.example.nav.fileprovider", file);

            try {
                Uri uri = Uri.fromFile(file);
                ContentResolver cR = this.getContentResolver();
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                String type = mime.getExtensionFromMimeType(cR.getType(url));
                intent.setDataAndType(url, MimeTypeMap.getSingleton().getMimeTypeFromExtension(type));
            } catch (Exception e) {
                e.printStackTrace();
            }
            startActivity(intent);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//当返回按键被按下
            //调用exit()方法
            dirList(this.file.getParentFile());
        }
        return true;
    }

    public void sendFile(final File file) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("filePath",file.getParent())
                        .addFormDataPart("uploadfile", file.getName(),
                                RequestBody.create(MediaType.parse("multipart/form-data"), file))
                        .build();
                Request request = new Request.Builder()
                        .method("POST", requestBody)
                        .url("http://139.9.212.1:8080/upload").build();
                        //.url("http://192.168.1.208:8080/upload").build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        CharSequence charSequence = "上传成功";
                        Looper.prepare();
                        Toast.makeText(getBaseContext(), charSequence, Toast.LENGTH_LONG).show();
                        Looper.loop();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
