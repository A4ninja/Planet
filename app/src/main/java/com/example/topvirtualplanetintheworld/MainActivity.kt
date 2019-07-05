package com.example.topvirtualplanetintheworld

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*


@Suppress("SpellCheckingInspection")

class MainActivity : AppCompatActivity() {
    lateinit var scene: Scene
    lateinit var earthNode: Node
    private lateinit var arFragment: ArFragment
    private var isTracking: Boolean = false
    private var isHitting: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        arFragment = sceneform_fragment as ArFragment

        // Adds a listener to the ARSceneView
        // Called before processing each frame

        arFragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            arFragment.onUpdate(frameTime)
            onUpdate()
        }
        scene = sceneView.scene // get current scene
        renderObject(Uri.parse("earth.sfb")) // Render the object

        // Set the onclick lister for our button
        // Change this string to point to the .sfb file of your choice :)
        floatingActionButton.setOnClickListener { addObject(Uri.parse("earth.sfb")) }

        // Скрыть кнопку
        // one more
        showFab(false)

    }

    // Simple function to show/hide our FAB
    @SuppressLint("RestrictedApi")
    private fun showFab(enabled: Boolean) {
        if (enabled) {
            floatingActionButton.isEnabled = true
            floatingActionButton.visibility = View.VISIBLE
        } else {
            floatingActionButton.isEnabled = false
            floatingActionButton.visibility = View.GONE
        }
    }

    // Updates the tracking state
    private fun onUpdate() {
        updateTracking()
        // Check if the devices gaze is hitting a plane detected by ARCore
        if (isTracking) {
            val hitTestChanged = updateHitTest()
            if (hitTestChanged) {
                showFab(isHitting)
            }
        }
    }


    // Performs frame.HitTest and returns if a hit is detected
    private fun updateHitTest(): Boolean {
        val frame = arFragment.arSceneView.arFrame
        val point = getScreenCenter()
        val hits: List<HitResult>
        val wasHitting = isHitting
        isHitting = false
        if (frame != null) {
            hits = frame.hitTest(point.x, point.y)
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    isHitting = true
                    break
                }
            }
        }
        return wasHitting != isHitting
    }

//    override fun onPause() {
//        super.onPause()
//        sceneView.pause()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        sceneView.resume()
//    }

    // Makes use of ARCore's camera state and returns true if the tracking state has changed
    private fun updateTracking(): Boolean {
        val frame = arFragment.arSceneView.arFrame
        val wasTracking = isTracking
        isTracking = frame?.camera?.trackingState == TrackingState.TRACKING
        return isTracking != wasTracking
    }

    // Simply returns the center of the screen
    private fun getScreenCenter(): Vector3 {
        val view = findViewById<View>(android.R.id.content)
        return Vector3(view.width / 2F, view.height / 2F, 0F)
    }

    /**
     * @param model The Uri of our 3D sfb file
     *
     * This method takes in our 3D model and performs a hit test to determine where to place it
     */
//    private fun addObject(model: Uri) {
//        val frame = arFragment.arSceneView.arFrame
//        val point = getScreenCenter()
//        if (frame != null) {
//            val hits = frame.hitTest(point.x, point.y)
//            for (hit in hits) {
//                val trackable = hit.trackable
//                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
//                    placeObject(arFragment, hit.createAnchor(), model)
//                    break
//                }
//            }
//        }
//    }

    /**
     * @param fragment our fragment
     * @param anchor ARCore anchor from the hit test
     * @param model our 3D model of choice
     *
     * Uses the ARCore anchor from the hitTest result and builds the Sceneform nodes.
     * It starts the asynchronous loading of the 3D model using the ModelRenderable builder.
     */
//    private fun placeObject(fragment: ArFragment, anchor: Anchor, model: Uri) {
//        ModelRenderable.builder()
//            .setSource(fragment.context, model)
//            .build()
//            .thenAccept {
//                addNodeToScene(fragment, anchor, it)
//            }
//            .exceptionally {
//                Toast.makeText(this@MainActivity, "Unexpected error", Toast.LENGTH_SHORT).show()
//                return@exceptionally null
//            }
//    }
    /** load the 3D model in the space
    * @param parse - URI of the model, imported using Sceneform plugin
    */
    private fun renderObject(parse: Uri) {
        ModelRenderable.builder()
            .setSource(this, parse)
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
        model?.let {
            earthNode = Node().apply {
                setParent(scene)
                localPosition = Vector3(0f, 0f, -1f)
                localScale = Vector3(3f, 3f, 3f)
                name = "Earth"
                renderable = it
            }

            scene.addChild(earthNode)
        }
    }

    /**
     * @param fragment our fragment
     * @param anchor ARCore anchor
     * @param renderable our model created as a Sceneform Renderable
     *
     * This method builds two nodes and attaches them to our scene
     * The Anchor nodes is positioned based on the pose of an ARCore Anchor. They stay positioned in the sample place relative to the real world.
     * The Transformable node is our Model
     * Once the nodes are connected we select the TransformableNode so it is available for interactions
     */
    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderable: ModelRenderable) {
        val anchorNode = AnchorNode(anchor)
        // TransformableNode means the user to move, scale and rotate the model
        val transformableNode = TransformableNode(fragment.transformationSystem)
        transformableNode.renderable = renderable
        transformableNode.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
        transformableNode.select()
    }
}