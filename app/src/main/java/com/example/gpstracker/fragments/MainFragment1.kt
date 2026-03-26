package com.example.gpstracker.fragments


import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import com.example.gpstracker.MainApp
import com.example.gpstracker.MainViewModel
import com.example.gpstracker.R
import com.example.gpstracker.data.TrackItemEntity
import com.example.gpstracker.databinding.FragmentMainBinding
import com.example.gpstracker.location.LocationModel
import com.example.gpstracker.location.LocationModelEvent
import com.example.gpstracker.location.LocationService
import com.example.gpstracker.utils.DialogManager
import com.example.gpstracker.utils.TimeUtils
import com.example.gpstracker.utils.checkPermission
import com.example.gpstracker.utils.showToast
import com.google.android.material.snackbar.Snackbar
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Timer
import java.util.TimerTask


class MainFragment1 : Fragment() {
    lateinit var binding: FragmentMainBinding
    lateinit var map: MapView
    lateinit var fineLocationLauncher: ActivityResultLauncher<String>
    lateinit var backgroundLocationLauncher: ActivityResultLauncher<String>
    lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    lateinit var requestServicePermissionLauncher: ActivityResultLauncher<String>
    private lateinit var mLocOverlay: MyLocationNewOverlay
    private var isServiceRunning = false
    private var firstStart  = true
    private var timer: Timer? = null
    private var startTime: Long = 0
    private var locationModel: LocationModel? = null
    private val polyline: Polyline? = Polyline()

    private val viewModel: MainViewModel by activityViewModels{
        MainViewModel.ViewModelFactory((requireContext().applicationContext as MainApp).database)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        settingsOsm()
        binding = FragmentMainBinding.inflate(inflater,container,false)
        return binding.root




    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        map = binding.mapView1
        registerPermission()
        setOnClicks()
        checkServiceState()
        updateTime()
        locationUpdates()
        checkLocPermission()
    }
    private fun settingsOsm(){
        Configuration.getInstance().load(
            requireContext(),
            // Важно: сохраните настройки кеша в своем файле preferences
            requireContext().getSharedPreferences("osm_pref", Context.MODE_PRIVATE)
        )

        Configuration.getInstance().userAgentValue = BuildConfig.BUILD_TYPE
    }


    @SuppressLint("UseKtx")
    private fun initOSM(){
        polyline?.outlinePaint?.color = Color.parseColor(
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString("color_key","#14347C")
        )
        map.setTileSource(TileSourceFactory.MAPNIK) // Использование тайлов с mapnik
        map.setMultiTouchControls(true) // Включение мультитача
        map.controller.setZoom(18.0) // Установка начального уровня масштабирования
        map.controller.setCenter(GeoPoint(55.751244, 37.618423)) // Центрирование на Москве
        // Настройка элементов управления масштабом
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)

