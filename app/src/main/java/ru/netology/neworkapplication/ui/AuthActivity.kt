package ru.netology.neworkapplication.ui


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.neworkapplication.R
import ru.netology.neworkapplication.auth.AppAuth
import ru.netology.neworkapplication.databinding.ActivityAuthBinding
import ru.netology.neworkapplication.dto.RequestLogin
import ru.netology.neworkapplication.viewmodel.AuthViewModel
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {
    @Inject
    lateinit var appAuth: AppAuth
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authState = appAuth.authStateFlow.value
        if (authState.token != null) {
            navigateToFeed()
            return
        }
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            signIn.setOnClickListener {
                val login = login.text.toString().trim()
                val password = password.text.toString().trim()
                if (login.isNotBlank() && password.isNotBlank()) {
                    val loginRequest = RequestLogin(login, password)
                    viewModel.login(loginRequest)
                } else {
                    Toast.makeText(
                        this@AuthActivity,
                        getString(R.string.fill_fields),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            signUp.setOnClickListener {
                val intent = Intent(this@AuthActivity, SignUpActivity::class.java)
                startActivity(intent)
            }
        }

        viewModel.loginResult.observe(this) { isSuccess ->
            if (isSuccess) {
                val intent = Intent(this@AuthActivity, FeedActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loginError.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }


    }

    private fun navigateToFeed() {
        val intent = Intent(this@AuthActivity, FeedActivity::class.java)
        startActivity(intent)
        finish()
    }
}