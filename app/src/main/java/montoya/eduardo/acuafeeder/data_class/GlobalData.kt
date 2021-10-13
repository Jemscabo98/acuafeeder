package montoya.eduardo.acuafeeder.data_class

import android.app.Application

open class GlobalData: Application() {
   companion object{
       val URL: String = "https://bytefruit.com/practicas-acuafeeder/php/"
       var pool: Int = 0
       var listaComandos: ArrayList<Command> = ArrayList()

       var listaTemp: ArrayList<temp> = ArrayList()
       var fechaTemp: String = ""
       var listaDevices: ArrayList<String> = ArrayList()
   }

    override fun onCreate() {
        super.onCreate()
    }


}