package com.example.bookapp

import android.widget.Filter

class FilterCategory: Filter {

    private var filterList:ArrayList<ModelCategory>

    private var adapterCategory:AdapterCategory

    constructor(filterList: ArrayList<ModelCategory>, adapterCategory: AdapterCategory) {
        this.filterList = filterList
        this.adapterCategory = adapterCategory
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()

        //değelerin kontrolü
        if(constraint != null && constraint.isNotEmpty()){
            //büyük ve küçük harf duyarlılığını değişitirme
            constraint = constraint.toString().uppercase()
            val filteredModels:ArrayList<ModelCategory> = ArrayList()
            for(i in 0 until filterList.size){
                if(filterList[i].category.uppercase().contains(constraint)){
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        }
        else{
            //arama kutusu boş ya da geçersiz ise
            results.count = filterList.size
            results.values= filterList
        }
        return results
    }

    override fun publishResults(constraint:CharSequence?, results: FilterResults) {
        //filtreyi uygulayama kısmı
        adapterCategory.categoryArrayList = results.values as ArrayList<ModelCategory>

        //değişiklik bildirimi
        adapterCategory.notifyDataSetChanged()
    }
}