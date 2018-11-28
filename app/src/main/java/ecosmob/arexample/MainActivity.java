package ecosmob.arexample;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import ecosmob.myarlib.MyArCameraActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etRenderObjectURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etRenderObjectURL = findViewById(R.id.etRenderObjectURL);
//        etRenderObjectURL.setText("https://github.com/KhronosGroup/glTF-Sample-Models/raw/master/2.0/DamagedHelmet/glTF/DamagedHelmet.gltf");
        etRenderObjectURL.setText("https://github.com/KhronosGroup/glTF-Sample-Models/raw/master/2.0/Duck/glTF/Duck.gltf");
        findViewById(R.id.btnRender).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renderArObject(etRenderObjectURL.getText().toString());
            }
        });
    }

    private void renderArObject(String objectURL) {
        MyArCameraActivity.doesDeviceSupportAR(MainActivity.this, new MyArCameraActivity.IsArSupported() {
            @Override
            public void isArSupported(boolean isSupported) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isSupported) {
                    new MyArCameraActivity.Builder(MainActivity.this)
                            .setNetworkResource(objectURL)
                            .launchArCameraActivity();
                } else {
                    Toast.makeText(getApplicationContext(), "Sorry, this device does not support AR Core!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}