package com.example.teamrocketgo.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.teamrocketgo.databinding.ActivityProfileBinding
import com.example.teamrocketgo.fragments.CreditFragment
import com.example.teamrocketgo.fragments.UserFragment
import com.google.android.material.tabs.TabLayout

class ProfileActivity : AppCompatActivity() {

    lateinit var binding : ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewPager: ViewPager = binding.viewPager
        val tabLayout: TabLayout = binding.tabLayout

        // 프래그먼트를 추가할 어댑터 설정
        val adapter = MyPagerAdapter(supportFragmentManager)
        adapter.addFragment(UserFragment(), "User")
        adapter.addFragment(CreditFragment(), "Credit")

        // 뷰페이저에 어댑터 설정
        viewPager.adapter = adapter

        // 탭 레이아웃과 뷰페이저 연결
        tabLayout.setupWithViewPager(viewPager)
    }

    // 프래그먼트를 추가할 어댑터 클래스
    class MyPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val fragmentList: MutableList<Fragment> = ArrayList()
        private val fragmentTitleList: MutableList<String> = ArrayList()

        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitleList[position]
        }

        // 프래그먼트 추가 메서드
        fun addFragment(fragment: Fragment, title: String) {
            fragmentList.add(fragment)
            fragmentTitleList.add(title)
        }
    }
}