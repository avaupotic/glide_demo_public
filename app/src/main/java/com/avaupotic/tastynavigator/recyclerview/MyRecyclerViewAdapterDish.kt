package com.avaupotic.tastynavigator.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.avaupotic.lib.Dish
import com.avaupotic.tastynavigator.R
import com.bumptech.glide.Glide


class MyRecyclerViewAdapterDish(private val data: MutableList<Dish>, private val onClickObject: MyIRecyclerView)
    : RecyclerView.Adapter<MyRecyclerViewAdapterDish.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.ivPhotoCV2)
        val tvName: TextView = itemView.findViewById(R.id.tvNameCV2)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPriceCV2)
        val row: CardView = itemView.findViewById(R.id.cvRow2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_dish, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemsViewModel = data[position]

        holder.tvName.text = itemsViewModel.name
        holder.tvPrice.text = itemsViewModel.price.toString() + "€"

        Glide.with(holder.itemView)
            .load(itemsViewModel.imgLink)
            .placeholder(R.drawable.dish_placeholder_white)
            .error(R.drawable.dish_placeholder_white)
            .into(holder.imageView)


        holder.row.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                onClickObject.onClick(v, holder.adapterPosition)
                notifyItemChanged(holder.adapterPosition)
            }
        })

        holder.row.setOnLongClickListener(object: View.OnLongClickListener{
            override fun onLongClick(v: View?): Boolean {
                onClickObject.onLongClick(v, holder.adapterPosition)
                notifyItemChanged(holder.adapterPosition)
                return true
            }
        })
    }
}