package com.antcloud.app.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.antcloud.app.R
import com.antcloud.app.binding.PlatformBinding
import com.antcloud.app.binding.crypto.AndroidCryptoProvider
import com.antcloud.app.common.AppUtils
import com.antcloud.app.common.FPSSpinnerAdapter
import com.antcloud.app.common.GlobalData
import com.antcloud.app.common.ResolutionSpinnerAdapter
import com.antcloud.app.components.makeToast
import com.antcloud.app.computers.ComputerManagerService
import com.antcloud.app.computers.ComputerManagerService.ApplistPoller
import com.antcloud.app.computers.ComputerManagerService.ComputerManagerBinder
import com.antcloud.app.data.GetVMIPState
import com.antcloud.app.data.PreferenceManger
import com.antcloud.app.dependencyinjection.AppModule
import com.antcloud.app.dependencyinjection.AuthRepository
import com.antcloud.app.grid.AppGridAdapter
import com.antcloud.app.nvstream.http.ComputerDetails
import com.antcloud.app.nvstream.http.ComputerDetails.AddressTuple
import com.antcloud.app.nvstream.http.NvApp
import com.antcloud.app.nvstream.http.NvHTTP
import com.antcloud.app.nvstream.http.PairingManager
import com.antcloud.app.nvstream.http.PairingManager.PairState
import com.antcloud.app.nvstream.jni.AntBridge
import com.antcloud.app.preferences.PreferenceConfiguration
import com.antcloud.app.utils.CacheHelper
import com.antcloud.app.utils.RestClient
import com.antcloud.app.utils.ServerHelper
import com.antcloud.app.utils.ShortcutHelper
import com.antcloud.app.utils.SpinnerDialog
import com.antcloud.app.viewmodel.StreamViewModel
import com.antcloud.app.viewmodel.VMStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParserException
import java.io.File
import java.io.IOException
import java.io.StringReader
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.URI
import java.net.URISyntaxException
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Collections
@Suppress("DEPRECATION")
@AndroidEntryPoint
class AppView : AppCompatActivity() {
    private var appGridAdapter: AppGridAdapter? = null
    private var uuidString: String? = null
    private var shortcutHelper: ShortcutHelper? = null
    private var computer: ComputerDetails? = null
    private var poller: ApplistPoller? = null
    private var blockingLoadSpinner: SpinnerDialog? = null
    private var lastRawApplist: String? = null
    private var lastRunningAppId = 0
    private var suspendGridUpdates = false
    private var inForeground = false
    private var showHiddenApps = false
    private val hiddenAppIds = HashSet<Int>()
    private var freezeUpdates = false
    private var runningPolling = false
    var computerName: String? = null
    private var managerBinder1: ComputerManagerBinder? = null
    private var managerBinder: ComputerManagerBinder? = null
    private var loadingDialog: AlertDialog? = null
    var connect :String? = ""
    var getVmipState = GetVMIPState()
    private val api = AppModule.injectBackendRetrofitApi()
    private val repository =  AuthRepository(api)
    var prefs : SharedPreferences? = null
    val globalInstance = GlobalData.getInstance()
    lateinit var loadingLayout : ConstraintLayout
    lateinit  var resolutionLayout : LinearLayout
    lateinit  var socketTimer_layout : ConstraintLayout
    lateinit  var connection_error_layout : ConstraintLayout
    lateinit  var errorText : TextView
    var errorTimerFlag = ""
    lateinit  var prefManager: PreferenceManger


    private val serviceConnection1: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            val localBinder = (binder as ComputerManagerBinder)

