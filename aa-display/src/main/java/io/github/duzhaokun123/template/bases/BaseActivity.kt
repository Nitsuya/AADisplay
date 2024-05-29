package io.github.duzhaokun123.template.bases

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import io.github.nitsuya.aa.display.R
import io.github.nitsuya.aa.display.databinding.ActivityBaseRoot2Binding
import io.github.nitsuya.template.bases.TipUtil
import io.github.nitsuya.template.bases.maxSystemBarsDisplayCutout
import net.matsudamper.viewbindingutil.ViewBindingUtil

abstract class BaseActivity<BaseBinding : ViewBinding>(
    private val baseBindingClass: Class<BaseBinding>, vararg val configs: Config, @StyleRes val themeId: Int = R.style.Theme_AADisplay
) : AppCompatActivity() {
    enum class Config {
        NO_TOOL_BAR,
        LAYOUT_NO_TOOL_BAR,
        TRANSPARENT_TOOL_BAR,
        NO_BACK,
        LAYOUT_MATCH_HORI,
    }

    val className by lazy { this::class.simpleName }
    val startIntent by lazy { intent }

    lateinit var rootBinding: ActivityBaseRoot2Binding
    lateinit var baseBinding: BaseBinding
        private set
    var isFirstCreate = true

    private val windowInsetsCompatModel by viewModels<WindowInsetsCompatModel>()

    class WindowInsetsCompatModel : ViewModel() {
        val windowInsetsCompat = MutableLiveData<WindowInsetsCompat>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        isFirstCreate = savedInstanceState == null
        setTheme(themeId)
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS

        rootBinding = ViewBindingUtil.inflate(layoutInflater)
        setContentView(rootBinding.root)
        if (Config.NO_TOOL_BAR in configs) rootBinding.rootTb.visibility = View.GONE
        if (Config.LAYOUT_NO_TOOL_BAR in configs)
            rootBinding.rootFl.updateLayoutParams<RelativeLayout.LayoutParams> {
                removeRule(RelativeLayout.BELOW)
            }
        if (Config.TRANSPARENT_TOOL_BAR in configs) {
            rootBinding.rootAbl.outlineProvider = null
            rootBinding.rootAbl.background = null
        }
        ViewCompat.setOnApplyWindowInsetsListener(rootBinding.root) { _, insets ->
            windowInsetsCompatModel.windowInsetsCompat.value = insets
            insets
        }

        baseBinding = ViewBindingUtil.inflate(layoutInflater, rootBinding.rootFl, true, baseBindingClass)

        findViews()
        setSupportActionBar(initActionBar())
        if (Config.NO_BACK !in configs) supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)
        }
        initViews()
        initEvents()
        initData()

        TipUtil.registerCoordinatorLayout(this, registerCoordinatorLayout())
        windowInsetsCompatModel.windowInsetsCompat.observe(this, ::onApplyWindowInsetsCompat)
    }

    override fun onDestroy() {
        super.onDestroy()
        TipUtil.unregisterCoordinatorLayout(this)
    }

    override fun setTitle(title: CharSequence?) {
        supportActionBar?.title = title
    }

    fun setSubtitle(subtitle: CharSequence?) {
        supportActionBar?.subtitle = subtitle
    }

    fun setSubtitle(@StringRes subtitleId: Int) = setSubtitle(getText(subtitleId))

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else
            super.onOptionsItemSelected(item)
    }

    @CallSuper
    open fun onApplyWindowInsetsCompat(insets: WindowInsetsCompat) {
        with(insets.maxSystemBarsDisplayCutout) {
            if (Config.LAYOUT_MATCH_HORI !in configs) {
                rootBinding.rootAbl.updatePadding(left = left, right = right)
                rootBinding.rootFl.updatePadding(left = left, right = right)
            }
            if (Config.NO_TOOL_BAR !in configs)
                rootBinding.rootAbl.updatePadding(top = top)
        }
    }

    open fun registerCoordinatorLayout(): CoordinatorLayout? = rootBinding.rootCl

    open fun findViews() {}
    open fun initActionBar() =
        if (Config.NO_TOOL_BAR in configs) null else rootBinding.rootTb

    open fun initViews() {}
    open fun initEvents() {}
    open fun initData() {}
}