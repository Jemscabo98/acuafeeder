package montoya.eduardo.acuafeeder.ui.dashboard

import android.app.VoiceInteractor
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.volley.*
import com.android.volley.Request.Method.POST
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import montoya.eduardo.acuafeeder.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class DashboardFragment : Fragment() {
    //val URL = "http://practicaspro.webhop.me:8080/acuafeeder/"
    val URL = "https://bytefruit.com/practicas-acuafeeder/php/"
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var queue: RequestQueue

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)

        dashboardViewModel.text.observe(viewLifecycleOwner, Observer {

            val btnGuardar: Button = root.findViewById(R.id.btnComida)
            val txtIDFood: EditText = root.findViewById(R.id.txtIDFood)
            val txtAlimento: EditText = root.findViewById(R.id.txtAlimento)
            val txtProveedor: EditText = root.findViewById(R.id.txtProveedor)

            val btnBuscar: Button = root.findViewById(R.id.btnBuscar)
            val txtBuscarIDFood: EditText = root.findViewById(R.id.txtBuscarIDFood)
            val txtBuscarAlimento: EditText = root.findViewById(R.id.txtBuscarAlimento)
            val txtBuscarProveedor: EditText = root.findViewById(R.id.txtBuscarProveedor)

            btnGuardar.setOnClickListener {
                var URLAux = URL + "insertar_comida.php"
                var params = HashMap<String, String>()
                params["idFood"] = txtIDFood.text.toString()
                params["proveedor"] = txtProveedor.text.toString()
                params["alimentoGrPorSegundo"] = txtAlimento.text.toString()

                var request: StringRequest =
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
                queue.add(request)

            }

            btnBuscar.setOnClickListener {
                var URLAux = URL + "buscar_comida.php?idFood=" + txtBuscarIDFood.getText() + ""

                var jsonArrayRequest: JsonArrayRequest = JsonArrayRequest(
                    Request.Method.GET,
                    URLAux,
                    null,

                    {
                        var jsonObject: JSONObject? = null
                        for (i in 0 until it.length()) {
                            try {
                                jsonObject = it.getJSONObject(i)
                                txtBuscarAlimento.setText(jsonObject.getInt("alimentoGrPorSegundo").toString())
                                txtBuscarProveedor.setText(jsonObject.getString("proveedor"))
                            } catch (error: JSONException) {
                                Toast.makeText(context, "No se encuantra el producto", Toast.LENGTH_SHORT).show()
                            }
                        }

                    },

                    {
                        Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                    })

                queue = Volley.newRequestQueue(context)
                queue.add(jsonArrayRequest)
            }

        })
        return root
    }
}