package montoya.eduardo.acuafeeder.ui.dashboard

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.volley.*
import montoya.eduardo.acuafeeder.EditCommand
import montoya.eduardo.acuafeeder.MainActivity
import montoya.eduardo.acuafeeder.R
import montoya.eduardo.acuafeeder.data_class.*
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.AddCommand
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.DeleteCommand
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.MainAct
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.actualizar_enviarProgramacion
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.alerta
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.listaComandos
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.listaComandosOriginal
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.listaDevices
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.obtenerComidaBD
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.obtenerDevicesBD
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.obtenerDevicesComandoBD
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.pool
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.updateComida
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.updateCommand
import java.util.logging.Logger


class DashboardFragment : Fragment() {
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var queue: RequestQueue
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var selectComida: Spinner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val btnAdd: Button = root.findViewById(R.id.btnAdd)
        val btnMinus: Button = root.findViewById(R.id.btnMinus)
        val btnAct: Button = root.findViewById(R.id.btnActualizarComida)
        val listview: ListView = root.findViewById(R.id.ScrollView)
        val txtAlimento: TextView = root.findViewById(R.id.txtAlimento)
        selectComida = root.findViewById(R.id.selectComida)

        dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
            var adaptador: AdapterComando = AdapterComando(context, GlobalData.listaComandos)
            listview.adapter = adaptador

            if (GlobalData.deviceCom.grPorSegundo == 0 || GlobalData.deviceCom.pool != GlobalData.pool){
                obtenerDevicesComandoBD(requireContext(), pool)
            }


            if (GlobalData.listaComida.isEmpty() || GlobalData.listaComida.get(0).pool != GlobalData.pool){
                GlobalData.listaComida = ArrayList()
                obtenerComidaBD(requireContext())

                handler.postDelayed(Runnable {
                    fillSpinner(selectComida, context)

                    for ((contAux, x) in GlobalData.listaComida.withIndex()){
                        txtAlimento.text = GlobalData.deviceCom.alimentoTotal.toString()
                        if (GlobalData.deviceCom.grPorSegundo == x.allimentoGrSeg)
                            selectComida.setSelection(contAux)
                    }
                }, 350)
            }else{
                fillSpinner(selectComida, context)

                for ((contAux, x) in GlobalData.listaComida.withIndex()){
                    txtAlimento.text = GlobalData.deviceCom.alimentoTotal.toString()
                    if (GlobalData.deviceCom.grPorSegundo == x.allimentoGrSeg)
                        selectComida.setSelection(contAux)
                }
            }

            btnAdd.setOnClickListener {
                val com: Command = Command(GlobalData.pool)
                if (AddList(com)) {
                    //AddCommand(GlobalData.listaComandos.size.toString(), com, requireContext())
                }
                adaptador = AdapterComando(context, GlobalData.listaComandos)
                listview.adapter = adaptador
            }

            btnMinus.setOnClickListener {
                if (GlobalData.listaComandos.size ==1){
                    Toast.makeText(requireContext(), "Se necesita minimo 1 comando", Toast.LENGTH_LONG).show()
                }else {

                    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                    builder.setCancelable(true)
                    builder.setTitle("Confirmación")
                    builder.setMessage("¿Seguro que desea eliminar el ultimo comando?")
                    builder.setPositiveButton("Confirmar",
                        DialogInterface.OnClickListener { dialog, which ->
                            //DeleteCommand(GlobalData.listaComandos.size.toString(), requireContext())
                            GlobalData.listaComandos.removeLast()
                            adaptador = AdapterComando(context, GlobalData.listaComandos)
                            listview.adapter = adaptador
                        })
                    builder.setNegativeButton("Cancelar",
                        DialogInterface.OnClickListener { dialog, which -> })

                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                }
            }

            btnAct.setOnClickListener {
                GlobalData.deviceCom.alimentoTotal = txtAlimento.text.toString().toInt()
                GlobalData.deviceCom.grPorSegundo = GlobalData.selectFood.allimentoGrSeg
                GlobalData.deviceCom.enviarProgramacion = 1

                if (alerta) {
                    mandarDatosBD(requireContext())
                    updateComida(GlobalData.deviceCom, requireContext())
                    actualizar_enviarProgramacion(requireContext(), pool, listaDevices.size,1)
                    MainAct.verificarActualizacionBD(requireContext(), pool, listaDevices.size)
                }else{
                    Toast.makeText(context, "En espera de comprobacion", Toast.LENGTH_SHORT).show()
                }
            }

            selectComida.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    GlobalData.selectFood = GlobalData.selectFood
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (parent != null) {
                        (parent.getChildAt(0) as TextView).setTextColor(Color.WHITE)
                    }

                    for (x in GlobalData.listaComida){
                        if(x.idFood == selectComida.selectedItem.toString()){
                            GlobalData.selectFood = x
                        }
                    }
                }

            }


        })
        return root
    }

    fun AddList(com: Command): Boolean{
        var comAUX = GlobalData.listaComandos.get(GlobalData.listaComandos.size - 1)
        if (comAUX.horario_final_hr > 22){
            Toast.makeText(context, "Limite Alcanzado", Toast.LENGTH_SHORT).show()
            return false
        }

        else{
            com.horario_inicial_hr = comAUX.horario_final_hr+1
            com.horario_final_hr = comAUX.horario_final_hr+1
            com.horario_final_min = 59
            com.porcentajeAlimento = 20
            com.s = 1

            GlobalData.listaComandos.add(com)
        }
        return  true
    }

    fun fillSpinner(selectDevice: Spinner, context: Context?){
        if (context!=null){
            val listaAux: ArrayList<String> = ArrayList()
            for (x in GlobalData.listaComida) {
                listaAux.add(x.idFood)
            }

            val lista = ArrayAdapter(context, android.R.layout.simple_spinner_item, listaAux)
            lista.setDropDownViewResource(R.layout.spinner_dropdown_item)
            selectDevice.adapter = lista
            try {
                GlobalData.selectFood = GlobalData.listaComida[0]
            } catch (e: java.lang.IndexOutOfBoundsException) {

            }
        }
    }

    fun mandarDatosBD(context: Context){
        if (listaComandos.size >= listaComandosOriginal.size){

            val listaAux = listaComandos.subList(listaComandosOriginal.size, listaComandos.size)

            var cont = listaComandos.size
            for (x in listaAux){
                AddCommand(cont.toString(), x, requireContext())
                cont++
            }

        }

        else if (listaComandos.size <= listaComandosOriginal.size){

            val listaAux = listaComandosOriginal.subList(listaComandos.size, listaComandosOriginal.size)

            var cont = listaComandosOriginal.size
            for (x in listaAux){
                DeleteCommand(cont.toString(), requireContext())
                cont--
            }
        }

        var aux = 1
        for(x in listaComandos){
            updateCommand(aux.toString(), x, requireContext())
            aux++
        }
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
        val vista = inflater.inflate(R.layout.programacion_view, null)

        val txtSComando: CheckBox = vista.findViewById(R.id.txtSComando)
        val txtPorcAlimento: TextView = vista.findViewById(R.id.txtPorcAlimento)
        val txtHoraInicio: TextView = vista.findViewById(R.id.txtHoraInicio)
        val txtHoraFinal: TextView = vista.findViewById(R.id.txtHoraFinal)
        val btnEditar: Button = vista.findViewById(R.id.btnEditar)

        txtSComando.isChecked = com.s == 1

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