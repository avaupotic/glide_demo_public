package com.avaupotic.tastynavigator.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.avaupotic.lib.Vendor
import com.avaupotic.tastynavigator.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

class MyRecyclerViewAdapterVendor(private val data: MutableList<Vendor>, private val onClickObject: MyIRecyclerView)
    : RecyclerView.Adapter<MyRecyclerViewAdapterVendor.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.ivPhotoCV1)
        val tvName: TextView = itemView.findViewById(R.id.tvNameCV1)
        val tvLocation: TextView = itemView.findViewById(R.id.tvLocationCV1)
        val row: CardView = itemView.findViewById(R.id.cvRow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_vendor, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemsViewModel = data[position]

        holder.tvName.text = itemsViewModel.name
        holder.tvLocation.text = itemsViewModel.location

        Glide.with(holder.itemView)
            .load(itemsViewModel.imgLink)
            .placeholder(R.drawable.dish_placeholder_white)
            .error(R.drawable.dish_placeholder_white)
            .apply(
                RequestOptions
                //.centerCropTransform() // Slika je popolnoma vidna brez whitespace-a
                .circleCropTransform() // Izreži sliko kot krog
                //.transform(RoundedCorners(16)) // Dodaj mehke robove
                //.override(300, 300) // Podaj širino in višino
            )
            .into(holder.imageView);


        holder.row.setOnClickListener(object:View.OnClickListener{
            override fun onClick(v: View?) {
                onClickObject.onClick(v, holder.adapterPosition)
                notifyItemChanged(holder.adapterPosition)
            }
        })

        holder.row.setOnLongClickListener(object:View.OnLongClickListener{
            override fun onLongClick(v: View?): Boolean {
                onClickObject.onLongClick(v, holder.adapterPosition)
                notifyItemChanged(holder.adapterPosition)
                return true
            }
        })
    }
}