package com.example.bookapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.coordinatorlayout.widget.CoordinatorLayout.AttachedBehavior
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.bookapp.databinding.ActivityDashboardUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class DashboardUserActivity : AppCompatActivity() {
    //view binding
    private lateinit var binding: ActivityDashboardUserBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    private lateinit var viewPageAdapter: ViewPageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //inift firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        setupWithViewPageAdapter(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        //çıkış butonuna tıklandığında
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }

    private fun setupWithViewPageAdapter(viewPager: ViewPager){
        viewPageAdapter = ViewPageAdapter(
        supportFragmentManager,
        FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
        this)

         categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()

                val modelAll = ModelCategory("01","All",1,"")
                val modelMostViewed = ModelCategory("01","Most Viewed",1,"")
                val modelMostDownloaded = ModelCategory("01","Most Download",1,"")

                categoryArrayList.add(modelAll)
                categoryArrayList.add(modelMostViewed)
                categoryArrayList.add(modelMostDownloaded)
                //add to viewPageAdapter
                viewPageAdapter.addFragment(
                    BookUserFragment.newInstance(
                        "${modelAll.id}",
                        "${modelAll.category}",
                        "${modelAll.uid}"
                    ),modelAll.category
                )
                viewPageAdapter.addFragment(
                    BookUserFragment.newInstance(
                        "${modelMostViewed.id}",
                        "${modelMostViewed.category}",
                        "${modelMostViewed.uid}"
                    ),modelMostViewed.category
                )
                viewPageAdapter.addFragment(
                    BookUserFragment.newInstance(
                        "${modelMostDownloaded.id}",
                        "${modelMostDownloaded.category}",
                        "${modelMostDownloaded.uid}"
                    ),modelMostDownloaded.category
                )

                //refresh list
                viewPageAdapter.notifyDataSetChanged()

                for(ds in snapshot.children){
                    //get data in model
                    val model = ds.getValue(ModelCategory::class.java)
                    //add to list
                    categoryArrayList.add(model!!)
                    //add to viewPageAdapter
                    viewPageAdapter.addFragment(
                        BookUserFragment.newInstance(
                            "${model.id}",
                            "${model.category}",
                            "${model.uid}"
                        ),model.category
                    )
                    //refresh list
                    viewPageAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        viewPager.adapter = viewPageAdapter
    }

    class ViewPageAdapter(fm: FragmentManager, behavior: Int, context:Context): FragmentPagerAdapter(fm,behavior){

        private val fragmentList: ArrayList<BookUserFragment> = ArrayList();
        private val fragmentTitleList: ArrayList<String> = ArrayList();
        private val context : Context
        init {
            this.context = context
        }
        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList[position]
        }

        public fun addFragment(fragment: BookUserFragment,title:String){
            fragmentList.add(fragment)
            fragmentTitleList.add(title)
        }
    }

    private fun checkUser() {
        //şu an oturumu olan kullanıcıyı getir
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null){
            //giriş yapan kullanıcı yok ana menüye yallahh
           binding.subTittleTv.text="Giriş Yapılmadı"
        }
        else{
            //giriş yapmış kardeşimizin kullanıcı bilgileri
            val email = firebaseUser.email
            binding.subTittleTv.text = email
        }
    }
}