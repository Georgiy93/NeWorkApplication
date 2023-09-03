package ru.netology.neworkapplication.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.neworkapplication.R
import ru.netology.neworkapplication.auth.AppAuth
import ru.netology.neworkapplication.ui.event.EventFragment
import ru.netology.neworkapplication.ui.job.JobFragment
import ru.netology.neworkapplication.ui.wall.WallFeedFragment
import javax.inject.Inject

@AndroidEntryPoint
class FeedActivity : AppCompatActivity() {
    @Inject
    lateinit var auth: AppAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, FeedFragment())
                .commitNow()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.signout -> {
                auth.clearAuth()
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
                finish()
                true
            }

            R.id.wall -> {
                supportFragmentManager.commit {
                    replace(R.id.container, WallFeedFragment())
                    addToBackStack(null)
                }
                true
            }

            R.id.job -> {
                supportFragmentManager.commit {
                    replace(R.id.container, JobFragment())
                    addToBackStack(null)
                }
                true
            }

            R.id.event -> {
                supportFragmentManager.commit {
                    replace(R.id.container, EventFragment())
                    addToBackStack(null)
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}