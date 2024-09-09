package com.example.teamrocketgo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.Choreographer
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.teamrocketgo.activities.CatchActivity
import com.example.teamrocketgo.activities.MainMenuActivity
import com.example.teamrocketgo.activities.ProfileActivity
import com.example.teamrocketgo.classes.Wild_Pokemon
import com.example.teamrocketgo.databinding.ActivityMainBinding
import com.google.android.filament.utils.FPI
import com.google.android.filament.utils.Float3
import com.google.android.filament.utils.KtxLoader
import com.google.android.filament.utils.Mat4
import com.google.android.filament.utils.Utils
import com.google.android.filament.utils.dot
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        init { Utils.init() }
    }

    private lateinit var binding: ActivityMainBinding

    private lateinit var map: GoogleMap
    var currentLocation: LatLng? = null
    private var locationCallback: LocationCallback? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var choreographer: Choreographer
    private lateinit var modelViewer: My_ModelViewer

    private val mapZoom: Float = 20.5f
    private val mapAngle: Float = 40f

    var cameraPosX: Double = 0.0
    var cameraPosY: Double = 2.5
    var cameraPosZ: Double = -1.0
    var playerDirection: Double = 0.0

    private var walkingAnimation: Boolean = false

    private val db = DBHelper(this@MainActivity)

    private var wildPokemons: ArrayList<Wild_Pokemon> = ArrayList<Wild_Pokemon>()
    private val spawnRange: Double = 0.00015
    private val despawnTime: Int = 1
    private val markerArray: ArrayList<Marker> = ArrayList<Marker>()

    private val handlerThread = HandlerThread("MyHandlerThread")
    lateinit var handler: Handler
    lateinit var runnable: Runnable
    private val threadDelay: Long = 5000

    class RandomLocation(fromX: Double, toX: Double, fromY: Double, toY: Double) {
        val latitude: Double = Random.nextDouble(min(fromX, toX), max(fromX, toX))
        val longitude: Double = Random.nextDouble(min(fromY, toY), max(fromY, toY))
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN



        var permissionArray = arrayOf<String>()
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) permissionArray = permissionArray.plus(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) permissionArray= permissionArray.plus(android.Manifest.permission.ACCESS_FINE_LOCATION)
        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if(it.all{ permission -> permission.value == true}) mapIsReady()
            else
            {
                Toast.makeText(applicationContext, "권한을 허용해주세요", Toast.LENGTH_LONG).show()
                finish()
                System.exit(0)
            }
        }
        if(permissionArray.size != 0) requestPermissionLauncher.launch(permissionArray)
        else mapIsReady()



        choreographer = Choreographer.getInstance()
        modelViewer = My_ModelViewer(binding.surfaceView)

        loadGltf("blue")
        loadEnvironment("pillars_2k")

        modelViewer.setCamera(cameraPosX, cameraPosY, cameraPosZ)



        handlerThread.start()
        handler = Handler(handlerThread.looper)

        runnable = object: Runnable {
            override fun run() {

                if (currentLocation != null)
                {
                    wildPokemons = db.WP_GetAll()

                    val quadrant = Random.nextInt(1, 5)
                    val buffer = 0.00005
                    val lat = currentLocation?.latitude
                    val lng = currentLocation?.longitude
                    val isNear = checkPokemonInArea(lat!!, lng!!, quadrant)
                    if (!isNear)
                    {
                        val randomLocation = when (quadrant) {
                            1 -> RandomLocation(lat!! + buffer, lat!! + spawnRange, lng!! + buffer, lng!! + spawnRange)
                            2 -> RandomLocation(lat!! - spawnRange, lat!! - buffer, lng!! + buffer, lng!! + spawnRange)
                            3 -> RandomLocation(lat!! - spawnRange, lat!! - buffer, lng!! - spawnRange, lng!! - buffer)
                            4 -> RandomLocation(lat!! + buffer, lat!! + spawnRange, lng!! - spawnRange, lng!! - buffer)
                            else -> RandomLocation(0.0, 0.0, 0.0, 0.0)
                        }

                        val pokemon = Wild_Pokemon()
                        //pokemon.num = Random.nextInt(1, 5)
                        pokemon.num = 3
                        pokemon.latitude = randomLocation.latitude
                        pokemon.longitude = randomLocation.longitude
                        pokemon.time = LocalDateTime.now()
                        pokemon.level = Random.nextInt(1, 11)
                        pokemon.hp = Random.nextInt(100, 201)
                        pokemon.attack = Random.nextInt(20, 31)
                        pokemon.defense = Random.nextInt(0, 11)

                        db.WP_Add(pokemon)
                    }
                }

                handler.postDelayed(runnable, threadDelay)
            }
        }



        binding.home.setOnClickListener {
            val intent = Intent(this@MainActivity, MainMenuActivity::class.java)
            startActivity(intent)
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, R.anim.load_fade_in, R.anim.none)
        }

        binding.profile.setOnClickListener {
            val intent = Intent(this@MainActivity, ProfileActivity::class.java)
            startActivity(intent)
        }


