package com.example.bookapp

import android.app.AlertDialog
import android.app.Application.ActivityLifecycleCallbacks
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Display.Mode
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.bookapp.databinding.ActivityBookAddBinding
import com.example.bookapp.databinding.ActivityCategoryAddBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class BookAddActivity : AppCompatActivity() {
    //view binding
    private lateinit var binding: ActivityBookAddBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    //kategori listesini çekme
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    private var pdfUri : Uri? = null

    private val TAG = "PDF_ADD_TAG"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance()
        loadPdfCategories()
        //show dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Lütfen bekleyiniz..")
        progressDialog.setCanceledOnTouchOutside(false)

        //geri gelme
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //kategorileri listelemeye tıklandığında
        binding.categoryTv.setOnClickListener {
            categoryPickDialog()
        }

        //dosya ekleme resmine tıklandığında
        binding.attachPdfButton.setOnClickListener {
            pdfPickIntent()
        }

        //kitap yükleme işlemi başlat
        binding.submitBtn.setOnClickListener {
            //veriler ve kontrolleri
            validateData()
        }
    }
    private var tittle = ""
    private var description = ""
    private var category = ""
    private fun validateData() {
        Log.d(TAG,"validateData: Veriler kontrol ediliyor")

        tittle = binding.tittleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()

        //veri kontrolü
        if(tittle.isEmpty()){
            Toast.makeText(this,"Başlık girin...",Toast.LENGTH_LONG).show()
        }else if(description.isEmpty()){
            Toast.makeText(this,"Açıklama girin...",Toast.LENGTH_LONG).show()
        }else if(category.isEmpty()){
            Toast.makeText(this,"Kategori seçiniz...",Toast.LENGTH_LONG).show()
        }
        else if(pdfUri == null){
            Toast.makeText(this,"Dosya seçin...",Toast.LENGTH_LONG).show()
        }else{
            uploadPdfToStorage()
        }
    }

    private fun uploadPdfToStorage() {
        Log.d(TAG,"uploadPdfToStorage: depo güncelleniyor...")

        progressDialog.setMessage("Kitaplık güncelleniyor...")
        progressDialog.show()

        val timestamp = System.currentTimeMillis()

        val filePathAndName = "Books/$timestamp"
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener {taskSnapshot ->
                Log.d(TAG,"uploadToStorage: Dosya yüklendi yönlendiliyorsunuz...")

                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while(!uriTask.isSuccessful);
                val uploadedPdfUrl = "${uriTask.result}"

                uploadPdfInfoToDb(uploadedPdfUrl,timestamp)
            }
            .addOnFailureListener{e->
                Log.d(TAG,"uploadToStorage: Kitap yüklenirken hata oluştu ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this,"Kitap yüklenirken hata oluştu...",Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadPdfInfoToDb(uploadedPdfUrl: String, timestamp: Long) {
        Log.d(TAG,"uploadPdfInfoToDb: veritabanına yükleniyor")
        progressDialog.setMessage("Kitap bilgilieri yükleniyor...")

        val uid = firebaseAuth.uid

        val hashMap: HashMap<String ,Any> = HashMap()
        hashMap["uid"]="$uid"
        hashMap["id"]="$timestamp"
        hashMap["title"]="$tittle"
        hashMap["description"]="$description"
        hashMap["categoryId"]= "$selectedCategoryId"
        hashMap["url"] = "$uploadedPdfUrl"
        hashMap["timestamp"] = timestamp
        hashMap["viewsCount"]= 0
        hashMap["downloadsCount"]= 0

        //kitaplara bağlanma
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG,"uploadPdfInfoToDb: kitap veritabanına kaydedildi")
                progressDialog.dismiss()
                Toast.makeText(this,"Kitap başarıyla eklendi",Toast.LENGTH_SHORT).show()
                pdfUri = null
            }
            .addOnFailureListener { e->
                Log.d(TAG,"uploadPdfInfoToDb: Kitap yüklenirken hata oluştu ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this,"Kitap yüklenirken hata oluştu...",Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadPdfCategories() {
        Log.d(TAG,"loadPdfCategories: Kategoriler Yükleniyor")
        categoryArrayList = ArrayList()

        //veritabanı bağlantı
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //veri eklemeden önce liste temizleme
                categoryArrayList.clear()
                for(ds in snapshot.children){
                    val model = ds.getValue(ModelCategory::class.java)

                    categoryArrayList.add(model!!)
                    Log.d(TAG,"onDataChange: ${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    private var selectedCategoryId = ""
    private var selectedCategoryTittle = ""

    private fun categoryPickDialog(){
        Log.d(TAG,"categoryPickDialog: Kategoriler görüntüleniyor")

        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for(i in categoryArrayList.indices){
            categoriesArray[i] = categoryArrayList[i].category
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Kategori Seçiniz")
            .setItems(categoriesArray){dialog, which->
                //kategoriye tıklandığında
                selectedCategoryId = categoryArrayList[which].id
                selectedCategoryTittle = categoryArrayList[which].category

                //seçilen kategoriyi text view aktarma
                binding.categoryTv.text = selectedCategoryTittle

                Log.d(TAG,"categoryPickDialog: Seçilen Kategori ID: $selectedCategoryId")
                Log.d(TAG,"categoryPickDialog: Seçilen Kategori Adı: $selectedCategoryTittle")
            }
            .show()
    }
    private fun pdfPickIntent(){
        Log.d(TAG,"pdfPickIntent: Dosya seçimi başlıyor")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }
    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{result ->
            if(result.resultCode == RESULT_OK){
                Log.d(TAG,"Kategori seçildi: ")
                pdfUri = result.data!!.data
            }
            else{
                Log.d(TAG,"Kategori seçimi iptal edildi")
                Toast.makeText(this,"İptal Edildi",Toast.LENGTH_SHORT).show()
            }
        }
    )
}