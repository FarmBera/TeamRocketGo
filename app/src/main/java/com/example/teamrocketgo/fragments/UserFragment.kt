package com.example.teamrocketgo.fragments

import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.Choreographer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.example.teamrocketgo.My_ModelViewer
import com.example.teamrocketgo.R
import com.example.teamrocketgo.databinding.FragmentUserBinding
import com.google.android.filament.utils.KtxLoader
import java.nio.ByteBuffer

class UserFragment : Fragment() {

    private lateinit var binding: FragmentUserBinding

    lateinit var choreographer: Choreographer
    lateinit var modelViewer: My_ModelViewer
    lateinit var userSurfaceView: SurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentUserBinding.inflate(inflater, container, false)

        choreographer = Choreographer.getInstance()
        userSurfaceView = binding.userSurfaceView
        modelViewer = My_ModelViewer(userSurfaceView)
        //binding.userSurfaceView.setOnTouchListener(modelViewer)

        val viewPager = activity?.findViewById<ViewPager>(R.id.viewPager)

        // SurfaceView 내에서 swipe 액션 금지
//        userSurfaceView.setOnTouchListener { _, event ->
//            when (event.actionMasked) {
//                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
//                    // 터치 다운, 업, 캔슬 이벤트 시 모델 회전 이벤트 반환
//                    modelViewer.onTouchEvent(event)
//
//                    // 터치 다운, 업, 캔슬 이벤트 시 스와이프 제어 이벤트 반환
//                    viewPager?.onTouchEvent(event)
//
//                    true
//                }
//                MotionEvent.ACTION_MOVE -> {
//                    // 무브 이벤트 시 모델 회전
//                    modelViewer.onTouchEvent(event)
//                    true
//                }
//
//                else -> false
//            }
//        }

        loadGltf("blue")
        loadEnvironment("pillars_2k")

        return binding.root
    }


    override fun onResume() {
        super.onResume()

        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        choreographer.postFrameCallback(frameCallback)
    }

    override fun onPause() {
        super.onPause()
        choreographer.removeFrameCallback(frameCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        choreographer.removeFrameCallback(frameCallback)
    }

    private val frameCallback = object : Choreographer.FrameCallback {

        private val startTime = System.nanoTime()

        override fun doFrame(currentTime: Long) {
            choreographer.postFrameCallback(this)
            var seconds = (currentTime - startTime).toDouble() / 1_000_000_000
            modelViewer.animator?.apply {
                if (animationCount > 0) {
                    applyAnimation(0, seconds.toFloat())
                }
                updateBoneMatrices()
            }

            modelViewer.render(currentTime)
        }

    }

    private fun readAsset(assetName: String): ByteBuffer {
        val input = requireActivity().assets.open(assetName)
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

        userSurfaceView.setBackgroundColor(Color.TRANSPARENT)
        userSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT)

        modelViewer.view.blendMode = com.google.android.filament.View.BlendMode.TRANSLUCENT
        modelViewer.scene.skybox = null
        modelViewer.setTransparent()
    }

}