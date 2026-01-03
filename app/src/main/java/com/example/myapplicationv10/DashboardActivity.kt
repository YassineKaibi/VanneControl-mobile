package com.example.myapplicationv10

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.transform.CircleCropTransformation
import com.example.myapplicationv10.adapter.ActiveValvesAdapter
import com.example.myapplicationv10.model.Valve
import com.example.myapplicationv10.databinding.ActivityDashboardBinding
import com.example.myapplicationv10.network.NetworkResult
import com.example.myapplicationv10.repository.UserRepository
import com.example.myapplicationv10.viewmodel.DashboardViewModel
import com.example.myapplicationv10.websocket.WebSocketManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * DashboardActivity - Main hub with device overview and avatar display
 *
 * Features:
 * - Active valves RecyclerView
 * - Navigation to other screens
 * - WebSocket real-time updates
 * - User avatar in header (loaded with Coil)
 */
class DashboardActivity : BaseActivity() {

    private lateinit var activeValvesAdapter: ActiveValvesAdapter
    private lateinit var webSocketManager: WebSocketManager
    private lateinit var userRepository: UserRepository

    // ViewModel
    private val viewModel: DashboardViewModel by viewModels()

    // View Binding
    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userRepository = UserRepository(this)

        initializeViews()
        setupProfileButton()
        setupActiveValvesRecyclerView()
        setupNavigationButtons()
        setupSwipeRefresh()
        observeViewModel()
        setupWebSocket()
        loadUserAvatar()
    }

    private fun initializeViews() {
        // Views are accessible via binding
    }

    private fun setupProfileButton() {
        binding.profileIcon.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setupActiveValvesRecyclerView() {
        binding.activeValvesRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        activeValvesAdapter = ActiveValvesAdapter(emptyList())
        binding.activeValvesRecyclerView.adapter = activeValvesAdapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshDevices()
            loadUserAvatar()
        }
    }

    private fun setupNavigationButtons() {
        binding.valveControlCard.setOnClickListener {
            val currentState = viewModel.devicesState.value
            if (currentState is NetworkResult.Success && currentState.data.isNotEmpty()) {
                val firstDevice = currentState.data.first()
                val intent = Intent(this, ValveManagementActivity::class.java)
                intent.putExtra("DEVICE_ID", firstDevice.id)
                intent.putExtra("DEVICE_NAME", firstDevice.name)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Aucun appareil disponible", Toast.LENGTH_SHORT).show()
            }
        }

        binding.timingPlanCard.setOnClickListener {
            val currentState = viewModel.devicesState.value
            if (currentState is NetworkResult.Success && currentState.data.isNotEmpty()) {
                val firstDevice = currentState.data.first()
                val intent = Intent(this, TimingPlanActivity::class.java)
                intent.putExtra("DEVICE_ID", firstDevice.id)
                intent.putExtra("DEVICE_NAME", firstDevice.name)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Aucun appareil disponible", Toast.LENGTH_SHORT).show()
            }
        }

        binding.historyCard.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        binding.statisticsCard.setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }

        binding.notificationsCard.setOnClickListener {
            Snackbar.make(binding.main, "Notifications à venir...", Snackbar.LENGTH_SHORT).show()
        }
    }

    /**
     * Load user profile and display avatar in header
     */
    private fun loadUserAvatar() {
        lifecycleScope.launch {
            when (val result = userRepository.getUserProfile()) {
                is NetworkResult.Success -> {
                    val user = result.data

                    // Update welcome text with user's first name
                    val welcomeText = if (!user.firstName.isNullOrEmpty()) {
                        "Welcome, ${user.firstName}!"
                    } else {
                        "Welcome!"
                    }
                    binding.welcome.text = welcomeText

                    // Load avatar with Coil (fix localhost URLs)
                    binding.profileIcon.load(com.example.myapplicationv10.utils.Constants.fixAvatarUrl(user.avatarUrl)) {
                        crossfade(true)
                        placeholder(R.drawable.ic_avatar_placeholder)
                        error(R.drawable.ic_avatar_placeholder)
                        transformations(CircleCropTransformation())
                    }
                }
                is NetworkResult.Error -> {
                    // Keep default avatar on error
                    binding.profileIcon.load(R.drawable.ic_avatar_placeholder) {
                        transformations(CircleCropTransformation())
                    }
                }
                else -> {}
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.devicesState.collect { result ->
                when (result) {
                    is NetworkResult.Idle -> binding.swipeRefreshLayout.isRefreshing = false
                    is NetworkResult.Loading -> { /* Optional: show loader */ }
                    is NetworkResult.Success -> {
                        binding.swipeRefreshLayout.isRefreshing = false
                        val activePistons = viewModel.getActivePistons()

                        // Convert to Valve objects for adapter
                        val activeValves = activePistons.map { (device, piston) ->
                            Valve(
                                name = "Valve ${piston.pistonNumber}",
                                lastChanged = piston.lastTriggered ?: "Unknown"
                            )
                        }

                        // Update adapter
                        activeValvesAdapter.updateValves(activeValves)

                        Toast.makeText(
                            this@DashboardActivity,
                            "${result.data.size} device(s), ${activeValves.size} active valve(s)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is NetworkResult.Error -> {
                        binding.swipeRefreshLayout.isRefreshing = false
                        Snackbar.make(binding.main, result.message, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isRefreshing.collect { binding.swipeRefreshLayout.isRefreshing = it }
        }
    }

    private fun setupWebSocket() {
        webSocketManager = WebSocketManager.getInstance(this)
        webSocketManager.connect()

        webSocketManager.addPistonUpdateListener { message ->
            lifecycleScope.launch(Dispatchers.Main) {
                viewModel.refreshDevices()
                Toast.makeText(
                    this@DashboardActivity,
                    "Piston ${message.pistonNumber} mis à jour: ${message.state}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        webSocketManager.addDeviceStatusListener { message ->
            lifecycleScope.launch(Dispatchers.Main) {
                viewModel.refreshDevices()
                Toast.makeText(
                    this@DashboardActivity,
                    "Appareil ${message.status}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDevices()
        loadUserAvatar()
        if (!webSocketManager.isConnected()) webSocketManager.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketManager.disconnect()
    }
}