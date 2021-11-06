package montoya.eduardo.acuafeeder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import montoya.eduardo.acuafeeder.data_class.Devices
import montoya.eduardo.acuafeeder.data_class.GlobalData
import montoya.eduardo.acuafeeder.data_class.deviceCommand
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class Log_In : AppCompatActivity() {
    private lateinit var queue: RequestQueue
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log__in)

        val txtMail: TextView = findViewById(R.id.txtMail)
        val txtPassword: TextView = findViewById(R.id.txtPassword)
        val btnLogin: Button = findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener {
            validarUsuario(txtMail.text.toString(), txtPassword.text.toString())
        }
    }

    fun validarUsuario(users_email: String, users_password: String){
        val URLAux = GlobalData.URL + "log_in.php"
        val params = HashMap<String, String>()
        params["users_email"] = users_email
        params["users_password"] = users_password

        val request: StringRequest =
            object : StringRequest(Request.Method.POST, URLAux, {
               if (it.contains("users_email")){
                   val intent = Intent(applicationContext, MainActivity::class.java)

                   var jsonArray: JSONArray? = null
                   try {
                       jsonArray= JSONArray(it)
                       var jsonObject = JSONObject(jsonArray[0].toString())
                       GlobalData.idUser = jsonObject.getString("users_id").toInt()
                       Toast.makeText(this, jsonObject.getString("users_id"), Toast.LENGTH_LONG).show()
                   } catch (error: JSONException) {
                       Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
                   }

                   this.startActivity(intent)
               }else{
                   Toast.makeText(this, "El usuario o la contraseña no fueron correctamente ingresados", Toast.LENGTH_SHORT).show()
               }
            }, { error: VolleyError ->
                println("Error $error.message")
                Toast.makeText(this, "Error de conexion", Toast.LENGTH_SHORT).show()
            }) {
                override fun getParams(): Map<String, String> {
                    return params
                }
            }

        request.retryPolicy =
            DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, 1f)

        queue = Volley.newRequestQueue(this)
        request.setShouldCache(false)
        queue.add(request)
    }

}