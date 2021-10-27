package montoya.eduardo.acuafeeder.ui.dashboard

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.volley.*
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import montoya.eduardo.acuafeeder.EditCommand
import montoya.eduardo.acuafeeder.MainActivity
import montoya.eduardo.acuafeeder.R
import montoya.eduardo.acuafeeder.data_class.*
import org.json.JSONException
import org.json.JSONObject


class DashboardFragment : Fragment() {
    //val URL = "http://practicaspro.webhop.me:8080/acuafeeder/"
    val URL = "https://bytefruit.com/practicas-acuafeeder/php/"
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var queue: RequestQueue
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var selectComida: Spinner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val btnAdd: Button = root.findViewById(R.id.btnAdd)
        val btnMinus: Button = root.findViewById(R.id.btnMinus)
        val btnAct: Button = root.findViewById(R.id.btnActualizarComida)
        val listview: ListView = root.findViewById(R.id.ScrollView)
        val txtAlimento: TextView = root.findViewById(R.id.txtAlimento)
        selectComida = root.findViewById(R.id.selectComida)

        dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
            var adaptador: AdapterComando = AdapterComando(context, GlobalData.listaComandos)
            listview.adapter = adaptador

            if (GlobalData.listaDevices.isEmpty() || GlobalData.listaDevices.get(0).pool != GlobalData.pool){
                GlobalData.listaDevices = ArrayList()
                obtenerDevicesBD()
            }

            if (GlobalData.deviceCom.grPorSegundo == 0 || GlobalData.deviceCom.pool != GlobalData.pool){
                obtenerDevicesComandoBD()
            }


            if (GlobalData.listaComida.isEmpty() || GlobalData.listaComida.get(0).pool != GlobalData.pool){
                GlobalData.listaComida = ArrayList()
                obtenerComidaBD()

                handler.postDelayed(Runnable {
                    fillSpinner(selectComida, context)

                    for ((contAux, x) in GlobalData.listaComida.withIndex()){
                        txtAlimento.text = GlobalData.deviceCom.alimentoTotal.toString()
                        if (GlobalData.deviceCom.grPorSegundo == x.allimentoGrSeg)
                            selectComida.setSelection(contAux)
                    }
                }, 350)
            }else{
                fillSpinner(selectComida, context)

                for ((contAux, x) in GlobalData.listaComida.withIndex()){
                    txtAlimento.text = GlobalData.deviceCom.alimentoTotal.toString()
                    if (GlobalData.deviceCom.grPorSegundo == x.allimentoGrSeg)
                        selectComida.setSelection(contAux)
                }
            }

            btnAdd.setOnClickListener {
                val com: Command = Command(GlobalData.pool)
                if (AddList(com)) {
                    for (x in GlobalData.listaDevices){
                        AddCommand(x.idDevices, GlobalData.listaComandos.size.toString(), com)
                    }
                }
                adaptador = AdapterComando(context, GlobalData.listaComandos)
                listview.adapter = adaptador
            }

            btnMinus.setOnClickListener {
                val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                builder.setCancelable(true)
                builder.setTitle("Confirmación")
                builder.setMessage("¿Seguro que desea eliminar el ultimo comando?")
                builder.setPositiveButton("Confirmar",
                    DialogInterface.OnClickListener { dialog, which ->
                        for (x in GlobalData.listaDevices)
                            DeleteCommand(x.idDevices, GlobalData.listaComandos.size.toString())

                        GlobalData.listaComandos.removeLast()
                        adaptador = AdapterComando(context, GlobalData.listaComandos)
                        listview.adapter = adaptador
                    })
                builder.setNegativeButton("Cancelar",
                    DialogInterface.OnClickListener { dialog, which -> })

                val dialog: AlertDialog = builder.create()
                dialog.show()
            }

            btnAct.setOnClickListener {
                GlobalData.deviceCom.alimentoTotal = txtAlimento.text.toString().toInt()
                GlobalData.deviceCom.grPorSegundo = GlobalData.selectFood.allimentoGrSeg
                GlobalData.deviceCom.enviarProgramacion = 1
                updateDevicesComandoBD(GlobalData.deviceCom)
            }



