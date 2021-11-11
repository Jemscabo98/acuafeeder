package montoya.eduardo.acuafeeder

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import montoya.eduardo.acuafeeder.data_class.Command
import montoya.eduardo.acuafeeder.data_class.Devices
import montoya.eduardo.acuafeeder.data_class.GlobalData
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.obtenerDevicesBD
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.updateCommand
import org.json.JSONException
import org.json.JSONObject

class EditCommand : Fragment() {
    private lateinit var queue: RequestQueue
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var txtSComando: CheckBox
    private lateinit var txtPorcAlimento: TextView
    private lateinit var txtHoraInicio: TextView
    private lateinit var txtHoraFinal: TextView
    private lateinit var btnCancelar: Button
    private lateinit var btnGuardar: Button

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_edit_command, container, false)
        txtSComando = root.findViewById(R.id.checkS)
        txtPorcAlimento = root.findViewById(R.id.txtEditAlimento)
        txtHoraInicio= root.findViewById(R.id.txtEditHoraI)
        txtHoraFinal = root.findViewById(R.id.txtEditHoraF)
        btnCancelar = root.findViewById(R.id.btnCancelar)
        btnGuardar = root.findViewById(R.id.btnGuardar)

        val com = GlobalData.listaComandos.get(GlobalData.index)
        txtSComando.text = "S" + (GlobalData.index + 1)
        txtSComando.isChecked = com.s == 1
        txtPorcAlimento.text = com.porcentajeAlimento.toString()

        if (GlobalData.listaDevices.isEmpty())
            obtenerDevicesBD(requireContext())

        var time = ""
        if (("" + com.horario_inicial_min.toString()).length == 1) {
            time = "0" + com.horario_inicial_min.toString()
        } else
            time = "" + com.horario_inicial_min.toString()

        var time1 = ""
        if (("" + com.horario_final_min.toString()).length == 1) {
            time1 = "0" + com.horario_final_min.toString()
        } else
            time1 = "" + com.horario_final_min.toString()

        txtHoraInicio.text = "" + com.horario_inicial_hr + ":" + time
        txtHoraFinal.text = "" + com.horario_final_hr + ":" + time1

        btnCancelar.setOnClickListener {
            salirFragmento()
        }

        btnGuardar.setOnClickListener {
            if(verificarDatos(com)){
                val aux = GlobalData.index+1

                if (txtSComando.isChecked)
                    GlobalData.listaComandos.get(GlobalData.index).s = 1
                else
                    GlobalData.listaComandos.get(GlobalData.index).s = 0

                updateCommand(aux.toString(), GlobalData.listaComandos.get(GlobalData.index), requireContext())
                salirFragmento()
            }
        }

        return root
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun verificarDatos(com: Command): Boolean {
        val comAux: Command = Command(com.pool)
        if (!txtPorcAlimento.text.isDigitsOnly() || txtPorcAlimento.text.isEmpty()) {
            mostrarMensaje("Solo numeros enteros en porcentaje")
            return false
        }

        val regex: Regex = """([01]?[0-9]|2[0-3]):[0-5][0-9]""".toRegex()
        if (!regex.matches(txtHoraInicio.text) || !regex.matches(txtHoraFinal.text)) {
            mostrarMensaje( "Error en sintaxis de fechas")
            return false
        }

        val splitInit = txtHoraInicio.text.split(":").toTypedArray()
        comAux.horario_inicial_hr = splitInit[0].toInt()
        comAux.horario_inicial_min = splitInit[1].toInt()
        val splitEnd = txtHoraFinal.text.split(":").toTypedArray()
        comAux.horario_final_hr = splitEnd[0].toInt()
        comAux.horario_final_min = splitEnd[1].toInt()
        comAux.porcentajeAlimento = Integer.parseInt(txtPorcAlimento.text.toString())

        if (comAux.horario_final_hr < comAux.horario_inicial_hr){
            mostrarMensaje("La fecha Inicial no puede ser mayor que la fecha final")
            return false
        }

        if (comAux.horario_final_hr == comAux.horario_inicial_hr && comAux.horario_final_min <= comAux.horario_inicial_min){
            mostrarMensaje("La fecha Inicial no puede ser igual o mayor que la fecha final")
            return false
        }

        if (GlobalData.index == 0 && GlobalData.listaComandos.size >= 2){
            if (comAux.horario_final_hr == GlobalData.listaComandos[1].horario_inicial_hr ){
                if (comAux.horario_final_min >= GlobalData.listaComandos[1].horario_inicial_min){
                    mostrarMensaje("La fecha final no puede ser igual o mayor a " + GlobalData.listaComandos[1].horario_inicial_hr.toString() + ":" + GlobalData.listaComandos[1].horario_inicial_min.toString())
                    return false
                }
            }
            else if (comAux.horario_final_hr >= GlobalData.listaComandos[1].horario_inicial_hr ){
                mostrarMensaje("La fecha final no puede ser mayor a " + GlobalData.listaComandos[1].horario_inicial_hr.toString() + ":" + GlobalData.listaComandos[1].horario_inicial_min.toString())
                return false
            }
        }

        else if (GlobalData.index == GlobalData.listaComandos.size-1){
            if (comAux.horario_inicial_hr == GlobalData.listaComandos[GlobalData.index-1].horario_final_hr ){
                if (comAux.horario_inicial_min <= GlobalData.listaComandos[GlobalData.index-1].horario_final_min){
                    mostrarMensaje("La fecha inicial no puede ser igual o menor a " + GlobalData.listaComandos[GlobalData.index-1].horario_final_hr.toString() + ":" + GlobalData.listaComandos[GlobalData.index-1].horario_final_min.toString())
                    return false
                }
            }
            else if (comAux.horario_inicial_hr <= GlobalData.listaComandos[GlobalData.index-1].horario_final_hr ){
                mostrarMensaje("La fecha inicial no puede ser menor a " + GlobalData.listaComandos[GlobalData.index-1].horario_final_hr.toString() + ":" + GlobalData.listaComandos[GlobalData.index-1].horario_final_min.toString())
                return false
            }
        }

        else if (GlobalData.index > 0 ||GlobalData.index < GlobalData.listaComandos.size-1){
            if (comAux.horario_inicial_hr == GlobalData.listaComandos[GlobalData.index-1].horario_final_hr ){
                if (comAux.horario_inicial_min <= GlobalData.listaComandos[GlobalData.index-1].horario_final_min){
                    mostrarMensaje("La fecha inicial no puede ser igual o menor a " + GlobalData.listaComandos[GlobalData.index-1].horario_final_hr.toString() + ":" + GlobalData.listaComandos[GlobalData.index-1].horario_final_min.toString())
                    return false
                }
            }
            else if (comAux.horario_inicial_hr <= GlobalData.listaComandos[GlobalData.index-1].horario_final_hr ){
                mostrarMensaje("La fecha inicial no puede ser menor a " + GlobalData.listaComandos[GlobalData.index-1].horario_final_hr.toString() + ":" + GlobalData.listaComandos[GlobalData.index-1].horario_final_min.toString())
                return false
            }

            if (comAux.horario_final_hr == GlobalData.listaComandos[GlobalData.index+1].horario_inicial_hr ){
                if (comAux.horario_final_min >= GlobalData.listaComandos[GlobalData.index+1].horario_inicial_min){
                    mostrarMensaje("La fecha final no puede ser igual o mayor a " + GlobalData.listaComandos[GlobalData.index+1].horario_inicial_hr.toString() + ":" + GlobalData.listaComandos[GlobalData.index+1].horario_inicial_min.toString())
                    return false
                }
            }
            else if (comAux.horario_final_hr >= GlobalData.listaComandos[GlobalData.index+1].horario_inicial_hr ){
                mostrarMensaje("La fecha final no puede ser mayor a " + GlobalData.listaComandos[GlobalData.index+1].horario_inicial_hr.toString() + ":" + GlobalData.listaComandos[GlobalData.index+1].horario_inicial_min.toString())
                return false
            }
        }

        com.horario_inicial_hr = comAux.horario_inicial_hr
        com.horario_inicial_min = comAux.horario_inicial_min
        com.horario_final_hr = comAux.horario_final_hr
        com.horario_final_min = comAux.horario_final_min
        com.porcentajeAlimento = comAux.porcentajeAlimento
        GlobalData.listaComandos[GlobalData.index] = com
        return true
    }

    fun mostrarMensaje(msg: String){
        val toast = Toast.makeText(context, msg, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    fun salirFragmento(){
        val transaction =
            (context as MainActivity).getSupportFragmentManager().beginTransaction()
        transaction.detach(this)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}