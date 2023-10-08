package com.example.birdclassificationappfinal;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.birdclassificationappfinal.database.BirdsDataSource;
import com.example.birdclassificationappfinal.ml.Birdfinal;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {

    Button camera, gallery, sendfeedback;
    ImageView imageView;
    TextView result;
    EditText userenteredfeedback;
    int imageSize = 224;

    private BirdsDataSource birdsDataSource;
    private WebView newsWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        birdsDataSource = new BirdsDataSource(this);
        birdsDataSource.open();

        camera = findViewById(R.id.button);
        gallery = findViewById(R.id.button2);
        sendfeedback = findViewById(R.id.sendFeedbackButton);
        userenteredfeedback = findViewById(R.id.feedbackEditText);
        result = findViewById(R.id.result);
        imageView = findViewById(R.id.imageView);

        // Set initial visibility
        userenteredfeedback.setVisibility(View.GONE);
        sendfeedback.setVisibility(View.GONE);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableFeedbackControls();
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 3);
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableFeedbackControls();
                Intent cameraIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(cameraIntent, 1);
            }
        });

        sendfeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String feedbackText = userenteredfeedback.getText().toString().trim();

                if (!feedbackText.isEmpty()) {
                    String birdName = result.getText().toString();
                    birdsDataSource.insertDataBirdsFeedback(birdName, feedbackText);
                    // Disable the button after clicking
                    sendfeedback.setEnabled(false);
                    sendfeedback.setText("Feedback has been sent");
                    userenteredfeedback.setEnabled(false);
                } else {
                    // Provide feedback to the user that feedback is required
                    userenteredfeedback.setError("Please enter feedback");
                }
            }
        });

        // Set OnClickListener for the classified bird name
        result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String birdName = result.getText().toString();
                if (!birdName.isEmpty()) {
                    // Open Google search for the bird name
                    openGoogleSearch(birdName);
                }
            }
        });
    }

    private void enableFeedbackControls() {
        userenteredfeedback.setEnabled(true);
        userenteredfeedback.setText("");
        sendfeedback.setEnabled(true);
        sendfeedback.setText("Send Feedback");

        // Make the EditText and Button visible
        userenteredfeedback.setVisibility(View.VISIBLE);
        sendfeedback.setVisibility(View.VISIBLE);
    }

    public void classifyImage(Bitmap image) {
        try {
            Birdfinal model = Birdfinal.newInstance(getApplicationContext());

            // Creating the input for the reference
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            // iterating over each pixel and extract RGB values and add those val to individually to byte buffer
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 1));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 1));
                    byteBuffer.putFloat((val >> 0xFF) * (1.f / 1));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Running the model inference and getting the result
            Birdfinal.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            // checking the max confidence index of the classification made by.
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
            String[] classes = {"WHITE THROATED BEE EATER", "WOODLAND KINGFISHER", "YELLOW CACIQUE"};
            result.setText(classes[maxPos]);

            // Releases model resources if no longer used here to added here.
            model.close();
        } catch (IOException e) {
            /* TODO Handle the exception */
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 3) {
                Bitmap image = (Bitmap) data.getExtras().get("data");
                int dimension = Math.min(image.getWidth(), image.getHeight());
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                imageView.setImageBitmap(image);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
            } else {
                Uri dat = data.getData();
                Bitmap image = null;
                try {
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), dat);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageView.setImageBitmap(image);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                classifyImage(image);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Method to open Google search with users browers for the bird name and more details
    private void openGoogleSearch(String query) {
        String searchUrl = "https://www.google.com/search?q=" + Uri.encode(query);
        Uri uri = Uri.parse(searchUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}
