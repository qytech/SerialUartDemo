package com.qytech.serialportdemo

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SerialPortWrite(viewModel: SerialPortViewModel = viewModel()) {
    var current by remember {
        mutableStateOf(0)
    }

//    var selectedIndex by remember {
//        mutableStateOf(0)
//    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        LazyColumn {
//            itemsIndexed(viewModel.devicesList, { _, path -> path }) { index, path ->
//
//                Row(Modifier.padding(vertical = 16.dp)) {
//                    RadioButton(selected = selectedIndex == index, onClick = {
//                        selectedIndex = index
//                        viewModel.selectDevice(path)
//                    })
//                    Text(text = "device path $path")
//                }
//            }
//        }
        Button(
            onClick = {
                current++
                if (current >= CarStatus.values().size) {
                    current = 0
                }
                viewModel.write(CarStatus.values()[current])
            },
            modifier = Modifier
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = animateColorAsState(
                    targetValue = CarStatus.values()[current].color,
                    tween(600)
                ).value,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "当前状态为: ${CarStatus.values()[current].message}",
                style = MaterialTheme.typography.h6,
            )
        }
        LazyRow {
            items(CarStatus.values()) { status ->
                Button(
                    onClick = {
                        viewModel.write(status)
                        current = status.ordinal
                    },
                    modifier = Modifier
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = animateColorAsState(
                            targetValue = status.color,
                            tween(600)
                        ).value,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = status.message,
                        style = MaterialTheme.typography.h6,
                    )
                }
            }
        }

    }

}

@Preview
@Composable
fun SerialPortWritePreview() {
    SerialPortWrite()
}