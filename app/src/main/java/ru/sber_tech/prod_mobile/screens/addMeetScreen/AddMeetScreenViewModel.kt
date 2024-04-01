package ru.sber_tech.prod_mobile.screens.addMeetScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.sber_tech.domain.addMeetScreen.AddMeetModel
import ru.sber_tech.domain.addMeetScreen.AddMeetState
import ru.sber_tech.domain.addMeetScreen.AddMeetState.Adding
import ru.sber_tech.domain.addMeetScreen.AddMeetState.Loading
import ru.sber_tech.domain.addMeetScreen.AddMeetUseCase
import ru.sber_tech.domain.addMeetScreen.MeetStatus.ERROR_ON_RECEIPT
import ru.sber_tech.domain.addMeetScreen.MeetStatus.SUCCESS
import ru.sber_tech.domain.getAddress.GetAddressUseCase
import ru.sber_tech.prod_mobile.utils.GetCoordsCallBack

class AddMeetScreenViewModel(
    private val addMeetUseCase: AddMeetUseCase,
    private val getAddressUseCase: GetAddressUseCase,
) : ViewModel() {

    private lateinit var getCoordinates: GetCoordsCallBack

    private val _addMeetState = MutableStateFlow<AddMeetState>(Loading)
    val addMeetState = _addMeetState.asStateFlow()

    fun loadElements() {
        _addMeetState.value = Adding(model = AddMeetModel(emptyList(), "", "", 0.0, 0.0))
    }

    fun addOrDeleteElement(element: String) {
        if (addMeetState.value is Adding) {
            val addingState = addMeetState.value as Adding
            if (element in addingState.model.selectedEvents) {
                _addMeetState.value = addingState.copy(
                    model = addingState.model.copy(selectedEvents = addingState.model.selectedEvents.filter { it != element })
                )
            } else {
                _addMeetState.value = addingState.copy(
                    model = addingState.model.copy(
                        selectedEvents = addingState.model.selectedEvents + listOf(element)
                    )
                )
            }
        }
    }

    fun setCoords(getCoords: GetCoordsCallBack) {
        getCoordinates = getCoords
    }

    fun setPoint(latitude: Double, longitude: Double){
        if(addMeetState.value is Adding){
            val addingState = addMeetState.value as Adding
            _addMeetState.value = addingState.copy(model = addingState.model.copy(latitude = latitude, longitude = longitude))
        }
    }

    fun setDate(date: String) {
        if (addMeetState.value is Adding) {
            val addingState = addMeetState.value as Adding
            _addMeetState.value = addingState.copy(
                model = addingState.model.copy(
                    date = date
                )
            )
        }
    }

    fun setTime(time: String) {
        if (addMeetState.value is Adding) {
            val addingState = addMeetState.value as Adding
            _addMeetState.value = addingState.copy(
                model = addingState.model.copy(
                    time = time
                )
            )
        }
    }

    fun publish(onSuccess: () -> Unit, onError: () -> Unit) {
        if (addMeetState.value is Adding) {
            val addingState = addMeetState.value as Adding
            val point = getCoordinates.getCoords()
            if (addingState.model.date == "" || addingState.model.time == "" || addingState.model.selectedEvents.isEmpty() || point == null) {
                return
            }
            _addMeetState.value = addingState.copy(
                model = addingState.model.copy(
                    latitude = point.latitude,
                    longitude = point.longitude
                )
            )
            viewModelScope.launch {
                when (addMeetUseCase.execute(addingState.model)) {
                    SUCCESS -> onSuccess()
                    ERROR_ON_RECEIPT -> onError()
                }
            }
        }
    }
}