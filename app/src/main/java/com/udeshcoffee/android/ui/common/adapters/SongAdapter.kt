package com.udeshcoffee.android.ui.common.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import com.udeshcoffee.android.R
import com.udeshcoffee.android.interfaces.OnSongItemClickListener
import com.udeshcoffee.android.model.Song
import com.udeshcoffee.android.ui.common.viewholders.AlbumSongViewHolder
import com.udeshcoffee.android.ui.common.viewholders.SongViewHolder
import io.reactivex.functions.Consumer
/**
 * Created by Udesh on 2/18/2017.
 */

class SongAdapter(private val dataType: Int, private val hasShuffle: Boolean):
        RecyclerView.Adapter<RecyclerView.ViewHolder>(), FastScrollRecyclerView.SectionedAdapter, Consumer<List<Song>>{

    private var mDataset: List<Song> = ArrayList()
    private var context: Context? = null
    var currentSong: Int = 0
        private set
    var listener: OnSongItemClickListener? = null
    var currentId: Long = 0
        set(value) {
            field = value
            checkCurrentSong()
        }

//    TODO - Selection
//    var isSelection: Boolean = false
//        set(value) {
//            if (!value)
//                selected.clear()
//            field = value
//        }
//    private var selected = ArrayList<Song>()
//    private var selectListener = object : OnItemSelectListener{
//        override fun onSelectItem(postion: Int) {
//            selected.add(mDataset[postion])
//            notifyItemChanged(if (hasShuffle) postion + 1 else postion)
//        }
//
//        override fun onDeselectItem(postion: Int) {
//            selected.remove(mDataset[postion])
//            notifyItemChanged(if (hasShuffle) postion + 1 else postion)
//        }
//    }

    companion object {
        private const val TAG = "SongAdapter"

        const val ITEM_TYPE_NORMAL = 0
        const val ITEM_TYPE_ALBUM_ITEM = 1
        const val ITEM_TYPE_SHUFFLE = 2
    }

    override fun accept(songs: List<Song>) {
        this.mDataset = songs
        notifyDataSetChanged()
        checkCurrentSong()
        Log.d(TAG, songs.size.toString())
    }

    private fun checkCurrentSong() {
        if (currentId.toInt() != -1) {
            for (e in mDataset.indices) {
                if (mDataset[e].id == currentId) {
                    val prevSong = currentSong
                    if (prevSong != -1) {
                        notifyItemChanged(prevSong + 1)
                    }
                    currentSong = e
                    notifyItemChanged(e + 1)
                    break
                }
            }
        } else {
            currentSong = -1
            notifyDataSetChanged()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (hasShuffle && position == 0) {
            ITEM_TYPE_SHUFFLE
        } else {
            dataType
        }
    }

    override fun getSectionName(position: Int): String = when {
        position == 0 -> "#"
        position != -1 -> Character.toString(mDataset[position - 1].title[0]).toUpperCase()
        else -> ""
    }

    internal inner class ShuffleViewHolder(itemView: View):
            RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            listener!!.onShuffleClick()
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    init {
        mDataset = ArrayList()
        currentSong = -1
        currentId = -1
    }

    val songList: ArrayList<Song>
        get() = mDataset as ArrayList<Song>

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        return when (viewType) {
            ITEM_TYPE_NORMAL -> {
                val songView = inflater.inflate(R.layout.song, parent, false)
                SongViewHolder(context!!, songView, listener, hasShuffle) // view holder for normal items
            }
            ITEM_TYPE_ALBUM_ITEM -> {
                val songView = inflater.inflate(R.layout.albumsongitem, parent, false)
                AlbumSongViewHolder(context!!, songView, listener) // view holder for normal items
            }
            else -> {
                val shuffleView = inflater.inflate(R.layout.shuffle, parent, false)
                ShuffleViewHolder(shuffleView) // view holder for header items
            }
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, pos: Int) {
        var position = pos
        val itemType = getItemViewType(position)

        if (hasShuffle)
            position--

        if (itemType == ITEM_TYPE_NORMAL) {
            (holder as SongViewHolder).bindData(mDataset[position], currentSong == position)
        } else if (itemType == ITEM_TYPE_ALBUM_ITEM) {
            (holder as AlbumSongViewHolder).bindData(mDataset[position], currentSong == position)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int = if (mDataset.isNotEmpty()) {
        if (hasShuffle)
            mDataset.size + 1
        else
            mDataset.size
    } else 0

    fun getItem(pos: Int): Song {
        return mDataset[pos]
    }

    val songCount: Int
        get() = mDataset.size

}
