package com.example.kms;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.text.SimpleDateFormat;

@RequiresApi(api = Build.VERSION_CODES.Q)

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final int GET_GALLERY_IMAGE = 200;
    ImageView imageview;

    String fileName;
    String fileDate;
    Uri cur_img;
    File cur_file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkSelfPermission();

        Button button1 = (Button)findViewById(R.id.button3);
        Button button2 = (Button)findViewById(R.id.button2);



        button1.setOnClickListener(this);
        button2.setOnClickListener(this);


        //이미지 업데이트 버튼

        imageview = (ImageView)findViewById(R.id.imageView);
        imageview.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent. setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, GET_GALLERY_IMAGE);
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch( v.getId() ){

            case R.id.button3:
                notification();
                break;

            case R.id.button2:
                ImageUpdate();
                break;
        }

    }


    private void notification() {   //알림 띄우기

        String channelId = "channel";
        String channelName = "Channel Name";

        NotificationManager notifManager

                = (NotificationManager) getSystemService  (Context.NOTIFICATION_SERVICE);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notifManager.createNotificationChannel(mChannel);

        }
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), channelId);

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);

        int requestID = (int) System.currentTimeMillis();

        PendingIntent pendingIntent
                = PendingIntent.getActivity(getApplicationContext(), requestID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentTitle("파일 이름은!") // required
                .setContentText(fileName)  // required
                .setDefaults(Notification.DEFAULT_ALL) // 알림, 사운드 진동 설정
                .setAutoCancel(true) // 알림 터치시 반응 후 삭제
                //알림의 각종 설정들 설정
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(android.R.drawable.btn_star)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.canvas))    //R.drawable.calculator
                .setContentIntent(pendingIntent);

        notifManager.notify(0, builder.build());    //알림 실행
    }


    private void ImageUpdate() {    //선택해서 이미지 업데이트
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, GET_GALLERY_IMAGE);

        //  String ImagePath;
    }



    @Override   //이미지 선택한거 or 랜덤 적용
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            imageview.setImageURI(selectedImageUri);

            //파일 이름 전역변수에 저장
            Cursor returnCursor =
                    getContentResolver().query(selectedImageUri, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

            returnCursor.moveToFirst();
            TextView nameView = (TextView) findViewById(R.id.textView);

            fileName = returnCursor.getString(nameIndex);


            //sizeView.setText(Long.toString(returnCursor.getLong(sizeIndex)));
            //String getPathFromUri(Uri uri)
            File file = new File(getPathFromUri(selectedImageUri));
            cur_file = file;
            cur_img = selectedImageUri;
            if(file.exists()){
                SimpleDateFormat simpleDataFormat = new SimpleDateFormat("yyyy년 MM월 dd일\nHH시 mm분 ss초\n");
                fileDate = simpleDataFormat.format(file.lastModified());
            }
            nameView.setText(fileDate);
        }
    }



    public void checkSelfPermission() {
        String temp = "";
        //파일 읽기 권한 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            temp += Manifest.permission.READ_EXTERNAL_STORAGE + " ";
        }
        //파일 쓰기 권한 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            temp += Manifest.permission.WRITE_EXTERNAL_STORAGE + " ";
        }
        if (TextUtils.isEmpty(temp) == false) {
            // 권한 요청
            ActivityCompat.requestPermissions(this, temp.trim().split(" "),1);
        }
        else { // 모두 허용 상태
            Toast.makeText(this, "권한을 모두 허용", Toast.LENGTH_SHORT).show();
        }

    }

    public String getPathFromUri(Uri uri){//uri로부터 파일의 경로 얻기
        Cursor cursor = getContentResolver().query(uri, null, null, null, null );
        cursor.moveToNext();
        String path = cursor.getString( cursor.getColumnIndex( "_data" ) );
        cursor.close();
        return path;
    }
}