package com.benmohammad.mobiuselm.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.benmohammad.mobiuselm.R
import com.benmohammad.mobiuselm.SampleApp
import com.benmohammad.mobiuselm.data.GitHubService
import com.benmohammad.mobiuselm.domain.main.*
import com.spotify.mobius.First
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.android.AndroidLogger
import com.spotify.mobius.android.MobiusAndroid
import com.spotify.mobius.rx2.RxConnectables
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.eclipse.egit.github.core.Repository

class MainActivity : AppCompatActivity(), IMainView {

    @BindView(R.id.repos_list) lateinit var reposList: RecyclerView
    @BindView(R.id.repos_progress) lateinit var progressBar: ProgressBar
    @BindView(R.id.error_text) lateinit var errorText: TextView

    lateinit var api: GitHubService

    var rxEffectHandler = RxMobius.subtypeEffectHandler<MainEffect, MainEvent>()
        .add(LoadReposEffect::class.java, this::handleLoadRepos)
        .build()

    var loopFactory: MobiusLoop.Factory<MainModel, MainEvent, MainEffect> =
        RxMobius
            .loop(MainUpdate(), rxEffectHandler)
            .init{
                First.first(
                    MainModel(userName = api.getUserName()), setOf(LoadReposEffect)
                )
            }
            .logger(AndroidLogger.tag<MainModel, MainEvent, MainEffect>("may_app"))

    lateinit var controller: MobiusLoop.Controller<MainModel, MainEvent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        reposList.layoutManager = LinearLayoutManager(applicationContext)

        api = (application as SampleApp).service
        controller = MobiusAndroid.controller(
            loopFactory,
            MainModel(userName = api.getUserName())
        )
        controller.connect(RxConnectables.fromTransformer(this::connectViews))
    }

    fun handleLoadRepos(request: Observable<LoadReposEffect>): Observable<MainEvent> {
        return request.flatMap { effect ->
            api.getStarredRepos().map { repos ->
                ReposLoadedEvent(
                    repos
                )
            }.toObservable()
        }
    }

    override fun onResume() {
        super.onResume()
        controller.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.stop()
    }

    fun connectViews(models: Observable<MainModel>): Observable<MainEvent> {
        val disposable = models
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{render(it)}

        return Observable
            .just(IdleEvent as MainEvent)
            .doOnDispose(disposable::dispose)
    }

    fun render(state: MainModel) {
        state.apply {
            setTitle(
                state.userName + "'s starred repos"
            )
            if(isLoading) {
                if(reposList.isEmpty()) {
                    showProgress()
                }
            } else {
                hideProgress()
                if(reposList.isEmpty()) {
                    setErrorText("User has no starred repos")
                    showErrorText()
                }
            }
            setRepos(reposList)
        }
    }

    override fun setTitle(s: String) {
        supportActionBar!!.setTitle(s)
    }

    override fun showProgress() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progressBar.visibility = View.GONE
    }

    override fun setErrorText(s: String) {
        errorText.text = s
    }

    override fun showErrorText() {
        errorText.visibility = View.VISIBLE
    }

    override fun setRepos(repos: List<Repository>) {
        reposList.adapter = ReposAdapter(repos, layoutInflater)
    }

    private inner class ReposAdapter(private val repos: List<Repository>, private val inflater: LayoutInflater):
            RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return RepoViewHolder(inflater.inflate(R.layout.repo_list_item, parent, false))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as RepoViewHolder).bind(repos[position])
        }

        override fun getItemCount(): Int {
            return repos.size
        }

        internal inner class RepoViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            var repoName: TextView
            var repoStarCount: TextView

            init {
                repoName = itemView.findViewById(R.id.repo_name) as TextView
                repoStarCount = itemView.findViewById(R.id.repo_stars_count) as TextView
            }

            fun bind(repository: Repository) {
                repoName.text = repository.name
                repoStarCount.text = "Watchers: " + repository.watchers
            }
        }
    }
}