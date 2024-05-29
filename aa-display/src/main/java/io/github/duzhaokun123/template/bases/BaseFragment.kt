package io.github.duzhaokun123.template.bases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewbinding.ViewBinding
import net.matsudamper.viewbindingutil.ViewBindingUtil

abstract class BaseFragment<BaseBinding : ViewBinding>(private val baseBindingClass: Class<BaseBinding>) : Fragment() {
    val className by lazy { this::class.simpleName }

    lateinit var baseBinding: BaseBinding
        private set
    var isFirstCreate = true
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        isFirstCreate = savedInstanceState == null
        super.onCreate(savedInstanceState)
//        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        baseBinding = ViewBindingUtil.inflate(layoutInflater, baseBindingClass)
        findViews()
        initViews()
        initEvents()
        initData()
        return baseBinding.root
    }

    val baseActivity
        get() = activity as? BaseActivity<*>

    fun requireBaseActivity() =
        baseActivity
            ?: throw IllegalStateException("Fragment $this not attached to an baseActivity.")


    open fun findViews() {}
    open fun initViews() {}
    open fun initEvents() {}
    open fun initData() {}
}