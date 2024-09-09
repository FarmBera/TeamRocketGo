package com.example.teamrocketgo.activities

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PixelFormat
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Choreographer
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.teamrocketgo.DBHelper
import com.example.teamrocketgo.My_ModelViewer
import com.example.teamrocketgo.PokemonInfoActivity
import com.example.teamrocketgo.classes.My_Pokemon
import com.example.teamrocketgo.classes.Pokedex
import com.example.teamrocketgo.classes.Wild_Pokemon
import com.example.teamrocketgo.databinding.ActivityCatchBinding
import com.google.android.filament.utils.FPI
import com.google.android.filament.utils.Float3
import com.google.android.filament.utils.KtxLoader
import com.google.android.filament.utils.Mat4
import com.google.android.filament.utils.Utils
import com.google.android.filament.utils.dot
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception
import java.nio.ByteBuffer
import java.time.LocalDateTime
import kotlin.math.sqrt

class CatchActivity : AppCompatActivity(), SensorEventListener {

    companion object {
        init { Utils.init() }
    }

    private lateinit var binding: ActivityCatchBinding

    private lateinit var db: DBHelper
    private lateinit var pokemon: Wild_Pokemon

    lateinit var choreographer: Choreographer
    lateinit var modelViewer: My_ModelViewer

    private lateinit var model: String

    var size: Float = 120.0f
    var cameraPosX: Double = 0.0
    var cameraPosY: Double = 0.0
    var cameraPosZ: Double = 0.0
    var cameraTargetX: Double = 0.0
    var cameraTargetY: Double = 0.0
    var cameraTargetZ: Double = 0.0

    private lateinit var sensorManager: SensorManager
    private var lastUpdate: Long = 0
    private var lastX: Float = 0f
    private var lastY: Float = 0f
    private var lastZ: Float = 0f
    private val shakeThreshold = 100
    private var shakeCount = 0
    private val requiredShakeCount = 2

    var capture: Float = 0f

    lateinit var modelViewer2: My_ModelViewer
    var temp: Boolean = true
    private var meteorStartTime = System.nanoTime()

    private var catchSuccess: Boolean = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCatchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        db = DBHelper.getInstance(this)!!

        choreographer = Choreographer.getInstance()
        modelViewer = My_ModelViewer(binding.surfaceView)



        val id = intent.getStringExtra("id")

        pokemon = db.WP_Get(id!!)

        model = Pokedex.getName(pokemon.num!!).lowercase()
        loadGltf(model)
        //loadGltf("meteor")
        loadEnvironment("pillars_2k")


        when (model) {
            "pikachu" -> {
                cameraPosY = 0.3
                cameraTargetY = 0.3
                cameraTargetZ = 2.0
            }
            "squirtle" -> {
                cameraPosY = 0.3
                cameraTargetY = 0.3
                cameraTargetZ = 2.2
            }
            "charizard" -> {
                cameraPosY = 1.5
                cameraTargetY = 0.5
                cameraTargetZ = 0.5
            }
            "rayquaza" -> {
                cameraPosY = 0.8
                cameraTargetX = -0.2
                cameraTargetY = 1.0
                cameraTargetZ = -2.2
            }
        }

        modelViewer.asset?.apply {
            modelViewer.transformToUnitCube()
            val m = scaling(Float3(size, size, size)) * this.root.getTransform() * translation(Float3(0f, 0f, cameraTargetZ.toFloat()))
            this.root.setTransform(m)
        }
        modelViewer.setCamera(cameraPosX, cameraPosY, cameraPosZ, cameraTargetX, cameraTargetY, cameraTargetZ)



        binding.catchBack.setOnClickListener {
            finish()
        }

        binding.catchScreenshot.setOnClickListener {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)

