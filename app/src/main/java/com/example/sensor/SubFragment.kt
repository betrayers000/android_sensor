package com.example.sensor

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.anastr.speedviewlib.SpeedView
import com.github.anastr.speedviewlib.components.Section
import com.github.anastr.speedviewlib.components.Style
import com.github.anastr.speedviewlib.components.indicators.Indicator
import kotlinx.android.synthetic.main.fragment_sub.*
import java.util.*


class SubFragment : Fragment() {
    lateinit var activity : Main2Activity
    lateinit var resultView : SpeedView
    lateinit var resultTempView : TextView

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = getActivity() as Main2Activity

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sub, container, false)

        resultView = view.findViewById(R.id.fragment_result_viewer)
        resultTempView = view.findViewById(R.id.fragment_result_viewer_tmp)
        activity.setSensorParameter()
        return view
    }

    fun setResultViewer(unit : String, measureMax: Float, minV : Float, maxV : Float){
        resultView.maxSpeed = measureMax
        val minSection = Section(0f, minV, Color.RED, resultView.speedometerWidth, Style.BUTT)
        val middleSection = Section(minV, maxV, Color.BLUE, resultView.speedometerWidth, Style.BUTT)
        resultView.clearSections()
        val maxSection = Section(maxV, 1f, Color.RED, resultView.speedometerWidth, Style.BUTT)
        if (minV == 0f){
            if (maxV == 1.0f){
                resultView.addSections(middleSection)
            } else {
                resultView.addSections(middleSection, maxSection)
            }
        } else {
            resultView.addSections(minSection, middleSection, maxSection)
        }
        resultView.unit = unit
        resultView.tickNumber = 4
        resultView.setIndicator(Indicator.Indicators.SpindleIndicator)
        resultView.ticks = arrayListOf(0f, minV, maxV, 1f)
    }


    fun setResult(value: Float){
        fragment_result_viewer.speedTo(value)
    }

    fun setResultTemp(value: String){
        resultTempView.text = value
    }

    fun visibilityResultTemp(check : Boolean){
        if (check){
            resultTempView.visibility = View.GONE
        } else {
            resultTempView.visibility = View.VISIBLE
        }
    }

    fun setCo2Viewer(){
        resultTempView.visibility = View.VISIBLE
        resultTempView.text = "percent"
        resultView.onPrintTickLabel = {
                tickPosition: Int, tick: Float ->
            if (tick >= 1000) {
                String.format(Locale.getDefault(), "%.1f", tick/10000)
            }
            else {
                null
            }
        }
    }
}

