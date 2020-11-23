package com.example.sensor.main

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.example.sensor.App
import com.example.sensor.R
import com.github.anastr.speedviewlib.SpeedView
import com.github.anastr.speedviewlib.components.Section
import com.github.anastr.speedviewlib.components.Style
import com.github.anastr.speedviewlib.components.indicators.Indicator
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.android.synthetic.main.fragment_sub.*
import java.util.*


class SubFragment : Fragment() {
    lateinit var activity : Main2Activity
    lateinit var resultView : TextView
    lateinit var resultSubView : TextView
    lateinit var resultTitle : TextView
    lateinit var unitView : TextView
    lateinit var backgroundLayout : FrameLayout


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = getActivity() as Main2Activity
        activity.main_btn_layout.visibility = View.VISIBLE

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

        resultView = view.findViewById(R.id.connect_result)
        resultSubView = view.findViewById(R.id.connect_sub_result)
        resultTitle = view.findViewById(R.id.connect_title)
        unitView = view.findViewById(R.id.unit_view)
        backgroundLayout = view.findViewById(R.id.fragment_sub_background_layout)


        activity.setSensorParameter()


        // resultView Click
        resultView.setOnClickListener {
            if (resultView.text == resources.getText(R.string.startText)){
                activity.startMeasure()
            }
        }
        return view
    }

    fun setResultViewer(unit: String, measureMax: Float, minV : Float, maxV: Float){
        setResultTitle(App.prefs.sensor)

    }

    fun setResult(value: String){
        resultView.text= value
    }

    fun setResultSub(value: String){
        resultSubView.text = value
    }

    fun setResultTitle(value: String){
        resultTitle.text = value
    }

    fun setUnit(value : String){
        unitView.text = value
    }

    fun changeBackground(check : Boolean){
        if (check){
            backgroundLayout.setBackgroundColor(resources.getColor(R.color.contentBodyColor))
        }
        else {
            backgroundLayout.setBackgroundColor(resources.getColor(R.color.customRed))

        }
    }

}

