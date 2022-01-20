package com.qytech.serialportdemo

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
    val statusS3 by viewModel.readStatusS3.collectAsState()
    val statusS4 by viewModel.readStatusS4.collectAsState()
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            StatusButton(
                modifier = Modifier
                    .weight(1.0f)
                    .padding(16.dp),
                onClickListener = { viewModel.write("LEDR") },
                status = statusS3,
                text = "ttyS4 Status: ${statusS3.message} "
            )
            StatusButton(
                modifier = Modifier
                    .weight(1.0f)
                    .padding(16.dp),
                onClickListener = { viewModel.write("LEDR") },
                status = statusS4,
                text = "ttyS4 Status: ${statusS4.message} "
            )
        }
        LazyRow {
            items(CarStatus.values().filter { it.value.isNotEmpty() }) { status ->
                StatusButton(
                    modifier = Modifier
                        .width(200.dp)
                        .padding(16.dp),
                    onClickListener = {
                        viewModel.write(status)
                    },
                    status = status,
                    text = status.message
                )
            }
        }
    }

}

@Composable
fun StatusButton(modifier: Modifier, onClickListener: () -> Unit, status: CarStatus, text: String) {
    Button(
        onClick = onClickListener,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = animateColorAsState(
                targetValue = status.color,
                tween(600)
            ).value,
            contentColor = Color.White
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.h6,
        )
    }
}

@Preview
@Composable
fun SerialPortWritePreview() {
    SerialPortWrite()
}