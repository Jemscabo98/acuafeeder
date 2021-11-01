package montoya.eduardo.acuafeeder.data_class

import java.io.Serializable
import java.sql.Timestamp

data class Devices(var devices_etiqueta: String, var pool: Int) : Serializable{
                    var idDevices: String = ""
                    var userID: Int = 0
                    var fechaCreacion: Timestamp = Timestamp(System.currentTimeMillis())
                   }