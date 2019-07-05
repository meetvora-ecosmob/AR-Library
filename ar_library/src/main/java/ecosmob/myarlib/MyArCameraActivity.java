/*
Copyright 2018 Google LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package ecosmob.myarlib;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class MyArCameraActivity extends AppCompatActivity {

    AnchorNode myAnchorNode;
    private ArFragment fragment;
    private ModelRenderable myRenderable;
    private ProgressDialog loadingProgressDialog;

    // Check device capability for AR-Core library support and its availability on the device
    // [Source: https://stackoverflow.com/a/51684172/5373110]
    static void maybeEnableArButton(Context context, IsArSupported isArSupported) {
        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (availability.isTransient()) {
                // Re-query at 5Hz while compatibility is checked in the background.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        maybeEnableArButton(context, isArSupported);
                    }
                }, 200);
            }
            isArSupported.isArSupported(availability.isSupported());
        } else {
            isArSupported.isArSupported(false);
        }
    }

    public static void doesDeviceSupportAR(Context context, IsArSupported isArSupported) {
        maybeEnableArButton(context, isArSupported);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ecosmob.myarlib.R.layout.activity_my_ar_camera);

        fragment = (ArFragment) getSupportFragmentManager().findFragmentById(ecosmob.myarlib.R.id.sceneform_fragment);

        if (fragment == null) {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            return;
        }

        fragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            if (myRenderable == null) return;
            addNodeToScene(fragment, hitResult.createAnchor(), myRenderable);
        });

        loadingProgressDialog = new ProgressDialog(this);
        loadingProgressDialog.setMessage("Please wait while we load the 3D model");
        loadingProgressDialog.setCancelable(false);
        loadingProgressDialog.show();

        // Preparing Network 3D Model
        ModelRenderable.builder()
                .setSource(this, RenderableSource.builder().setSource(
                        this,
                        Uri.parse(Builder.networkResourceUri),
                        RenderableSource.SourceType.GLTF2)
                        .setScale(0.25f)    // initial scale value
                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                        .build())
                .setRegistryId(Builder.networkResourceUri)
                .build()
                .thenAccept(renderable -> {
                    if (loadingProgressDialog.isShowing()) loadingProgressDialog.dismiss();
                    myRenderable = renderable;
                    Toast.makeText(getApplicationContext(), "3D Model is loaded!", Toast.LENGTH_SHORT).show();
                })
                .exceptionally(
                        throwable -> {
                            if (loadingProgressDialog.isShowing()) loadingProgressDialog.dismiss();
                            new AlertDialog.Builder(this)
                                    .setTitle("Error!")
                                    .setMessage("We are unable to load 3D Model.\n\nError: " + throwable.getMessage())
                                    .setCancelable(false)
                                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                                    .show();
                            return null;
                        });
    }

    private void addNodeToScene(ArFragment fragment, Anchor anchor, Renderable renderable) {
        if (myAnchorNode != null)
            fragment.getArSceneView().getScene().removeChild(myAnchorNode);
        myAnchorNode = new AnchorNode(anchor);
        TransformableNode node = new TransformableNode(fragment.getTransformationSystem());
        node.setRenderable(renderable);
        node.setParent(myAnchorNode);
        fragment.getArSceneView().getScene().addChild(myAnchorNode);
        node.getScaleController().setMinScale(0.5f);
        node.select();

        findViewById(ecosmob.myarlib.R.id.ivRemoveModel).setVisibility(View.VISIBLE);
    }

    public void onClickRemoveModel(View view) {
        if (fragment == null || myAnchorNode == null) return;
        fragment.getArSceneView().getScene().removeChild(myAnchorNode);
        view.setVisibility(View.GONE);
    }

    public interface IsArSupported {
        void isArSupported(boolean isSupported);
    }

    public static class Builder {
        static String networkResourceUri;
        private Activity activity;

        public Builder(Activity activity) {
            this.activity = activity;
        }

        public Builder setNetworkResource(@NonNull String stringUri) {
            networkResourceUri = stringUri;
            return this;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public void launchArCameraActivity() {
            activity.startActivity(new Intent(activity, MyArCameraActivity.class));
        }
    }
}
