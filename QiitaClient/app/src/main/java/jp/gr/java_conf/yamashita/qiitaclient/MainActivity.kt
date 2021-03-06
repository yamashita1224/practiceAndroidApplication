package jp.gr.java_conf.yamashita.qiitaclient

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.ProgressBar
import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import jp.gr.java_conf.yamashita.qiitaclient.client.ArticleClient
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject
import kotterknife.bindView
import org.jetbrains.anko.setContentView

class MainActivity : RxAppCompatActivity() {
    @Inject
    lateinit var articleClient: ArticleClient

    /*
    val listView by bindView<ListView>(R.id.list_view)
    val progressBar by bindView<ProgressBar>(R.id.progress_bar)
    val searchButton by bindView<Button>(R.id.search_button)
    val queryEditText by bindView<EditText>(R.id.query_edit_text)*/
    private val ui: MainActivityUI by lazy {
        MainActivityUI().apply { setContentView(this@MainActivity) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as QiitaClientApp).component.inject(this)
        // setContentView(R.layout.activity_main)

        /*
        val listView: ListView = findViewById(R.id.list_view)
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        val queryEditText = findViewById<EditText>(R.id.query_edit_text)
        val searchButton = findViewById<Button>(R.id.search_button)*/


        val listAdapter = ArticleListAdapter(applicationContext)
        // listView.adapter = listAdapter
        ui.listView.adapter = listAdapter
        ui.listView.setOnItemClickListener{ adapterView, view, position, id ->
            val intent = ArticleActivity.intent(this, listAdapter.articles[position])
            startActivity(intent)
        }

        /*
        val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()
        val retrofit = Retrofit.Builder()
                .baseUrl("https://qiita.com")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
        val articleClient = retrofit.create(ArticleClient::class.java)*/

        ui.searchButton.setOnClickListener {
            ui.progressBar.visibility = View.VISIBLE

            articleClient.search(ui.queryEditText.text.toString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doAfterTerminate{
                        ui.progressBar.visibility = View.GONE
                    }
                    .bindToLifecycle(this)
                    .subscribe({
                        ui.queryEditText.text.clear()
                        listAdapter.articles = it
                        listAdapter.notifyDataSetChanged()
                    }, {
                        toast("エラー： $it")
                    })
        }
    }


    /*private fun dummyArticle(title: String, userName: String): Article =
            Article(id = "",
                    title = title,
                    url = "https://kotlinlang.org/",
                    user = User(id = "", name = userName, profileImageUrl = ""))*/
}
