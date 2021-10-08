package montoya.eduardo.acuafeeder.ui.home

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
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
import org.json.JSONException
import org.json.JSONObject
import java.sql.Time
import java.text.SimpleDateFormat


class HomeFragment : Fragment() {
    //val URL = "https://bytefruit.com/practicas-acuafeeder/php/"
    val URL = "http://practicaspro.webhop.me:8080/acuafeeder/"
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var queue: RequestQueue
    private lateinit var listaComandos: ArrayList<Command>

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
            var sdf: SimpleDateFormat = SimpleDateFormat("HH:mm")
            listaComandos = ArrayList()

            //Cuando se le de click al boton
            btnBuscar.setOnClickListener {
                //Verifica que el dato sea un numero
                if (checarDato(txtNumPiscina)) {
                    try {
                        //Obtiene los datos de la BD
                        obtenerDatosBD(txtNumPiscina)

                        //Hace un retraso de .5 segundos en lo que procesa los datos
                        val handler = Handler()
                        handler.postDelayed(Runnable {
                            graph.removeAllSeries()
                            val series = BarGraphSeries(getDataBD())
                            series.setSpacing(20)
                            series.setDrawValuesOnTop(true);
                            series.setValuesOnTopColor(Color.WHITE);
                            graph.addSeries(series)

                            if(!listaComandos.isEmpty()){
                                var labels = ArrayList<String>()

                                for (x in listaComandos) {
                                    if (x.horario_inicial_min.toString().length != 2){
                                        labels.add("" + x.horario_inicial_hr + ":0" + x.horario_inicial_min)
                                    }else{
                                        labels.add("" + x.horario_inicial_hr + ":" + x.horario_inicial_min)
                                    }
                                }
                                var stockArr = arrayOfNulls<String>(labels.size)
                                stockArr = labels.toArray(stockArr)

                                var labelX: StaticLabelsFormatter = StaticLabelsFormatter(graph)
                                labelX.setHorizontalLabels(stockArr)
                                graph.gridLabelRenderer.labelFormatter = labelX
                            }else{
                                Toast.makeText(context, "Problemas de conexión", Toast.LENGTH_SHORT).show()
                            }

                        }, 500)
                    } catch (ex: Exception) {

                    }

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
    fun obtenerDatosBD(txtNumPiscina: EditText){
        var URLAux = URL + "buscar_commandoXalberca.php?piscina=" + txtNumPiscina.getText() + ""
        listaComandos.clear()

        var jsonArrayRequest: JsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            URLAux,
            null,

            {
                var jsonObject: JSONObject? = null
                for (i in 0 until it.length()) {
                    try {
                        jsonObject = it.getJSONObject(i)
                        var command = Command(txtNumPiscina.getText().toString().toInt())
                        command.horario_inicial_hr = jsonObject.getInt("horario_inicial_hr")
                        command.horario_inicial_min = jsonObject.getInt("horario_inicial_min")
                        command.horario_final_hr = jsonObject.getInt("horario_final_hr")
                        command.horario_final_min = jsonObject.getInt("horario_final_min")
                        command.porcentajeAlimento = jsonObject.getInt("porcentajeAlimento")
                        command.s = jsonObject.getInt("s")

                        listaComandos.add(command)

                    } catch (error: JSONException) {
                        Toast.makeText(context, "Problemas de conexión", Toast.LENGTH_SHORT).show()
                    }
                }
            },

            {
                Toast.makeText(context, "No se encuentran datos con este num. de piscina", Toast.LENGTH_LONG).show()
            })

        queue = Volley.newRequestQueue(context)
        queue.add(jsonArrayRequest)
    }

    fun getDataBD(): Array<DataPoint> {
        var arrayListLineaTemp: ArrayList<DataPoint> = ArrayList()

        var aux: Int = 1
        for (x in listaComandos){
            arrayListLineaTemp.add(DataPoint(aux.toDouble(), x.porcentajeAlimento.toDouble()))
            aux++
        }

        var arrayLineaTemp: Array<DataPoint> = arrayListLineaTemp.toTypedArray()
        return arrayLineaTemp
    }
}