package montoya.eduardo.acuafeeder

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.sql.Time
import java.text.SimpleDateFormat


class InformationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_informacion, container, false)
        var graph: GraphView = root.findViewById(R.id.graph) as GraphView
        var sdf: SimpleDateFormat = SimpleDateFormat("hh:mm")


        val lineaTemp = LineGraphSeries(getDataBD())
        lineaTemp.color = Color.BLACK

        val lineaAlimento = LineGraphSeries(getDataPoint1())
        lineaAlimento.color = Color.RED

        graph.addSeries(lineaTemp)
        graph.addSeries(lineaAlimento)

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


fun getDataPoint1(): Array<DataPoint> {
    var tiempo: Time = Time.valueOf("10:18:41")
    var tiempo2: Time = Time.valueOf("10:20:52")
    var tiempo3: Time = Time.valueOf("10:22:11")
    var tiempo4: Time = Time.valueOf("10:24:51")
    var tiempo5: Time = Time.valueOf("10:26:05")

    var arrayListLineaTemp: ArrayList<DataPoint> = ArrayList()
    arrayListLineaTemp.add(DataPoint(tiempo.time.toDouble(), 49.5))
    arrayListLineaTemp.add(DataPoint(tiempo2.time.toDouble(), 45.5))
    arrayListLineaTemp.add(DataPoint(tiempo3.time.toDouble(), 48.5))
    arrayListLineaTemp.add(DataPoint(tiempo4.time.toDouble(), 44.5))
    arrayListLineaTemp.add(DataPoint(tiempo5.time.toDouble(), 49.5))

    var arrayLineaTemp: Array<DataPoint> = arrayListLineaTemp.toTypedArray()
    return arrayLineaTemp
}

fun getDataPoint2(): Array<DataPoint> {
    return arrayOf(
        DataPoint(0.0, 0.0),
        DataPoint(1.0, 4.8),
        DataPoint(2.0, 5.2),
        DataPoint(3.0, 5.0),
        DataPoint(4.0, 6.8)
    )
}
