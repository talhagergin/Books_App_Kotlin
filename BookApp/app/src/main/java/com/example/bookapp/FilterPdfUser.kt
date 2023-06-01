package com.example.bookapp

import android.widget.Filter

class FilterPdfUser: Filter {
    var filterList:ArrayList<ModelBook>

    var adapterPdfUser : AdapterBookUser

    constructor(filterList: ArrayList<ModelBook>, adapterPdfUser: AdapterBookUser) : super() {
        this.filterList = filterList
        this.adapterPdfUser = adapterPdfUser
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint : CharSequence? = constraint

        val results = FilterResults()
        if(constraint != null && constraint.isNotEmpty()){
            constraint = constraint.toString().uppercase()

            val filteredModels = ArrayList<ModelBook>()
            for(i in filterList.indices){
                if(filterList[i].title.uppercase().contains(constraint)){
                    filteredModels.add(filterList[i])
                }
            }
            //return filter
            results.count = filteredModels.size
            results.values = filteredModels
        }else{
            results.count = filterList.size
            results.values=filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        //apply filter
        adapterPdfUser.pdfArrayList = results.values as ArrayList<ModelBook>

        adapterPdfUser.notifyDataSetChanged()
    }
}