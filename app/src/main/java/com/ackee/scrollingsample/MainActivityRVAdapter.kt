package com.ackee.scrollingsample

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivityRVAdapter : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView

    val adapter = TestAdapter()

    val scope = CoroutineScope(Job() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(RecyclerView(this).apply {
            recyclerView = this
            adapter = this@MainActivityRVAdapter.adapter
            layoutManager = LinearLayoutManager(context)
            preserveFocusAfterLayout = false
        })
        adapter.items = (0 until 10).map { Item.Article("Article $it") }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add("New articles").setOnMenuItemClickListener {
            simulateFetchNewArticles()
            true
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun simulateFetchNewArticles() {
        scope.launch {
            val newArticlesCount = 10
            adapter.items = (0 until newArticlesCount).map { Item.Placeholder("Placeholder $it") } + adapter.items
            delay(100)
            recyclerView.smoothScrollToPosition(0)
            delay(5000)
            // Uncomment for workaround fix
//            adapter.items = emptyList()
            adapter.items = (0 until newArticlesCount).map { Item.Article("New article $it") } + adapter.items.drop(newArticlesCount)
        }
    }

    sealed class Item {

        abstract val itemType: Int

        data class Placeholder(val text: String) : Item() {
            override val itemType: Int
                get() = 1
        }

        data class Article(val text: String) : Item() {
            override val itemType: Int
                get() = 2
        }
    }

    class TestAdapter : RecyclerView.Adapter<BaseViewHolder>() {

        var items: List<Item> = emptyList()
            set(value) {
                val oldList = field
                field = value
                val diffCallback = object : DiffUtil.Callback() {
                    override fun areItemsTheSame(
                        oldItemPosition: Int,
                        newItemPosition: Int
                    ): Boolean {
                        return oldList[oldItemPosition].javaClass == value[newItemPosition].javaClass
                    }

                    override fun getOldListSize(): Int = oldList.size

                    override fun getNewListSize(): Int = value.size

                    override fun areContentsTheSame(
                        oldItemPosition: Int,
                        newItemPosition: Int
                    ): Boolean {
                        return oldList[oldItemPosition].javaClass == value[newItemPosition].javaClass && oldList[oldItemPosition] == value[newItemPosition]
                    }
                }
                val result = DiffUtil.calculateDiff(diffCallback)
                result.dispatchUpdatesTo(this)
            }

        override fun getItemViewType(position: Int): Int {
            return items[position].itemType
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            return if (viewType == 1) {
                PlaceholderViewHolder(PlaceholderView(parent.context))
            } else {
                ArticleViewHolder(ArticleView(parent.context))
            }
        }

        override fun getItemCount(): Int {
            return items.count()
        }

        override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
            if (holder is PlaceholderViewHolder) {
                holder.bind(items[position] as Item.Placeholder)
            } else if (holder is ArticleViewHolder) {
                holder.bind(items[position] as Item.Article)
            }
        }
    }

    abstract class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view)

    open class PlaceholderViewHolder(
        val placeholderView: PlaceholderView
    ) : BaseViewHolder(placeholderView) {

        fun bind(placeholder: Item.Placeholder) {
            placeholderView.text = placeholder.text
        }
    }

    open class ArticleViewHolder(
        val articleView: ArticleView
    ) : BaseViewHolder(articleView) {

        fun bind(article: Item.Article) {
            articleView.text = article.text
        }
    }

    class PlaceholderView(context: Context) : AppCompatTextView(context) {

        init {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 250)
            setBackgroundColor(Color.CYAN)
        }
    }

    class ArticleView(context: Context) : AppCompatTextView(context) {

        init {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 250)
            setBackgroundColor(Color.GREEN)
        }
    }
}
