package com.example.sensor.main

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.sensor.App
import com.example.sensor.R
import com.example.sensor.setting.SettingActivity
import com.example.sensor.utils.SerialCommunication
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.activity_main2.*

class Main2Activity : AppCompatActivity() {

    // Fragment List
    lateinit var mainFragment: MainFragment
    lateinit var subFragment: SubFragment
    lateinit var errorFragment: ErrorFragment

    // Host Mode
    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"

    // Usb Connect
    lateinit var manager : UsbManager
    var connect = false
    var loopChk = true
    lateinit var scManager: SerialCommunication
    private val SENSOR_MESSAGE = "sensorMessage"

    // Measure Thread
    lateinit var thread : ThreadClass

    // Connected Sensor Setting
    var minVal : Float? = null
    var maxVal : Float? = null
    var measureMax : Float? = null
    var unit : String = "ppm"
    var type : Int  = 0
    var decimal : Int = 0

    // Alarm Time
    private var defaultTime : Long = 1597932005417
    private var blockTime : Int = 600000

    // Intent Filter
    val filter = IntentFilter(ACTION_USB_PERMISSION)

    // alertdialog
    lateinit var dialog : AlertDialog

    @ExperimentalUnsignedTypes
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        // actionbar 색 변경
//        val actionBar = supportActionBar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)

        mainFragment = MainFragment()
        subFragment = SubFragment()
        errorFragment = ErrorFragment()


        onFragmentChange(1)
        // button event
        // start toggle button
//        measure_toggle_btn.setOnCheckedChangeListener { buttonView, isChecked ->
//            Log.d("Main", "click toggle")
//            if(!connect){
//                measure_toggle_btn.isChecked = false
//            } else {
//                if(isChecked){
//                    loopChk = true
//                    thread.start()
//                } else {
//                    loopChk = false
//                    ringOff()
//                }
//            }
//        }

        // measure stop
        main_btn_measure_stop.setOnClickListener{
            stopMeasure()
        }

        // alarm stop
        main_btn_alarm_stop.setOnClickListener{
            ringOff(1)
        }

