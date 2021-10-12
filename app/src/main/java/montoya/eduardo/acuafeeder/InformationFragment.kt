package montoya.eduardo.acuafeeder

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class InformationFragment : Fragment() {

    private lateinit var mDateSetListener: DatePickerDialog.OnDateSetListener

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_informacion, container, false)
        val graph: GraphView = root.findViewById(R.id.graph) as GraphView
        val selecFecha: TextView = root.findViewById(R.id.selFechaTemp)
        val fechaTemp: TextView = root.findViewById(R.id.fechaTemp)
        val sdf: SimpleDateFormat = SimpleDateFormat("hh:mm")

        fechaTemp.text = arguments?.getInt("pool").toString()

        selecFecha.setOnClickListener {
            //Variables
            val cal: Calendar = Calendar.getInstance() //Objeto Calendario
            val anou = cal.get(Calendar.YEAR) //Donde se guarda el año
            val mes = cal.get(Calendar.MONTH) //Donde se guarda el mes
            val dia = cal.get(Calendar.DAY_OF_MONTH) //Donde se guarda el dia
            val colorAux: ColorDrawable = ColorDrawable(Color.TRANSPARENT)

            mDateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                Log.d(TAG, "onDateSet: date: $year/$month/$dayOfMonth")
                fechaTemp.text = "$year/$month/$dayOfMonth"




            }

            val selecFechaAux: DatePickerDialog = DatePickerDialog(
                this.requireContext(),
                android.R.style.Theme_Holo_Light_Dialog,
                mDateSetListener,
                anou, mes, dia)
            selecFechaAux.window?.setBackgroundDrawable(colorAux)
            selecFechaAux.show()
        }


        val lineaTemp = LineGraphSeries(getDataBD())
        lineaTemp.color = Color.RED

        graph.addSeries(lineaTemp)

        graph.getGridLabelRenderer().setLabelFormatter(object : DefaultLabelFormatter() {
            override fun formatLabel(value: Double, isValueX: Boolean): String {
                if (isValueX) {
                    return sdf.format(value)
                }
                return super.formatLabel(value, isValueX)
            }
        })

        return root
    }



}

fun getDataBD(): Array<DataPoint> {/*
    val info = informacionDAO()
    var listaTiempos: ArrayList<Time> = ArrayList()
     listaTiempos = info.getTime()*/

    var arrayListLineaTemp: ArrayList<DataPoint> = ArrayList()
    var tiempo: Time = Time.valueOf("10:18:41")
    var tiempo2: Time = Time.valueOf("10:20:52")
    var tiempo3: Time = Time.valueOf("10:22:11")
    var tiempo4: Time = Time.valueOf("10:24:51")
    var tiempo5: Time = Time.valueOf("10:26:05")
    arrayListLineaTemp.add(DataPoint(tiempo.time.toDouble(), 49.5))
    arrayListLineaTemp.add(DataPoint(tiempo2.time.toDouble(), 45.5))
    arrayListLineaTemp.add(DataPoint(tiempo3.time.toDouble(), 48.5))
    arrayListLineaTemp.add(DataPoint(tiempo4.time.toDouble(), 44.5))
    arrayListLineaTemp.add(DataPoint(tiempo5.time.toDouble(), 49.5))


    var arrayLineaTemp: Array<DataPoint> = arrayListLineaTemp.toTypedArray()
    return arrayLineaTemp
}