            object : Thread() {
                override fun run() {
                    localBinder.waitForReady()
                    managerBinder1 = localBinder
                    startComputerUpdates1()

                    AndroidCryptoProvider(this@AppView).clientCertificate
                }
            }.start()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            managerBinder1 = null
        }
    }
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            val localBinder =
                (binder as ComputerManagerBinder)

            object : Thread() {
                override fun run() {
                    localBinder.waitForReady()
                    try {
                        appGridAdapter = AppGridAdapter(
                            this@AppView, PreferenceConfiguration.readPreferences(this@AppView),
                            computer, localBinder.uniqueId, showHiddenApps
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        finish()
                        return
                    }
                    appGridAdapter!!.updateHiddenApps(hiddenAppIds, true)
                    managerBinder = localBinder
                    populateAppGridWithCache()
                    startComputerUpdates()
                }
            }.start()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            managerBinder = null
        }
    }


    private fun startComputerUpdates() {
        if (managerBinder == null || !inForeground) {
            return
        }
        managerBinder!!.startPolling { details: ComputerDetails ->
            if (suspendGridUpdates) {
                return@startPolling
            }
            if (!details.uuid.equals(uuidString, ignoreCase = true)) {
                return@startPolling
            }

            if (details.state == ComputerDetails.State.OFFLINE) {
                this@AppView.runOnUiThread {
                    Toast.makeText(
                        this@AppView,
                        "connection lost",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                return@startPolling
            }

            if (details.state == ComputerDetails.State.ONLINE && details.pairState != PairState.PAIRED) {
                this@AppView.runOnUiThread {
                    shortcutHelper!!.disableComputerShortcut(
                        details,
                        resources.getString(R.string.scut_not_paired)
                    )
                    Toast.makeText(
                        this@AppView,
                        resources.getText(R.string.scut_not_paired),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                return@startPolling
            }

            if (details.rawAppList == null || details.rawAppList == lastRawApplist) {
                if (details.runningGameId != lastRunningAppId) {
                    lastRunningAppId = details.runningGameId
                    updateUiWithServerinfo(details)
                }
                return@startPolling
            }

            lastRunningAppId = details.runningGameId
            lastRawApplist = details.rawAppList
            try {
                updateUiWithAppList(NvHTTP.getAppListByReader(StringReader(details.rawAppList)))
                updateUiWithServerinfo(details)
                if (blockingLoadSpinner != null) {
                    blockingLoadSpinner!!.dismiss()
                    blockingLoadSpinner = null
                }
            } catch (e: XmlPullParserException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        if (poller == null) {
            poller = managerBinder!!.createAppListPoller(computer)
        }
        poller!!.start()
    }

    private fun stopComputerUpdates() {
        if (poller != null) {
            poller!!.stop()
        }
        if (managerBinder != null) {
            managerBinder!!.stopPolling()
        }
        if (appGridAdapter != null) {
            appGridAdapter!!.cancelQueuedOperations()
        }
    }

    private fun stopComputerUpdates1(wait: Boolean) {
        if (managerBinder1 != null) {
            if (!runningPolling) {
                return
            }
            freezeUpdates = true
            managerBinder1!!.stopPolling()
            if (wait) {
                managerBinder1!!.waitForPollingStopped()
            }
            runningPolling = false
        }
    }

    @SuppressLint("SetTextI18n", "NewApi", "MissingInflatedId", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inForeground = true
        shortcutHelper = ShortcutHelper(this)
        setContentView(R.layout.activity_app_view)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        prefs = PreferenceManager.getDefaultSharedPreferences(this@AppView)
        prefs!!.edit().putBoolean(PreferenceConfiguration.ONSCREEN_CONTROLLER_PREF_STRING,false).apply();
        val viewModel: StreamViewModel by viewModels()
        prefManager = PreferenceManger(this)

        getVmipState = viewModel.getVMIPState.value
        loadingLayout =  findViewById(R.id.loadingLayout)
        resolutionLayout=  findViewById(R.id.resolutionLayout)
        socketTimer_layout=  findViewById(R.id.socketTimer_layout)
        connection_error_layout =  findViewById(R.id.connection_error_layout)
        errorText =  findViewById(R.id.errorText)
        val loadingText=  findViewById<TextView>(R.id.loadingText)
        val spinnerImage =  findViewById<ImageView>(R.id.spinnerImage)
        val backBtn =  findViewById<Button>(R.id.backBtn)
        val errorBackBtn =  findViewById<Button>(R.id.error_backBtn)
        val contactButton =  findViewById<Button>(R.id.contactButton)
        val startVMButton = findViewById<Button>(R.id.start_vm_button)
        val shutdowVMButton = findViewById<Button>(R.id.shutdown_vm_button)
        val cbController = findViewById<CheckBox>(R.id.cbController)
        val bitrateSeekbar = findViewById<SeekBar>(R.id.bitrateSeekbar)
        val bitrateLabel = findViewById<TextView>(R.id.bitrate_value)
        val resolutionSpinner = findViewById<Spinner>(R.id.resolution_spinner)
        val fpsSpinner = findViewById<Spinner>(R.id.fps_spinner)

        backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        errorBackBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        contactButton.setOnClickListener {
            globalInstance.openSupportScreen =  true
            onBackPressedDispatcher.onBackPressed()
        }
        onBackPressedDispatcher.addCallback(this) {
            viewModel.onBackPressed()
          //  pcExit()
            this.remove()
            onBackPressedDispatcher.onBackPressed()
        }

        val imageLoader = ImageLoader.Builder(this).components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
        val request = ImageRequest.Builder(this)
            .data(R.drawable.spinner)
            .target(spinnerImage)
            .build()
        imageLoader.enqueue(request)

        lifecycleScope.launchWhenStarted {
            viewModel.requestVmIpEvent.collect {
                val token = "JWT " + GlobalData.getInstance().accountData.token
                val process = repository.getVmip(token)
                if(process.code()==200) {
                    globalInstance.vmIP = process.body()?.vmip!!
                    if (globalInstance.vmIP != "") {
                        errorTimerFlag =  "vmip"
                        viewModel.startDisconnectTimer()
                        resolutionLayout.visibility = View.VISIBLE
                        loadingLayout.visibility = View.INVISIBLE
                        CoroutineScope(Dispatchers.Main).launch {
                            viewModel.onSocketStatusChanged(VMStatus.Connected)
                        }
                        Log.d("Socket", "VM IP: ${globalInstance.vmIP}")
                    }
                }
            }
        }
        if(intent.hasExtra("connect")){
            loadingLayout.visibility = View.VISIBLE
            resolutionLayout.visibility = View.INVISIBLE
            viewModel.callSocket()
        }
        else {
            errorTimerFlag =  "streamEnd"
            viewModel.startDisconnectTimer()
            resolutionLayout.visibility = View.VISIBLE
            loadingLayout.visibility = View.INVISIBLE
        }


        lifecycleScope.launch {
            viewModel.disConnTimeLeft.collect { time ->
                if(time == "00:00"){
                    viewModel.stopDisconnectTimer()
                    if(errorTimerFlag=="vmip") {
                        errorText.text = resources.getString(R.string.one_min_issue_msg)
                    }
                    else
                        errorText.text = resources.getString(R.string.stream_end_issue)
                    pcExit()
                    loadingLayout.visibility = View.INVISIBLE
                    resolutionLayout.visibility = View.INVISIBLE
                    socketTimer_layout.visibility = View.INVISIBLE
                    connection_error_layout.visibility = View.VISIBLE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.timeLeft.collect { time ->
                if(time != "00:00"){
                    if(viewModel.loadingData.contains("Your PC is starting"))
                        loadingText.text =  "${viewModel.loadingData}$time minutes"
                    else
                        loadingText.text =  viewModel.loadingData
                }
                else {
                    loadingLayout.visibility =  View.INVISIBLE
                    resolutionLayout.visibility =  View.INVISIBLE
                    connection_error_layout.visibility =  View.INVISIBLE
                    socketTimer_layout.visibility =  View.VISIBLE
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setShouldDockBigOverlays(false)
        }
        bindService(Intent(this@AppView, ComputerManagerService::class.java), serviceConnection1, BIND_AUTO_CREATE)
        val dbFile = getDatabasePath("computers4.db")
            if (dbFile.exists()) {
                val deleted = deleteDatabase("computers4.db")
                Log.d("Database", "Database existed and deleted: $deleted")
            } else {
                Log.d("Database", "Database does not exist.")
            }
        val keyFile = File(filesDir.absolutePath + File.separator + "client.key")
        val certFile = File(filesDir.absolutePath + File.separator + "client.crt")
        if (keyFile.exists()) {
            keyFile.delete()
        }
        if (certFile.exists()) {
            certFile.delete()
        }
        bitrateSeekbar.max = 50000
        bitrateSeekbar.progress = 5000
        bitrateSeekbar.min = 2000

        val myList = mutableListOf("360p", "480p")
        when(GlobalData.getInstance().accountData.resolution){
            "720" ->  myList.add("720p")
            "1080"->  {
                myList.add("720p")
                myList.add("1080p")
            }
            "1440"-> {
                myList.add("720p")
                myList.add("1080p")
                myList.add("1440p")
            }
            "2160" -> {
                myList.add("720p")
                myList.add("1080p")
                myList.add("1440p")
                myList.add("2160p")
            }
        }
        val resolutions  = myList.toTypedArray()
        val resolutionAdapter = ResolutionSpinnerAdapter(this, resolutions)
        resolutionSpinner.adapter = resolutionAdapter
        resolutionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val value = resolutions[position]
                bitrateSeekbar.progress = AppUtils.setBitrate(value)
                setResolutionInPreferences(value)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val fpsNames = resources.getStringArray(R.array.fps_names)
        val fpsAdapter = FPSSpinnerAdapter(this, fpsNames)
        fpsSpinner.adapter = fpsAdapter
        fpsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val value = fpsNames[position]
                setFPSInPreferences(value)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        val bitrateValue = bitrateSeekbar.progress / 1000

        bitrateLabel.text = "$bitrateValue Mbps"
        setBitratevalue(bitrateValue)
        bitrateSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val mbps = progress / 1000
                bitrateLabel.text  = "$mbps Mbps"
                setBitratevalue(mbps)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val snapped = ((seekBar?.progress ?: 1) / 500) * 500
//                Log.i("test" , "" + snapped)
            }
        })
        startVMButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.stopDisconnectTimer()
                loadingLayout.visibility = View.VISIBLE
                resolutionLayout.visibility = View.INVISIBLE
                loadingText.text = getResources().getString(R.string.conn_establishing_msg)
                withContext(Dispatchers.IO) {
                    doAddPc(GlobalData.getInstance().vmIP)
                }
            }
        }
        shutdowVMButton.setOnClickListener {
//            val intent = Intent(this@AppView,StreamSettings::class.java)
//            startActivity(intent)
            viewModel.closeStream()
            pcExit()
            onBackPressedDispatcher.onBackPressed()
        }

        cbController.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                prefs!!.edit().putBoolean(PreferenceConfiguration.ONSCREEN_CONTROLLER_PREF_STRING,true).apply();

            } else {
                prefs!!.edit().putBoolean(PreferenceConfiguration.ONSCREEN_CONTROLLER_PREF_STRING,false).apply();
            }
        }

        showHiddenApps = false
        uuidString = intent.getStringExtra(UUID_EXTRA)
        val hiddenAppsPrefs = getSharedPreferences(HIDDEN_APPS_PREF_FILENAME, MODE_PRIVATE)
        for (hiddenAppIdStr in hiddenAppsPrefs.getStringSet(uuidString, HashSet())!!) {
            hiddenAppIds.add(hiddenAppIdStr.toInt())
        }
    }



    private fun setBitratevalue(mbps: Int) {
        val value =  mbps*1000
        prefs!!.edit().putInt(PreferenceConfiguration.BITRATE_PREF_STRING,value).apply();


    }

    private fun setFPSInPreferences(value: String?) {
        var valueFPS :String = ""
        when(value){
            "30 FPS" -> valueFPS = "30"
            "60 FPS" -> valueFPS = "60"
            "90 FPS" -> valueFPS = "90"
            "120 FPS" -> valueFPS = "120"
        }

        prefs!!.edit()
            .putString(PreferenceConfiguration.FPS_PREF_STRING,valueFPS)
            .apply();

    }

    private fun setResolutionInPreferences(value: String) {
        var resValue : String = ""
        when(value){
            "360p"-> resValue =  PreferenceConfiguration.RES_360P
            "480p"-> resValue = PreferenceConfiguration.RES_480P
            "720p"-> resValue = PreferenceConfiguration.RES_720P
            "1080p"-> resValue = PreferenceConfiguration.RES_1080P
            "1440p"-> resValue = PreferenceConfiguration.RES_1440P
            "2160p"-> resValue = PreferenceConfiguration.RES_4K
            else -> resValue = PreferenceConfiguration.RES_720P
        }

        if(resValue!="")
          prefs!!.edit()
            .putString(PreferenceConfiguration.RESOLUTION_PREF_STRING,resValue)
            .apply();
    }

    fun pcExit(){
        prefManager.setProperExit(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            prefManager.setExitTime(LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC))
        }
    }

    private fun populateAppGridWithCache() {
        try {
            lastRawApplist = CacheHelper.readInputStreamToString(
                CacheHelper.openCacheFileForInput(
                    cacheDir, "applist", uuidString
                )
            )
            val applist: List<NvApp> = NvHTTP.getAppListByReader(StringReader(lastRawApplist))
            updateUiWithAppList(applist)
//            LimeLog.info("Loaded applist from cache")
        } catch (e: IOException) {
            if (lastRawApplist != null) {
                e.printStackTrace()
            }
        } catch (e: XmlPullParserException) {
            if (lastRawApplist != null) {
                e.printStackTrace()
            }
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        if (managerBinder != null) {
            unbindService(serviceConnection)
        }
        if (managerBinder1 != null) {
            unbindService(serviceConnection1)
        }

    }



    override fun onResume() {
        super.onResume()
        inForeground = true
    }


    override fun onPause() {
        super.onPause()
        stopComputerUpdates()
        stopComputerUpdates1(true)
    }

    override fun onStop() {
        super.onStop()
    }


    private fun updateUiWithServerinfo(details: ComputerDetails) {
        this@AppView.runOnUiThread {
            var updated = false
            for (i in 0 until appGridAdapter!!.count) {
                val existingApp = appGridAdapter!!.getItem(i) as AppObject
                if (existingApp.isRunning && existingApp.app.appId == details.runningGameId) {
                    return@runOnUiThread
                } else if (existingApp.app.appId == details.runningGameId) {
                    existingApp.isRunning = true
                    updated = true
                } else if (existingApp.isRunning) {
                    existingApp.isRunning = false
                    updated = true
                }
            }
            if (updated) {
                appGridAdapter!!.notifyDataSetChanged()
            }
        }
    }

    private fun updateUiWithAppList(appList: List<NvApp>) {
        this@AppView.runOnUiThread {
            var updated = false
            for (app in appList) {
                var foundExistingApp = false
                for (i in 0 until appGridAdapter!!.count) {
                    val existingApp = appGridAdapter!!.getItem(i) as AppObject
                    if (existingApp.app.appId == app.appId) {
                        if (existingApp.app.appName != app.appName) {
                            existingApp.app.appName = app.appName
                            updated = true
                        }
                        foundExistingApp = true
                        suspendGridUpdates = true
                        ServerHelper.doQuit(
                            this@AppView, computer,
                            app, managerBinder
                        ) {
                            suspendGridUpdates = false
                            if (poller != null) {
                                poller!!.pollNow()
                            }
                        }

                        val handlerThread = Thread {
                            if (!suspendGridUpdates) {

                                ServerHelper.doStart(this@AppView, app, computer, managerBinder)
                                finish()
                            }
                        }
                        handlerThread.start()
                        break
                    }
                }
                if (!foundExistingApp) {

                    ServerHelper.doStart(this@AppView, app, computer, managerBinder)
                    finish()
                }
            }
        }
    }


    class AppObject(app: NvApp) {
        @JvmField
        val app: NvApp
        @JvmField
        var isRunning: Boolean = false
        @JvmField
        var isHidden: Boolean = false

        init {
            requireNotNull(app) { "app must not be null" }
            this.app = app
        }

        override fun toString(): String {
            return app.appName
        }
    }


    @Throws(InterruptedException::class)
    suspend fun doAddPc(rawUserInput: String) {
        var wrongSiteLocal = false
        var invalidInput = false
        var success: Boolean

        try {
            val details = ComputerDetails()
            val uri = parseRawUserInputToUri(rawUserInput)
            if (uri != null && uri.host != null && !uri.host.isEmpty()) {
                val host = uri.host
                var port = uri.port
                if (port == -1) {
                    port = NvHTTP.DEFAULT_HTTP_PORT
                }
                details.manualAddress = AddressTuple(host, port)
                success = managerBinder1!!.addComputerBlocking(details)
                if (!success) {
                    wrongSiteLocal = isWrongSubnetSiteLocalAddress(host)
                }
            } else {
                success = false
                invalidInput = true
            }
        } catch (e: InterruptedException) {
            //dialog.dismiss()
            throw e
        } catch (e: IllegalArgumentException) {

            e.printStackTrace()
            success = false
            invalidInput = true
        }

        val portTestResult =
            if (!success && !wrongSiteLocal && !invalidInput) AntBridge.testClientConnectivity(
                ServerHelper.CONNECTION_TEST_SERVER, 443,
                AntBridge.ML_PORT_FLAG_TCP_47984 or AntBridge.ML_PORT_FLAG_TCP_47989
            )
            else AntBridge.ML_TEST_RESULT_INCONCLUSIVE

        if (invalidInput) {
            withContext(Dispatchers.Main) {
                pcExit()
                errorText.text =   resources.getString(R.string.addpc_unknown_host)
                loadingLayout.visibility = View.INVISIBLE
                socketTimer_layout.visibility = View.INVISIBLE
                resolutionLayout.visibility = View.INVISIBLE
                connection_error_layout.visibility = View.VISIBLE
            }

        }else if (wrongSiteLocal) {
            withContext(Dispatchers.Main) {
                errorText.text =  resources.getString(R.string.addpc_wrong_sitelocal)
                pcExit()
                loadingLayout.visibility = View.INVISIBLE
                socketTimer_layout.visibility = View.INVISIBLE
                resolutionLayout.visibility = View.INVISIBLE
                connection_error_layout.visibility = View.VISIBLE
            }
        }else if (!success) {
            withContext(Dispatchers.Main) {

                errorText.text =  resources.getString(R.string.one_min_issue_msg)
                pcExit()
                loadingLayout.visibility = View.INVISIBLE
                socketTimer_layout.visibility = View.INVISIBLE
                resolutionLayout.visibility = View.INVISIBLE
                connection_error_layout.visibility = View.VISIBLE
            }
        } else {
            startComputerUpdates1()
        }
    }

    private fun isWrongSubnetSiteLocalAddress(address: String): Boolean {
        try {
            val targetAddress = InetAddress.getByName(address)
            if (targetAddress !is Inet4Address || !targetAddress.isSiteLocalAddress()) {
                return false
            }
            for (iface in Collections.list<NetworkInterface>(NetworkInterface.getNetworkInterfaces())) {
                for (addr in iface.interfaceAddresses) {
                    if (addr.address !is Inet4Address || !addr.address.isSiteLocalAddress) {
                        continue
                    }

                    val targetAddrBytes = targetAddress.getAddress()
                    val ifaceAddrBytes = addr.address.address
                    var addressMatches = true
                    for (i in 0 until addr.networkPrefixLength) {
                        if ((ifaceAddrBytes[i / 8].toInt() and (1 shl (i % 8))) != (targetAddrBytes[i / 8].toInt() and (1 shl (i % 8)))) {
                            addressMatches = false
                            break
                        }
                    }
                    if (addressMatches) {
                        return false
                    }
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun parseRawUserInputToUri(rawUserInput: String): URI? {
        try {
            val uri = URI("antcloud://$rawUserInput")
            if (uri.host != null && uri.host.isNotEmpty()) {
                return uri
            }
        } catch (ignored: URISyntaxException) {
        }
        try {
            val uri = URI("antcloud://[$rawUserInput]")
            if (uri.host != null && uri.host.isNotEmpty()) {
                return uri
            }
        } catch (ignored: URISyntaxException) {
        }
        return null
    }


    private fun startComputerUpdates1() {
        if (managerBinder1 != null && !runningPolling && inForeground) {
            freezeUpdates = false
            managerBinder1!!.startPolling { details: ComputerDetails ->
                if (!freezeUpdates) {
                    this@AppView.runOnUiThread {
                        doPair(details)
                    }
                }
            }
            runningPolling = true
        }
    }

    private fun doPair(computer: ComputerDetails) {
        this.computer = computer
        Thread {
            val httpConn: NvHTTP
            val message: String?
            var success = false
            try {
                stopComputerUpdates1(true)
                httpConn = NvHTTP(
                    ServerHelper.getCurrentAddressFromComputer(computer),
                    computer.httpsPort,
                    managerBinder1!!.uniqueId,
                    computer.serverCert,
                    PlatformBinding.getCryptoProvider(this@AppView)
                )
                    val pinStr = PairingManager.generatePinString()
                     val accessToken = "JWT "+ GlobalData.getInstance().accountData.token
                    val handlerThread = Thread {
                        sendAndVerifySecurityPinManually(pinStr, accessToken)
                    }
                    handlerThread.start()
                    val pm = httpConn.pairingManager
                    val pairState = pm.pair(httpConn.getServerInfo(true), pinStr)
                   if (pairState == PairState.PAIRED) {
                        message = null
                        success = true
                        bindService(Intent(this@AppView, ComputerManagerService::class.java), serviceConnection, BIND_AUTO_CREATE
                        )
                        managerBinder1!!.getComputer(computer.uuid).serverCert = pm.pairedCert
                        managerBinder1!!.invalidateStateForComputer(computer.uuid)
                    } else {
                       message = resources.getString(R.string.pairing_fail)
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
            val toastMessage = message
            val toastSuccess = success
            runOnUiThread {
                if (toastMessage != null) {
                    makeToast(toastMessage)
                }
                if (toastSuccess) {
                    computerName = computer.name
                    uuidString = computer.uuid

                    startComputerUpdates()
                } else {
                    startComputerUpdates1()
                }
            }
        }.start()
    }

    private fun sendAndVerifySecurityPinManually(pinStr: String, accessToken: String) {
        val pinMap = HashMap<String, String>()
        pinMap["pin"] = pinStr

        RestClient(this@AppView).postRequestWithHeader("update_pin",
            "vm/vmauth",
            pinMap,
            accessToken,
            "",
            { tag, response ->
                if (response != null) {
                    try {
                        val jsonObject = JSONObject(response)
                        val status = jsonObject.getJSONObject("data").getString("status")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            },
            { tag: String?, errorMsg: String, statusCode: Long ->
                Log.d(
                    "TEST",
                    "Message : $errorMsg"
                )
            })
    }


    companion object {
        const val HIDDEN_APPS_PREF_FILENAME: String = "HiddenApps"
        const val NAME_EXTRA: String = "Name"
        const val UUID_EXTRA: String = "UUID"
    }
}


