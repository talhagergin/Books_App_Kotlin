package com.example.bookapp

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.core.app.ActivityCompat
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.bookapp.databinding.ActivityPdfDetailBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.FileOutputStream
import kotlin.math.log

class PdfDetailActivity : AppCompatActivity() {
    //view binding
    private lateinit var binding :ActivityPdfDetailBinding

    private companion object{
        //TAG
        const val TAG = "BOOK_DETAILS_TAG"
    }
    //book id get from intent
    private var bookId = ""
    //get from firebase
    private var bookTitle = ""
    private var bookUrl = ""

    private lateinit var progressDialog : ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId")!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Lütfen bekleyiniz...")
        progressDialog.setCanceledOnTouchOutside(false)

        MyApplication.incrementBookViewCount(bookId)
        loadBookDetails()

        //geri
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //kitabın tamamını görüntüleme
        binding.readBookBtn.setOnClickListener {
            val intent = Intent(this,PdfViewActivity::class.java)
            intent.putExtra("bookId",bookId);
            startActivity(intent)
        }

        //indir butonuna basıldığında
        binding.downloadBookBtn.setOnClickListener {
            downloadBook()
        }
        }

    private fun downloadBook(){
        Log.d(TAG,"downloadBook: Kitap indiriliyor")
        //progress bar
        progressDialog.setMessage("Kitap indiriliyor...")
        progressDialog.show()

        //indirme işlemi
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        storageReference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener {bytes->
                Log.d(TAG,"downloadBook: Kitap indirliyor...")
                saveToDownloadsFolder(bytes)
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Log.d(TAG,"downloadBook: Kitap indirilirken hata ${e.message}")
                Toast.makeText(this,"İndirme başarısız oldu ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveToDownloadsFolder(bytes: ByteArray?) {
        Log.d(TAG,"saveToDownloadsFolder: kitap kaydedildi")
        val nameWithExtention = "${System.currentTimeMillis()}.pdf"
        try {
            val downloadsFolders = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsFolders.mkdir() //dosya yoksa oluşturduk

            val filePath = downloadsFolders.path + "/"+ nameWithExtention

            val out = FileOutputStream(filePath)
            out.write(bytes)
            out.close()

            Toast.makeText(this,"saveToDownloadsFolder: Dosya başarıyla kaydedildi",Toast.LENGTH_SHORT).show()
            Log.d(TAG,"saveToDownloadsFolder: hata oluştu burda")
            progressDialog.dismiss()
            increementDownloadCount()

        }catch (e: Exception){
            progressDialog.dismiss()
            Log.d(TAG,"saveToDownloadsFolder: Dosya kaydedilirken hata oluştu ${e.message}")
            Toast.makeText(this,"Dosya kaydedildi",Toast.LENGTH_SHORT).show()
        }
    }

    private fun increementDownloadCount() {
    //indirme sayısını artırma
        Log.d(TAG,"increementDownloadCount: ")

        //connect firebase
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //indirme sayısını görüntüleme
                    var downloadsCount = "${snapshot.child("downloadsCount").value}"
                    Log.d(TAG,"onDataChange: İndirime sayısı: $downloadsCount")

                    if(downloadsCount == "" || downloadsCount== "null"){
                        downloadsCount="0"
                    }

                    val newDownloadsCount: Long = downloadsCount.toLong() + 1
                    Log.d(TAG,"onDataChange: Yeni indirme sayısı:$newDownloadsCount")

                    //veritabanını güncelleme
                    val hashMap: HashMap<String, Any> = HashMap()
                    hashMap["downloadsCount"] = newDownloadsCount

                    val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                    dbRef.child(bookId)
                        .updateChildren(hashMap)
                        .addOnSuccessListener {
                            Log.d(TAG,"onDataChange: indirme sayısı güncellendi")

                        }
                        .addOnFailureListener {e->
                            Log.d(TAG,"onDataChange: indirme sayısında hata ${e.message}")
                        }


                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun loadBookDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get data
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                     bookTitle = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                     bookUrl = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    val date = MyApplication.formatTimeStamp(timestamp.toLong())

                    MyApplication.loadCategory(categoryId,binding.categoryTv)

                    MyApplication.loadPdfFormatUrlSinglePage("$bookUrl","$bookTitle",binding.pdfView,binding.progressBar,binding.pagesTv)
                    MyApplication.loadPdfSize("$bookUrl","$bookTitle",binding.sizeTv)

                    //set data
                    binding.titleTv.text=bookTitle
                    binding.descriptionTv.text=description
                    binding.viewsTv.text=viewsCount
                    binding.downloadsTv.text=downloadsCount
                    binding.dateTv.text=date


                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}