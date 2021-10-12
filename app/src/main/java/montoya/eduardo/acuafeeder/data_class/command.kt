package montoya.eduardo.acuafeeder.data_class

import android.os.Parcelable
import java.io.Serializable

data class Command (var pool: Int): Serializable{
    var ID: Int = 0
    var s: Int = 0
    var etapa: Int = 0
    var horario_inicial_hr: Int = 0
    var horario_inicial_min: Int = 0
    var horario_final_hr: Int = 0
    var horario_final_min: Int = 0
    var porcentajeAlimento: Int = 0

}