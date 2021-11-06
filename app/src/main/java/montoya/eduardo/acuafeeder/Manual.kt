package montoya.eduardo.acuafeeder

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.volley.RequestQueue
import montoya.eduardo.acuafeeder.data_class.GlobalData
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.obtenerDevicesComandoBD
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.updateComida
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.updateManual


class Manual : Fragment() {
    private lateinit var queue: RequestQueue
    private val handler = Handler(Looper.getMainLooper())

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

        if (GlobalData.deviceCom.grPorSegundo == 0 || GlobalData.deviceCom.pool != GlobalData.pool){
            obtenerDevicesComandoBD(requireContext())
        }else{
            if (GlobalData.deviceCom.modo == 0)
                sensorFeeder.setBackgroundResource(R.drawable.circle_black)
            else
                sensorFeeder.setBackgroundResource(R.drawable.circle_red)

            if (GlobalData.deviceCom.manualSW == 0)
                sensorManual.setBackgroundResource(R.drawable.circle_black)
            else
                sensorManual.setBackgroundResource(R.drawable.circle_red)
        }

        btnOn.setOnClickListener {
            sensorFeeder.setBackgroundResource(R.drawable.circle_red)
            GlobalData.deviceCom.modo = 1
            updateManual(requireContext())
        }

        btnOff.setOnClickListener {
            sensorFeeder.setBackgroundResource(R.drawable.circle_black)
            GlobalData.deviceCom.modo = 0
            updateManual(requireContext())
        }

        btnEntrar.setOnClickListener {
            sensorManual.setBackgroundResource(R.drawable.circle_red)
            GlobalData.deviceCom.manualSW = 1
            updateManual(requireContext())
        }

        btnSalir.setOnClickListener {
            sensorManual.setBackgroundResource(R.drawable.circle_black)
            GlobalData.deviceCom.manualSW = 0
            updateManual(requireContext())
        }

        return root
    }


}
