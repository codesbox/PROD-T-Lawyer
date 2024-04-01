package ru.sber_tech.prod_mobile.screens.addMeetScreen


import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ru.sber_tech.domain.addMeetScreen.AddMeetState.*
import ru.sber_tech.prod_mobile.R.drawable
import ru.sber_tech.prod_mobile.components.SegmentedButtonSelect
import ru.sber_tech.prod_mobile.components.TimePickerDialog
import ru.sber_tech.prod_mobile.components.YandexMap
import ru.sber_tech.prod_mobile.navigation.Destinations
import ru.sber_tech.prod_mobile.utils.DateUtils
import ru.sber_tech.prod_mobile.utils.GetLocation
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun AddMeetScreen(navController: NavController, viewModel: AddMeetScreenViewModel) {

    GetLocation(viewModel = viewModel)

    LaunchedEffect(key1 = Unit, block = {
        viewModel.loadElements()
    })

    when (val uiState = viewModel.addMeetState.collectAsStateWithLifecycle().value) {
        is Adding -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                YandexMap.Render(viewModel, onBack = {
                    navController.popBackStack()
                })
                Image(
                    painter = painterResource(id = drawable.map_cursor),
                    contentDescription = "",
                    Modifier.size(20.dp)
                )
            }

            val address by viewModel.addressState.collectAsStateWithLifecycle()

            OutlinedCard(
                Modifier
                    .padding(start = 15.dp, end = 15.dp)
                    .fillMaxWidth().clickable {
                        viewModel.setSearchState("")
                        navController.navigate(Destinations.SearchScreenRoute.route)
                    }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier
                            .padding(15.dp)
                            .weight(1f),
                        text = address ?: "Введите город, улицу, дом",
                    )
                }

            }

            val operations by viewModel.operations.collectAsStateWithLifecycle()
            SegmentedButtonSelect(
                selectedElements = uiState.model.selectedEvents,
                options = operations
            ) {
                viewModel.addOrDeleteElement(it)
            }

            PickDateDialog(onConfirm = {
                viewModel.setDate(it)
            })
            PickTimeDialog(onConfirm = {
                viewModel.setTime(it)
            })
            val context = LocalContext.current
            Button(
                onClick = {
                    viewModel.publish(onSuccess = {
                        navController.popBackStack()
                    }, onError = {
                        Toast.makeText(context, "Error", Toast.LENGTH_LONG).show()
                    })
                },
                enabled = uiState.model.date != "" && uiState.model.time != "" && uiState.model.selectedEvents.isNotEmpty(),
            ) {
                Text(text = "Готово")
            }
        }

        is ErrorOnReceipt -> Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Error",
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center,
            )
        }

        is Loading -> Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickDateDialog(onConfirm: (String) -> Unit) {
    var openDialog by remember { mutableStateOf(false) }
    Button(onClick = { openDialog = true }) {
        Text(text = "Выбрать дату")
    }


    if (openDialog) {
        val datePickerState = rememberDatePickerState()
        val formattedDate by remember {
            derivedStateOf {
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    .format(DateUtils.convertMillisToLocalDate(datePickerState.selectedDateMillis!!))
            }
        }
        val confirmEnabled = remember {
            derivedStateOf { datePickerState.selectedDateMillis != null }
        }
        DatePickerDialog(
            onDismissRequest = {
                openDialog = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog = false
                        onConfirm(formattedDate)
                    }, enabled = confirmEnabled.value
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    openDialog = false
                }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickTimeDialog(onConfirm: (String) -> Unit) {
    var openDialog by remember { mutableStateOf(false) }
    Button(onClick = { openDialog = true }) {
        Text(text = "Выбрать время")
    }

    if (openDialog) {
        val timePickerState = rememberTimePickerState()
        val pickedTime by remember {
            derivedStateOf { LocalTime.of(timePickerState.hour, timePickerState.minute) }
        }
        val formattedTime by remember {
            derivedStateOf { DateTimeFormatter.ofPattern("hh:mm").format(pickedTime) }
        }

        TimePickerDialog(
            onDismissRequest = { openDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    openDialog = false
                    onConfirm(formattedTime)
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    openDialog = false
                }) { Text("Cancel") }
            },
        ) {
            TimePicker(state = timePickerState)
        }
    }
}



