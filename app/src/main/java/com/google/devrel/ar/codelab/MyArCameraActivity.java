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
package com.google.devrel.ar.codelab;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

    public static final String GLTF_ASSET =
            "https://github.com/KhronosGroup/glTF-Sample-Models/raw/master/2.0/Duck/glTF/Duck.gltf";
    //            "https://github.com/KhronosGroup/glTF-Sample-Models/raw/master/2.0/DamagedHelmet/glTF/DamagedHelmet.gltf";
    AnchorNode myAnchorNode;
    private ArFragment fragment;
    //    private PointerDrawable pointer = new PointerDrawable();
//    private boolean isTracking;
//    private boolean isHitting;
    private ModelRenderable myRenderable;
    private ProgressDialog loadingProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);

//        // Set custom plane discovery image
//        fragment.getPlaneDiscoveryController().hide();
//        ViewGroup container = findViewById(R.id.sceneform_hand_layout);
//        container.removeAllViews();
//        View planeDiscoveryCustomView = getLayoutInflater().inflate(R.layout.layout_plane_discovery, container, true);
//        fragment.getPlaneDiscoveryController().setInstructionView(planeDiscoveryCustomView);

//        // Set custom color for rendering the plane
//        fragment.getArSceneView().getPlaneRenderer().getMaterial().thenAccept(material -> material.setFloat3(PlaneRenderer.MATERIAL_COLOR, new Color(0.0f, 1.0f, 0.0f, 1.0f)));

//        // Set custom plane render image
//        Texture.Sampler sampler = Texture.Sampler.builder()
//                .setMinFilter(Texture.Sampler.MinFilter.LINEAR)
//                .setMagFilter(Texture.Sampler.MagFilter.LINEAR)
//                .setWrapMode(Texture.Sampler.WrapMode.REPEAT).build();
//        CompletableFuture<Texture> trigrid = Texture.builder()
//                .setSource(this, R.drawable.trigrid)
//                .setSampler(sampler).build();
//        fragment.getArSceneView()
//                .getPlaneRenderer()
//                .getMaterial()
//                .thenAcceptBoth(trigrid, (material, texture) -> {
//                    material.setTexture(PlaneRenderer.MATERIAL_TEXTURE, texture);
//                });

        if (fragment == null) {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            return;
        }

//        fragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
//            fragment.onUpdate(frameTime);
//            onUpdate();
//        });

        fragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            if (myRenderable == null) return;
            addNodeToScene(fragment, hitResult.createAnchor(), myRenderable);
        });

        loadingProgressDialog = new ProgressDialog(this);
        loadingProgressDialog.setMessage("Please wait while we load the 3D model");
        loadingProgressDialog.setCancelable(false); // disable dismiss by tapping outside of the dialog
        loadingProgressDialog.show();

        /* When you build a Renderable, Sceneform loads model and related resources
         * in the background while returning a CompletableFuture.
         * Call thenAccept(), handle(), or check isDone() before calling get().
         */
        ModelRenderable.builder()
                .setSource(this, RenderableSource.builder().setSource(
                        this,
                        Uri.parse(GLTF_ASSET),
                        RenderableSource.SourceType.GLTF2)
                        .setScale(0.25f)
                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                        .build())
                .setRegistryId(GLTF_ASSET)
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
                                    .show();
                            return null;
                        });
    }

//    private void onUpdate() {
//        boolean trackingChanged = updateTracking();
//        View contentView = findViewById(android.R.id.content);
//        if (trackingChanged) {
//            if (isTracking) {
//                contentView.getOverlay().add(pointer);
//            } else {
//                contentView.getOverlay().remove(pointer);
//            }
//            contentView.invalidate();
//        }
//
//        if (isTracking) {
//            boolean hitTestChanged = updateHitTest();
//            if (hitTestChanged) {
//                pointer.setEnabled(isHitting);
//                contentView.invalidate();
//            }
//        }
//    }

//    private boolean updateTracking() {
//        Frame frame = fragment.getArSceneView().getArFrame();
//        boolean wasTracking = isTracking;
//        isTracking = frame.getCamera().getTrackingState() == TrackingState.TRACKING;
//        return isTracking != wasTracking;
//    }
//
//    private boolean updateHitTest() {
//        Frame frame = fragment.getArSceneView().getArFrame();
//        android.graphics.Point pt = getScreenCenter();
//        List<HitResult> hits;
//        boolean wasHitting = isHitting;
//        isHitting = false;
//        if (frame != null) {
//            hits = frame.hitTest(pt.x, pt.y);
//            for (HitResult hit : hits) {
//                Trackable trackable = hit.getTrackable();
//                if ((trackable instanceof Plane &&
//                        ((Plane) trackable).isPoseInPolygon(hit.getHitPose()))) {
//                    isHitting = true;
//                    break;
//                }
//            }
//        }
//        return wasHitting != isHitting;
//    }

