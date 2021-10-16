package montoya.eduardo.acuafeeder.ui.home

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.*
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.StaticLabelsFormatter
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import montoya.eduardo.acuafeeder.R
import montoya.eduardo.acuafeeder.data_class.Command
import montoya.eduardo.acuafeeder.data_class.GlobalData
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat


class HomeFragment : Fragment() {
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var queue: RequestQueue
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            val graph: GraphView = root.findViewById(R.id.graph) as GraphView
            val btnBuscar: Button = root.findViewById(R.id.btnBuscarDispositivos)
            val txtNumPiscina: EditText = root.findViewById(R.id.txtNumPiscina)
            GlobalData.listaComandos = ArrayList()

            txtNumPiscina.setText(GlobalData.pool.toString())

            if (GlobalData.listaComandos.isEmpty()){
                //Obtiene los datos de la BD
                obtenerDatosBD()

                handler.postDelayed(Runnable {
                    cargarDatos(graph)
                },25)
            }
            if (!GlobalData.listaComandos.isEmpty()){
                cargarDatos(graph)
            }


            //Cuando se le de click al boton
            btnBuscar.setOnClickListener {
                //Verifica que el dato sea un numero
                if (checarDato(txtNumPiscina)) {
                    GlobalData.pool = txtNumPiscina.text.toString().toInt()

                    //Obtiene los datos de la BD
                    obtenerDatosBD()

                    handler.postDelayed(Runnable {
                        cargarDatos(graph)
                     },25)
                } else {
                    Toast.makeText(context,
                        "Favor de solo usar números en piscina",
                        Toast.LENGTH_LONG).show()
                }
            }
        })
        return root
    }

    //Verifica que el numero de psicina sea numerico
    fun checarDato(txtNumPiscina: EditText): Boolean{
        val respuesta: Boolean
        val texto = txtNumPiscina.text

        respuesta = TextUtils.isDigitsOnly(texto)

        return respuesta
    }

    //Obtener datos de BD
    fun obtenerDatosBD(){
        val URLAux = GlobalData.URL + "buscar_commandoXalberca.php?piscina=" + GlobalData.pool + ""
        GlobalData.listaComandos.clear()

        val jsonArrayRequest: JsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            URLAux,
            null,

            {
                var jsonObject: JSONObject? = null
                for (i in 0 until it.length()) {
                    try {
                        jsonObject = it.getJSONObject(i)
                        val command = Command(GlobalData.pool)
                        command.horario_inicial_hr = jsonObject.getInt("horario_inicial_hr")
                        command.horario_inicial_min = jsonObject.getInt("horario_inicial_min")
                        command.horario_final_hr = jsonObject.getInt("horario_final_hr")
                        command.horario_final_min = jsonObject.getInt("horario_final_min")
                        command.porcentajeAlimento = jsonObject.getInt("porcentajeAlimento")
                        command.s = jsonObject.getInt("s")

                        GlobalData.listaComandos.add(command)

                    } catch (error: JSONException) {
                        Toast.makeText(context, "Problemas de conexión", Toast.LENGTH_SHORT).show()
                    }
                }
            },

            {
                Toast.makeText(context,
                    "No se encuentran datos con este num. de piscina",
                    Toast.LENGTH_LONG).show()
            })

        queue = Volley.newRequestQueue(context)
        queue.add(jsonArrayRequest)
    }

    fun cargarDatos(graph: GraphView){
        try {
            // Limpi la grafica cada vez que se busque datos
            graph.removeAllSeries()
            // Tipo de grafica /barra/
            val series = BarGraphSeries(getDataPoint())
            //Valores de la grafica
            series.spacing = 20
            series.isDrawValuesOnTop = true
            series.valuesOnTopColor = Color.WHITE
            graph.viewport.isYAxisBoundsManual = true
            graph.viewport.setMinY(0.0)
            graph.addSeries(series)

            //Se agrega los tag (hora) en la axis x
            val labels = ArrayList<String>()
            var maxValue: Double = 0.0

            for (x in GlobalData.listaComandos) {

                if (x.porcentajeAlimento > maxValue)
                    maxValue = x.porcentajeAlimento.toDouble()

                if (x.horario_inicial_min.toString().length != 2) {
                    labels.add("" + x.horario_inicial_hr + ":0" + x.horario_inicial_min)
                } else {
                    labels.add("" + x.horario_inicial_hr + ":" + x.horario_inicial_min)
                }
            }

            maxValue += 20.0
            graph.viewport.setMaxY(maxValue)
            var stockArr = arrayOfNulls<String>(labels.size)
            stockArr = labels.toArray(stockArr)
            val labelX: StaticLabelsFormatter = StaticLabelsFormatter(graph)
            labelX.setHorizontalLabels(stockArr)
            graph.gridLabelRenderer.labelFormatter = labelX
        } catch (ex: Exception) {

        }
    }

    fun getDataPoint(): Array<DataPoint> {
        val arrayListLineaTemp: ArrayList<DataPoint> = ArrayList()

        var aux: Int = 1
        for (x in GlobalData.listaComandos){
            arrayListLineaTemp.add(DataPoint(aux.toDouble(), x.porcentajeAlimento.toDouble()))
            aux++
        }

        val arrayLineaTemp: Array<DataPoint> = arrayListLineaTemp.toTypedArray()
        return arrayLineaTemp
    }
}

