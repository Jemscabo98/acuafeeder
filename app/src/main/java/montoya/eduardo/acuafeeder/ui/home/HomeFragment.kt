package montoya.eduardo.acuafeeder.ui.home

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import montoya.eduardo.acuafeeder.R
import montoya.eduardo.acuafeeder.data_class.Devices
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.roundToInt

class HomeFragment : Fragment() {
    //val URL = "https://bytefruit.com/practicas-acuafeeder/php/"
    val URL = "http://practicaspro.webhop.me:8080/acuafeeder/"

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var queue: RequestQueue
    private lateinit var listaDispositivos: ArrayList<Devices>

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            val graph: GraphView = root.findViewById(R.id.graph) as GraphView
            val btnBuscar: Button = root.findViewById(R.id.btnBuscarDispositivos)
            val txtNumPiscina: EditText = root.findViewById(R.id.txtNumPiscina)

            btnBuscar.setOnClickListener {

                if(checarDato(txtNumPiscina)){


                }else{
                    Toast.makeText(context, "Favor de solo usar números en piscina", Toast.LENGTH_LONG).show()
                }
            }



            val series = BarGraphSeries(arrayOf(
                DataPoint(0.toDouble(), 0.0),
                DataPoint(1.0, 5.2.roundToInt().toDouble()),
                DataPoint(2.0, 3.7.roundToInt().toDouble()),
                DataPoint(3.0, 5.9.roundToInt().toDouble()),
                DataPoint(4.0, 1.63.roundToInt().toDouble()),
                DataPoint(5.0, 3.4.roundToInt().toDouble()),
                DataPoint(6.0, 5.6.roundToInt().toDouble()),
                DataPoint(7.0, 1.1.roundToInt().toDouble())
            ));
            series.setSpacing(20)
            series.setDrawValuesOnTop(true);
            series.setValuesOnTopColor(Color.WHITE);
            graph.addSeries(series)

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

    //Guarda en una lista los dispositivos de la BD
    fun obtenerListaDevices(numPisina: Int) {
        val URLAux = URL + "buscar_dispositivos.php?devices_piscina=" + numPisina + ""


        val jsonArrayRequest: JsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            URLAux,
            null,

            {
                var jsonObject: JSONObject? = null
                listaDispositivos.clear()
                for (i in 0 until it.length()) {
                    try {
                        jsonObject = it.getJSONObject(i)
                        var dispo: Devices = Devices(jsonObject.getString("devices_etiqueta"))
                        dispo.idDevices = jsonObject.getString("idDevice")
                        dispo.userID = jsonObject.getString("devices_user_id").toInt()

                        listaDispositivos.add(dispo)

                    } catch (error: JSONException) {
                        Toast.makeText(context, "No se encuantran dispositivos en la piscina", Toast.LENGTH_SHORT).show()
                    }
                }

            },

            {
                Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
            })

        queue = Volley.newRequestQueue(context)
        queue.add(jsonArrayRequest)
    }

    //Carga la lista de dispositivos en el combo box
    fun cargarLista(txtNumPiscina: EditText, cmbDispositivos: Spinner){
        var lista: ArrayList<String> = ArrayList()

        for(x in listaDispositivos){
            lista.add(x.devices_etiqueta)
        }

        var adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, lista.toArray())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cmbDispositivos.adapter = adapter
    }

}