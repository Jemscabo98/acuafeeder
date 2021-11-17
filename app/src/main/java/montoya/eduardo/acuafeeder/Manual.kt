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
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.MainAct
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.actualizar_enviarProgramacion
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.deviceCom
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.listaDevices
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.obtenerDevicesComandoBD
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.pool
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
        val btnActualizarManual: Button = root.findViewById(R.id.btnActualizarManual)
        val sensorFeeder: LinearLayout = root.findViewById(R.id.sensorFeeder)
        val sensorManual: LinearLayout = root.findViewById(R.id.sensorManual)


        if (listaDevices.isEmpty() || listaDevices.get(0).pool != pool) {
            listaDevices = ArrayList()

            GlobalData.obtenerDevicesBD(requireContext())
        }

        if (GlobalData.deviceCom.grPorSegundo == 0 || GlobalData.deviceCom.pool != GlobalData.pool){
            obtenerDevicesComandoBD(requireContext(), pool)
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

        btnEntrar.setOnClickListener {
            sensorManual.setBackgroundResource(R.drawable.circle_red)
            deviceCom.manualSW = 1
        }

        btnSalir.setOnClickListener {
            sensorManual.setBackgroundResource(R.drawable.circle_black)
            deviceCom.manualSW = 0
        }

        btnOn.setOnClickListener {
            sensorFeeder.setBackgroundResource(R.drawable.circle_red)
            deviceCom.modo = 1
        }

        btnOff.setOnClickListener {
            sensorFeeder.setBackgroundResource(R.drawable.circle_black)
            deviceCom.modo = 0
        }

        btnActualizarManual.setOnClickListener{
            if (GlobalData.alerta) {
                updateManual(requireContext())
                actualizar_enviarProgramacion(requireContext(), pool, listaDevices.size, 1)
                MainAct.verificarActualizacionBD(requireContext(), pool, listaDevices.size)
            }else{
                Toast.makeText(context, "En espera de comprobacion", Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }


}
