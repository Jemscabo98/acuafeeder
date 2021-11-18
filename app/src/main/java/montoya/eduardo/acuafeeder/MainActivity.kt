package montoya.eduardo.acuafeeder

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.*
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import montoya.eduardo.acuafeeder.data_class.GlobalData
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.alerta
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.deviceCom
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.obtenerComandos
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.obtenerComidaBD
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.obtenerDevicesBD
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.obtenerDevicesComandoBD
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.pool
import java.util.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    private var piscinaAux: Int = pool
    val timer = Timer()
    val handler = Handler(Looper.getMainLooper())
    lateinit var actbar : ActionBar
    lateinit var navController: NavController
    lateinit var barraCarga: ProgressBar
    lateinit var txtProgreso: TextView
    private lateinit var sharedPref: SharedPreferences
    private lateinit var edit: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {

        //Barra superior
        actbar = getSupportActionBar() as ActionBar
        actionBar(this)
        barraCarga = actbar.customView.findViewById(R.id.barraCarga)
        txtProgreso = actbar.customView.findViewById(R.id.txtProgreso)

        //Barra de navegacion inferior
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_home,
            R.id.navigation_dashboard,
            R.id.navigation_notifications,
            R.id.navigation_devices,
            R.id.navigation_manual))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        //Si fue autocompletar carga el ID
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        edit = sharedPref.edit()

        GlobalData.idUser = sharedPref.getInt("idUser", 0)

        //Obtienen datos generales
        obtenerDevicesBD(this)
        obtenerDevicesComandoBD(this, pool)
        obtenerComidaBD(this)
        obtenerComandos(this)

    }

    @Override
    override fun onBackPressed() {
        val fragmentList: List<*> = supportFragmentManager.fragments

        var handled = false
        for (f in fragmentList) {
            if (f is EditCommand) {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.detach(f)
                transaction.addToBackStack(null)
                transaction.commit()
            }
        }

        if (!handled) {
            super.onBackPressed()
        }
    }

    private fun actionBar(context: Context){
        getSupportActionBar()?.setCustomView(R.layout.actionbar)

        val texto: EditText = actbar!!.customView.findViewById(R.id.idPiscina)
        val btn: ImageButton = actbar.customView.findViewById(R.id.btnBuscarPiscina)

        btn.setOnClickListener {
            pool = texto.text.toString().toInt()
            handler.postDelayed(Runnable {
                obtenerComandos(this)
                obtenerDevicesBD(this)
                obtenerDevicesComandoBD(this, pool)
                obtenerComidaBD(this)
            }, 100)

            handler.postDelayed(Runnable {
                resetFragment()
            }, 450)
        }

        actbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
    }

    fun resetFragment(){
        val id = navController.currentDestination?.id
        navController.popBackStack(id!!, false)
        navController.navigate(id)
    }

    fun verificarActualizacionBD(context: Context, piscina: Int, numDisp: Int){
        if (alerta || piscinaAux != piscina){
            alerta = false
            piscinaAux = piscina
            cargarDatosProgressBar()
            val task: TimerTask = object : TimerTask() {
                override fun run() {
                    obtenerDevicesComandoBD(context, piscina)

                    handler.postDelayed(Runnable {
                        if (deviceCom.enviarProgramacion == 2) {
                            Toast.makeText(context,
                                "Las maquinas se actualizaron en la piscina: $piscina",
                                Toast.LENGTH_LONG).show()
                            GlobalData.actualizar_enviarProgramacion(context, piscina, numDisp, 0)
                        } else {
                            Toast.makeText(context,
                                "Las maquinas NO se actualizaron en la piscina: $piscina",
                                Toast.LENGTH_LONG).show()
                        }
                    }, 850)
                    alerta = true;
                }
            }
            timer.schedule(task, 45 * 1000)
        }


    }

    fun cargarDatosProgressBar(){
        barraCarga.visibility = View.VISIBLE
        txtProgreso.visibility = View.VISIBLE

        barraCarga.setProgress(45)
        val time = 1 * 45 * 1000 // 1 minute in milli seconds

        var auxTiempo = 0
        val cdt = object : CountDownTimer(
            time.toLong(), 1000) {

            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                auxTiempo += 1
                val aux: Float = auxTiempo/45f*100
                aux.rem(2)
                txtProgreso.text = aux.roundToInt().toString() + "%"
                val total = (auxTiempo / 60 * 100) as Int
                barraCarga.setProgress(total)
            }

            override fun onFinish() {
                barraCarga.visibility = View.INVISIBLE
                txtProgreso.visibility = View.INVISIBLE
            }
        }.start()
    }
}


