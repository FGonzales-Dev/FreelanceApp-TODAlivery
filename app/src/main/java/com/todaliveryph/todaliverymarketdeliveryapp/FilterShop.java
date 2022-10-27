package com.todaliveryph.todaliverymarketdeliveryapp;

import android.widget.Filter;

import com.todaliveryph.todaliverymarketdeliveryapp.adapters.AdapterProductSeller;
import com.todaliveryph.todaliverymarketdeliveryapp.adapters.AdapterShop;
import com.todaliveryph.todaliverymarketdeliveryapp.models.ModelShop;

import java.util.ArrayList;

public class FilterShop extends Filter {


    private AdapterShop adapter;
    private ArrayList<ModelShop> filterlist;

    public FilterShop(AdapterShop adapter, ArrayList<ModelShop> filterlist) {
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
            ArrayList<ModelShop> filteredModels = new ArrayList<>();
            for (int i=0; i<filterlist.size(); i++){
                //chekcing..., serach by shopname and place
                if (filterlist.get(i).getShopName().toUpperCase().contains(constraint) ||
                        filterlist.get(i).getAddress().toUpperCase().contains(constraint)){

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
        adapter.shopsList = (ArrayList<ModelShop>)results.values;
        //refresh adapter
        adapter.notifyDataSetChanged();

    }
}