        setChart()


    }

    /**
     * chart basic set
     */
    private fun setChart(){
        val xAxis = main_chart_measure.xAxis

        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            textSize = 10f
            setDrawGridLines(false)
            granularity = 1f
            axisMinimum = 2f
            isGranularityEnabled = true
        }

        main_chart_measure.apply{
            axisRight.isEnabled = false // y축의 오른쪽 데이터 비활성화
            axisLeft.mAxisMaximum = 50f // y축 왼쪽 데이터 최대값은 50
        }
        val lineData = LineData()
        main_chart_measure.data = lineData
    }

    private fun addEntry(value : Float){
        val data = main_chart_measure.data
        data?.let{
            var set = data.getDataSetByIndex(0)
            if (set==null){
                set = createSet()
                data.addDataSet(set)
            }
            data.addEntry(Entry(set.entryCount.toFloat(), value), 0)
            data.notifyDataChanged()
            main_chart_measure.apply {
                notifyDataSetChanged()
                moveViewToX(data.entryCount.toFloat())
                setVisibleXRangeMaximum(4f)
                setPinchZoom(false)
                isDoubleTapToZoomEnabled =false
                setBackgroundColor(resources.getColor(R.color.contentBodyColor))
                setExtraOffsets(8f, 16f, 8f, 16f)
            }
        }
    }

    private fun createSet(): LineDataSet{
        val set = LineDataSet(null, App.prefs.sensor)
        set.apply {
            axisDependency = YAxis.AxisDependency.LEFT
            color = resources.getColor(R.color.red)
            setCircleColor(resources.getColor(R.color.buttonDarkRed))
            valueTextSize = 10f
            fillAlpha = 0
            fillColor = resources.getColor(R.color.red)
            setDrawValues(true)
        }
        return set
    }

    fun initManager(){
        manager = getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList : HashMap<String, UsbDevice> = manager.deviceList
        deviceList.values.forEach { d ->
            val permissionIntent =
                PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
            manager.requestPermission(d, permissionIntent)
        }
    }

    /**
     * 결과창 각 센서에 맞게 셋팅하는 과정
     */
    fun setSensorParameter(){
        when(App.prefs.sensor){
            resources.getStringArray(R.array.sensor_lists)[0] -> {
                minVal = App.prefs.min_o2
                maxVal = App.prefs.max_o2
                measureMax = 25.0f
            }
            resources.getStringArray(R.array.sensor_lists)[1] -> {
                minVal = App.prefs.min_co2
                maxVal = 30000f
                measureMax = 200000f
            }
            resources.getStringArray(R.array.sensor_lists)[2] -> {
                minVal = App.prefs.min_co
                maxVal = App.prefs.max_co
                measureMax = 1000.0f
            }
            resources.getStringArray(R.array.sensor_lists)[3] -> {
                minVal = App.prefs.min_no2
                maxVal = App.prefs.max_no2
                measureMax = 100.0f
            }
            resources.getStringArray(R.array.sensor_lists)[4] -> {
                minVal = App.prefs.min_so2
                maxVal = App.prefs.max_so2
                measureMax = 100.0f
            }
            resources.getStringArray(R.array.sensor_lists)[5] -> {
                minVal = App.prefs.min_h2s
                maxVal = App.prefs.max_h2s
                measureMax = 100.0f
            }
            resources.getStringArray(R.array.sensor_lists)[6] -> {
                minVal = App.prefs.min_hcho
                maxVal = App.prefs.max_hcho
                measureMax = 1000.0f
            }
        }
        val minV = minVal!!/measureMax!!
        val maxV = maxVal!!/measureMax!!
        subFragment.setResultViewer(unit, measureMax!!, minV, maxV)

    }

    fun onFragmentChange(fragmentNum : Int){
        Log.d("MainActivity", unit)
        Log.d("MainActivity", type.toString())
        Log.d("MainActivity", decimal.toString())
        if (fragmentNum == 1){
            supportFragmentManager.beginTransaction().replace(R.id.result_viewer_frame, mainFragment).commitAllowingStateLoss()
        } else if (fragmentNum == 2){
            supportFragmentManager.beginTransaction().replace(R.id.result_viewer_frame, subFragment).commitAllowingStateLoss()
        } else if (fragmentNum == 3){
            supportFragmentManager.beginTransaction().replace(R.id.result_viewer_frame, errorFragment).commitAllowingStateLoss()
        }
    }

    /**
     * Create ByteArray
     */
    fun byteArrayOfInt(vararg ints : Int) = ByteArray(ints.size) {pos -> ints[pos].toByte()}

    /**
     * TB sensor check
     */
    @ExperimentalUnsignedTypes
    fun tbSensorCheck(type : String): Boolean{
        val command = byteArrayOfInt(0xD1)
        val serialCommunication =
            SerialCommunication(
                manager,
                0,
                9600,
                8,
                1,
                0
            )
        val result = serialCommunication.write(command)
        if (!result.equals(type)){
            return false
        }
        return true

    }

    /**
     * sensor type getter
     */
    fun getSensorType() : String {
        when(App.prefs.sensor){
            resources.getStringArray(R.array.sensor_lists)[0] -> {
                return resources.getStringArray(R.array.sensor_type)[0]
            }
            resources.getStringArray(R.array.sensor_lists)[1] -> {
                return resources.getStringArray(R.array.sensor_type)[1]
            }
            resources.getStringArray(R.array.sensor_lists)[2] -> {
                return resources.getStringArray(R.array.sensor_type)[2]
            }
            resources.getStringArray(R.array.sensor_lists)[3] -> {
                return resources.getStringArray(R.array.sensor_type)[3]
            }
            resources.getStringArray(R.array.sensor_lists)[4] -> {
                return resources.getStringArray(R.array.sensor_type)[4]
            }
            resources.getStringArray(R.array.sensor_lists)[5] -> {
                return resources.getStringArray(R.array.sensor_type)[5]
            }
            resources.getStringArray(R.array.sensor_lists)[6] -> {
                return resources.getStringArray(R.array.sensor_type)[6]
            }
        }
        return ""
    }


    // 액션바에 설정버튼 추가
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }
    /**
     * 설정버튼 이벤트
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.setting_btn -> {
                val settingIntent = Intent(this, SettingActivity::class.java)
                startActivity(settingIntent)
                stopMeasure()
                finish()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }


    inner class ThreadClass() : Thread(){

        @ExperimentalUnsignedTypes
        override fun run() {
            when(App.prefs.sensor){
                resources.getStringArray(R.array.sensor_lists)[0] -> {
                    o2Sensor()
                }
                resources.getStringArray(R.array.sensor_lists)[1] -> {
                    co2Sensor()
                }
                resources.getStringArray(R.array.sensor_lists)[2] -> {
                    tbSensor()
                }
                resources.getStringArray(R.array.sensor_lists)[3] -> {
                    tbSensor()
                }
                resources.getStringArray(R.array.sensor_lists)[4] -> {
                    tbSensor()
                }
                resources.getStringArray(R.array.sensor_lists)[5] -> {
                    tbSensor()
                }
                resources.getStringArray(R.array.sensor_lists)[6] -> {
                    tbSensor()
                }
            }
            runOnUiThread {

                subFragment.setResult(resources.getString(R.string.startText))
                subFragment.setUnit("")
            }

        }
    }

    /**
     * Measure Value set
     *
     */
    private fun setMeasureValue(main : String, sub : String?){
        subFragment.setResult(main)
        subFragment.setUnit(unit)
        if (sub != null){
            subFragment.setResultSub(sub)
        }
        if (dialog.isShowing){
            dialog.dismiss()
        }

    }

    /**
     * 이산환탄소 센서 측정
     */
    fun co2Sensor(){
        var msg : String? = ""
        while(loopChk){
            msg = scManager.SCRead() // Z xxxxx

            if (msg != null) {
                val sensorVal = msg.split(" ")[2]
                runOnUiThread {

                    try{
                        val co2val = sensorVal.toFloat() * 10
                        setMeasureValue(co2val.toString(), null)
//                        subFragment.setResult(co2val.toString())
//                        subFragment.setUnit(unit)

                        Log.d("MainActivity", maxVal.toString())
                        if (sensorVal.toFloat() < minVal!!){
                            changeBack(false)
                            ringOn()
                        } else if (sensorVal.toFloat() > maxVal!! ){
                            changeBack(false)
                            ringOn()
                        } else {
                            changeBack(true)
                            ringOff(0)
                        }
                    } catch (e : Exception){

                    }
                }
            }
        }
    }

    /**
     * 산소 센서 측정
     */
    fun o2Sensor(){
        var msg : String? = ""
        var cnt = 0
        while(loopChk){
            cnt += 1
            msg = scManager.SCRead()
            if (msg != null) {
                val hashMap = getMap(msg)
                val oxygen = hashMap.get("%")!!.toFloat()
                // 온도
                val temp = hashMap.get("T") + " °C"
                runOnUiThread {
                    setMeasureValue(oxygen.toString(), temp)
//                    subFragment.setResult(oxygen.toString())
//                    subFragment.setUnit(unit)
//                    subFragment.setResultSub(temp)

                    // 산소농도에 따라 배경화면 색이 변함
                    if (oxygen < minVal!!){
                        changeBack(false)
                        ringOn()
                    } else if (oxygen > maxVal!! ){
                        changeBack(false)
                        ringOn()
                    } else {
                        changeBack(true)
                        ringOff(0)
                    }

                }
            }
        }
    }


    /**
     * 산소측정 센서 각 수치별로 나눠주는 함수
     */
    fun getMap(msg : String?) : MutableMap<String, String>{
        val checkList = listOf<String>("O", "P", "e", "%", "T")
        var checkString = "O"
        val hashMap = mutableMapOf<String, String>("O" to "")
        val msgArray = msg!!.split("")
        for (i in 0 until msgArray.size-1) {
            val n = msgArray[i]
            if (n == " "){
                continue
            }
            if (n in checkList){
                checkString = n
                hashMap.put(n, "")
                continue
            }
            val temp = hashMap.get(checkString)
            hashMap.put(checkString, temp + n)
        }
        return hashMap
    }

    /**
     * TB 센서 함수
     */
    @ExperimentalUnsignedTypes
    fun tbSensor(){
        while(loopChk){
            val res = scManager.readTbSensor()
            val measureVal = res.toFloat()
            val result = measureVal/(Math.pow(10.toDouble(), decimal.toDouble()))
            Log.d("MainActivty", res + " and " + measureVal + " and " + result + " decimal : " + decimal)
            runOnUiThread {
                // 가스 농도 값 넣기
                try {
                    setMeasureValue(result.toString(), null)
//                    subFragment.setResult(result.toString())
//                    subFragment.setUnit(unit)
//                    addEntry(result.toFloat())
                } catch (e : Exception){

                }
                if (result < minVal!!){
                    changeBack(false)
                    ringOn()
                } else if (result > maxVal!! ){
                    changeBack(false)
                    ringOn()
                } else {
                    changeBack(true)
                    ringOff(0)
                }
            }


        }

    }


    /**
     * alarm start 함수
     * 특정시간 (defaultTime) 이후의 시간에서만 알람 시작
     */
    fun ringOn(){
        if (System.currentTimeMillis() > defaultTime){
            App.ringtone.run {
                if(!isPlaying) play()
            }
        }
    }

    /**
     * alarm 중지 함수
     * value = 1 -> 알람 정지 후 다음 시간까지 알람 ON block
     * value = 0 -> 알람 정지
     */
    fun ringOff(value : Int){
        when(value){
            1-> {
                defaultTime = System.currentTimeMillis() + blockTime
                App.ringtone.run {
                    if(isPlaying) stop()
                }
            }
            0 -> {
                App.ringtone.run {
                    if(isPlaying) stop()
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        Log.d("Main", "onResume")
        registerReceiver(usbReceiver, filter)
        if (App.resumed){
            val intent = Intent(ACTION_USB_PERMISSION)
            sendBroadcast(intent)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("Main", "onPause")
        unregisterReceiver(usbReceiver)
    }

    /**
     *  usb 연결 / 연결해제시 실행되는 작업
     */
    private val usbReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            // 연결됐을 때
            App.resumed = false
            Log.d("MAinActivity", intent.action.toString())
            if (ACTION_USB_PERMISSION == intent.action || UsbManager.ACTION_USB_DEVICE_ATTACHED == intent.action) {
                synchronized(this) {
                    Log.d("MainActivity", "connect")
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.apply {
                            scManager = SerialCommunication(
                                manager,
                                0,
                                9600,
                                8,
                                1,
                                0)
                            val pendingResult = goAsync()
                            val asyncTask = Task(pendingResult, intent)
                            asyncTask.execute()
                            connect = true

                        }
                    } else {
                        // 권한 허용이 안되어있는 경우
                        Log.d("MainActivity", "permission denied for device $device")
                        initManager()
                    }

                }
            }
            // 연결이 끊겼을때
            if (UsbManager.ACTION_USB_DEVICE_DETACHED == intent.action) {
                ringOff(0)
                Log.d("Main connect", "Disconnect")
                onFragmentChange(1)
                connect = false
                loopChk = false
//                measure_toggle_btn.isChecked = false
            }
        }
        private inner class Task(
            private val pedndingResult: PendingResult,
            private val intent : Intent
        ): AsyncTask<String, Int, String>(){
            override fun doInBackground(vararg params: String?): String {
                /**
                 * Sensor Type check
                 * TBSensor 기준으로 작성
                 * Co2, O2 센서는 확인해봐야 함 -> 아직 미체크
                 */
                val result = scManager.InitTbSensor()
                try {

                    unit = result.get("unit").toString()
                    type = result.get("type").toString().toInt()
                    decimal = result.get("decimal").toString().toInt()
                } catch (e: Exception){
                    onFragmentChange(3)
                    println(e)
                }

                if (type.toString().equals(getSensorType())){
                    onFragmentChange(2)
                } else{
                    onFragmentChange(3)
                }
                return toString().also{
                    log-> Log.d("MainActivty", log)
                }
            }

            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
                App.resumed = true
            }
        }
    }

    fun startMeasure(){
        loopChk = true
        thread = ThreadClass()
        dialog = setProgressDialog(this, "Loading...")
        dialog.show()
        thread.start()

    }

    fun stopMeasure(){
        ringOff(1)
        connect = false
        loopChk = false
    }

    private fun changeBack(value: Boolean){
        try{
            subFragment.changeBackground(value)
        } catch(e: Exception){

        }

    }
    fun setProgressDialog(context:Context, message:String):AlertDialog {
        val llPadding = 30
        val ll = LinearLayout(context)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam

        val progressBar = ProgressBar(context)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam

        llParam = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        val tvText = TextView(context)
        tvText.text = message
        tvText.setTextColor(Color.parseColor("#000000"))
        tvText.textSize = 20.toFloat()
        tvText.layoutParams = llParam

        ll.addView(progressBar)
        ll.addView(tvText)

        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setView(ll)

        val dialog = builder.create()
        val window = dialog.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window?.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            dialog.window?.attributes = layoutParams
        }
        return dialog
    }


}