            val bitmap = getImageOfView(binding.catchRoot)
            if (bitmap != null) saveToStorage(bitmap)
        }

        binding.catchAR.setOnClickListener {
//            val intent = Intent(this, ARActivity::class.java)
//            intent.putExtra("id", id)
//            startActivity(intent)
        }



        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)



    }

    override fun onResume() {
        super.onResume()

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        choreographer.postFrameCallback(frameCallback)

        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        choreographer.removeFrameCallback(frameCallback)
        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        choreographer.removeFrameCallback(frameCallback)
    }


    private val frameCallback = object : Choreographer.FrameCallback {

        private val startTime = System.nanoTime()

        override fun doFrame(currentTime: Long) {
            choreographer.postFrameCallback(this)
            val seconds = (currentTime - startTime).toDouble() / 1_000_000_000
            modelViewer.animator?.apply {
                if (animationCount > 0) {
                    applyAnimation(0, seconds.toFloat())
                }
                updateBoneMatrices()
            }
            if (capture > 0)
            {
                val sec = (currentTime - meteorStartTime).toDouble() / 1_000_000_000
                modelViewer2.animator?.apply {
                    if (animationCount > 0) {
                        applyAnimation(0, sec.toFloat())
                    }
                    updateBoneMatrices()
                }

                modelViewer2.asset?.apply {
                    val S: Mat4 = scaling(Float3(2.0f, 2.0f, 2.0f))

                    modelViewer2.transformToUnitCube()
                    val T: Mat4 = this.root.getTransform2() * translation(Float3(0.0f, capture, 0.0f))

                    val matrix: Mat4 = S * T
                    this.root.setTransform2(matrix)
                }

                capture -= 0.25f

                when (model) {
                    "pikachu" -> modelViewer.setCamera(cameraPosX, cameraPosY + 5.0, cameraPosZ + 6.0, 0.0, capture.toDouble() - 1.0, 0.0)
                    "squirtle" -> modelViewer.setCamera(cameraPosX, cameraPosY + 5.0, cameraPosZ + 6.0, 0.0, capture.toDouble() - 1.0, 0.0)
                    "charizard" -> modelViewer.setCamera(cameraPosX, cameraPosY + 5.0, cameraPosZ + 6.0, 0.0, capture.toDouble(), 0.0)
                    "rayquaza" -> modelViewer.setCamera(cameraPosX, cameraPosY + 5.0, cameraPosZ + 4.0, 0.0, capture.toDouble() + 1.7, 0.0)
                }
                modelViewer2.setCamera(cameraPosX - 1.5, cameraPosY + 10.0, cameraPosZ - 0.9, 0.0, capture.toDouble(), 0.0)

                if (capture <= 0) catchSuccess = true
            }

            Log.d("myLog", capture.toString())

            if (capture < 15f) modelViewer.render(currentTime)
            if (capture > 0) modelViewer2.render(currentTime)

            if (catchSuccess)
            {
                Thread.sleep(800)

                val p = My_Pokemon()
                p.num = pokemon.id
                p.name = Pokedex.getName(pokemon.id!!)
                p.favorite = 0
                p.latitude = pokemon.latitude
                p.longitude = pokemon.longitude
                p.time = LocalDateTime.now()
                p.level = pokemon.level
                p.exp = 0
                p.hp = pokemon.hp
                p.current_hp = pokemon.hp
                p.attack = pokemon.attack
                p.defense = pokemon.defense
                db.MP_Add(p)

                db.WP_Delete(pokemon.id!!)

                val intent = Intent(this@CatchActivity, PokemonInfoActivity::class.java)
                intent.putExtra("id", "0")
                startActivity(intent)
            }
        }

    }

    private fun scaling(m: Float3): Mat4 {
        return Mat4.of(
            m.x,    0.0f,   0.0f,   0.0f,
            0.0f,   m.y,    0.0f,   0.0f,
            0.0f,   0.0f,   m.z,    0.0f,
            0.0f,   0.0f,   0.0f,   1.0f
        )
    }
    private fun rotationX(angle: Double): Mat4 {
        val r = angle * (FPI / 180.0f)
        val c = Math.cos(r.toDouble()).toFloat()
        val s = Math.sin(r.toDouble()).toFloat()

        return Mat4.of(
            1.0f,   0.0f,   0.0f,   0.0f,
            0.0f,   c,      s,      0.0f,
            0.0f,   -s,     c,      0.0f,
            0.0f,   0.0f,   0.0f,   1.0f
        )
    }
    private fun rotationY(angle: Double): Mat4 {
        val r = angle * (FPI / 180.0f)
        val c = Math.cos(r).toFloat()
        val s = Math.sin(r).toFloat()

        return Mat4.of(
            c,      0.0f,   -s,     0.0f,
            0.0f,   1.0f,   0.0f,   0.0f,
            s,      0.0f,   c,      0.0f,
            0.0f,   0.0f,   0.0f,   1.0f
        )
    }
    private fun rotationZ(angle: Double): Mat4 {
        val r = angle * (FPI / 180.0f)
        val c = Math.cos(r.toDouble()).toFloat()
        val s = Math.sin(r.toDouble()).toFloat()

        return Mat4.of(
            c,      s,      0.0f,   0.0f,
            -s,     c,      0.0f,   0.0f,
            0.0f,   0.0f,   1.0f,   0.0f,
            0.0f,   0.0f,   0.0f,   1.0f
        )
    }
    private fun translation(m: Float3): Mat4 {
        return Mat4.of(
            1.0f,   0.0f,   0.0f,   0.0f,
            0.0f,   1.0f,   0.0f,   0.0f,
            0.0f,   0.0f,   1.0f,   0.0f,
            m.x,    m.y,    m.z,    1.0f
        )
    }

    private fun Int.getTransform(): Mat4 {
        val tm = modelViewer.engine.transformManager
        return Mat4.of(*tm.getTransform(tm.getInstance(this), null))
    }
    private fun Int.setTransform(mat: Mat4) {
        val tm = modelViewer.engine.transformManager
        tm.setTransform(tm.getInstance(this), mat.toFloatArray())
    }

    private fun Int.getTransform2(): Mat4 {
        val tm = modelViewer2.engine.transformManager
        return Mat4.of(*tm.getTransform(tm.getInstance(this), null))
    }
    private fun Int.setTransform2(mat: Mat4) {
        val tm = modelViewer2.engine.transformManager
        tm.setTransform(tm.getInstance(this), mat.toFloatArray())
    }

    private fun readAsset(assetName: String): ByteBuffer {
        val input = assets.open(assetName)
        val bytes = ByteArray(input.available())
        input.read(bytes)
        return ByteBuffer.wrap(bytes)
    }
    private fun loadGltf(name: String) {
        val buffer = readAsset("models/${name}.gltf")
        modelViewer.loadModelGltf(buffer) { uri -> readAsset("models/$uri") }
        modelViewer.transformToUnitCube()
    }

    fun loadEnvironment(ibl: String) {
        var buffer = readAsset("environments/$ibl/${ibl}_ibl.ktx")
        KtxLoader.createIndirectLight(modelViewer.engine, buffer).apply {
            intensity = 20_000f
            modelViewer.scene.indirectLight = this
        }

        binding.surfaceView.setBackgroundColor(Color.TRANSPARENT)
        binding.surfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT)

        modelViewer.view.blendMode = com.google.android.filament.View.BlendMode.TRANSLUCENT
        modelViewer.scene.skybox = null
        modelViewer.setTransparent()
    }


    private fun loadGltf2(name: String) {
        val buffer = readAsset("models/${name}.gltf")
        modelViewer2.loadModelGltf(buffer) { uri -> readAsset("models/$uri") }
        modelViewer2.transformToUnitCube()
    }

    fun loadEnvironment2(ibl: String) {
        var buffer = readAsset("environments/$ibl/${ibl}_ibl.ktx")
        KtxLoader.createIndirectLight(modelViewer2.engine, buffer).apply {
            intensity = 20_000f
            modelViewer2.scene.indirectLight = this
        }

        binding.surfaceView2.setBackgroundColor(Color.TRANSPARENT)
        binding.surfaceView2.getHolder().setFormat(PixelFormat.TRANSLUCENT)

        modelViewer2.view.blendMode = com.google.android.filament.View.BlendMode.TRANSLUCENT
        modelViewer2.scene.skybox = null
        modelViewer2.setTransparent()
    }


    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUpdate > 100) {
                val diffTime = currentTime - lastUpdate
                lastUpdate = currentTime

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val speed = (x + y + z - lastX - lastY - lastZ) / diffTime * 10000

                if (speed > shakeThreshold) {
                    shakeCount++
                    if (shakeCount == requiredShakeCount) {

                        if (capture == 0f)
                        {

                            if (temp)
                            {

                                modelViewer2 = My_ModelViewer(binding.surfaceView2)
                                loadGltf2("meteor")
                                loadEnvironment2("pillars_2k")
                                modelViewer2.setCamera(0.0, 1.0, 0.0, 0.0, 0.0, 0.0)

                                temp = false
                            }



                            capture = 40f
                            meteorStartTime = System.nanoTime()
                        }

                        shakeCount = 0
                    }
                } else {
                    shakeCount = 0
                }

                lastX = x
                lastY = y
                lastZ = z
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}



    private fun saveToStorage(bitmap: Bitmap) {
        val imageName = "${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, imageName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        }
        else {
            val imagesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDirectory, imageName)
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this, "Capture Success", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getImageOfView(view: View): Bitmap? {
        var image: Bitmap? = null
        try {
            image = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(image)
            view.draw(canvas)
        }
        catch (e: Exception) {
            Log.e("user", "Cannot Capture Screen")
        }
        return image
    }


}