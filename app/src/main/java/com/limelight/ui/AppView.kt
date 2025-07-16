package com.limelight.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.limelight.R
import com.limelight.binding.PlatformBinding
import com.limelight.binding.crypto.AndroidCryptoProvider
import com.limelight.common.AppUtils
import com.limelight.common.FPSSpinnerAdapter
import com.limelight.common.GlobalData
import com.limelight.common.ResolutionSpinnerAdapter
import com.limelight.computers.ComputerManagerService
import com.limelight.computers.ComputerManagerService.ApplistPoller
import com.limelight.computers.ComputerManagerService.ComputerManagerBinder
import com.limelight.data.GetVMIPState
import com.limelight.dependencyinjection.AppModule
import com.limelight.dependencyinjection.AuthRepository
import com.limelight.grid.AppGridAdapter
import com.limelight.nvstream.http.ComputerDetails
import com.limelight.nvstream.http.ComputerDetails.AddressTuple
import com.limelight.nvstream.http.NvApp
import com.limelight.nvstream.http.NvHTTP
import com.limelight.nvstream.http.PairingManager
import com.limelight.nvstream.http.PairingManager.PairState
import com.limelight.nvstream.jni.MoonBridge
import com.limelight.preferences.PreferenceConfiguration
import com.limelight.utils.CacheHelper
import com.limelight.utils.RestClient
import com.limelight.utils.ServerHelper
import com.limelight.utils.ShortcutHelper
import com.limelight.utils.SpinnerDialog
import com.limelight.utils.UiHelper
import com.limelight.viewmodel.StreamViewModel
import com.limelight.viewmodel.UserViewModel
import com.limelight.viewmodel.VMStatus
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
import java.util.Collections
import java.util.Timer

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
                        resources.getText(R.string.lost_connection),
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
                        viewModel.startDisconnectTimer()
                        viewModel.socketConnect = "connected"
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
            viewModel.startDisconnectTimer()
            resolutionLayout.visibility = View.VISIBLE
            loadingLayout.visibility = View.INVISIBLE
        }


        lifecycleScope.launch {
            viewModel.disConnTimeLeft.collect { time ->
                if(time == "00:00"){
                    viewModel.stopDisconnectTimer()
                    errorText.text = resources.getString(R.string.stream_end_issue)
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
                Log.i("test11" , "$mbps Mbps")
                bitrateLabel.text  = "$mbps Mbps"
                setBitratevalue(mbps)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val snapped = ((seekBar?.progress ?: 1) / 500) * 500
                Log.i("test" , "" + snapped)
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
            viewModel.closeStream()
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

    private fun updateHiddenApps(hideImmediately: Boolean) {
        val hiddenAppIdStringSet = HashSet<String>()
        for (hiddenAppId in hiddenAppIds) {
            hiddenAppIdStringSet.add(hiddenAppId.toString())
        }
        getSharedPreferences(HIDDEN_APPS_PREF_FILENAME, MODE_PRIVATE).edit()
            .putStringSet(uuidString, hiddenAppIdStringSet).apply()
        appGridAdapter!!.updateHiddenApps(hiddenAppIds, hideImmediately)
    }

    private fun populateAppGridWithCache() {
        try {
            lastRawApplist = CacheHelper.readInputStreamToString(
                CacheHelper.openCacheFileForInput(
                    cacheDir, "applist", uuidString
                )
            )
            Log.i("test", "test$lastRawApplist")
            val applist: List<NvApp> = NvHTTP.getAppListByReader(StringReader(lastRawApplist))
            updateUiWithAppList(applist)
            LimeLog.info("Loaded applist from cache")
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

    private fun loadAppsBlocking() {
        blockingLoadSpinner = SpinnerDialog.displayDialog(
            this,
            resources.getString(R.string.applist_refresh_title),
            resources.getString(R.string.applist_refresh_msg),
            true
        )
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
        Log.i("test", "onResume")
        super.onResume()
        UiHelper.showDecoderCrashDialog(this)
        inForeground = true
    }


    override fun onPause() {
        super.onPause()
        stopComputerUpdates()
        stopComputerUpdates1(true)
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
        Log.i("test", "test11")

        try {
            val details = ComputerDetails()
            val uri = parseRawUserInputToUri(rawUserInput)
            if (uri != null && uri.host != null && !uri.host.isEmpty()) {
                val host = uri.host
                var port = uri.port
                if (port == -1) {
                    port = NvHTTP.DEFAULT_HTTP_PORT
                }
                Log.i("test", "test12")
                details.manualAddress = AddressTuple(host, port)
                success = managerBinder1!!.addComputerBlocking(details)
                if (!success) {
                    Log.i("test", "test13")
                    wrongSiteLocal = isWrongSubnetSiteLocalAddress(host)
                }
            } else {
                Log.i("test", "test14")
                success = false
                invalidInput = true
            }
        } catch (e: InterruptedException) {
            Log.i("test", "test15")
            //dialog.dismiss()
            throw e
        } catch (e: IllegalArgumentException) {
            Log.i("test", "test16")
            e.printStackTrace()
            success = false
            invalidInput = true
        }

        val portTestResult =
            if (!success && !wrongSiteLocal && !invalidInput) MoonBridge.testClientConnectivity(
                ServerHelper.CONNECTION_TEST_SERVER, 443,
                MoonBridge.ML_PORT_FLAG_TCP_47984 or MoonBridge.ML_PORT_FLAG_TCP_47989
            )
            else MoonBridge.ML_TEST_RESULT_INCONCLUSIVE

        if (invalidInput) {
            withContext(Dispatchers.Main) {
                errorText.text =   resources.getString(R.string.addpc_unknown_host)
                loadingLayout.visibility = View.INVISIBLE
                socketTimer_layout.visibility = View.INVISIBLE
                resolutionLayout.visibility = View.INVISIBLE
                connection_error_layout.visibility = View.VISIBLE
            }

        }else if (wrongSiteLocal) {
            withContext(Dispatchers.Main) {
                errorText.text =  resources.getString(R.string.addpc_wrong_sitelocal)
                loadingLayout.visibility = View.INVISIBLE
                socketTimer_layout.visibility = View.INVISIBLE
                resolutionLayout.visibility = View.INVISIBLE
                connection_error_layout.visibility = View.VISIBLE
            }
        }else if (!success) {
            withContext(Dispatchers.Main) {
                errorText.text =  resources.getString(R.string.one_min_issue_msg)
                loadingLayout.visibility = View.INVISIBLE
                socketTimer_layout.visibility = View.INVISIBLE
                resolutionLayout.visibility = View.INVISIBLE
                connection_error_layout.visibility = View.VISIBLE
            }
        } else {
            Log.i("test", "test17")
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
            val uri = URI("moonlight://$rawUserInput")
            if (uri.host != null && !uri.host.isEmpty()) {
                return uri
            }
        } catch (ignored: URISyntaxException) {
        }
        try {
            val uri = URI("moonlight://[$rawUserInput]")
            if (uri.host != null && !uri.host.isEmpty()) {
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
                    if (pairState == PairState.PIN_WRONG) {
                        message = resources.getString(R.string.pair_incorrect_pin)
                    } else if (pairState == PairState.FAILED) {
                        message = if (computer.runningGameId != 0) {
                            resources.getString(R.string.pair_pc_ingame)
                        } else {
                            resources.getString(R.string.pair_fail)
                        }
                    } else if (pairState == PairState.ALREADY_IN_PROGRESS) {
                        message = resources.getString(R.string.pair_already_in_progress)
                    }

                    else if (pairState == PairState.PAIRED) {
                        message = null
                        success = true
                        Log.i("test", "qldchnbq")
                        bindService(Intent(this@AppView, ComputerManagerService::class.java), serviceConnection, BIND_AUTO_CREATE
                        )
                        managerBinder1!!.getComputer(computer.uuid).serverCert = pm.pairedCert
                        managerBinder1!!.invalidateStateForComputer(computer.uuid)
                    } else {
                        message = null

                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
            val toastMessage = message
            val toastSuccess = success
            runOnUiThread {
                if (toastMessage != null) {
                    Toast.makeText(this@AppView, toastMessage, Toast.LENGTH_LONG)
                        .show()
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
                        Log.i("demo", "wcv3$status")
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


