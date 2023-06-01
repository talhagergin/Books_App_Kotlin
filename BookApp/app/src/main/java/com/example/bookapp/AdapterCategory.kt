package com.example.bookapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.Display.Mode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.databinding.RowCategoryBinding
import com.google.firebase.database.FirebaseDatabase

class AdapterCategory :RecyclerView.Adapter<AdapterCategory.HolderCategory>,Filterable {
    private val context:Context
    public var categoryArrayList:ArrayList<ModelCategory>
    private var filterList: ArrayList<ModelCategory>

    private var filter : FilterCategory? = null
    private lateinit var binding :RowCategoryBinding
    //constructor

    constructor(context: Context, categoryArrayList: ArrayList<ModelCategory>) {
        this.context = context
        this.categoryArrayList = categoryArrayList
        this.filterList = categoryArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        //inflate
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context),parent,false)

        return HolderCategory(binding.root)
    }

    override fun getItemCount(): Int {
      return categoryArrayList.size // listedeki eleman sayısını alıyor
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        //verileri almak
        val model = categoryArrayList[position]
        val id = model.id
        val category = model.category
        val uid = model.uid
        val timestamp = model.timestamp

        //verileri eşleme
        holder.categoryTv.text = category

        //kategori silme işlemi
        binding.deleteBtn.setOnClickListener {
            //onay işlemi
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
                .setMessage("Bu kategoriyi silmek istediğinizden emin misiniz ?")
                .setPositiveButton("Onayla"){a,d->
                    Toast.makeText(context,"Siliniyor...",Toast.LENGTH_SHORT).show()
                    deleteCategory(model,holder)
                }
                .setNegativeButton("İptal"){a,d->
                    a.dismiss()
                }
                .show()
        }
        //pdf admin list sayfasına
        holder.itemView.setOnClickListener{
            val intent = Intent(context,PdfListAdminActivity::class.java)
            intent.putExtra("categoryId",id)
            intent.putExtra("category",category)
            context.startActivity(intent)
        }
    }

    private fun deleteCategory(model: ModelCategory, holder: HolderCategory) {
        //kategori id çekiyor
        val id = model.id
        //Firebase kategoriler
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context,"Kategori silindi",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                Toast.makeText(context,"Bir hata oluştu ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    //row_category de görüntülenecek verileri çekiyoruz
    inner class HolderCategory(itemView: View): RecyclerView.ViewHolder(itemView){
        var categoryTv:TextView = binding.categoryTv
        var deleteBtn:ImageButton = binding.deleteBtn
    }

    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterCategory(filterList,this)
        }
        return filter as FilterCategory
    }


}