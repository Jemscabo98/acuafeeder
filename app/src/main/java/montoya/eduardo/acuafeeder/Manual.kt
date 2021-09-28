package montoya.eduardo.acuafeeder

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.fragment.app.Fragment


class Manual : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root: View = inflater.inflate(R.layout.fragment_manual, container, false)

        val btnOn: Button = root.findViewById(R.id.btnOn)
        val btnOff: Button = root.findViewById(R.id.btnOff)
        val btnEntrar: Button = root.findViewById(R.id.btnEntrar)
        val btnSalir: Button = root.findViewById(R.id.btnSalir)
        val sensorFeeder: LinearLayout = root.findViewById(R.id.sensorFeeder)
        val sensorManual: LinearLayout = root.findViewById(R.id.sensorManual)

        btnOn.setOnClickListener {
            sensorFeeder.setBackgroundResource(R.drawable.circle_red)
        }

        btnOff.setOnClickListener {
            sensorFeeder.setBackgroundResource(R.drawable.circle_black)
        }

        btnEntrar.setOnClickListener {
            sensorManual.setBackgroundResource(R.drawable.circle_red)
        }

        btnSalir.setOnClickListener {
            sensorManual.setBackgroundResource(R.drawable.circle_black)
        }

        return root
    }

}
