package montoya.eduardo.acuafeeder

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import montoya.eduardo.acuafeeder.data_class.GlobalData
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.obtenerComandos
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.obtenerComidaBD
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.obtenerDevicesBD
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.obtenerDevicesComandoBD
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.obtenerTempBD
import java.util.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        GlobalData.pool = 1

        val navController = findNavController(R.id.nav_host_fragment)
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
        obtenerDevicesComandoBD(this)
        obtenerComidaBD(this)
        obtenerTempBD(this)
        obtenerComandos(this)

        setRepeatingAsyncTask(this)
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

    private fun setRepeatingAsyncTask(context: Context) {
        val handler = Handler(Looper.getMainLooper())
        val timer = Timer()
        val task: TimerTask = object : TimerTask() {
            override fun run() {
                handler.post(Runnable {
                    try {
                        obtenerDevicesBD(context)
                        obtenerDevicesComandoBD(context)
                        obtenerComidaBD(context)
                        obtenerTempBD(context)
                        obtenerComandos(context)

                        Toast.makeText(context, "Cada miunto", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {


                    }
                })
            }
        }
        timer.schedule(task, 0, 60 * 1000) // interval of one minute
    }
}


