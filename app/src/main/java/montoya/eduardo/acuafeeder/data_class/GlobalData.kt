package montoya.eduardo.acuafeeder.data_class

import android.app.Application

open class GlobalData: Application() {
   companion object{
       var pool = 0
       var listaComandos: ArrayList<Command> = ArrayList()
   }

    override fun onCreate() {
        super.onCreate()
    }
}