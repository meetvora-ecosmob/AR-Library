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
package com.google.devrel.ar.codelab

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.google.ar.core.ArCoreApk
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.FootprintSelectionVisualizer
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.TransformationSystem
import kotlinx.android.synthetic.main.activity_model_preview.*


class MyModelPreviewActivity : AppCompatActivity() {

    lateinit var scene: Scene

    private lateinit var transformationSystem: TransformationSystem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_preview)

        btnOpenArCamera.setOnClickListener {
            startActivity(Intent(applicationContext, MyArCameraActivity::class.java))
            finish()
        }

        maybeEnableArButton()

        scene = sceneView.scene // get current scene
        renderObjectFromWeb(MyArCameraActivity.GLTF_ASSET)

        scene.setOnTouchListener(Scene.OnTouchListener { hitTestResult, motionEvent ->
            transformationSystem.onTouch(hitTestResult, motionEvent)
            return@OnTouchListener false
        })

//        scene.addOnPeekTouchListener { hitTestResult, motionEvent ->
//
//            if(hitTestResult.node == null)  return@addOnPeekTouchListener
//
//            Log.d("myTAG", "hitTestResult.node : " + hitTestResult.node)
//
//
////            hitTestResult.node.localRotation = Quaternion.axisAngle(hitTestResult.point, 50.0f)
////            hitTestResult.node.localPosition = hitTestResult.point
////            hitTestResult.node.worldRotation = Quaternion.axisAngle(hitTestResult.point, 1.0f)
//
//
//            val camera = sceneView.scene.camera
//            camera.localRotation = Quaternion.axisAngle(hitTestResult.point, -90.0f)
//
//            sceneView.
//
//
//        }
    }

    /**
     * load the 3D model in the space
     * @param parse URI of the model, imported using Sceneform plugin
     */
    private fun renderObjectFromWeb(assetsPath: String) {
        ModelRenderable.builder()
                .setSource(this, RenderableSource.builder().setSource(
                        this,
                        Uri.parse(assetsPath),
                        RenderableSource.SourceType.GLTF2)
                        .setScale(0.25f)
                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                        .build())
                .setRegistryId(MyArCameraActivity.GLTF_ASSET)
                .build()
                .thenAccept { renderable ->
                    addNodeToScene(renderable)
                    Toast.makeText(applicationContext, "3D Model is loaded!", Toast.LENGTH_SHORT).show()
                }
                .exceptionally { throwable ->
                    AlertDialog.Builder(this)
                            .setTitle("Error!")
                            .setMessage("We are unable to load 3D Model.\n\nError: " + throwable.message)
                            .show()
                    null
                }
    }

    /**
     * load the 3D model in the space
     * @param assetsPath String path of the model, imported using Sceneform plugin
     */
    private fun renderObjectFromAssets(assetsPath: String) {
        ModelRenderable.builder()
                .setSource(this, Uri.parse(assetsPath))
                .build()
                .thenAccept {
                    addNodeToScene(it)
                }
                .exceptionally {
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage(it.message)
                            .setTitle("error!")
                    val dialog = builder.create()
                    dialog.show()
                    return@exceptionally null
                }
    }

    /**
     * Adds a node to the current scene
     * @param model - rendered model
     */
    private fun addNodeToScene(model: ModelRenderable?) {
//        model?.let {
//            cupCakeNode = Node().apply {
//                setParent(scene)
//                localPosition = Vector3(0f, 0f, -1f)
//                localScale = Vector3(1f, 1f, 1f)
//                name = "3D Model"
//                renderable = it
//            }
//            scene.addChild(cupCakeNode)
//        }

        //===========================================================

//        transformationSystem = TransformationSystem(resources.displayMetrics, FootprintSelectionVisualizer())
//        var transNode = TransformableNode(transformationSystem)
//        transNode.setParent(sceneView.scene)
//        transNode.renderable = model
//
//        transNode.localPosition = Vector3(0f, 0f, -1f)
//        transNode.localScale = Vector3(1f, 1f, 1f)
//        transNode.name = "3D Model"
//
//        scene.addChild(transNode)
//
//        transNode.scaleController.minScale = 0.5f
//        transNode.select()

        //===========================================================


        transformationSystem = TransformationSystem(resources.displayMetrics, FootprintSelectionVisualizer())
        var transNode = TransformableNode(transformationSystem)
        transNode.setParent(sceneView.scene)

        var andyNode = Node()
        andyNode.setParent(transNode)
        andyNode.renderable = model
        andyNode.localPosition = Vector3(0f, 0f, -1f)
        andyNode.localScale = Vector3(1f, 1f, 1f)
        andyNode.name = "3D Model"
        transNode.scaleController.minScale = 0.5f
        transNode.select()
        scene.addChild(transNode)

    }

    override fun onPause() {
        super.onPause()
        sceneView.pause()
    }

    override fun onResume() {
        super.onResume()
        sceneView.resume()
    }

    fun maybeEnableArButton() {
        val availability = ArCoreApk.getInstance().checkAvailability(this)
        if (availability.isTransient) {
            // Re-query at 5Hz while compatibility is checked in the background.
            Handler().postDelayed({
                maybeEnableArButton()
            }, 200)
        }
        if (availability.isSupported) {
            btnOpenArCamera.visibility = View.VISIBLE
            btnOpenArCamera.isEnabled = true
            // indicator on the button.
        } else { // Unsupported or unknown.
            btnOpenArCamera.visibility = View.INVISIBLE
            btnOpenArCamera.isEnabled = false
        }
    }

}