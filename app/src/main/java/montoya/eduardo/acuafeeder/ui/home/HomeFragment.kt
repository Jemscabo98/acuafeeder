package montoya.eduardo.acuafeeder.ui.home

import android.graphics.Color
import android.os.*
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.android.volley.RequestQueue
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.StaticLabelsFormatter
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import montoya.eduardo.acuafeeder.MainActivity
import montoya.eduardo.acuafeeder.R
import montoya.eduardo.acuafeeder.data_class.GlobalData
import montoya.eduardo.acuafeeder.data_class.GlobalData.Companion.obtenerComandos


class HomeFragment : Fragment() {
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var queue: RequestQueue
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            val graph: GraphView = root.findViewById(R.id.graph) as GraphView

            GlobalData.MainAct = activity as MainActivity

            val aux = activity as MainActivity
            val actbar = aux.getSupportActionBar() as ActionBar
            val texto: EditText = actbar.customView.findViewById(R.id.idPiscina)

            if (GlobalData.listaComandos.isEmpty()){
                GlobalData.listaComandos = ArrayList()
                //Obtiene los datos de la BD
                obtenerComandos(requireContext())

                handler.postDelayed(Runnable {
                    cargarDatos(graph)
                },500)
            }
            if (GlobalData.listaComandos.isNotEmpty()){
                cargarDatos(graph)
            }

        })
        return root
    }


    //Verifica que el numero de psicina sea numerico
    fun checarDato(txtNumPiscina: EditText): Boolean{
        val respuesta: Boolean
        val texto = txtNumPiscina.text

        respuesta = TextUtils.isDigitsOnly(texto)

        return respuesta
    }

    fun cargarDatos(graph: GraphView){
        try {
            // Limpi la grafica cada vez que se busque datos
            graph.removeAllSeries()
            // Tipo de grafica /barra/
            val series = BarGraphSeries(getDataPoint())
            //Valores de la grafica
            series.spacing = 20
            series.isDrawValuesOnTop = true
            series.valuesOnTopColor = Color.WHITE
            graph.viewport.isYAxisBoundsManual = true
            graph.viewport.setMinY(0.0)
            graph.addSeries(series)

            //Se agrega los tag (hora) en la axis x
            val labels = ArrayList<String>()
            var maxValue: Double = 0.0

            for (x in GlobalData.listaComandos) {

                if (x.porcentajeAlimento > maxValue)
                    maxValue = x.porcentajeAlimento.toDouble()

                if (x.horario_inicial_min.toString().length != 2) {
                    labels.add("" + x.horario_inicial_hr + ":0" + x.horario_inicial_min)
                } else {
                    labels.add("" + x.horario_inicial_hr + ":" + x.horario_inicial_min)
                }
            }

            maxValue += 20.0
            graph.viewport.setMaxY(maxValue)
            var stockArr = arrayOfNulls<String>(labels.size)
            stockArr = labels.toArray(stockArr)
            val labelX: StaticLabelsFormatter = StaticLabelsFormatter(graph)
            labelX.setHorizontalLabels(stockArr)
            graph.gridLabelRenderer.labelFormatter = labelX
        } catch (ex: Exception) {

        }
    }

    fun getDataPoint(): Array<DataPoint> {
        val arrayListLineaTemp: ArrayList<DataPoint> = ArrayList()

        var aux: Int = 1
        for (x in GlobalData.listaComandos){
            arrayListLineaTemp.add(DataPoint(aux.toDouble(), x.porcentajeAlimento.toDouble()))
            aux++
        }

        val arrayLineaTemp: Array<DataPoint> = arrayListLineaTemp.toTypedArray()
        return arrayLineaTemp
    }
}

