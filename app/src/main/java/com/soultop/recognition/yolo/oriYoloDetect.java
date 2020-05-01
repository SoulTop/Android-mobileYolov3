package com.soultop.recognition.yolo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.soultop.recognition.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class oriYoloDetect extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * Copies specified asset to the file in /files app directory and returns this file absolute path.
     *
     * @return absolute file path
     */
    public static String assetFilePath(String assetName, Context context) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    private Scalar randomColor() {
        Random random = new Random();
        int r = random.nextInt(255);
        int g = random.nextInt(255);
        int b = random.nextInt(255);
        return new Scalar(r,g,b);
    }

    private List<String> readLabels (String file, Context context)
    {
        AssetManager assetManager = context.getAssets();
        BufferedInputStream inputStream;
        List<String> labelsArray = new ArrayList<>();
        try {
            // Read data from assets.
            inputStream = new BufferedInputStream(assetManager.open(file));
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
            // Create copy file in storage.
            File outFile = new File(context.getFilesDir(), file);
            FileOutputStream os = new FileOutputStream(outFile);
            os.write(data);
            os.close();
            Scanner fileScanner = new Scanner(new File(outFile.getAbsolutePath())).useDelimiter("\n");
            String label;
            while (fileScanner.hasNext()) {
                label = fileScanner.next();
                labelsArray.add(label);
            }
            fileScanner.close();
        } catch (IOException ex) {
            Log.i(TAG, "Failed to read labels!");
        }
        return labelsArray;
    }

    // 创建 Handler 任务
    private Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(msg.what == DETECT_FINSH){
                oMat = new Mat();

                Imgproc.cvtColor(iMat, oMat, Imgproc.COLOR_BGR2BGRA);

                Bitmap tmpBitmap = Bitmap.createBitmap(oMat.cols(), oMat.rows(), Bitmap.Config.ARGB_8888);

                Utils.matToBitmap(oMat, tmpBitmap);
                imageView.setImageBitmap(tmpBitmap);;
                textView.setText("花费时间" + (double)msg.obj + "s.");
            }
            else{
                ;
            }
        }
    };

    private static final String TAG = "YoloDetectDemo";
    public static final String INTENT_MODULE_NAME = "INTENT_MODULE_ASSET_NAME";
    public static final String INTENT_MODULE_WEIGHTS_NAME = "INTENT_INFO_VIEW_TYPE";

    private static final int MOVING_AVG_PERIOD = 10;
    private static final String FORMAT_MS = "%dms";
    private static final String FORMAT_AVG_MS = "avg:%.0fms";

    private static final String FORMAT_FPS = "%.1fFPS";
    public static final String SCORES_FORMAT = "%.2f";


    private static final int INPUT_TENSOR_WIDTH = 416;
    private static final int INPUT_TENSOR_HEIGHT = 416;

    private static AssetManager assetManager;
    private static String[] filelist;
    private static int idx = 0;

    // opencv 识别相关
    private static final int DETECT_FINSH = 1;
    private static List<String> classNames;                     // 存放名字
    private static List<Scalar> colors=new ArrayList<>();       // 绘制框用

    private String cfg;
    private String weight;
    private String modelConfiguration;          //yolov3.cfg
    private String modelWeights;                //yolov3.weights

    private ImageView imageView;
    private TextView textView;
    private TextView textView_title;
    private Bitmap bitmap;
    private Net net;
    private Mat iMat, oMat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ori_yolo_detect);

        String model_name = "";
        cfg = getIntent().getStringExtra(INTENT_MODULE_NAME);
        weight = getIntent().getStringExtra(INTENT_MODULE_WEIGHTS_NAME);
        assetManager = this.getAssets();
        try {
            filelist = assetManager.list("img");
            bitmap = BitmapFactory.decodeStream(assetManager.open("img/" + filelist[idx++]));
            modelConfiguration = assetFilePath(cfg, this);          //yolov3.cfg
            modelWeights = assetFilePath(weight, this);                //yolov3.weights
            if(cfg.charAt(0) == 'y'){
                classNames =  readLabels("CoCo_labels.txt", this);
                if(cfg.length() < 11)
                    model_name = "yoloV3";
                else
                    model_name = "yoloV3-tiny";
            }
            else{
                classNames =  readLabels("VOC_labels.txt", this);
                model_name = "mobile_yoloV3";
            }

        }catch (IOException e){
            Log.e(TAG, "Error reading assets", e);
            finish();
        }

        textView = findViewById(R.id.timeshow);
        textView_title = findViewById(R.id.model_title);
        textView_title.setText("当前模型为: " + model_name);
        imageView = findViewById(R.id.img);
        imageView.setImageBitmap(bitmap);
        for(int i=0; i<classNames.size(); i++){
            colors.add(randomColor());
        }
    }


    public double YOLO_Detect(){

        net = Dnn.readNetFromDarknet(modelConfiguration, modelWeights);
        Mat blob = Dnn.blobFromImage(iMat, 1.0 / 255.0, new Size(INPUT_TENSOR_WIDTH, INPUT_TENSOR_HEIGHT), new Scalar(0,0,0), false, false);
        net.setInput(blob);

        List<Mat> result = new ArrayList<>();
        List<String> outBlobNames = net.getUnconnectedOutLayersNames();
        float confThreshold = 0.5f;


        long startTime = System.currentTimeMillis();        // 开始计时

        net.forward(result, outBlobNames);

        long endTime = System.currentTimeMillis();
        double usedTime = (endTime - startTime)/ 1000.0;    //结束计时

        for (int i = 0; i < result.size(); ++i) {
            // each row is a candidate detection, the 1st 4 numbers are
            // [center_x, center_y, width, height], followed by (N-4) class probabilities
            Mat level = result.get(i);
            for (int j = 0; j < level.rows(); ++j) {
                Mat row = level.row(j);
                Mat scores = row.colRange(5, level.cols());
                Core.MinMaxLocResult mm = Core.minMaxLoc(scores);
                float confidence = (float) mm.maxVal;
                Point classIdPoint = mm.maxLoc;
                if (confidence > confThreshold) {

                    int centerX = (int) (row.get(0, 0)[0] * iMat.cols());
                    int centerY = (int) (row.get(0, 1)[0] * iMat.rows());
                    int width = (int) (row.get(0, 2)[0] * iMat.cols());
                    int height = (int) (row.get(0, 3)[0] * iMat.rows());

                    int left = (int) (centerX - width * 0.5);
                    int top =(int)(centerY - height * 0.5);
                    int right =(int)(centerX + width * 0.5);
                    int bottom =(int)(centerY + height * 0.5);

                    Point left_top = new Point(left, top);
                    Point right_bottom=new Point(right, bottom);
                    Point label_left_top = new Point(left, top-5);
                    DecimalFormat df = new DecimalFormat("#.##");

                    int class_id = (int) classIdPoint.x;
                    String label= classNames.get(class_id) + ": " + df.format(confidence);
                    Scalar color= colors.get(class_id);

                    Imgproc.rectangle(iMat, left_top,right_bottom , color, 3, 2);
                    Imgproc.putText(iMat, label, label_left_top, Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 0), 4);
                    Imgproc.putText(iMat, label, label_left_top, Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 255, 255), 2);
                }
            }
        }
        return usedTime;
    }


    public double Mobile_Detect(){

        float confThreshold = 0.5f;

        net = Dnn.readNetFromCaffe(modelConfiguration, modelWeights);
        Mat Iblob = Dnn.blobFromImage(iMat, 0.007843, new Size(300, 300), new Scalar(127.5,127.5,127.5), false, false);
        net.setInput(Iblob);
        long startTime = System.currentTimeMillis();        // 开始计时
        Mat Oblob = net.forward();
        long endTime = System.currentTimeMillis();
        double usedTime = (endTime - startTime)/ 1000.0;

        List<Mat> resultsList = new ArrayList<>();
        Dnn.imagesFromBlob(Oblob, resultsList);
        Mat results = resultsList.get(0);

        int objectIdx;

        for(int i=0; i<results.rows();i++){

            float confidence = (float)results.get(i, 2)[0];

            if(confidence > confThreshold){
                objectIdx = (int)results.get(i, 1)[0];

                int left = (int) (results.get(i, 3)[0] * iMat.cols());
                int top =(int) (results.get(i, 4)[0] * iMat.rows());
                int right =(int) (results.get(i, 5)[0] * iMat.cols());
                int bottom =(int) (results.get(i, 6)[0] * iMat.rows());

                Point left_top = new Point(left, top);
                Point right_bottom=new Point(right, bottom);
                Point label_left_top = new Point(left, top-5);
                DecimalFormat df = new DecimalFormat("#.##");

                String label= classNames.get(objectIdx) + ": " + df.format(confidence);
                Scalar color= colors.get(objectIdx);

                Imgproc.rectangle(iMat, left_top,right_bottom , color, 3, 2);
                Imgproc.putText(iMat, label, label_left_top, Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 0), 4);
                Imgproc.putText(iMat, label, label_left_top, Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 255, 255), 2);

            }
        }

        return usedTime;
    }

    public void yoloDetect(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                double runTime;
                if(cfg.charAt(0) == 'y')
                    runTime = YOLO_Detect();
                else
                    runTime = Mobile_Detect();

                Message msg = new Message();
                msg.what = DETECT_FINSH;
                msg.obj = runTime;
                mhandler.sendMessage(msg);

            }
        }).start();

    }

    public void Recognize(View view){
        iMat = new Mat();
        Utils.bitmapToMat(bitmap,iMat);
        Imgproc.cvtColor(iMat, iMat, Imgproc.COLOR_RGBA2RGB);

        yoloDetect();
    }

    public void Next(View view){
        if(idx >= filelist.length)
            idx = 0;
        try {
            bitmap = BitmapFactory.decodeStream(assetManager.open("img/" + filelist[idx++]));
            imageView.setImageBitmap(bitmap);
        }catch (IOException e){
            Log.e(TAG, "Error reading assets", e);
            finish();
        }
    }



    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        idx = 0;
    }
}