//        val pokemon = My_Pokemon()
//        pokemon.num = 1
//        pokemon.name = Pokedex.getName(1)
//        pokemon.favorite = 0
//        pokemon.latitude = -34.0
//        pokemon.longitude = 151.0
//        pokemon.time = LocalDateTime.now()
//        pokemon.level = Random.nextInt(1, 11)
//        pokemon.exp = 0
//        pokemon.hp = Random.nextInt(100, 201)
//        pokemon.current_hp = pokemon.hp
//        pokemon.attack = Random.nextInt(20, 31)
//        pokemon.defense = Random.nextInt(0, 11)
//        db.MP_Add(pokemon)


    }

    override fun onResume() {
        super.onResume()

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        choreographer.postFrameCallback(frameCallback)

        handler.postDelayed(runnable, threadDelay)
    }

    override fun onPause() {
        super.onPause()
        choreographer.removeFrameCallback(frameCallback)
        handler.removeCallbacks(runnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handlerThread.quit()
        choreographer.removeFrameCallback(frameCallback)
        if (fusedLocationProviderClient != null) fusedLocationProviderClient.removeLocationUpdates(locationCallback!!)
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap



        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))



        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object: LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                val result = p0.locations[0]
                val newLocation = LatLng(result.latitude, result.longitude)

                if (currentLocation == null)
                {
                    currentLocation = newLocation

                    map.uiSettings.isScrollGesturesEnabled = false
                    map.setMinZoomPreference(mapZoom)
                    map.setMaxZoomPreference(mapZoom)
                    val cameraPos = CameraPosition.Builder()
                        .target(newLocation)
                        .bearing(0f)
                        .tilt(mapAngle)
                        .zoom(mapZoom)
                        .build()
                    map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
                }
                else if (newLocation.equals(currentLocation))
                {
                    walkingAnimation = false
                }
                else
                {
                    walkingAnimation = true

                    Log.d("myLog newLocation", newLocation.latitude.toString() + ", " + newLocation.longitude.toString())
                    Log.d("myLog currentLocation", currentLocation?.latitude.toString() + ", " + currentLocation?.longitude.toString())

                    val facing = map.cameraPosition.bearing
                    val newCameraPos = CameraPosition.Builder()
                        .target(newLocation)
                        .bearing(facing)
                        .tilt(mapAngle)
                        .build()
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPos));

                    playerDirection = getAngle(currentLocation?.latitude!! - newLocation.latitude, currentLocation?.longitude!! - newLocation.longitude)
                    currentLocation = newLocation
                }

                if (!wildPokemons.isEmpty())
                {
                    val now = LocalDateTime.now()
                    for (i in wildPokemons)
                    {
                        if (markerArray.find { findId -> i.id!!.equals(findId.tag) } == null)
                        {
                            val mark = MarkerOptions()
                                .position(LatLng(i.latitude!!, i.longitude!!))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.question_mark))
                            val marker =  map.addMarker(mark)
                            marker?.tag = i.id!!
                            markerArray.add(marker!!)
                        }

                        if (ChronoUnit.MINUTES.between(i.time, now) >= despawnTime)
                        {
                            markerArray.find { findId -> i.id!!.equals(findId.tag) }?.remove()
                            db.WP_Delete(i.id!!)
                        }
                    }
                }
            }
        }
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 100).build()
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback!!, Looper.getMainLooper())



        map.setOnMarkerClickListener(object: OnMarkerClickListener {
            override fun onMarkerClick(p0: Marker): Boolean {
                val id = p0.tag.toString()
                //Toast.makeText(applicationContext, id, Toast.LENGTH_SHORT).show()

                db.DEX_SEEN(id)

                val intent = Intent(this@MainActivity, CatchActivity::class.java)
                intent.putExtra("id", id)
                startActivity(intent)

                return true
            }
        })




    }
    private fun getAngle(x: Double, y: Double): Double {
        return atan2(x, y) * 180 / PI - 90
    }

    private fun mapIsReady() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this@MainActivity)
    }



    private fun checkPokemonInArea(lat: Double, lng: Double, quadrant: Int): Boolean {
        if (wildPokemons.isEmpty()) return false
        val toLat = when (quadrant) {
            1 -> lat + spawnRange
            2 -> lat - spawnRange
            3 -> lat - spawnRange
            4 -> lat + spawnRange
            else -> 0.0
        }
        val toLng = when (quadrant) {
            1 -> lng + spawnRange
            2 -> lng + spawnRange
            3 -> lng - spawnRange
            4 -> lng - spawnRange
            else -> 0.0
        }
        val minLat = min(lat, toLat)
        val maxLat = max(lat, toLat)
        val minLng = min(lng, toLng)
        val maxLng = max(lng, toLng)
        var exist: Boolean = false
        for (i in wildPokemons)
        {
            if ((i.latitude!! >= minLat && i.latitude!! <= maxLat) && (i.longitude!! >= minLng && i.latitude!! <= maxLng))
            {
                exist = true
                break
            }
        }
        return exist
    }



    private val frameCallback = object : Choreographer.FrameCallback {

        private val startTime = System.nanoTime()

        override fun doFrame(currentTime: Long) {
            choreographer.postFrameCallback(this)
            var seconds: Double = 0.0
            if (walkingAnimation) seconds = (currentTime - startTime).toDouble() / 1_000_000_000
            modelViewer.animator?.apply {
                if (animationCount > 0) {
                    applyAnimation(0, seconds.toFloat())
                }
                updateBoneMatrices()
            }

            modelViewer.asset?.apply {
                var rotation = playerDirection
                if (this@MainActivity::map.isInitialized) rotation += map.cameraPosition.bearing
                val Ry: Mat4 = rotationY(rotation)

                modelViewer.transformToUnitCube()

                val matrix: Mat4 = Ry * this.root.getTransform()
                this.root.setTransform(matrix)
            }

            modelViewer.render(currentTime)
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

    private fun normal(v: Float3): Float3 {
        val magnitude = sqrt(v.x * v.x + v.y * v.y + v.z * v.z)
        if (magnitude > 0) return Float3(v.x / magnitude, v.y / magnitude, v.z / magnitude)
        else return v
    }
    private fun cross(v1: Float3, v2: Float3): Float3 {
        return Float3(v1.y * v2.z - v1.z * v2.y, v1.z * v2.x - v1.x * v2.z, v1.x * v2.y - v1.y * v2.x)
    }
    private fun viewMatrix(Eye: Float3, At: Float3, Up: Float3): Mat4 {
        val zaxis = normal(At - Eye)
        val xaxis = normal(cross(Up, zaxis))
        val yaxis = cross(zaxis, xaxis)

        return Mat4.of(
            xaxis.x,            yaxis.x,            zaxis.x,            0.0f,
            xaxis.y,            yaxis.y,            zaxis.y,            0.0f,
            xaxis.z,            yaxis.z,            zaxis.z,            0.0f,
            -dot(xaxis, Eye),   -dot(yaxis, Eye),   -dot(zaxis, Eye),   1.0f,
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
            intensity = 50_000f
            modelViewer.scene.indirectLight = this
        }

        binding.surfaceView.setBackgroundColor(Color.TRANSPARENT)
        binding.surfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT)

        modelViewer.view.blendMode = com.google.android.filament.View.BlendMode.TRANSLUCENT
        modelViewer.scene.skybox = null
        modelViewer.setTransparent()
    }




}