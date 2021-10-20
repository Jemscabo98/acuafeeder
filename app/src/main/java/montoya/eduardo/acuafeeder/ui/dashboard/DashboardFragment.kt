package montoya.eduardo.acuafeeder.ui.dashboard

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.volley.*
import montoya.eduardo.acuafeeder.EditCommand
import montoya.eduardo.acuafeeder.MainActivity
import montoya.eduardo.acuafeeder.R
import montoya.eduardo.acuafeeder.data_class.Command
import montoya.eduardo.acuafeeder.data_class.GlobalData


class DashboardFragment : Fragment() {
    //val URL = "http://practicaspro.webhop.me:8080/acuafeeder/"
    val URL = "https://bytefruit.com/practicas-acuafeeder/php/"
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var queue: RequestQueue

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        var btnAdd: Button = root.findViewById(R.id.btnAdd)
        var btnMinus: Button = root.findViewById(R.id.btnMinus)
        var listview: ListView = root.findViewById(R.id.ScrollView)

        dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
            var adaptador: AdapterComando = AdapterComando(context, GlobalData.listaComandos)
            listview.adapter = adaptador

            btnAdd.setOnClickListener {
                val com: Command = Command(GlobalData.pool)
                GlobalData.listaComandos.add(com)
                adaptador = AdapterComando(context, GlobalData.listaComandos)
                listview.adapter = adaptador
            }

            btnMinus.setOnClickListener {
                GlobalData.listaComandos.removeLast()
                adaptador = AdapterComando(context, GlobalData.listaComandos)
                listview.adapter = adaptador
            }

        })
        return root
    }
}

class AdapterComando: BaseAdapter {
    var command = ArrayList<Command>()
    var context: Context? = null

    constructor(context: Context?, movies: ArrayList<Command>) : super() {
        this.command = movies
        this.context = context
    }


    override fun getCount(): Int {
        return command.size
    }

    override fun getItem(position: Int): Any {
        return command[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("SetTextI18n", "ViewHolder", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val com = command[position]
        val inflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val vista = inflater.inflate(R.layout.progamacion_view, null)

        val txtSComando: CheckBox = vista.findViewById(R.id.txtSComando)
        val txtPorcAlimento: TextView = vista.findViewById(R.id.txtPorcAlimento)
        val txtHoraInicio: TextView = vista.findViewById(R.id.txtHoraInicio)
        val txtHoraFinal: TextView = vista.findViewById(R.id.txtHoraFinal)
        val btnEditar: Button = vista.findViewById(R.id.btnEditar)

        txtSComando.isChecked = com.s != 0
        txtSComando.text = "S" + (position+1)
        txtPorcAlimento.text = "" + com.porcentajeAlimento
        
        var time = ""
        if (("" + com.horario_inicial_min.toString()).length == 1){
            time = "0" + com.horario_inicial_min.toString()
        }else
            time = "" + com.horario_inicial_min.toString()

        var time1 = ""
        if (("" + com.horario_final_min.toString()).length == 1){
            time1 = "0" + com.horario_final_min.toString()
        }else
            time1 = "" + com.horario_final_min.toString()

        txtHoraInicio.text = "" + com.horario_inicial_hr + ":" + time
        txtHoraFinal.text = "" + com.horario_final_hr + ":" + time1

        btnEditar.setOnClickListener {
            GlobalData.index = position
            val frag = EditCommand()
            val transaction = (context as MainActivity).getSupportFragmentManager().beginTransaction()
            transaction.replace(R.id.nav_host_fragment, frag)
            transaction.addToBackStack(null)
            transaction.commit()
        }
        return vista
    }

}