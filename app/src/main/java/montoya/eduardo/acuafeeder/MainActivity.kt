package montoya.eduardo.acuafeeder

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import montoya.eduardo.acuafeeder.data_class.GlobalData
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.deviceCom
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.obtenerComandos
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.obtenerComidaBD
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.obtenerDevicesBD
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.obtenerDevicesComandoBD
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.obtenerTempBD
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.pool
import montoya.eduardo.acuafeeder.data_class.deviceCommand
import java.util.*


class MainActivity : AppCompatActivity() {
    private var alerta: Boolean = true
    private var piscinaAux: Int = pool
    val timer = Timer()
    lateinit var navController: NavController
    val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {

        actionBar(this)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        GlobalData.pool = 1

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
        val actbar = getSupportActionBar() as ActionBar
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
        navController.popBackStack(id!!,false)
        navController.navigate(id)
    }

    fun setRepeatingAsyncTask(context: Context) {
        val handler = Handler(Looper.getMainLooper())
        val timer = Timer()
        val task: TimerTask = object : TimerTask() {
            override fun run() {
                handler.post(Runnable {
                    try {
                        obtenerDevicesBD(context)
                        obtenerDevicesComandoBD(context, pool)
                        obtenerComidaBD(context)
                        obtenerComandos(context)

                        val id = navController.currentDestination?.id
                        navController.popBackStack(id!!,false)
                        Toast.makeText(context, "Sincro App", Toast.LENGTH_LONG).show()
                        navController.navigate(id)
                    } catch (e: Exception) {
                    }
                })
            }
        }
        timer.schedule(task, 45 * 1000, 45 * 1000) // interval of one minute
    }

    fun verificarActualizacionBD(context: Context, piscina: Int, numDisp: Int){
        if (alerta || piscinaAux != piscina){
            alerta = false
            piscinaAux = piscina
            val task: TimerTask = object : TimerTask() {
                override fun run() {
                    obtenerDevicesComandoBD(context, piscina)

                    handler.postDelayed(Runnable {
                        if (deviceCom.enviarProgramacion==2){
                            Toast.makeText(context,
                                "Las maquinas se actualizaron en la piscina: $piscina", Toast.LENGTH_LONG).show()
                            GlobalData.actualizar_enviarProgramacion(context, piscina, numDisp)
                        }
                        else{
                            Toast.makeText(context, "Las maquinas NO se actualizaron en la piscina: $piscina", Toast.LENGTH_LONG).show()
                        }
                    }, 450)
                    alerta = true;
                }
            }
            timer.schedule(task, 45 * 1000)
        }


    }
}


