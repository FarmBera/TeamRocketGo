package com.example.teamrocketgo

import android.util.Log
import android.view.MotionEvent
import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView
import com.google.android.filament.*
import com.google.android.filament.android.DisplayHelper
import com.google.android.filament.android.UiHelper
import com.google.android.filament.gltfio.*
import com.google.android.filament.utils.*
import java.nio.Buffer

class My_ModelViewer: android.view.View.OnTouchListener {

    var asset: FilamentAsset? = null
        private set

    var animator: Animator? = null
        private set

    @Suppress("unused")
    val progress
        get() = resourceLoader.asyncGetLoadProgress()

    val engine: Engine
    val scene: Scene
    val view: View
    val camera: Camera
    @Entity
    val light: Int

    private val uiHelper: UiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK)
    private lateinit var displayHelper: DisplayHelper
    private val cameraManipulator: Manipulator
    private val gestureDetector: GestureDetector
    private val renderer: Renderer
    private lateinit var surfaceView: SurfaceView
    private var swapChain: SwapChain? = null
    private var assetLoader: AssetLoader
    private var resourceLoader: ResourceLoader
    private val readyRenderables = IntArray(128) // add up to 128 entities at a time

    private val eyePos = DoubleArray(3)
    private val target = DoubleArray(3)
    private val upward = DoubleArray(3)

    private val my_eyePos = DoubleArray(3)
    private val my_target = DoubleArray(3)
    private val my_upward = DoubleArray(3)

    private val kNearPlane = 0.5
    private val kFarPlane = 10000.0
    private val kFovDegrees = 45.0
    private val kAperture = 16f
    private val kShutterSpeed = 1f / 125f
    private val kSensitivity = 100f

    init {
        engine = Engine.create()
        renderer = engine.createRenderer()
        scene = engine.createScene()
        camera = engine.createCamera().apply { setExposure(kAperture, kShutterSpeed, kSensitivity) }
        view = engine.createView()
        view.scene = scene
        view.camera = camera

        assetLoader = AssetLoader(engine, MaterialProvider(engine), EntityManager.get())
        resourceLoader = ResourceLoader(engine)

        // Always add a direct light source since it is required for shadowing.
        // We highly recommend adding an indirect light as well.

        light = EntityManager.get().create()



        my_upward[0] = 0.0
        my_upward[1] = 1.0
        my_upward[2] = 0.0



        val (r, g, b) = Colors.cct(6_500.0f)
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(r, g, b)
            .intensity(300_000.0f)
            .direction(0.0f, -1.0f, 0.0f)
            .castShadows(true)
            .build(engine, light)

        scene.addEntity(light)
    }

    constructor(surfaceView: SurfaceView) {
        cameraManipulator = Manipulator.Builder()
            .targetPosition(0.0f, 0.0f, -4.0f)
            .viewport(surfaceView.width, surfaceView.height)
            .build(Manipulator.Mode.ORBIT)

        this.surfaceView = surfaceView
        gestureDetector = GestureDetector(surfaceView, cameraManipulator)
        displayHelper = DisplayHelper(surfaceView.context)

        //--> edit
        uiHelper.isOpaque = false

        uiHelper.renderCallback = SurfaceCallback()
        uiHelper.attachTo(surfaceView)
        addDetachListener(surfaceView)
    }

    @Suppress("unused")
    constructor(textureView: TextureView) {
        cameraManipulator = Manipulator.Builder()
            .targetPosition(0.0f, 0.0f, -4.0f)
            .viewport(textureView.width, textureView.height)
            .build(Manipulator.Mode.ORBIT)

        gestureDetector = GestureDetector(textureView, cameraManipulator)

        //--> edit
        uiHelper.isOpaque = false

        uiHelper.renderCallback = SurfaceCallback()
        uiHelper.attachTo(textureView)
        addDetachListener(textureView)
    }


    //--> edit
    fun setTransparent() {
        val options = renderer.clearOptions
        options.clear = true
        renderer.clearOptions = options
    }

    fun setCamera(
        posX: Double, posY: Double, posZ: Double,
        targX: Double = target[0], targY: Double = target[1], targZ: Double = target[2]) {
        my_eyePos[0] = posX
        my_eyePos[1] = posY
        my_eyePos[2] = posZ
        my_target[0] = targX
        my_target[1] = targY
        my_target[2] = targZ - 4.0
    }
    fun setCamera1(
        posX: Double, posY: Double, posZ: Double,
        targX: Double, targY: Double, targZ: Double,
        upX: Double, upY: Double, upZ: Double, viewMatrix: Mat4) {
        camera.lookAt(posX, posY, posZ, targX, targY, targZ, upX, upY, upZ)
        eyePos[0] = posX
        eyePos[1] = posY
        eyePos[2] = posZ
        target[0] = targX
        target[1] = targY
        target[2] = targZ
        upward[0] = upX
        upward[1] = upY
        upward[2] = upZ
        camera.setModelMatrix(viewMatrix.toFloatArray())
    }
    fun setCamera2(x: Int, y: Int) {
        cameraManipulator.grabUpdate(x, y)
    }
    fun setCamera3(
        posX: Double, posY: Double, posZ: Double,
        targX: Double, targY: Double, targZ: Double,
        upX: Double, upY: Double, upZ: Double) {
        my_eyePos[0] = posX
        my_eyePos[1] = posY
        my_eyePos[2] = posZ
        my_target[0] = targX
        my_target[1] = targY
        my_target[2] = targZ
        my_upward[0] = upX
        my_upward[1] = upY
        my_upward[2] = upZ
    }


    /**
     * Loads a monolithic binary glTF and populates the Filament scene.
     */
    fun loadModelGlb(buffer: Buffer) {
        destroyModel()
        asset = assetLoader.createAssetFromBinary(buffer)
        asset?.let { asset ->
            resourceLoader.asyncBeginLoad(asset)
            animator = asset.animator
            asset.releaseSourceData()
        }
    }

    /**
     * Loads a JSON-style glTF file and populates the Filament scene.
     */
    fun loadModelGltf(buffer: Buffer, callback: (String) -> Buffer) {
        destroyModel()
        asset = assetLoader.createAssetFromJson(buffer)
        asset?.let { asset ->
            for (uri in asset.resourceUris) {
                resourceLoader.addResourceData(uri, callback(uri))
            }
            resourceLoader.asyncBeginLoad(asset)
            animator = asset.animator
            asset.releaseSourceData()
        }
    }

    /**
     * Sets up a root transform on the current model to make it fit into the viewing frustum.
     */
    fun transformToUnitCube() {
        asset?.let { asset ->
            val tm = engine.transformManager
            val center = asset.boundingBox.center.let { v-> Float3(v[0], v[1], v[2]) }
            val halfExtent = asset.boundingBox.halfExtent.let { v-> Float3(v[0], v[1], v[2]) }
            val maxExtent = 2.0f * max(halfExtent)
            val scaleFactor = 2.0f / maxExtent
            center.z = center.z + 4.0f / scaleFactor
            val transform = scale(Float3(scaleFactor)) * translation(Float3(-center))
            tm.setTransform(tm.getInstance(asset.root), transpose(transform).toFloatArray())
        }
    }

    /**
     * Frees all entities associated with the most recently-loaded model.
     */
    fun destroyModel() {
        asset?.let { asset ->
            assetLoader.destroyAsset(asset)
            this.asset = null
            this.animator = null
        }
    }

    /**
     * Renders the model and updates the Filament camera.
     */
    fun render(frameTimeNanos: Long) {
        if (!uiHelper.isReadyToRender) {
            return
        }

        // Allow the resource loader to finalize textures that have become ready.
        resourceLoader.asyncUpdateLoad()

        // Add renderable entities to the scene as they become ready.
        asset?.let { populateScene(it) }

        // Extract the camera basis from the helper and push it to the Filament camera.
        cameraManipulator.getLookAt(eyePos, target, upward)
//        camera.lookAt(
//            eyePos[0], eyePos[1], eyePos[2],
//            target[0], target[1], target[2],
//            upward[0], upward[1], upward[2])
        //Log.d("myLog", "target:    " + target[0].toString() + ", " + target[1].toString() + ", " + target[2].toString())
        camera.lookAt(
            my_eyePos[0], my_eyePos[1], my_eyePos[2],
            my_target[0], my_target[1], my_target[2],
            my_upward[0], my_upward[1], my_upward[2])

        // Render the scene, unless the renderer wants to skip the frame.
        if (renderer.beginFrame(swapChain!!, frameTimeNanos)) {
            renderer.render(view)
            renderer.endFrame()
        }
    }

    private fun populateScene(asset: FilamentAsset) {
        var count = 0
        val popRenderables = {count = asset.popRenderables(readyRenderables); count != 0}
        while (popRenderables()) {
            scene.addEntities(readyRenderables.take(count).toIntArray())
        }
    }

    private fun addDetachListener(view: android.view.View) {
        view.addOnAttachStateChangeListener(object: android.view.View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: android.view.View) {}
            override fun onViewDetachedFromWindow(v: android.view.View) {
                uiHelper.detach()

                destroyModel()
                assetLoader.destroy()
                resourceLoader.destroy()

                engine.destroyEntity(light)
                engine.destroyRenderer(renderer)
                engine.destroyView(this@My_ModelViewer.view)
                engine.destroyScene(scene)
                engine.destroyCamera(camera)

                EntityManager.get().destroy(light)

                engine.destroy()
            }
        })
    }

    /**
     * Handles a [MotionEvent] to enable one-finger orbit, two-finger pan, and pinch-to-zoom.
     */
    fun onTouchEvent(event: MotionEvent) {
        gestureDetector.onTouchEvent(event)
    }

    @SuppressWarnings("ClickableViewAccessibility")
    override fun onTouch(view: android.view.View, event: MotionEvent): Boolean {
        onTouchEvent(event)
        return true
    }

    inner class SurfaceCallback : UiHelper.RendererCallback {
        override fun onNativeWindowChanged(surface: Surface) {
            swapChain?.let { engine.destroySwapChain(it) }
            swapChain = engine.createSwapChain(surface)
            displayHelper.attach(renderer, surfaceView.display)
        }

        override fun onDetachedFromSurface() {
            displayHelper.detach()
            swapChain?.let {
                engine.destroySwapChain(it)
                engine.flushAndWait()
                swapChain = null
            }
        }

        override fun onResized(width: Int, height: Int) {
            view.viewport = Viewport(0, 0, width, height)
            val aspect = width.toDouble() / height.toDouble()
            camera.setProjection(kFovDegrees, aspect, kNearPlane, kFarPlane, Camera.Fov.VERTICAL)
            cameraManipulator.setViewport(width, height)
        }
    }
}