            selectComida.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    GlobalData.selectFood = GlobalData.selectFood
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    for (x in GlobalData.listaComida){
                        if(x.idFood == selectComida.selectedItem.toString()){
                            GlobalData.selectFood = x
                        }
                    }
                }

            }


        })
        return root
    }

    fun AddList(com: Command): Boolean{
        var comAUX = GlobalData.listaComandos.get(GlobalData.listaComandos.size - 1)
        if (comAUX.horario_final_hr > 22){
            Toast.makeText(context, "Limite Alcanzado", Toast.LENGTH_SHORT).show()
            return false
        }

        else{
            com.horario_inicial_hr = comAUX.horario_final_hr+1
            com.horario_final_hr = comAUX.horario_final_hr+1
            com.horario_final_min = 59
            com.porcentajeAlimento = 20
            com.s = 1

            GlobalData.listaComandos.add(com)
        }
        return  true
    }

    fun AddCommand(idDevice: String, ID: String, com: Command){
        val URLAux = GlobalData.URL + "insertar_comando.php"
        val params = HashMap<String, String>()
        params["idDevice"] = idDevice
        params["ID"] = ID
        params["s"] = "1"
        params["etapa"] = "0"
        params["horario_inicial_hr"] = com.horario_inicial_hr.toString()
        params["horario_inicial_min"] = com.horario_inicial_min.toString()
        params["horario_final_hr"] = com.horario_final_hr.toString()
        params["horario_final_min"] = com.horario_final_min.toString()
        params["porcentajeAlimento"] = com.porcentajeAlimento.toString()

        val request: StringRequest =
            object : StringRequest(Request.Method.POST, URLAux, {
                Toast.makeText(context, "Operacion Exitosa", Toast.LENGTH_SHORT).show()
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

    fun DeleteCommand(idDevice: String, ID: String){
        val URLAux = GlobalData.URL + "eliminar_comando.php"
        val params = HashMap<String, String>()
        params["idDevice"] = idDevice
        params["ID"] = ID

        val request: StringRequest =
            object : StringRequest(Request.Method.POST, URLAux, {
                Toast.makeText(context, "Se elimino el comando con exito", Toast.LENGTH_SHORT).show()
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

    fun obtenerDevicesBD(){
        val URLAux = GlobalData.URL + "buscar_dispositivos.php?devices_piscina=" + GlobalData.pool
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
                        val device: Devices = Devices(jsonObject.getString("devices_etiqueta"), GlobalData.pool)
                        device.idDevices = jsonObject.getString("idDevice")
                        device.userID = jsonObject.getInt("devices_user_id")

                        GlobalData.listaDevices.add(device)

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

    fun updateDevicesComandoBD(com: deviceCommand){
        val URLAux = GlobalData.URL + "actualizar_comidaXpool.php"
        val params = HashMap<String, String>()
        params["grPorSegundo"] = com.grPorSegundo.toString()
        params["AlimentoTotal"] = com.alimentoTotal.toString()
        params["enviarProgramacion"] = com.enviarProgramacion.toString()
        params["piscina"] = GlobalData.pool.toString()
        params["idUser"] = GlobalData.idUser.toString()

        val request: StringRequest =
            object : StringRequest(Request.Method.POST, URLAux, {
                Toast.makeText(context, "Operacion Exitosa", Toast.LENGTH_SHORT).show()
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

    fun obtenerComidaBD(){
        val URLAux = GlobalData.URL + "buscar_comidas.php"
        GlobalData.listaComida.clear()

        val jsonArrayRequest: JsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            URLAux,
            null,

            {
                var jsonObject: JSONObject? = null
                for (i in 0 until it.length()) {
                    try {
                        jsonObject = it.getJSONObject(i)
                        val food: Comida = Comida(GlobalData.pool)
                        food.idFood = jsonObject.getString("idFood")
                        food.allimentoGrSeg = jsonObject.getInt("alimentoGrPorSegundo")
                        GlobalData.listaComida.add(food)
                    } catch (error: JSONException) {
                        Toast.makeText(context, "Problemas de conexión", Toast.LENGTH_SHORT).show()
                    }
                }
            },

            {
                Toast.makeText(context,
                    "Problemas de conectandose al servidor",
                    Toast.LENGTH_LONG).show()
            })

        queue = Volley.newRequestQueue(context)
        queue.add(jsonArrayRequest)
    }

    fun fillSpinner(selectDevice: Spinner, context: Context?){
        if (context!=null){
            val listaAux: ArrayList<String> = ArrayList()
            for (x in GlobalData.listaComida) {
                listaAux.add(x.idFood)
            }

            val lista = ArrayAdapter(context, android.R.layout.simple_spinner_item, listaAux)
            lista.setDropDownViewResource(R.layout.spinner_dropdown_item)
            selectDevice.adapter = lista
            try {
                GlobalData.selectFood = GlobalData.listaComida[0]
            } catch (e: java.lang.IndexOutOfBoundsException) {

            }
        }
    }
}

class AdapterComando: BaseAdapter {
    var command = ArrayList<Command>()
    var context: Context? = null

    constructor(context: Context?, movies: ArrayList<Command>) : super() {
        this.command = movies
        this.context = context
    }


    override fun getCount(): Int {
        return command.size
    }

    override fun getItem(position: Int): Any {
        return command[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("SetTextI18n", "ViewHolder", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val com = command[position]
        val inflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val vista = inflater.inflate(R.layout.progamacion_view, null)

        val txtSComando: CheckBox = vista.findViewById(R.id.txtSComando)
        val txtPorcAlimento: TextView = vista.findViewById(R.id.txtPorcAlimento)
        val txtHoraInicio: TextView = vista.findViewById(R.id.txtHoraInicio)
        val txtHoraFinal: TextView = vista.findViewById(R.id.txtHoraFinal)
        val btnEditar: Button = vista.findViewById(R.id.btnEditar)

        txtSComando.isChecked = com.s == 1

        txtSComando.text = "S" + (position+1)
        txtPorcAlimento.text = "" + com.porcentajeAlimento
        
        var time = ""
        if (("" + com.horario_inicial_min.toString()).length == 1){
            time = "0" + com.horario_inicial_min.toString()
        }else
            time = "" + com.horario_inicial_min.toString()

        var time1 = ""
        if (("" + com.horario_final_min.toString()).length == 1){
            time1 = "0" + com.horario_final_min.toString()
        }else
            time1 = "" + com.horario_final_min.toString()

        txtHoraInicio.text = "" + com.horario_inicial_hr + ":" + time
        txtHoraFinal.text = "" + com.horario_final_hr + ":" + time1

        btnEditar.setOnClickListener {
            GlobalData.index = position
            val frag = EditCommand()
            val transaction = (context as MainActivity).getSupportFragmentManager().beginTransaction()
            transaction.replace(R.id.nav_host_fragment, frag)
            transaction.addToBackStack(null)
            transaction.commit()
        }
        return vista
    }

}