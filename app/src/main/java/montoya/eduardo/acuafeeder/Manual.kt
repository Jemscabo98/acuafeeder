package montoya.eduardo.acuafeeder

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import montoya.eduardo.acuafeeder.data_class.GlobalData
import montoya.eduardo.acuafeeder.data_class.deviceCommand
import org.json.JSONException
import org.json.JSONObject


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
            obtenerDevicesComandoBD()
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
            updateDevicesComandoBD()
        }

        btnOff.setOnClickListener {
            sensorFeeder.setBackgroundResource(R.drawable.circle_black)
            GlobalData.deviceCom.modo = 0
            updateDevicesComandoBD()
        }

        btnEntrar.setOnClickListener {
            sensorManual.setBackgroundResource(R.drawable.circle_red)
            GlobalData.deviceCom.manualSW = 1
            updateDevicesComandoBD()
        }

        btnSalir.setOnClickListener {
            sensorManual.setBackgroundResource(R.drawable.circle_black)
            GlobalData.deviceCom.manualSW = 0
            updateDevicesComandoBD()
        }

        return root
    }

    fun obtenerDevicesComandoBD(){
        val URLAux = GlobalData.URL + "buscar_deviceCommandoXalberca.php?piscina=" + GlobalData.pool
        GlobalData.listaDevices.clear()

        val jsonArrayRequest: JsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            URLAux,
            null,

            {
                var jsonObject: JSONObject? = null
                for (i in 0 until it.length()) {
                    try {
                        jsonObject = it.getJSONObject(i)
                        val deviceCom: deviceCommand = deviceCommand(GlobalData.pool)
                        deviceCom.dispositivosXpiscina = jsonObject.getInt("dispositivosXpiscina")
                        deviceCom.modo = jsonObject.getInt("Modo")
                        deviceCom.manualSW = jsonObject.getInt("ManualSw")
                        deviceCom.alimentoTotal = jsonObject.getInt("AlimentoTotal")
                        deviceCom.grPorSegundo = jsonObject.getInt("grPorSegundo")
                        deviceCom.enviarProgramacion = jsonObject.getInt("enviarProgramacion")

                        GlobalData.deviceCom = deviceCom

                    } catch (error: JSONException) {
                        Toast.makeText(context, "Problemas de conexión", Toast.LENGTH_SHORT).show()
                    }
                }
            },

            {
                Toast.makeText(context,
                    "No se encuentran datos en esta piscina",
                    Toast.LENGTH_LONG).show()
            })

        queue = Volley.newRequestQueue(context)
        jsonArrayRequest.setShouldCache(false)
        queue.add(jsonArrayRequest)
    }

    fun updateDevicesComandoBD(){
        val URLAux = GlobalData.URL + "actualizar_manual.php"
        val params = HashMap<String, String>()
        params["Modo"] = GlobalData.deviceCom.modo.toString()
        params["ManualSw"] = GlobalData.deviceCom.manualSW.toString()
        params["piscina"] = GlobalData.pool.toString()
        params["idUser"] = GlobalData.idUser.toString()

        val request: StringRequest =
            object : StringRequest(Request.Method.POST, URLAux, {
                //Toast.makeText(context, "Operacion Exitosa", Toast.LENGTH_SHORT).show()
            }, { error: VolleyError ->
                println("Error $error.message")
                Toast.makeText(context, "Error de Conexion", Toast.LENGTH_SHORT).show()
            }) {
                override fun getParams(): Map<String, String> {
                    return params
                }
            }

        request.retryPolicy =
            DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, 1f)

        queue = Volley.newRequestQueue(context)
        request.setShouldCache(false)
        queue.add(request)
    }

}
