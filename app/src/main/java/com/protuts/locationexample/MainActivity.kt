package com.protuts.locationexample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.protuts.location.Coordinates
import com.protuts.location.CoordinatesResult
import com.protuts.location.LocationConfig
import com.protuts.location.configureLocationRequest
import com.protuts.location.defaultLocationRequest
import com.protuts.locationexample.ui.MainViewModel
import com.protuts.locationexample.ui.theme.LocationExampleTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocationExampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val state = viewModel.uiState.collectAsState()
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                Coordinates.configureLocationRequest(
                                    LocationConfig(
                                        isBackgroundLocation = true,
                                        isPreciseLocation = true,
                                        showForegroundService = false
                                    )
                                ).startLocationUpdates(this@MainActivity, lifecycleScope)
                            }
                        ) {
                            Text(text = "Start Location Updates")
                        }

                        Button(
                            modifier = Modifier.padding(top = 20.dp),
                            onClick = {
                                Coordinates.configureLocationRequest(
                                    LocationConfig(
                                        locationRequest = defaultLocationRequest(true),
                                        isBackgroundLocation = true,
                                        isPreciseLocation = true,
                                        showForegroundService = false
                                    )
                                ).startLocationUpdates(this@MainActivity, lifecycleScope)
                            }
                        ) {
                            Text(text = "Start Single Updates")
                        }

                        Text(text = "${state.value?.latitude}--${state.value?.longitude}--${state.value?.accuracy}")
                    }
                }
            }
        }

        lifecycleScope.launch {
            Coordinates.location.collectLatest {
                when (it) {
                    is CoordinatesResult.Success -> {
                        viewModel.onLocationUpdate(location = it.location)
                    }

                    is CoordinatesResult.Failure -> {
                        Toast.makeText(
                            this@MainActivity, "${it.locationFailure.ordinal}", Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Coordinates.stopLocationUpdates()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!", modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LocationExampleTheme {
        Greeting("Android")
    }
}