        val mLocProvider = GpsMyLocationProvider(requireContext())
        mLocOverlay = MyLocationNewOverlay(mLocProvider,map)//слой наложения маркера местоположения
        mLocOverlay.enableMyLocation()//включить определение положения устройства
        mLocOverlay.enableFollowLocation()
        mLocOverlay.runOnFirstFix { //запустится как только будет получена геолокация
            map.overlays.clear()
            map.overlays.add(mLocOverlay)
            map.overlays.add(polyline)

        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("ObsoleteSdkInt")
    private fun registerPermission(){
        requestServicePermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                startLocationService()
            } else {
                showToast("Запрос отклонен, повторите попытку!")
            }
        }
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted ) {
                // Разрешение получено — можно отправлять уведомления
                showToast("Уведомления разрешены!")
            } else {
                // Пользователь отказал

                showToast("Уведомления отключены!")
            }
        }


        fineLocationLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted -> if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q){
            initOSM()
            checkLocationEnabled()
            if (isGranted){
                backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

            }else{
                // Пользователь отказал
                        AlertDialog.Builder(requireContext())
                            .setTitle("Необходим доступ к фоновому местоположению")
                            .setMessage("Предоставьте доступ.")
                            .setPositiveButton("ОК") { dialog, _ ->
                                fineLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                dialog.dismiss()

                            }
                            .setNegativeButton("Отмена") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()

                        }

        }else {
            initOSM()
            checkLocationEnabled()
            if (isGranted) {
                showToast("Разрешение на геолокацию получено!")
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle("Необходим доступ к местоположению")
                    .setMessage("Предоставьте доступ.")
                    .setPositiveButton("ОК") { dialog, _ ->
                        fineLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        dialog.dismiss()

                    }
                    .setNegativeButton("Отмена") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }

        }
        }

        backgroundLocationLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            initOSM()
            checkLocationEnabled()
            when {
                isGranted -> {
                    // Разрешение получено
                    showToast("Фоновый доступ к местоположению разрешён")
                }
                else -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Необходим доступ к фоновому местоположению")
                        .setMessage("Предоставьте доступ.")
                        .setPositiveButton("ОК") { dialog, _ ->
                            backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            dialog.dismiss()
                        }
                        .setNegativeButton("Отмена") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }
    }

    //ПРОВЕРКА ДЛЯ ГЕОПОЗИЦИИ И ФОНОВОГО МЕСТОПОЛОЖЕНИЯ
    private fun  checkLocPermission(){
        initOSM()
        checkLocationEnabled()
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q){
            checkPermissionFineLocAfter10()
        }else{
            checkPermissionBefore10()
        }
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkPermissionFineLocAfter10(){
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            checkPermissionBackLocAfter10()
        }else{
            fineLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkPermissionBackLocAfter10(){
        if (checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            showToast("Фоновое разрешение получено!")
        }else{
            backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

        }
    }
    private fun checkPermissionBefore10(){
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            showToast("Местоположение получено!")
        }else {
            fineLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

        }
    }
    private fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context?.packageName ?: "", null)
            }
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            showToast("Не удалось открыть настройки")
        }
    }



    // Открытие настроек приложения

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    startLocationService()
                }
                else -> {
                    requestServicePermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // На Android < 13 разрешение не требуется
            startLocationService()
        }
    }

    //СОЗДАЕМ ЕДИНЫЙ СЛУШАТЕЛЬ ДЛЯ КНОПОК
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setOnClicks() = with(binding){
        val listener = onClicks()
        fStartStop.setOnClickListener(listener)
        fMyPosition.setOnClickListener(listener)
    }
    @SuppressLint("ObsoleteSdkInt")
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun onClicks(): View.OnClickListener {
        return View.OnClickListener{
            when(it.id){
                R.id.fStartStop->{
                    if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q) {
                    if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)){
                        when(checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)){
                            true->{
                                startStopService()
                            }
                            false->{
                                AlertDialog.Builder(requireContext())
                                    .setTitle("Необходим доступ к фоновому местоположению")
                                    .setMessage("Предоставьте доступ.")
                                    .setPositiveButton("ОК") { dialog, _ ->
                                        backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                        dialog.dismiss()

                                    }
                                    .setNegativeButton("Отмена") { dialog, _ ->
                                        dialog.dismiss()
                                    }
                                    .show()
                            }
                        }
                    }else{
                        AlertDialog.Builder(requireContext())
                            .setTitle("Необходим доступ к местоположению")
                            .setMessage("Предоставьте доступ для корректной работы приложения.")
                            .setPositiveButton("ОК") { dialog, _ ->
                                fineLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                dialog.dismiss()

                            }
                            .setNegativeButton("Отмена") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }
                }else {
                    if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)){
                        startStopService()
                    }else{
                        AlertDialog.Builder(requireContext())
                            .setTitle("Необходим доступ к местоположению")
                            .setMessage("Предоставьте доступ для корректной работы приложения.")
                            .setPositiveButton("ОК") { dialog, _ ->
                                fineLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                dialog.dismiss()
                            }
                            .setNegativeButton("Отмена") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }
                }

                }
                R.id.fMyPosition->{
                    if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q) {
                        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)){
                            when(checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)){
                                true->{
                                    centerMyLocation()
                                }
                                false->{
                                    AlertDialog.Builder(requireContext())
                                        .setTitle("Необходим доступ к фоновому местоположению")
                                        .setMessage("Предоставьте доступ.")
                                        .setPositiveButton("ОК") { dialog, _ ->
                                            backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                            dialog.dismiss()

                                        }
                                        .setNegativeButton("Отмена") { dialog, _ ->
                                            dialog.dismiss()
                                        }
                                        .show()
                                }
                            }
                        }else{
                            AlertDialog.Builder(requireContext())
                                .setTitle("Необходим доступ к местоположению")
                                .setMessage("Предоставьте доступ для корректной работы приложения.")
                                .setPositiveButton("ОК") { dialog, _ ->
                                    fineLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    dialog.dismiss()

                                }
                                .setNegativeButton("Отмена") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .show()
                        }
                    }else {
                        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)){
                            centerMyLocation()
                        }else{
                            AlertDialog.Builder(requireContext())
                                .setTitle("Необходим доступ к местоположению")
                                .setMessage("Предоставьте доступ для корректной работы приложения.")
                                .setPositiveButton("ОК") { dialog, _ ->
                                    fineLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    dialog.dismiss()
                                }
                                .setNegativeButton("Отмена") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .show()
                        }
                    }
                }
            }
        }
    }
    //ФУНКЦИЯ ДЛЯ ОТЦЕНТРОВКИ МОЕГО ПОЛОЖЕНИЯ НА КАРТЕ ПО НАЖАТИЮ
    private fun centerMyLocation(){
        binding.mapView1.controller.animateTo(mLocOverlay.myLocation)
        mLocOverlay.enableFollowLocation()

    }
    //ФУНКЦИЯ ДЛЯ ПРОВЕРКИ СОСТОЯНИЯ СЕРВИСА ПРИ ИЗМЕНЕНИИ ЖИЗНЕННОГО ЦИКЛА
    private fun checkServiceState(){
        isServiceRunning = LocationService.isRunning
        if (isServiceRunning){
            binding.fStartStop.setImageResource(R.drawable.stop)
            startTimer()
        }
    }
    private fun updateTime(){
        viewModel.timeData.observe(viewLifecycleOwner){
         binding.tvTime.text = it
        }
    }
    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun locationUpdates(){
        viewModel.locationUpdates.observe(viewLifecycleOwner){
            val distance =  "Дистанция: ${String.format("%.1f м", it.distance)}"
            val currentVelocity = "Текущая скорость: ${String.format("%.1f км/ч", 3.6*it.velocity)}"
            binding.tvAverageVel.text = "Средняя скорость: ${getAverageSpeed(it.distance)} км/ч"
            binding.tvDistance.text = distance
            binding.tvVelocity.text = currentVelocity
            locationModel = it
            updatePolyline(it.geoPointsList)
        }
    }

    //ФУНКЦИЯ ДЛЯ РАССЧЕТА СРЕДНЕЙ СКОРОСТИ ДВИЖЕНИЯ ПРИ ЗАПИСИ МАРШРУТА

    @SuppressLint("DefaultLocale")
    private fun getAverageSpeed(distance: Float): String{
        return String.format("%.1f",
            (distance/((System.currentTimeMillis() - startTime)/1000f))*3.6f)
    }


    //ФУНКЦИЯ ДЛЯ ЗАПУСКА ТАЙМЕРА
    private fun startTimer(){
        timer?.cancel()
        timer = Timer()
        startTime = LocationService.startTime//время из компаньон обджект в сервис
        timer?.schedule(object : TimerTask(){
            override fun run() {
               activity?.runOnUiThread {
                   viewModel.timeData.value = getCurrentTime()
               }
            }

        },1000,1000)
    }
    //ФУНКЦИЯ КОТОРАЯ БЕРЕТ ТЕКУЩЕЕ ВРЕМЯ
    private fun getCurrentTime(): String{
        return "Время: ${
            TimeUtils.getTime(System.currentTimeMillis() - startTime)} "
    }



    //ФУНКЦИЯ ДЛЯ ПРОВЕРКИ ЗАПУЩЕН ЛИ УЖЕ СЕРВИС
    private fun startStopService(){
        if (!isServiceRunning){
            requestNotificationPermission()
        }else{
            activity?.stopService(Intent(requireContext()
                , LocationService::class.java))//остановили сервис
            binding.fStartStop.setImageResource(R.drawable.play)//поменяли иконку кнопки
            timer?.cancel()
            val track = getTrackItem()
            DialogManager.showSaveDialog(requireContext(),
                track,
                object : DialogManager.Listener{
                override fun onClick() {
                    viewModel.insertTrack(track)
                }

            })
        }
        isServiceRunning = !isServiceRunning
    }
    @SuppressLint("DefaultLocale")
    private fun getTrackItem(): TrackItemEntity{
        return TrackItemEntity(
            null,
            getCurrentTime(),
            TimeUtils.getDate(),
            String.format("%.1f", locationModel?.distance?.div(1000)?:0),
            getAverageSpeed(locationModel?.distance?:0.0f),
            geoPointsToString(locationModel?.geoPointsList?:listOf())
        )
    }
    private fun startLocationService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.startForegroundService(Intent(requireContext(), LocationService::class.java))
        }else {
            activity?.startService(Intent(requireContext(), LocationService::class.java))
        }
        binding.fStartStop.setImageResource(R.drawable.stop)
        LocationService.startTime = System.currentTimeMillis()
        startTimer()

    }







    @SuppressLint("ServiceCast")
    private fun checkLocationEnabled() {
        val locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            true
        } else {
            DialogManager.showLocEnableDialog(requireContext(),
                object : DialogManager.Listener{
                    override fun onClick() {
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivity(intent)

                    }

                })
        }
    }
    //ФУНКЦИЯ В КОТОРУЮ ПРИХОДЯТ ЗНАЧЕНИЯ СКОРОСТИ, ДИСТАНЦИИ И ГЕО ТОЧЕК ИЗ СЕРВИСА
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onModelEvent(event: LocationModelEvent) {
        val model = event.model
         updateUi(model)

    }
    fun updateUi(model: LocationModel) {
        // Обновляем UI с данными из модели
        viewModel.locationUpdates.value = model

    }


    //ФУНКЦИЯ ДЛЯ СОЗДАНИЯ РАЗМЕТКИ НА КАРТЕ

    //ФУНКЦИЯ ДОБАВЛЯЕТ ТОЧКИ НА ЭКРАН
    private fun addPoint(list: List<GeoPoint>){
        if (list.isNotEmpty())polyline?.addPoint(list[list.size-1])
    }
    //ФУНКЦИЯ В КОТОРОЙ ДОАВБЯЕМ НА ЭКРАН ВСЕ НОВЫЕ ТОЧКИ,ДОБАВЛЕННЫЕ, ПОКА ПРИЛОЖЕНИЕ БЫЛО СВЕРНУТО
    private fun fillPolyline(list:List<GeoPoint>){
        list.forEach {
            polyline?.addPoint(it)
        }
    }
    private fun updatePolyline(list:List<GeoPoint>){
        if (list.size > 1 && firstStart){
            fillPolyline(list)
            firstStart = false
        }else{
            addPoint(list)
        }
    }

    private fun geoPointsToString(list:List<GeoPoint>): String{
        val sb = StringBuilder()
        list.forEach {
            sb.append("${it.latitude},${it.longitude}/")
        }
        Log.d("MyLog","Points:$sb")
        return sb.toString()
    }







    companion object {
        @JvmStatic
        fun newInstance() = MainFragment1()

    }



    override fun onResume() {
        super.onResume()
        binding.mapView1.onResume()
        firstStart = true
    }
    override fun onPause() {
        super.onPause()
        binding.mapView1.onPause()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        // Отписываемся при уничтожении view
        EventBus.getDefault().unregister(this)
    }





}