//    private android.graphics.Point getScreenCenter() {
//        View vw = findViewById(android.R.id.content);
//        return new android.graphics.Point(vw.getWidth() / 2, vw.getHeight() / 2);
//    }

//    private void addObject(Uri model) {
//        Frame frame = fragment.getArSceneView().getArFrame();
//        Point pt = getScreenCenter();
//        List<HitResult> hits;
//        if (frame != null) {
//            hits = frame.hitTest(pt.x, pt.y);
//            for (HitResult hit : hits) {
//                Trackable trackable = hit.getTrackable();
//                if ((trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose()))) {
//
//                    ModelRenderable.builder()
//                            .setSource(fragment.getContext(), model)
//                            .build()
//                            .thenAccept(renderable -> addNodeToScene(fragment, hit.createAnchor(), renderable))
//                            .exceptionally((throwable -> {
//                                new AlertDialog.Builder(this)
//                                        .setTitle("Error!")
//                                        .setMessage(throwable.getMessage())
//                                        .show();
//                                return null;
//                            }));
//                    break;
//                }
//            }
//        }
//    }

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

        findViewById(R.id.ivRemoveModel).setVisibility(View.VISIBLE);
    }

    public void onClickRemoveModel(View view) {
        if (fragment == null || myAnchorNode == null) return;
        fragment.getArSceneView().getScene().removeChild(myAnchorNode);
        view.setVisibility(View.GONE);
    }

//    private void takePhoto() {
//        final String filename = generateFilename();
//        ArSceneView view = fragment.getArSceneView();
//
//        // Create a bitmap the size of the scene view.
//        final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
//                Bitmap.Config.ARGB_8888);
//
//        // Create a handler thread to offload the processing of the image.
//        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
//        handlerThread.start();
//        // Make the request to copy.
//        PixelCopy.request(view, bitmap, new PixelCopy.OnPixelCopyFinishedListener() {
//            @Override
//            public void onPixelCopyFinished(int copyResult) {
//                if (copyResult == PixelCopy.SUCCESS) {
//                    try {
//                        MainActivity.this.saveBitmapToDisk(bitmap, filename);
//                    } catch (IOException e) {
//                        Toast toast = Toast.makeText(MainActivity.this, e.toString(),
//                                Toast.LENGTH_LONG);
//                        toast.show();
//                        return;
//                    }
//                    Snackbar snackbar = Snackbar.make(MainActivity.this.findViewById(android.R.id.content),
//                            "Photo saved", Snackbar.LENGTH_LONG);
//                    snackbar.setAction("Open in Photos", v -> {
//                        File photoFile = new File(filename);
//
//                        Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
//                                MainActivity.this.getPackageName() + ".ar.codelab.name.provider",
//                                photoFile);
//                        Intent intent = new Intent(Intent.ACTION_VIEW, photoURI);
//                        intent.setDataAndType(photoURI, "image/*");
//                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                        MainActivity.this.startActivity(intent);
//
//                    });
//                    snackbar.show();
//                } else {
//                    Toast toast = Toast.makeText(MainActivity.this,
//                            "Failed to copyPixels: " + copyResult, Toast.LENGTH_LONG);
//                    toast.show();
//                }
//                handlerThread.quitSafely();
//            }
//        }, new Handler(handlerThread.getLooper()));
//    }
//
//    private void saveBitmapToDisk(Bitmap bitmap, String filename) throws IOException {
//        File out = new File(filename);
//        if (!out.getParentFile().exists()) {
//            out.getParentFile().mkdirs();
//        }
//        try (FileOutputStream outputStream = new FileOutputStream(filename);
//             ByteArrayOutputStream outputData = new ByteArrayOutputStream()) {
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputData);
//            outputData.writeTo(outputStream);
//            outputStream.flush();
//            outputStream.close();
//        } catch (IOException ex) {
//            throw new IOException("Failed to save bitmap to disk", ex);
//        }
//    }
//
//    private String generateFilename() {
//        String date = new SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault()).format(new Date());
//        return Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES) + File.separator + "Sceneform/" + date + "_screenshot.jpg";
//    }

    void maybeEnableArButton() {
        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(this);
        if (availability.isTransient()) {
            // Re-query at 5Hz while compatibility is checked in the background.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    maybeEnableArButton();
                }
            }, 200);
        }
        if (availability.isSupported()) {   // AR Supported

        } else { // Unsupported or unknown.

        }
    }

}
