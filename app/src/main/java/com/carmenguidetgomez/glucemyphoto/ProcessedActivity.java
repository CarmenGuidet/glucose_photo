package com.carmenguidetgomez.glucemyphoto;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static org.opencv.android.Utils.matToBitmap;
import static org.opencv.imgproc.Imgproc.cvtColor;


public class ProcessedActivity extends AppCompatActivity {
    ImageView imageView;
    Button Camera;
    FloatingActionButton Gallery;

    Button processing;
    TextView textViewMedia;
    TextView textViewDate;
    TextView textViewGlucosa;
    Uri imagenUri;

    int SELEC_IMAGEN = 200;
    int PICK_IMAGE_REQUEST = 100;

    StorageReference mStorage;
    String selectedImagePath;


    String routeImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processed);

        imageView = findViewById(R.id.imageViewPhoto);
        Camera = findViewById(R.id.floatingActionButtonCamera);
        Gallery = findViewById(R.id.floatingActionButtonGallery);
        textViewGlucosa = findViewById(R.id.textViewGlucosa);
        textViewDate = findViewById(R.id.textViewDate);

        mStorage = FirebaseStorage.getInstance().getReference();



        Camera.setOnClickListener(v -> openCamera());

        Gallery.setOnClickListener(v -> openGallery());



    }


    private void openCamera() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (getIntent().resolveActivity(getPackageManager()) !=null){

            File imageFile = null;
            try {
                imageFile = createImage();
            } catch (IOException ex) {
                Log.e("Error", ex.toString());
            }

            if (imageFile != null) {
                Uri imageuri = FileProvider.getUriForFile(this, "com.carmenguidetgomez.glucemyphoto", imageFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        }
    }


    private File createImage() throws IOException {
        String namePhoto = "image_";
        File directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(namePhoto, ".jpg", directory);
        routeImage = image.getAbsolutePath();

        return image;

    }

    @SuppressLint("IntentReset")
    private void openGallery() {
        Intent galeria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        galeria.setType("image/");
        startActivityForResult(galeria, SELEC_IMAGEN);

    }

    @SuppressLint("SetTextI18n")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && (resultCode == RESULT_OK)) {
            StorageReference filePath = mStorage.child("Samples").child(routeImage);
            Calendar calendar = Calendar.getInstance();

            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");
            String dateTime = simpleDateFormat.format(calendar.getTime());

            Bitmap bitmap_route = BitmapFactory.decodeFile(routeImage);
            Mat sourceImage = new Mat();
            Utils.bitmapToMat(bitmap_route , sourceImage);
            final double media = GetGlucose(sourceImage);
            String string_media = Double.toString(media);
            final double glucose = 5.4525 * media - 75.72 ;

            textViewGlucosa.setText(Double.toString(glucose));
            imageView.setImageBitmap(bitmap_route);
            textViewDate.setText(dateTime);
            processing = findViewById(R.id.buttonProcessing);
            processing.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    saveGlucose(media, glucose, filePath.toString(), dateTime);
                }
            });

        } else if (resultCode == RESULT_OK && requestCode == SELEC_IMAGEN) {

            imagenUri = data.getData();
            try {
                Bitmap bitmap_Uri = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imagenUri);
                selectedImagePath = saveToInternalStorage(bitmap_Uri);
                Mat sourceImage = new Mat();
                Calendar calendar = Calendar.getInstance();

                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");
                String  dateTime = simpleDateFormat.format(calendar.getTime());

                Utils.bitmapToMat(bitmap_Uri, sourceImage);
                StorageReference filePath = mStorage.child("Samples").child(imagenUri.getLastPathSegment());

                final double media = GetGlucose(sourceImage);
                String string_media = Double.toString(media);
                imageView.setImageBitmap(bitmap_Uri);
                String textmedia = "El valor de la media es:";

                final double glucose = 5.4525 * media - 75.72 ;
                String textglucose ="El valor de la glucosa es:";
                textViewGlucosa.setText( textglucose + Double.toString(glucose));
                textViewDate.setText(dateTime);

                processing = findViewById(R.id.buttonProcessing);
                processing.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        saveGlucose(media, glucose, filePath.toString(), dateTime);
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(ProcessedActivity.this, "Select a photo!", Toast.LENGTH_LONG).show();
        }

    }


    private String saveToInternalStorage(Bitmap bitmapImage) throws IOException {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        String namePhoto = "image_";
        File directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File mypath = File.createTempFile(namePhoto , ".jpg", directory);
        //File mypath = new File(directory + namePhoto, "profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();

    }


    private static Bitmap convertMatToBitMap(Mat input){
        Bitmap bmp = null;
        Mat rgb = new Mat();
        cvtColor(input, rgb, Imgproc.COLOR_GRAY2RGB);
        try {
            bmp = Bitmap.createBitmap(rgb.cols(), rgb.rows(), ARGB_8888);
            matToBitmap(rgb, bmp);
        }
        catch (CvException e){
            Log.d("Exception",e.getMessage());
        }
        return bmp;
    }


    public Mat cutImage(Mat src, Rect rect) {
        // Recorte de imagen
        Mat src_roi = new Mat(src, rect);
        Mat cutImage = new Mat();
        src_roi.copyTo(cutImage);
        return cutImage;
    }

    public double mean_Mat(Mat mat){
        MatOfDouble mean_hsv=new MatOfDouble();
        MatOfDouble std_hsv=new MatOfDouble();
        Core.meanStdDev(mat,mean_hsv,std_hsv);
        double mean_sum =0.0;
        double [] mean_arr = mean_hsv.toArray();
        for (int i = 0; i < mean_arr.length; i++) {
            mean_sum += mean_arr[i];
        }

        return mean_sum;
    }
    public double std_Mat(Mat mat){
        MatOfDouble mean_hsv=new MatOfDouble();
        MatOfDouble std_hsv=new MatOfDouble();
        Core.meanStdDev(mat,mean_hsv,std_hsv);
        double std_sum =0.0;
        double [] std_arr = std_hsv.toArray();
        for (int i = 0; i < std_arr.length; i++) {
            std_sum += std_arr[i];
        }

        return std_sum;
    }


    private int MaxPosArray(double[] Array){
        double iNumeroMayor = 0;
        int iPosicion=0;
        for (int x=1;x<Array.length;x++){
            if (Array[x]>iNumeroMayor) {
                iNumeroMayor = Array[x];
                iPosicion = x;
            }
        }
        return iPosicion;
    }
    private double MaxArray(double[] Array){
        double iNumeroMayor = 0;
        for (int x=1;x<Array.length;x++){
            if (Array[x]>iNumeroMayor) {
                iNumeroMayor = Array[x];

            }
        }
        return iNumeroMayor;
    }

    private double mymedia(Mat mat){
        Mat hsv_image_pro = new Mat();
        List<Mat> channels = new ArrayList<Mat>(3);
        Imgproc.cvtColor(mat, hsv_image_pro, Imgproc.COLOR_RGB2HSV);
        Core.split(hsv_image_pro, channels);
        Mat H = channels.get(0);
        Mat S = channels.get(1);
        Mat V = channels.get(2);
        double suma_media = 0;
        double valores =0;
        for (int i = 0; i < S.rows(); i++) {
            for (int j = 0; j < S.cols(); j++) {
                double[] valor= S.get(i, j);
                double[] valorH = H.get(i, j);
                if(valor[0] > 10) {
                    suma_media = suma_media + valor[0];
                    valores = valores + 1;
                }
            }
        }
        double media = suma_media/valores;

        return media;
    }

    private Double MediaVector(double[] vector){
        double suma_media =0;
        for (int i = 0; i < vector.length; i++) {
            suma_media = suma_media + vector[i];
        }
        double media = suma_media/vector.length;
        return media;
    }

    private double GetGlucose(Mat mat_image ){

        Mat mask1 = new Mat();

        Mat maskblue= new Mat();
        Mat mat_image_GRAY = new Mat ();

        Mat hsv_image =  new Mat ();
        List<MatOfPoint> contourList = new ArrayList<MatOfPoint>();


        Scalar minColor1 = new Scalar(20, 16.5, 80);
        Scalar maxColor1 = new Scalar(120, 120, 255);
        //Scalar minColor2 = new Scalar(19, 120, 80);
        //Scalar maxColor2 = new Scalar(130, 255, 255);


        Imgproc.cvtColor(mat_image, hsv_image, Imgproc.COLOR_RGB2HSV);
        Imgproc.cvtColor(mat_image, mat_image_GRAY, Imgproc.COLOR_RGB2GRAY);
        Core.inRange(hsv_image, minColor1, maxColor1, mask1);
        //Core.inRange(hsv_image, minColor2, maxColor2, mask2);
        //Core.add(mask1,mask2,maskblue);

        Mat hierarchy =new Mat();
        Imgproc.findContours(mask1, contourList, hierarchy, Imgproc.RETR_LIST,  Imgproc.CHAIN_APPROX_NONE);
        Mat contours = new Mat();
        contours.create(maskblue.rows(), maskblue.cols(), CvType.CV_8UC3);
        Point[] centers = new Point[contourList.size()];
        float[][] radius = new float[contourList.size()][1];
        MatOfPoint2f[] contoursPoly  = new MatOfPoint2f[contourList.size()];
        Rect[] boundRect = new Rect[contourList.size()];
        for (int i = 0; i < contourList.size(); i++) {
            contoursPoly[i] = new MatOfPoint2f();
            Imgproc.approxPolyDP(new MatOfPoint2f(contourList.get(i).toArray()), contoursPoly[i], 4, true);
            boundRect[i] = Imgproc.boundingRect(new MatOfPoint(contoursPoly[i].toArray()));
            centers[i] = new Point();
            Imgproc.minEnclosingCircle(contoursPoly[i], centers[i], radius[i]);
        }
        List<MatOfPoint> contoursPolyList = new ArrayList<>(contoursPoly.length);
        for (MatOfPoint2f poly : contoursPoly) {
            contoursPolyList.add(new MatOfPoint(poly.toArray()));
        }
        Mat cut_Mat= new Mat();
        Mat cut_Mat_hsv= new Mat();
        double area[] = new double[contourList.size()];
        double mediaS[] = new double[contourList.size()];
        for (int i = 0; i < contourList.size(); i++) {
            //Imgproc.drawContours(mat_image, contourList, i, new Scalar(255, 255, 0), 2);
            area[i] = Imgproc.contourArea(contoursPoly[i]);
            //cut_Mat = cutImage(mat_image, boundRect[i]);
            //mediaS[i] = mymedia(cut_Mat);
        }

        //double media = MaxArray(mediaS);
        //double mediavector = MediaVector(mediaS);

        int posmaxArea = MaxPosArray(area);
        cut_Mat = cutImage(mat_image, boundRect[posmaxArea]);
        double media = mymedia(cut_Mat);


        return media;

    }


    private void saveGlucose(double smedia, double sglucose, String urlphoto, String dateTime){
        double media = smedia;
        double glucose = sglucose;


        CollectionReference notebookRef = FirebaseFirestore.getInstance().collection("users")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .collection("samples");
        notebookRef.add(new Glucose( glucose, media, urlphoto, dateTime));
        Toast.makeText(this, "Tasks added!", Toast.LENGTH_SHORT).show();
        finish();
    }


}
