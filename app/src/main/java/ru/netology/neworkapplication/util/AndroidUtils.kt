package ru.netology.neworkapplication.util

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import ru.netology.neworkapplication.R
import ru.netology.neworkapplication.databinding.FragmentNewEventBinding
import ru.netology.neworkapplication.databinding.FragmentNewJobBinding
import ru.netology.neworkapplication.databinding.FragmentNewPostBinding
import ru.netology.neworkapplication.viewmodel.EventViewModel
import ru.netology.neworkapplication.viewmodel.JobViewModel
import ru.netology.neworkapplication.viewmodel.PostViewModel

object AndroidUtils {
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun setupEventMenu(
        fragment: Fragment,
        lifecycleOwner: LifecycleOwner,
        viewModel: EventViewModel
    ) {
        fragment.requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_new_post, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.save -> {
                        fragment.view?.let { view ->
                            val binding = FragmentNewEventBinding.bind(view)
                            viewModel.changeDatetime(binding.datetime.text.toString())
                            viewModel.changeType(binding.type.text.toString())
                            viewModel.changeContent(binding.edit.text.toString())

                            if (binding.link.text.toString().isEmpty()) {
                                viewModel.changeLink(null)
                            } else {
                                viewModel.changeLink(binding.link.text.toString())
                            }

                            viewModel.saveEvent()
                            hideKeyboard(view)
                        }
                        fragment.parentFragmentManager.popBackStack()
                        true
                    }

                    else -> false
                }
            }
        }, lifecycleOwner)
    }

    fun setupJobMenu(fragment: Fragment, lifecycleOwner: LifecycleOwner, viewModel: JobViewModel) {
        fragment.requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_new_post, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.save -> {
                        fragment.view?.let { view ->
                            val binding = FragmentNewJobBinding.bind(view)
                            viewModel.changeName(binding.name.text.toString())
                            viewModel.changePosition(binding.position.text.toString())
                            viewModel.changeStart(binding.start.text.toString())

                            if (binding.finish.text.toString().isEmpty()) {
                                viewModel.changeFinish(null)
                            } else {
                                viewModel.changeFinish(binding.finish.text.toString())
                            }

                            if (binding.link.text.toString().isEmpty()) {
                                viewModel.changeLink(null)
                            } else {
                                viewModel.changeLink(binding.link.text.toString())
                            }

                            viewModel.save()
                            hideKeyboard(view)
                        }
                        fragment.parentFragmentManager.popBackStack()
                        true
                    }

                    else -> false
                }
            }
        }, lifecycleOwner)

    }

    fun setupPostMenu(
        fragment: Fragment,
        lifecycleOwner: LifecycleOwner,
        viewModel: PostViewModel
    ) {
        fragment.requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_new_post, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.save -> {
                        fragment.view?.let { view ->
                            val binding = FragmentNewPostBinding.bind(view)
                            viewModel.changeContent(binding.edit.text.toString())
                            viewModel.save()
                            hideKeyboard(view)
                        }
                        fragment.parentFragmentManager.popBackStack()
                        true
                    }

                    else -> false
                }
            }
        }, lifecycleOwner)
    }
}