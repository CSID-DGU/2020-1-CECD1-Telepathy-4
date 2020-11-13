package com.example.betterfit.ui.contents;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.betterfit.R;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ItemViewHolder> {
    List<Products> ProductList;

    public ProductAdapter(List<Products> ProductList) {
        this.ProductList = ProductList;
    }

    @Override public ProductAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from( parent.getContext()).inflate(R.layout.fragment_item, parent, false);
        return new ProductAdapter.ItemViewHolder(view);
    }

    @Override public void onBindViewHolder(ProductAdapter.ItemViewHolder holder, int position) {
        holder.thumbnail.setImageResource(ProductList.get(position).getThumbnail());
        holder.name.setText(ProductList.get(position).getName());
        holder.description.setText(ProductList.get(position).getDescription());
    }

    @Override public int getItemCount() {
        return ProductList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        protected ImageView thumbnail;
        protected TextView name;
        protected TextView description;

        public ItemViewHolder(View view) {
            super(view);
            this.thumbnail = (ImageView) view.findViewById(R.id.product);
            this.name = (TextView) view.findViewById(R.id.productName);
            this.description = (TextView) view.findViewById(R.id.description);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    Products products = ProductList.get(pos);
                    String url = products.url;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
                    v.getContext().startActivity(intent);
                }
            });
        }
    }
}