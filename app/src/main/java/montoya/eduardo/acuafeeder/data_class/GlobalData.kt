package montoya.eduardo.acuafeeder.data_class

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.sql.Timestamp
import java.util.*
import kotlin.collections.ArrayList

open class GlobalData: Application() {
   companion object{
       private lateinit var queue: RequestQueue
       const val URL: String = "https://bytefruit.com/practicas-acuafeeder/php/"
       //val URL = "http://192.168.1.111:8080/acuafeeder/"
       var idUser: Int = 0
       var pool: Int = 1
       var listaComandos: ArrayList<Command> = ArrayList()

       var listaTemp: ArrayList<temp> = ArrayList()
       var fechaTemp: String = ""
       var listaDevices: ArrayList<Devices> = ArrayList()
       var deviceTemp: String = ""

       var index: Int = 0
       var listaComida: ArrayList<Comida> = ArrayList()
       var deviceCom: deviceCommand = deviceCommand(pool)
       var selectFood: Comida = Comida(pool)


        //Metodos para acceder a BD
       fun obtenerDevicesBD(context: Context){
           val URLAux = URL + "buscar_dispositivos.php?devices_piscina=" + pool

           val jsonArrayRequest: JsonArrayRequest = JsonArrayRequest(
               Request.Method.GET,
               URLAux,
               null,

               {
                   listaDevices.clear()
                   var jsonObject: JSONObject? = null
                   for (i in 0 until it.length()) {
                       try {
                           jsonObject = it.getJSONObject(i)
                           val device: Devices = Devices(jsonObject.getString("devices_etiqueta"), pool)
                           device.idDevices = jsonObject.getString("idDevice")
                           device.userID = jsonObject.getInt("devices_user_id")
                           device.fechaCreacion = Timestamp.valueOf(jsonObject.getString("devices_date"))

                           listaDevices.add(device)

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

       fun agregarDevicesBD(dev: Devices, context: Context){
           val URLAux = URL + "insertar_devices.php"
           val params = HashMap<String, String>()
           params["idDevice"] = dev.idDevices
           params["devices_date"] = dev.fechaCreacion.toString()
           params["devices_etiqueta"] = dev.devices_etiqueta
           params["devices_piscina"] = pool.toString()
           params["devices_user_id"] = idUser.toString()

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

       fun agregarDevicesCommandBD(dev: Devices, context: Context){
           val URLAux = URL + "insertar_devicesCommand.php"
           val params = HashMap<String, String>()
           params["idDevice"] = dev.idDevices
           params["idUser"] = idUser.toString()
           params["piscina"] = pool.toString()
           params["dispositivosXpiscina"] = listaDevices.size.toString()
           params["Modo"] = deviceCom.modo.toString()
           params["PIN"] = "NXNX"
           params["enviarProgramacion"] = 1.toString()
           params["ManualSw"] = deviceCom.manualSW.toString()
           params["AlimentoTotal"] = deviceCom.alimentoTotal.toString()
           params["grPorSegundo"] = selectFood.allimentoGrSeg.toString()
           params["reloj"] = "0"

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

       fun updateManual(context: Context){
           val URLAux = URL + "actualizar_manual.php"
           val params = HashMap<String, String>()
           params["Modo"] = deviceCom.modo.toString()
           params["ManualSw"] = deviceCom.manualSW.toString()
           params["piscina"] = pool.toString()
           params["idUser"] = idUser.toString()

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

       fun obtenerDevicesComandoBD(context: Context){
           val URLAux = URL + "buscar_deviceCommando.php?piscina=" + pool

           val jsonArrayRequest: JsonArrayRequest = JsonArrayRequest(
               Request.Method.GET,
               URLAux,
               null,

               {
                   listaDevices.clear()
                   var jsonObject: JSONObject? = null
                   for (i in 0 until it.length()) {
                       try {
                           jsonObject = it.getJSONObject(i)
                           val deviceCom: deviceCommand = deviceCommand(pool)
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

       fun obtenerComandos(context: Context){
           val URLAux = URL + "buscar_commando.php?piscina=" + pool + "&devices_user_id=" + idUser

           val jsonArrayRequest: JsonArrayRequest = JsonArrayRequest(
               Request.Method.GET,
               URLAux,
               null,

               {
                   listaComandos.clear()
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

                           listaComandos.add(command)

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
           jsonArrayRequest.setShouldCache(false)
           queue.add(jsonArrayRequest)
       }

       fun updateCommand(idDevice: String, ID: String, com: Command,context: Context){
           val URLAux = URL + "actualizar_comando.php"
           val params = HashMap<String, String>()
           params["piscina"] = pool.toString()
           params["devices_user_id"] = idUser.toString()
           params["ID"] = ID
           params["s"] = com.s.toString()
           params["etapa"] = "0"
           params["horario_inicial_hr"] = com.horario_inicial_hr.toString()
           params["horario_inicial_min"] = com.horario_inicial_min.toString()
           params["horario_final_hr"] = com.horario_final_hr.toString()
           params["horario_final_min"] = com.horario_final_min.toString()
           params["porcentajeAlimento"] = com.porcentajeAlimento.toString()

           val request: StringRequest =
               object : StringRequest(Request.Method.POST, URLAux, {
                   //Toast.makeText(context, "Operacion Exitosa", Toast.LENGTH_SHORT).show()
               }, { error: VolleyError ->
                   println("Error $error.message")
                   //Toast.makeText(context, "Error de Conexion", Toast.LENGTH_SHORT).show()
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

       fun AddCommand(ID: String, com: Command, context: Context){
           val URLAux = URL + "insertar_comando.php"
           val params = HashMap<String, String>()
           params["piscina"] = pool.toString()
           params["ID"] = ID
           params["devices_user_id"] = idUser.toString()
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

       fun DeleteCommand(ID: String, context: Context){
           val URLAux = URL + "eliminar_comando.php"
           val params = HashMap<String, String>()
           params["piscina"] = pool.toString()
           params["devices_user_id"] = idUser.toString()
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

       fun updateComida(com: deviceCommand, context: Context){
           val URLAux = URL + "actualizar_comidaXpool.php"
           val params = HashMap<String, String>()
           params["grPorSegundo"] = com.grPorSegundo.toString()
           params["AlimentoTotal"] = com.alimentoTotal.toString()
           params["enviarProgramacion"] = com.enviarProgramacion.toString()
           params["piscina"] = pool.toString()
           params["idUser"] = idUser.toString()

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

       fun obtenerComidaBD(context: Context){
           val URLAux = URL + "buscar_comidas.php"

           val jsonArrayRequest: JsonArrayRequest = JsonArrayRequest(
               Request.Method.GET,
               URLAux,
               null,

               {
                   listaComida.clear()
                   var jsonObject: JSONObject? = null
                   for (i in 0 until it.length()) {
                       try {
                           jsonObject = it.getJSONObject(i)
                           val food: Comida = Comida(pool)
                           food.idFood = jsonObject.getString("idFood")
                           food.allimentoGrSeg = jsonObject.getInt("alimentoGrPorSegundo")
                           listaComida.add(food)
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

       fun obtenerTempBD(context: Context){
           val URLAux = URL + "buscar_temp.php?date=" + fechaTemp + "&idDevices=" + deviceTemp

           Log.d(ContentValues.TAG, "URL: $URLAux")
           var aux = ""

           val jsonArrayRequest: JsonArrayRequest = JsonArrayRequest(
               Request.Method.GET,
               URLAux,
               null,

               {
                   listaTemp.clear()
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
       }}

    override fun onCreate() {
        super.onCreate()
    }
}