package tk.zwander.fabricateoverlaysample

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.lsposed.hiddenapibypass.HiddenApiBypass
import tk.zwander.fabricateoverlay.FabricatedOverlay
import tk.zwander.fabricateoverlay.FabricatedOverlayEntry
import tk.zwander.fabricateoverlay.OverlayAPI
import tk.zwander.fabricateoverlay.ShizukuUtils
import tk.zwander.fabricateoverlaysample.ui.pages.AppListPage
import tk.zwander.fabricateoverlaysample.ui.pages.CurrentOverlayEntriesListPage
import tk.zwander.fabricateoverlaysample.ui.pages.HomePage

@SuppressLint("PrivateApi")
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        HiddenApiBypass.setHiddenApiExemptions("L")

        if (!ShizukuUtils.shizukuAvailable) {
            showShizukuDialog()
            return
        }

        if (ShizukuUtils.hasShizukuPermission(this)) {
            init()
        } else {
            ShizukuUtils.requestShizukuPermission(this) { granted ->
                if (granted) {
                    init()
                } else {
                    showShizukuDialog()
                }
            }
        }
    }

    private fun showShizukuDialog() {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.shizuku_not_set_up)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                finish()
            }
            .setCancelable(false)
            .create()
            .apply {
                setOnShowListener {
                    findViewById<TextView>(
                        Class.forName("com.android.internal.R\$id").getField("message").getInt(null)
                    )
                        ?.movementMethod = LinkMovementMethod()
                }
            }
            .show()
    }

    private fun init() {
        setContent {
            MaterialTheme(
                colors = darkColors()
            ) {
                Surface {
                    var appInfoArg by remember {
                        mutableStateOf<ApplicationInfo?>(null)
                    }
                    val navController = rememberNavController()
                    val activity = LocalContext.current as Activity

                    NavHost(navController = navController, startDestination = "main") {
                        composable("main") {
                            activity.setTitle(R.string.overlays)

                            HomePage(navController)
                        }
                        composable("app_list") {
                            activity.setTitle(R.string.apps)

                            AppListPage(navController)
                        }
                        composable(
                            route = "list_overlays"
                        ) {
                            navController.previousBackStackEntry?.arguments?.getParcelable<ApplicationInfo>(
                                "appInfo"
                            )?.let {
                                appInfoArg = it
                            }

                            activity.title = appInfoArg?.loadLabel(activity.packageManager)

                            CurrentOverlayEntriesListPage(
                                navController,
                                appInfoArg!!
                            )
                        }
                    }
                }
            }
        }

        val launcherPackage = "com.android.launcher3"
        val systemUIPackage = "com.android.systemui"
        val sourcePackage = OverlayAPI.servicePackage ?: "com.android.shell"

        val listOfOverlays = listOf(
            FabricatedOverlay(
                launcherPackage.overlay(),
                launcherPackage,
                sourcePackage
            ).apply {
                listOf(
                    FabricatedOverlayEntry(
                        "$launcherPackage:dimen/all_apps_search_bar_bottom_padding",
                        TypedValue.TYPE_DIMENSION,
                        getParsedDimen(TypedValue.COMPLEX_UNIT_DIP, 50)
                    )
                ).forEach { overlay ->
                    entries[overlay.resourceName] = overlay
                }
            },
            FabricatedOverlay(
                systemUIPackage.overlay(),
                systemUIPackage,
                sourcePackage
            ).apply {
                listOf(
                    FabricatedOverlayEntry(
                        "$systemUIPackage:dimen/keyguard_large_clock_top_margin",
                        TypedValue.TYPE_DIMENSION,
                        getParsedDimen(TypedValue.COMPLEX_UNIT_DIP, -300)
                    ),
                    FabricatedOverlayEntry(
                        "$systemUIPackage:dimen/qs_top_brightness_margin_bottom",
                        TypedValue.TYPE_DIMENSION,
                        getParsedDimen(TypedValue.COMPLEX_UNIT_DIP, 20)
                    ),
                    FabricatedOverlayEntry(
                        "$systemUIPackage:dimen/biometric_dialog_border_padding",
                        TypedValue.TYPE_DIMENSION,
                        getParsedDimen(TypedValue.COMPLEX_UNIT_DIP, 0)
                    ),
                    FabricatedOverlayEntry(
                        "$systemUIPackage:dimen/biometric_dialog_corner_size",
                        TypedValue.TYPE_DIMENSION,
                        getParsedDimen(TypedValue.COMPLEX_UNIT_DIP, 16)
                    )
                ).forEach { overlay ->
                    entries[overlay.resourceName] = overlay
                }
            }
        )

        OverlayAPI.getInstance(this) { api ->
            listOfOverlays.forEach { overlay ->
                api.registerFabricatedOverlay(overlay)
                api.setEnabled(
                    FabricatedOverlay.generateOverlayIdentifier(
                        overlay.overlayName,
                        overlay.sourcePackage
                    ), true, 0
                )
            }
        }

        Runtime.getRuntime().exec("su -c service call SurfaceFlinger 1022 f 1.20").waitFor()
        Runtime.getRuntime().exec("su -c setprop persist.sys.sf.color_saturation 1.20").waitFor()
    }

    fun String.overlay(): String {
        return "$packageName.$this.overlay"
    }

    fun getParsedDimen(type: Int, value: Int): Int {
        return TypedValue::class.java
            .getMethod("createComplexDimension", Int::class.java, Int::class.java)
            .invoke(null, value, type) as Int
    }
}