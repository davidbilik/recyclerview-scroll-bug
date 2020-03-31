package com.ackee.scrollingsample

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyModelWithView
import com.airbnb.epoxy.EpoxyRecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivityEpoxy : AppCompatActivity() {

    lateinit var recyclerView: EpoxyRecyclerView
    val controller = TestController()

    val scope = CoroutineScope(Job() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(EpoxyRecyclerView(this).apply {
            setControllerAndBuildModels(controller)
            recyclerView = this
        })
        controller.data = ControllerData(
            data = (0 until 10).map { "Article $it" }
        )
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
            controller.data = controller.data.copy(showPlaceholdersCount = newArticlesCount)
            delay(100)
            recyclerView.smoothScrollToPosition(0)
            delay(5000)
            // Uncomment for workaround fix
//            controller.data = ControllerData()
            controller.data =
                controller.data.copy(showPlaceholdersCount = 0, data = (0 until newArticlesCount).map { "New article $it" } + controller.data.data)
        }
    }

    data class ControllerData(
        val showPlaceholdersCount: Int = 0,
        val data: List<String> = emptyList()
    )

    class TestController : EpoxyController() {

        var data = ControllerData()
            set(value) {
                field = value
                requestModelBuild()
            }

        override fun buildModels() {
            repeat(data.showPlaceholdersCount) { index ->
                mainActivityEpoxyPlaceholder {
                    id(UUID.randomUUID().toString())
                    text("Placeholder $index")
                }
            }
            data.data.forEach { text ->
                mainActivityEpoxyArticle {
                    id(text)
                    text(text)
                }
            }
        }
    }

    open class PlaceholderModel : EpoxyModelWithView<PlaceholderView>() {
        @EpoxyAttribute lateinit var text: String

        override fun buildView(parent: ViewGroup) = PlaceholderView(parent.context)

        override fun bind(view: PlaceholderView) {
            super.bind(view)
            view.text = text
        }
    }

    open class ArticleModel : EpoxyModelWithView<ArticleView>() {

        @EpoxyAttribute lateinit var text: String

        override fun buildView(parent: ViewGroup) = ArticleView(parent.context)

        override fun bind(view: ArticleView) {
            super.bind(view)
            view.text = text
        }
    }

    class PlaceholderView(context: Context) : TextView(context) {

        init {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 250)
            setBackgroundColor(Color.CYAN)
        }
    }

    class ArticleView(context: Context) : TextView(context) {

        init {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 250)
            setBackgroundColor(Color.GREEN)
        }
    }
}
