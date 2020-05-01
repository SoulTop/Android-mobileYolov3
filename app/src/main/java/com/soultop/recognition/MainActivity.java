package com.soultop.recognition;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import com.soultop.recognition.yolo.oriYoloDetect;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.list_back).setOnClickListener(v -> finish());

        // <ViewStub> list_content_stub 展示 vision_list_content 的内容
        final View listContentStub = ((ViewStub) findViewById(R.id.list_content_stub)).inflate();

        findViewById(R.id.yolov3).setOnClickListener(v -> {
            final Intent intent = new Intent(MainActivity.this, oriYoloDetect.class);
            intent.putExtra(oriYoloDetect.INTENT_MODULE_NAME, "yolov3.cfg");
            intent.putExtra(oriYoloDetect.INTENT_MODULE_WEIGHTS_NAME, "yolov3.weights");
            startActivity(intent);
        });

        findViewById(R.id.yolo_tiny).setOnClickListener(v -> {
            final Intent intent = new Intent(MainActivity.this, oriYoloDetect.class);
            intent.putExtra(oriYoloDetect.INTENT_MODULE_NAME, "yolov3-tiny.cfg");
            intent.putExtra(oriYoloDetect.INTENT_MODULE_WEIGHTS_NAME, "yolov3-tiny.weights");
            startActivity(intent);
        });

        findViewById(R.id.mobile_yolo).setOnClickListener(v -> {
            final Intent intent = new Intent(MainActivity.this, oriYoloDetect.class);
            intent.putExtra(oriYoloDetect.INTENT_MODULE_NAME, "mobile_yolo.prototxt");
            intent.putExtra(oriYoloDetect.INTENT_MODULE_WEIGHTS_NAME, "mobile_yolo.caffemodel");
            startActivity(intent);
        });

    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
