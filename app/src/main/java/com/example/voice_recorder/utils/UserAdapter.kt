package com.example.voice_recorder.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.media.MediaMetadataRetriever
import com.example.voice_recorder.R
import java.io.File


class UserAdapter(
    private val texts: ArrayList<Element>,
    private val onClickButton: (Element, Button, Chronometer) -> Unit
): RecyclerView.Adapter<UserAdapter.UserViewHolder>() {


    class UserViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        private val title: TextView = root.findViewById(R.id.name)
        private val date: TextView = root.findViewById(R.id.text)
        var playButton: Button = root.findViewById(R.id.play_button)
        val time : Chronometer = root.findViewById(R.id.chronometer_current)
        private val fullTime: TextView = root.findViewById(R.id.chronometer_full)


        private fun getDuration(file: File): Long {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(file.absolutePath)
            val durationStr =
                mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            return durationStr!!.toLong()
        }


        fun bind(element: Element, button : Button) {
            title.text = element.title
            date.text = element.body
            playButton = button
            playButton.tag = false
            val milliseconds = getDuration(File(element.file))
            val minutes = milliseconds / 1000 / 60
            val seconds = milliseconds / 1000 % 60
            fullTime.text = "/" + if (minutes >= 10) minutes.toString() else "0$minutes:" +
                                  if (seconds >= 10) seconds.toString() else "0$seconds"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val holder = UserViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.list_item, parent, false)
        )
        holder.playButton.setOnClickListener {
            if (holder.absoluteAdapterPosition != -1) {
                onClickButton(texts[holder.absoluteAdapterPosition], holder.playButton, holder.time)
            }
        }
        return holder
    }

    fun setData(newPosts : ArrayList<Element>) {
        val diffResult = DiffUtil.calculateDiff(
            ProductDiffUtilCallBack(texts, newPosts)
        )
        texts.clear()
        texts.addAll(newPosts)
        diffResult.dispatchUpdatesTo(this)
    }


    fun deleteItem(e : Element) {
        val list = ArrayList(texts)
        if (list.remove(e)) {
            setData(list)
        }
    }

    fun addElement(e : Element) {
        val list = ArrayList(texts)
        list.add(e)
        setData(list)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) = holder.bind(texts[position], holder.playButton)
    override fun getItemCount() = texts.size
}