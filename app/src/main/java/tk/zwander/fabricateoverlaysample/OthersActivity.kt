package tk.zwander.fabricateoverlaysample

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
    }
}