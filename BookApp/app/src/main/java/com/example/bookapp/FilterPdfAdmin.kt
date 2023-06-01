package com.example.bookapp

import android.widget.Filter

class FilterPdfAdmin : Filter {
    var filterList:ArrayList<ModelBook>
    var adapterPdfAdmin:AdapterBookAdmin

    constructor(filterList: ArrayList<ModelBook>, adapterPdfAdmin: AdapterBookAdmin) {
        this.filterList = filterList
        this.adapterPdfAdmin = adapterPdfAdmin
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint:CharSequence? = constraint
        val results = FilterResults()
        if (constraint != null && constraint.isNotEmpty()){
            //büyük küçük harf duyarlılığı
            constraint = constraint.toString().lowercase()
            var filteredModels = ArrayList<ModelBook>()
            for(i in filterList.indices){
                if(filterList[i].title.lowercase().contains(constraint)){
                    filterList.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        }else{
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        //filtreyi uyguluyoruz
        adapterPdfAdmin.pdfArrayList = results.values as ArrayList<ModelBook>

        //değişiklik bildirimi
        adapterPdfAdmin.notifyDataSetChanged()
    }
}