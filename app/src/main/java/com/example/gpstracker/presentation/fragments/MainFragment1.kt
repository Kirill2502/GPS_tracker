package com.example.gpstracker.presentation.fragments


import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.gpstracker.R
import com.example.gpstracker.databinding.FragmentMainBinding
import com.example.gpstracker.presentation.map.MapRender
import com.example.gpstracker.presentation.permissions.PermissionManager
import com.example.gpstracker.presentation.utils.DialogManager
import com.example.gpstracker.presentation.utils.showToast
import com.example.gpstracker.presentation.viewModels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment1 : Fragment() {

    private lateinit var binding: FragmentMainBinding

    private lateinit var permissionManager: PermissionManager

    private lateinit var mapRender: MapRender

    private val viewModel: MainViewModel by activityViewModels()

    // Launchers теперь ТОЛЬКО тут (UI слой их обязан держать)
    private lateinit var fineLocationLauncher: ActivityResultLauncher<String>
    private lateinit var backgroundLocationLauncher: ActivityResultLauncher<String>
    private lateinit var notificationLauncher: ActivityResultLauncher<String>


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        // =========================
        // INIT
        // =========================

        permissionManager = PermissionManager(this)

        mapRender = MapRender(binding.mapView1)
        mapRender.initOSM(requireContext())

        registerLaunchers()

        setOnClicks()

        observeViewModel()

        // проверка GPS включен ли
        checkLocationEnabled()

        // проверка permissions (НО через manager)
        checkPermissionsFlow()

        // ✅ СИНХРОНИЗАЦИЯ СОСТОЯНИЯ (восстанавливаем иконку после пересоздания фрагмента)
        syncTrackingState()
    }

    // ------------------------------------------------------------
    // PERMISSIONS
    // ------------------------------------------------------------

    private fun registerLaunchers() {

        fineLocationLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->

                if (granted) {
                    // дальше может пойти background permission
                    requestBackgroundIfNeeded()
                } else {
                    showToast("Нет доступа к геолокации")
                }
            }

        backgroundLocationLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->

                if (granted) {
                    showToast("Фоновая геолокация разрешена")
                } else {
                    showToast("Фоновая геолокация отклонена")
                }
            }

        notificationLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->

                if (granted) {
                    viewModel.startTracking()
                } else {
                    showToast("Нет уведомлений")
                }
            }
    }

    /**
     * ВАЖНО:
     * Fragment НЕ знает как проверяются permissions
     */
    private fun checkPermissionsFlow() {

        if (!permissionManager.hasFineLocation()) {
            permissionManager.requestFineLocation(fineLocationLauncher)
            return
        }

        requestBackgroundIfNeeded()
    }

    private fun requestBackgroundIfNeeded() {
        if (!permissionManager.hasBackgroundLocation()) {
            permissionManager.requestBackgroundLocation(backgroundLocationLauncher)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {

        if (permissionManager.hasNotificationPermission()) {
            viewModel.startTracking()
        } else {
            permissionManager.requestNotificationPermission(notificationLauncher)
        }
    }

    // ✅ СИНХРОНИЗАЦИЯ СОСТОЯНИЯ (иконка Play/Stop при запуске)
    private fun syncTrackingState() {
        val icon = if (viewModel.isTrackingActive()) R.drawable.stop else R.drawable.play
        binding.fStartStop.setImageResource(icon)
    }

    // ------------------------------------------------------------
    // UI EVENTS
    // ------------------------------------------------------------

    private fun setOnClicks() {

        binding.fStartStop.setOnClickListener {
            // Только проверка прав
            if (!permissionManager.hasFineLocation()) {
                checkPermissionsFlow()
                return@setOnClickListener
            }
            if (!permissionManager.hasBackgroundLocation()) {
                requestBackgroundIfNeeded()
                return@setOnClickListener
            }
            if (!viewModel.isTrackingActive() && !permissionManager.hasNotificationPermission()) {
                permissionManager.requestNotificationPermission(notificationLauncher)
                return@setOnClickListener
            }

            // Всю логику старт/стоп и диалог сохранения делегируем ViewModel
            viewModel.onStartStopClicked()
        }

        binding.fMyPosition.setOnClickListener {
            handleMyPosition()
        }
    }

    // ------------------------------------------------------------
    // ACTIONS (UI → VIEWMODEL)
    // ------------------------------------------------------------

    private fun handleMyPosition() {

        if (permissionManager.hasFineLocation()) {
            mapRender.centerMyLocation()
        } else {
            checkPermissionsFlow()
        }
    }

    // ------------------------------------------------------------
    // VIEWMODEL OBSERVE
    // ------------------------------------------------------------

    private fun observeViewModel() {

        viewModel.trackingTime.observe(viewLifecycleOwner) { time ->
            binding.tvTime.text = time
        }

        viewModel.locationData.observe(viewLifecycleOwner) { model ->
            binding.tvDistance.text = "Дистанция: ${String.format("%.1f", model.distance)} м"
            binding.tvVelocity.text = "Скорость: ${String.format("%.1f", model.velocity)} км/ч"
            binding.tvAverageVel.text = "Средняя скорость: ${model.averageSpeed} км/ч"

            mapRender.updatePolyline(model.geoPointsList)
        }

        viewModel.isTracking.observe(viewLifecycleOwner) { isActive ->
            val icon = if (isActive) R.drawable.stop else R.drawable.play
            binding.fStartStop.setImageResource(icon)
        }

        viewModel.events.observe(viewLifecycleOwner) { event ->
            when (event) {
                is MainViewModel.Event.ShowSaveDialog -> {
                    DialogManager.showSaveDialog(
                        context = requireContext(),
                        time = event.time,
                        speed = event.speed,
                        distance = event.distance,
                        listener = object : DialogManager.Listener {
                            override fun onClick() {
                                viewModel.onSaveDialogConfirmed()
                            }
                        }
                    )
                }
                is MainViewModel.Event.TrackSaved -> showToast("Трек сохранен")
                is MainViewModel.Event.TrackingStarted -> showToast("Трекинг запущен")
                is MainViewModel.Event.TrackingStopped -> showToast("Трекинг остановлен")
                is MainViewModel.Event.Error -> showToast(event.message)
            }
        }
    }


    // ------------------------------------------------------------
    // SYSTEM CHECKS
    // ------------------------------------------------------------

    private fun checkLocationEnabled() {

        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            DialogManager.showLocEnableDialog(
                requireContext(),
                object : DialogManager.Listener {
                    override fun onClick() {
                        startActivity(
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        )
                    }
                }
            )
        }
    }




    override fun onDestroyView() {
        if (::mapRender.isInitialized) {
            mapRender.detach()
        }
        if (::binding.isInitialized) {
           // binding.mapView1.onDetach()
        }
        super.onDestroyView()
    }
    companion object {
        @JvmStatic
        fun newInstance() = MainFragment1()
    }

}