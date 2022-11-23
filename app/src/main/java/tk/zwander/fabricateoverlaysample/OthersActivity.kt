package tk.zwander.fabricateoverlaysample

import android.os.Bundle
import android.util.TypedValue
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import tk.zwander.fabricateoverlay.FabricatedOverlay
import tk.zwander.fabricateoverlay.FabricatedOverlayEntry
import tk.zwander.fabricateoverlay.OverlayAPI
import tk.zwander.fabricateoverlaysample.databinding.ActivityOthersBinding

class OthersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOthersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOthersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.saturationValue.setText(Prefs.saturationValue)
        binding.saveSaturation.setOnClickListener {
            Prefs.saturationValue = binding.saturationValue.text.toString()

            val saturation = Prefs.saturationValue
            Runtime.getRuntime().exec("su -c service call SurfaceFlinger 1022 f $saturation")
                .waitFor()
            Runtime.getRuntime().exec("su -c setprop persist.sys.sf.color_saturation $saturation")
                .waitFor()
            Toast.makeText(this, "Saturation Set Successfully", Toast.LENGTH_LONG).show()
        }

        val sourcePackage = OverlayAPI.servicePackage ?: "com.android.shell"

        binding.saveOverlay.setOnClickListener {
            val packageNameValue = binding.packageName.text.toString()
            val resourceName = binding.resourceName.text.toString()
            val isDimen = binding.dimen.isChecked
            val isBool = binding.bool.isChecked
            val isColor = binding.color.isChecked
            val isInteger = binding.integer.isChecked
            val isDimenDp = binding.dp.isChecked
            val isDimenPx = binding.px.isChecked
            val booleanValue = binding.boolValue.isChecked
            val resourceValue = binding.resourceValue.text.toString()
            val overlayName = binding.overlayName.text.toString()

            val resourceTypeString = when {
                isDimen -> "dimen"
                isBool -> "bool"
                isColor -> "color"
                isInteger -> "integer"
                else -> ""
            }

            val resourceType = when (resourceTypeString) {
                "dimen" -> TypedValue.TYPE_DIMENSION
                "bool" -> TypedValue.TYPE_INT_BOOLEAN
                "color" -> TypedValue.TYPE_INT_COLOR_ARGB8
                "integer" -> TypedValue.TYPE_INT_DEC
                else -> TypedValue.TYPE_NULL
            }

            val dimenUnit =
                if (isDimenDp) TypedValue.COMPLEX_UNIT_DIP else if (isDimenPx) TypedValue.COMPLEX_UNIT_PX else TypedValue.TYPE_NULL

            var resourceValueFinal = 0
            if (isDimen) {
                resourceValueFinal = getParsedDimen(dimenUnit, resourceValue)
            }
            if (isBool) {
                resourceValueFinal = if (booleanValue) 1 else 0
            }
            if (isColor) {
                resourceValueFinal = getParsedColor(resourceValue)
            }
            if (isInteger) {
                resourceValueFinal = resourceValue.toInt()
            }


            val listOfOverlays = listOf(
                FabricatedOverlay(
                    "$packageName.$packageNameValue.$overlayName",
                    packageNameValue,
                    sourcePackage
                ).apply {
                    listOf(
                        FabricatedOverlayEntry(
                            "$packageNameValue:$resourceTypeString/$resourceName",
                            resourceType,
                            resourceValueFinal
                        ),
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
        }
    }

    fun getParsedColor(value: String): Int {
        return Integer.parseUnsignedInt(value.substring(2), 16)
    }

    fun getParsedDimen(type: Int, value: Int): Int {
        return TypedValue::class.java
            .getMethod("createComplexDimension", Int::class.java, Int::class.java)
            .invoke(null, value, type) as Int
    }

    fun getParsedDimen(type: Int, value: String): Int {
        return getParsedDimen(type, value.toInt())
    }
}