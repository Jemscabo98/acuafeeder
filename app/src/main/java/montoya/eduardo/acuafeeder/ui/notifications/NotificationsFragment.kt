package montoya.eduardo.acuafeeder.ui.notifications

import android.os.Bundle
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
import montoya.eduardo.acuafeeder.R
import montoya.eduardo.acuafeeder.data_class.Devices
import org.json.JSONException
import org.json.JSONObject

class NotificationsFragment : Fragment() {
    //val URL = "https://bytefruit.com/practicas-acuafeeder/php/"
    val URL = "http://practicaspro.webhop.me:8080/acuafeeder/"

    private lateinit var notificationsViewModel: NotificationsViewModel
    private lateinit var listaDispositivos: ArrayList<Devices>
    private lateinit var queue: RequestQueue

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        notificationsViewModel =
                ViewModelProvider(this).get(NotificationsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_notifications, container, false)
        notificationsViewModel.text.observe(viewLifecycleOwner, Observer {






        })
        return root
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