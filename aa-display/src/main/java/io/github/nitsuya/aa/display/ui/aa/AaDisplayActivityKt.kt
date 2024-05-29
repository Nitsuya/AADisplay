package io.github.nitsuya.aa.display.ui.aa

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.add
import androidx.fragment.app.commit
import io.github.nitsuya.aa.display.CoreApi
import io.github.nitsuya.aa.display.R
import io.github.nitsuya.aa.display.ui.aa.fragment.AaMainFragment
import io.github.nitsuya.aa.display.ui.aa.fragment.AaRecentTaskFragment

object AaDisplayActivityKt {

    fun pressKey(action: Int){
        CoreApi.pressKey(action)
    }

    fun toggleDisplayPower(){
        CoreApi.toggleDisplayPower()
    }

    fun toast(msg: String){
        CoreApi.toast(msg)
    }

    fun showMain(fragmentManager: FragmentManager){
        fragmentManager.commit {
            setReorderingAllowed(true)
            add<AaMainFragment>(R.id.fragment_container_view)
        }
    }

    fun showRecentTask(fragmentManager: FragmentManager){
        val fragment = fragmentManager.findFragmentByTag("RecentTask")
        if(fragment == null){
            fragmentManager.commit {
                setReorderingAllowed(true)
                add<AaRecentTaskFragment>(R.id.fragment_container_view, "RecentTask")
            }
        } else {
            hideRecentTask(fragmentManager)
        }
    }

    fun hideRecentTask(fragmentManager: FragmentManager){
        val fragment = fragmentManager.findFragmentByTag("RecentTask") ?: return
        fragmentManager.commit {
            setReorderingAllowed(true)
            remove(fragment)
        }
    }

}