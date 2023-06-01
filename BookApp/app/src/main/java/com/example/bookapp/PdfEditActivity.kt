package com.example.bookapp

import android.app.AlertDialog
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.bookapp.databinding.ActivityPdfEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfEditActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityPdfEditBinding

    private companion object{
        private const val TAG = "PDF_EDIT_TAG"
    }

    //adapterpdfadmin sayfasından kitap id sini aldık
    private var bookId = ""

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    //kategorileri arraye attık
    private lateinit var categoryTitleArrayList: ArrayList<String>

    //kategori id lerinidi arraye attık
    private lateinit var categoryIdArrayList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId")!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Lütfen bekleyiniz...")
        progressDialog.setCanceledOnTouchOutside(false)

        loadCategories()
        loadBookInfo()

        //geri gelme
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //kategori seç
        binding.categoryTv.setOnClickListener {
            categoryDialog()
        }
        //güncelle butonu
        binding.submitBtn.setOnClickListener {
            validateData()

        }
    }

    private fun loadBookInfo() {
        Log.d(TAG,"loadBookInfo: Kitap bilgileri yükleniyor")

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    selectedCategoryId = snapshot.child("categoryId").value.toString()
                    val description  = snapshot.child("description").value.toString()
                    val title = snapshot.child("title").value.toString()

                    binding.tittleEt.setText(title)
                    binding.descriptionEt.setText(description)

                    Log.d(TAG,"onDataChange: Kitabın kategori bilgileri yükleniyor")
                    val refBookCategory = FirebaseDatabase.getInstance().getReference("Categories")
                    refBookCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object :ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                //kategoriyi çekicez
                                val category = snapshot.child("category").value
                                binding.categoryTv.text = category.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private var title = ""
    private var description = ""
    private fun validateData() {
        //veri kontrolü
        title = binding.tittleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()

        if(title.isEmpty()){
            Toast.makeText(this,"Kitap Başlığı Girin",Toast.LENGTH_LONG).show()
        }else if(description.isEmpty()){
            Toast.makeText(this,"Kitap Açıklaması Girin",Toast.LENGTH_LONG).show()
        }
        else if (selectedCategoryId.isEmpty()){
            Toast.makeText(this,"Kategori Seçin",Toast.LENGTH_LONG).show()
        }
        else{
            updatePdf()
        }
    }

    private fun updatePdf() {
        Log.d(TAG,"updatePdf: Kitap bilgileri güncelleniyor...")

        progressDialog.setMessage("Kitap bilgileri güncelleniyor...")
        progressDialog.show()

        //veritabanında güncelleme
        val hashMap = HashMap<String, Any>()
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        Log.d(TAG,"updateLoad: $selectedCategoryId")
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Log.d(TAG,"updatePdf: Güncelleme başarılı...")
                Toast.makeText(this,"Kitap başarıyla güncellendi",Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {e->
                Log.d(TAG,"updatePdf: Güncelleme başarısız hata oluştu")
                progressDialog.dismiss()
                Toast.makeText(this,"Güncelleme başarısız hata oluştu ${e.message}",Toast.LENGTH_LONG).show()
            }
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""
    private fun categoryDialog() {
        //string arraye dönüştürdük

        val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size)
        for(i in categoryTitleArrayList.indices){
            categoriesArray[i] = categoryTitleArrayList[i]
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Kategori Seçin")
            .setItems(categoriesArray){dialog,position->
                selectedCategoryId = categoryIdArrayList[position]
                selectedCategoryTitle = categoryTitleArrayList[position]

                //text view lara bilgi aktarımı
                binding.categoryTv.text= selectedCategoryTitle
            }
            .show()

    }

    private fun loadCategories() {
        Log.d(TAG,"loadCategories: kategoriler yükleniyor")

        categoryTitleArrayList = ArrayList()
        categoryIdArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object:ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                categoryIdArrayList.clear()
                categoryTitleArrayList.clear()

                for(ds in snapshot.children){
                    val id = "${ds.child("id").value}"
                    val category = "${ds.child("category").value}"

                    categoryIdArrayList.add(id)
                    categoryTitleArrayList.add(category)

                    Log.d(TAG,"onDataChange: Kategori Id $id")
                    Log.d(TAG,"onDataChange: Kategori $category")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}