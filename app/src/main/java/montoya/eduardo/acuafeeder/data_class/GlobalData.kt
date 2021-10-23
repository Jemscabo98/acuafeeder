package montoya.eduardo.acuafeeder.data_class

import android.app.Application
import com.jjoe64.graphview.series.DataPoint
import java.util.*
import kotlin.collections.ArrayList

open class GlobalData: Application() {
   companion object{
       val URL: String = "https://bytefruit.com/practicas-acuafeeder/php/"
       //val URL = "http://192.168.1.111:8080/acuafeeder/"
       var pool: Int = 0
       var listaComandos: ArrayList<Command> = ArrayList()

       var listaTemp: ArrayList<temp> = ArrayList()
       var fechaTemp: String = ""
       var listaDevices: ArrayList<Devices> = ArrayList()
       var deviceTemp: String = ""

       var index: Int = 0
   }

    override fun onCreate() {
        super.onCreate()
    }


}