package montoya.eduardo.acuafeeder

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import montoya.eduardo.acuafeeder.data_class.Devices
import montoya.eduardo.acuafeeder.data_class.GlobalData
import montoya.eduardo.acuafeeder.data_class.temp
import org.json.JSONException
import org.json.JSONObject
import java.sql.Time
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class InformationFragment : Fragment() {

    private lateinit var mDateSetListener: DatePickerDialog.OnDateSetListener
    private lateinit var queue: RequestQueue
    private val handler = Handler(Looper.getMainLooper())
    val sdf2: SimpleDateFormat = SimpleDateFormat("HH:mm:ss")
    val sdfDay: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
    private lateinit var txtNumPiscina: EditText


    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val root = inflater.inflate(R.layout.fragment_informacion, container, false)
        val graph: GraphView = root.findViewById(R.id.graph) as GraphView
        txtNumPiscina = root.findViewById(R.id.txtNumPiscina)
        val btnSelecFecha: Button = root.findViewById(R.id.selFechaTemp)
        val txtSelecFecha: TextView = root.findViewById(R.id.txtFechaTemp)
        val btnActualizarGraph: Button = root.findViewById(R.id.btnActualizarGraph)
        val selectDevice: Spinner = root.findViewById(R.id.selectDevice)
        val btnBuscarDispositivos: Button = root.findViewById(R.id.btnBuscarDispositivos)

        //Pone el dia actual como fecha automatica
        if (GlobalData.fechaTemp==""){
            val cal: Calendar = Calendar.getInstance() //Objeto Calendario
            GlobalData.fechaTemp = cal.get(Calendar.YEAR).toString() +
                    "-" + cal.get(Calendar.MONTH).toString() +
                    "-" + cal.get(Calendar.DAY_OF_MONTH).toString()
            txtSelecFecha.text = "<${GlobalData.fechaTemp}>"
        }else{
            txtSelecFecha.text = "<${GlobalData.fechaTemp}>"
        }

        //Carga el numero de piscina global
        txtNumPiscina.setText(GlobalData.pool.toString(), TextView.BufferType.EDITABLE)

        //Llena los datos al select box
        if (GlobalData.listaDevices.isEmpty() || GlobalData.listaDevices.get(0).pool != GlobalData.pool){
            GlobalData.listaDevices = ArrayList()

            //Llena la lista del Spinner
            obtenerDevicesBD()

            handler.postDelayed(Runnable {
                fillSpinner(selectDevice, context)
            }, 750)
        }else{
            fillSpinner(selectDevice, context)
        }

        obtenerTempBD()

        handler.postDelayed(Runnable {
            graficarResultados(graph)
        }, 50)


        btnSelecFecha.setOnClickListener {
            //Variables
            val cal: Calendar = Calendar.getInstance() //Objeto Calendario
            val date: Date = sdfDay.parse(GlobalData.fechaTemp)
            cal.time = date
            val anou = cal.get(Calendar.YEAR) //Donde se guarda el año
            var mes = cal.get(Calendar.MONTH) //Donde se guarda el mes
            val dia = cal.get(Calendar.DAY_OF_MONTH) //Donde se guarda el dia
            val colorAux: ColorDrawable = ColorDrawable(Color.TRANSPARENT)

            mDateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                mes = month + 1
                Log.d(TAG, "onDateSet: date: $year/$mes/$dayOfMonth")
                GlobalData.fechaTemp = "$year-$mes-$dayOfMonth"
                txtSelecFecha.text = "<$dayOfMonth-$mes-$year>"
            }

            val selecFechaAux: DatePickerDialog = DatePickerDialog(
                this.requireContext(),
                android.R.style.Theme_Holo_Light_Dialog,
                mDateSetListener,
                anou, mes, dia)

            selecFechaAux.window?.setBackgroundDrawable(colorAux)
            selecFechaAux.show()
        }

        btnBuscarDispositivos.setOnClickListener {
            if (TextUtils.isDigitsOnly(txtNumPiscina.text)){
                GlobalData.pool = txtNumPiscina.text.toString().toInt()
                /*//Llena la lista del Spinner
                obtenerDevicesBD()
                fillSpinner(selectDevice, context)*/
                    //Llena la lista del Spinner

                obtenerDevicesBD()
                handler.postDelayed(Runnable {
                    fillSpinner(selectDevice, context)
                }, 750)

            }else{
                Toast.makeText(context,
                    "Favor de solo usar números en piscina",
                    Toast.LENGTH_LONG).show()
            }
        }

        selectDevice.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                GlobalData.deviceTemp = GlobalData.listaDevices.get(0).idDevices
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                for (x in GlobalData.listaDevices){
                    if(x.devices_etiqueta == selectDevice.selectedItem.toString()){
                        GlobalData.deviceTemp = x.idDevices
                    }
                }
            }

        }

        btnActualizarGraph.setOnClickListener {
            obtenerTempBD()

            handler.postDelayed(Runnable {
                graficarResultados(graph)
            }, 50)
        }
        return root
    }

    fun graficarResultados(graph: GraphView){
        val lineaTemp = LineGraphSeries(getDataBD())
        lineaTemp.color = Color.GREEN
        // Limpi la grafica cada vez que se busque datos
        graph.removeAllSeries()
        if (!GlobalData.listaTemp.isEmpty()){


            val tiempo: Time = Time.valueOf(GlobalData.listaTemp[0].time + ":00")
            val tiempo2: Time = Time.valueOf(GlobalData.listaTemp[GlobalData.listaTemp.size - 1].time + ":00")

            graph.viewport.setMinX(tiempo.time.toDouble())
            graph.viewport.setMaxX(tiempo2.time.toDouble())
            graph.viewport.isXAxisBoundsManual = true
            graph.addSeries(lineaTemp)
            graph.gridLabelRenderer.setLabelHorizontalHeight(150);
            graph.gridLabelRenderer.setHorizontalLabelsAngle(135)
            graph.gridLabelRenderer.setLabelFormatter(object : DefaultLabelFormatter() {
                @SuppressLint("SimpleDateFormat")
                override fun formatLabel(value: Double, isValueX: Boolean): String {
                    if (isValueX) {
                        val sdf: SimpleDateFormat = SimpleDateFormat("HH:mm")
                        return sdf.format(value)
                    } else {
                        return "" + String.format("%.1f", value) + " C"
                    }
                }
            })
        }
    }

    fun getDataBD(): Array<DataPoint> {
        val arrayListLineaTemp: ArrayList<DataPoint> = ArrayList()
        val listaTiempo: ArrayList<Time> = ArrayList()
        for (x in GlobalData.listaTemp){
            val tiempo: Time = Time.valueOf(x.time + ":00")
            if (!listaTiempo.contains(tiempo)) {
                listaTiempo.add(tiempo)
                arrayListLineaTemp.add(DataPoint(tiempo.time.toDouble(), x.temp.toDouble()))
                //Toast.makeText(context, tiempo.toString() + " " + x.temp, Toast.LENGTH_SHORT).show()
            }
        }
        val arrayLineaTemp = arrayListLineaTemp.toTypedArray()

        return arrayLineaTemp
    }

    //Obtener datos de BD
    fun obtenerTempBD(){
        val URLAux = GlobalData.URL + "buscar_temp.php?date=" + GlobalData.fechaTemp +
                "&idDevices=" + GlobalData.deviceTemp

        Log.d(TAG, "URL: $URLAux")
        GlobalData.listaTemp.clear()
        var aux = ""

        val jsonArrayRequest: JsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            URLAux,
            null,

            {
                var jsonObject: JSONObject? = null
                for (i in 0 until it.length()) {
                    try {
                        jsonObject = it.getJSONObject(i)
                        val tmp = temp(jsonObject.getString("time"))
                        tmp.temp = jsonObject.getString("tempAgua")
                        if (tmp.time.equals(aux)) {
                        } else {
                            aux = tmp.time
                            GlobalData.listaTemp.add(tmp)
                        }

                    } catch (error: JSONException) {
                        Toast.makeText(context, "Problemas de conexión", Toast.LENGTH_SHORT).show()
                    }
                }

            },

            {
                Toast.makeText(context,
                    "No se encuentran datos con los datos ingresados ",
                    Toast.LENGTH_LONG).show()
            })

        queue = Volley.newRequestQueue(context)
        queue.add(jsonArrayRequest)
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
                        device.fechaCreacion = Timestamp.valueOf(jsonObject.getString("devices_date"))

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

    fun fillSpinner(selectDevice: Spinner, context: Context?){
        if (context!=null){
                val listaAux: ArrayList<String> = ArrayList()
                for (x in GlobalData.listaDevices) {
                    listaAux.add(x.devices_etiqueta)
                }

                val lista = ArrayAdapter(context, android.R.layout.simple_spinner_item, listaAux)
                lista.setDropDownViewResource(R.layout.spinner_dropdown_item)
                selectDevice.adapter = lista
                try {
                    GlobalData.deviceTemp = GlobalData.listaDevices[0].idDevices
                } catch (e: java.lang.IndexOutOfBoundsException) {

                }
        }
    }
}




