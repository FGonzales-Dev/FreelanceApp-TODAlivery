package com.todaliveryph.todaliverymarketdeliveryapp;

import android.widget.Filter;

import com.todaliveryph.todaliverymarketdeliveryapp.adapters.AdapterProductSeller;
import com.todaliveryph.todaliverymarketdeliveryapp.adapters.AdapterProductUser;
import com.todaliveryph.todaliverymarketdeliveryapp.models.ModelProduct;

import java.util.ArrayList;

public class FilterProductUser extends Filter {


    private AdapterProductUser adapter;
    private ArrayList<ModelProduct> filterlist;


    public FilterProductUser(AdapterProductUser adapter, ArrayList<ModelProduct> filterlist) {
        this.adapter = adapter;
        this.filterlist = filterlist;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //validate data for search query
        if (constraint !=null && constraint.length() >0){
            //change to uppercase to make case insensitive
            constraint = constraint.toString().toUpperCase();
            //store our filtered list
            ArrayList<ModelProduct> filteredModels = new ArrayList<>();
            for (int i=0; i<filterlist.size(); i++){
                //chekcing..., serach by title and category
                if (filterlist.get(i).getProductTitle().toUpperCase().contains(constraint) ||
                  filterlist.get(i).getProductCategory().toUpperCase().contains(constraint)){

                    // add filtered item  to list
                    filteredModels.add(filterlist.get(i));
                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;


        }else {
            results.count = filterlist.size();
            results.values = filterlist;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

        adapter.productsList = (ArrayList<ModelProduct>)results.values;
        //refresh adapter
        adapter.notifyDataSetChanged();

    }
}
