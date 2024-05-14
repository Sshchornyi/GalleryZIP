package com.example.galleryzip;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class RecyclerAdapter  extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private ArrayList<Uri> uriArrayList;
    private Context context;
    CountOfImagesWhenRemoved countOfImagesWhenRemoved;

    public RecyclerAdapter(ArrayList<Uri> uriArrayList, Context context, CountOfImagesWhenRemoved countOfImagesWhenRemoved) {
        this.uriArrayList = uriArrayList;
        this.context = context;
        this.countOfImagesWhenRemoved = countOfImagesWhenRemoved;
    }


    @NonNull
    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.single_image,parent,false);

        return new ViewHolder(view, countOfImagesWhenRemoved);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.ViewHolder holder, int position) {
        //holder.imageView.setImageURI(uriArrayList.get(position));

        Glide.with(context)
                .load(uriArrayList.get(position))
                .into(holder.imageView);

        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uriArrayList.remove(uriArrayList.get(holder.getAdapterPosition()));
                notifyItemRemoved(holder.getAdapterPosition());
                notifyDataSetChanged();
                countOfImagesWhenRemoved.clicked(uriArrayList.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return uriArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, remove;
        CountOfImagesWhenRemoved countOfImagesWhenRemoved;
        public ViewHolder(@NonNull View itemView, CountOfImagesWhenRemoved countOfImagesWhenRemoved) {
            super(itemView);

            this.countOfImagesWhenRemoved = countOfImagesWhenRemoved;

            imageView = itemView.findViewById(R.id.image);
            remove = itemView.findViewById(R.id.remove);
        }
    }
    public interface CountOfImagesWhenRemoved{
        void clicked (int getSize);
    }
}
