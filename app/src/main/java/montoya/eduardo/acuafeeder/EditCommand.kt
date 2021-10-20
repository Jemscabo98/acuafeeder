package montoya.eduardo.acuafeeder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.Fragment
import montoya.eduardo.acuafeeder.data_class.GlobalData

class EditCommand : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_edit_command, container, false)

        val txtSComando: CheckBox = root.findViewById(R.id.checkS)
        val txtPorcAlimento: TextView = root.findViewById(R.id.txtEditAlimento)
        val txtHoraInicio: TextView = root.findViewById(R.id.txtEditHoraI)
        val txtHoraFinal: TextView = root.findViewById(R.id.txtEditHoraF)
        val btnCancelar: Button = root.findViewById(R.id.btnCancelar)
        val btnGuardar: Button = root.findViewById(R.id.btnGuardar)

        val com = GlobalData.listaComandos.get(GlobalData.index)
        txtSComando.text = "S" + (GlobalData.index + 1)
        txtSComando.isChecked = com.s != 0
        txtPorcAlimento.text = com.porcentajeAlimento.toString()

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
            val transaction =
                (context as MainActivity).getSupportFragmentManager().beginTransaction()
            transaction.detach(this)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        return root
    }
}