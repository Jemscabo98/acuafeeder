package montoya.eduardo.acuafeeder.data_class

import java.io.Serializable

data class Devices(var devices_etiqueta: String, var pool: Int) : Serializable{
                    var idDevices: String = ""
                    var userID: Int = 0
                   }