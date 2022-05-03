package com.max.natifechat.presentation

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.max.natifechat.R
import com.max.natifechat.log

abstract class BaseFragment : Fragment() {

    protected fun changeFragment(fragment: Fragment, addToBackStack: Boolean) {
        if (addToBackStack) {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, fragment)
                .addToBackStack(null)
                .commit()
        } else {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, fragment)
                .commit()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        log("onDestroy ${this.javaClass.simpleName}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        log("onDestroyView ${this.javaClass.simpleName}")
    }

    protected fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}