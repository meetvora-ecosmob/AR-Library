package ecosmob.arexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.myarlib.MyArCameraActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyArCameraActivity.doesDeviceSupportAR(this, new MyArCameraActivity.IsArSupported() {
            @Override
            public void isArSupported(boolean isSupported) {
                if (isSupported) {
                    new MyArCameraActivity.Builder(MainActivity.this)
                            .setNetworkResource("https://github.com/KhronosGroup/glTF-Sample-Models/raw/master/2.0/Duck/glTF/Duck.gltf")
//                            .setNetworkResource("https://github.com/KhronosGroup/glTF-Sample-Models/raw/master/2.0/DamagedHelmet/glTF/DamagedHelmet.gltf")
                            .launchArCameraActivity();
                } else {
                    Toast.makeText(getApplicationContext(), "Sorry, this device does not support AR Core!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}