package com.example.bookapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.databinding.RowBookAdminBinding

class AdapterBookAdmin :RecyclerView.Adapter<AdapterBookAdmin.HolderPdfAdmin>,Filterable{
    //context
    private var context: Context
    //kitapları array liste aktarma
    public var pdfArrayList:ArrayList<ModelBook>
    private val filterList:ArrayList<ModelBook>
    //view binding
    private lateinit var binding:RowBookAdminBinding

    //filter object
    private var filter :FilterPdfAdmin? = null

    constructor(context: Context, pdfArrayList: ArrayList<ModelBook>) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin {
        binding = RowBookAdminBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderPdfAdmin(binding.root)
    }
    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) {
        //veri çekme / düzenleme/ daha fazla butonu
        //veri çekme
        val model = pdfArrayList[position]
        val pdfId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val pdfUrl = model.url
        val timestamp = model.timestamp
        val formattedData = MyApplication.formatTimeStamp(timestamp)

        //set data
        holder.titleTv.text = title
        binding.descriptionTv.text = description
        holder.dateTv.text = formattedData

        //kategori id
        MyApplication.loadCategory(categoryId, holder.categoryTv)

        MyApplication.loadPdfFormatUrlSinglePage(pdfUrl,title,holder.pdfView,holder.progressBar,null)

        //load pdf size
        MyApplication.loadPdfSize(pdfUrl,title,holder.sizeTv)

        //kitap düzenleme ve silme
        holder.moreBtn.setOnClickListener {
            moreOptionsDialog(model,holder)
        }
        //kitap detay sayfasına
        holder.itemView.setOnClickListener{
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId",pdfId)
            context.startActivity(intent)
        }

    }

    private fun moreOptionsDialog(model: ModelBook, holder: AdapterBookAdmin.HolderPdfAdmin) {
        //kitap bilgilerini çektik
        val bookId = model.id
        val bookUrl = model.url
        val bookTitle = model.title

        //seçenekleri gösterme
        val options = arrayOf("Düzenle","Sil")

        var builder = AlertDialog.Builder(context)
        builder.setTitle("İşlem Seçin")
            .setItems(options){dialog, position->
                //seçeneğe tıklandığında
                if(position == 0){
                    //Düzenle
                    val intent = Intent(context,PdfEditActivity::class.java)
                    intent.putExtra("bookId",bookId)
                    context.startActivity(intent)
                }else if(position == 1){
                    //sil
                    MyApplication.deleteBook(context,bookId, bookUrl,bookTitle)
                }

            }
            .show()
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }



    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterPdfAdmin(filterList,this)
        }
        return filter as FilterPdfAdmin
    }

    inner class HolderPdfAdmin(itemView : View):RecyclerView.ViewHolder(itemView){
        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val description = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
        val moreBtn = binding.moreBtn
    }

}