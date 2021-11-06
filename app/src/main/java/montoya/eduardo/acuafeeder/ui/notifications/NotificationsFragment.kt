package montoya.eduardo.acuafeeder.ui.notifications

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import montoya.eduardo.acuafeeder.EditCommand
import montoya.eduardo.acuafeeder.MainActivity
import montoya.eduardo.acuafeeder.R
import montoya.eduardo.acuafeeder.data_class.Command
import montoya.eduardo.acuafeeder.data_class.Devices
import montoya.eduardo.acuafeeder.data_class.GlobalData
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.agregarDevicesBD
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.obtenerDevicesBD
import montoya.eduardo.acuafeeder.ui.dashboard.AdapterComando
import org.json.JSONException
import org.json.JSONObject
import java.sql.Timestamp

class NotificationsFragment : Fragment() {
    val URL = "https://bytefruit.com/practicas-acuafeeder/php/"
    //val URL = "http://practicaspro.webhop.me:8080/acuafeeder/"

    private lateinit var notificationsViewModel: NotificationsViewModel
    private lateinit var listaDispositivos: ArrayList<Devices>
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var disp: Devices
    private lateinit var listaDevices: ArrayList<Devices>
    private var otraAlberca: Boolean = false

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        notificationsViewModel =
                ViewModelProvider(this).get(NotificationsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_notifications, container, false)
        notificationsViewModel.text.observe(viewLifecycleOwner, Observer {
            val listview: ListView = root.findViewById(R.id.listViewDevices)
            val layout2: LinearLayout = root.findViewById(R.id.linearLay2)
            val btnOcultar: Button = root.findViewById(R.id.btnOcultar)
            val btnM: Button = root.findViewById(R.id.btnMos)
            val txtEtiqueta: EditText = root.findViewById(R.id.txtEtiqueta)
            val txtPiscina: EditText = root.findViewById(R.id.txtPiscina)
            val txtSN: EditText = root.findViewById(R.id.txtSN)
            val txtGuardarDevice: Button = root.findViewById(R.id.txtGuardarDevice)
            listaDevices = ArrayList()
            listaDevices.clear()
            listview.adapter = null
            var adaptador: AdapterDevice = AdapterDevice(context, listaDevices)
            adaptador.notifyDataSetChanged()
            disp = Devices("",GlobalData.pool)
            txtPiscina.setText(GlobalData.pool.toString())

            if (GlobalData.listaDevices.isEmpty() || GlobalData.listaDevices.get(0).pool != GlobalData.pool) {
                GlobalData.listaDevices = ArrayList()
                //Llena la lista del Spinner
                obtenerDevicesBD(requireContext())
            }

            btnOcultar.setOnClickListener {
                layout2.layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
                listaDevices.clear()
                adaptador.notifyDataSetChanged()
                adaptador = AdapterDevice(context, listaDevices)
                adaptador.notifyDataSetChanged()
                listview.adapter = null
                obtenerDevicesBD(requireContext())
            }

            btnM.setOnClickListener {
                listaDevices = GlobalData.listaDevices
                layout2.layoutParams.height = 0
                adaptador = AdapterDevice(context, listaDevices)
                adaptador.notifyDataSetChanged()
                listview.adapter = adaptador
            }

            txtGuardarDevice.setOnClickListener {
                if (verificarDatos(txtEtiqueta, txtPiscina, txtSN)){
                    if (otraAlberca){
                        handler.postDelayed(Runnable {
                            agregarDevicesBD(disp, requireContext())
                            GlobalData.listaDevices.add(disp)
                            listaDevices = GlobalData.listaDevices
                            txtEtiqueta.setText ("")
                            txtSN.setText ("")
                            layout2.layoutParams.height = 0
                            adaptador = AdapterDevice(context, listaDevices)
                            adaptador.notifyDataSetChanged()
                            listview.adapter = adaptador
                        }, 650)
                    }else{
                        agregarDevicesBD(disp, requireContext())
                        GlobalData.listaDevices.add(disp)
                        listaDevices = GlobalData.listaDevices
                        txtEtiqueta.setText ("")
                        txtSN.setText ("")
                        layout2.layoutParams.height = 0
                        adaptador = AdapterDevice(context, listaDevices)
                        adaptador.notifyDataSetChanged()
                        listview.adapter = adaptador
                    }
                }
            }

        })
        return root
    }

    fun verificarDatos(txtEtiqueta: EditText, txtPiscina: EditText, txtSN: EditText): Boolean{
        if (txtEtiqueta.text.isEmpty() || txtPiscina.text.isEmpty() || txtSN.text.isEmpty()){
            Toast.makeText(context, "Favor de no dejar espacios vacios", Toast.LENGTH_LONG).show()
            return false
        }
        disp.idDevices = txtSN.text.toString()
        disp.fechaCreacion = Timestamp(System.currentTimeMillis())
        disp.userID = GlobalData.idUser
        disp.devices_etiqueta = txtEtiqueta.text.toString()

        if(GlobalData.pool != txtPiscina.text.toString().toInt()){
            GlobalData.pool = txtPiscina.text.toString().toInt()
            disp.pool = GlobalData.pool
            GlobalData.obtenerDevicesBD(requireContext())
            otraAlberca = true
        }
        else {
            disp.pool = GlobalData.pool
            otraAlberca = false
        }
        return true
    }
    



}

class AdapterDevice: BaseAdapter {
    var device = ArrayList<Devices>()
    var context: Context? = null

    constructor(context: Context?, devices: ArrayList<Devices>) : super() {
        this.device = devices
        this.context = context
    }

    override fun getCount(): Int {
        return device.size
    }

    override fun getItem(position: Int): Any {
        return device[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    fun eliminarDevicesBD(dev: Devices){
        val URLAux = GlobalData.URL + "eliminar_devices.php"
        val params = HashMap<String, String>()
        params["idDevice"] = dev.idDevices
        params["devices_user_id"] = GlobalData.idUser.toString()

        val request: StringRequest =
            object : StringRequest(Request.Method.POST, URLAux, {
                Toast.makeText(context, "Se elimino con exito: " + dev.devices_etiqueta, Toast.LENGTH_SHORT).show()
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

        val queue: RequestQueue = Volley.newRequestQueue(context)
        request.setShouldCache(false)
        queue.add(request)
    }

    @SuppressLint("SetTextI18n", "ViewHolder", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val dv = GlobalData.listaDevices[position]
        val inflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val vista = inflater.inflate(R.layout.device_view, null)

        val txtEtiqueta: TextView = vista.findViewById(R.id.txtEtiqueta)
        val txtPiscina: TextView = vista.findViewById(R.id.txtPiscina)
        val txtFechaRegistro: TextView = vista.findViewById(R.id.txtFechaRegistro)
        val btnEliminar: Button = vista.findViewById(R.id.btnEliminar)

        txtEtiqueta.text = dv.devices_etiqueta
        txtPiscina.text = dv.pool.toString()

        //Se extrae unicamente la fecha
        var aux = dv.fechaCreacion.toString()
        val aux2 = aux.split(" ")
        aux = aux2[0]
        txtFechaRegistro.text = aux

        btnEliminar.setOnClickListener {
            GlobalData.listaDevices.removeAt(position)
            notifyDataSetChanged()
            eliminarDevicesBD(dv)
        }
        return vista
    }

}