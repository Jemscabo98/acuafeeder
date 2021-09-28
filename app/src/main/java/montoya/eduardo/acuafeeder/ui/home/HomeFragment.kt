package montoya.eduardo.acuafeeder.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import montoya.eduardo.acuafeeder.R
import kotlin.math.roundToInt

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {


            var graph: GraphView = root.findViewById(R.id.graph) as GraphView
            val series = BarGraphSeries(arrayOf(
                DataPoint(0.toDouble(), 0.0),
                DataPoint(1.0, 5.2.roundToInt().toDouble()),
                DataPoint(2.0, 3.7.roundToInt().toDouble()),
                DataPoint(3.0, 5.9.roundToInt().toDouble()),
                DataPoint(4.0, 1.63.roundToInt().toDouble()),
                DataPoint(5.0, 3.4.roundToInt().toDouble()),
                DataPoint(6.0, 5.6.roundToInt().toDouble()),
                DataPoint(7.0, 1.1.roundToInt().toDouble())
            ));
            series.setSpacing(20)
            series.setDrawValuesOnTop(true);
            series.setValuesOnTopColor(Color.WHITE);
            graph.addSeries(series)

        })
        return root
    }


}