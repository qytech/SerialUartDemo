package com.qytech.serialportdemo

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qytech.serialportdemo.model.CarLED

@Composable
fun SerialPortView(
    isMarquee: Boolean,
    statusS3: CarLED.Status,
    statusS4: CarLED.Status,
    readCarStatus: () -> Unit = {},
    onCheckedChange: (Boolean) -> Unit = {},
    onWriteCharset: (CarLED.Status) -> Unit = {},
    onWriteStatusClick: (CarLED.Status) -> Unit = {},
    onUpdateFirmwareClick: () -> Unit = {},
    onCustomMessageClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isMarquee,
                onCheckedChange = onCheckedChange
            )
            Text(
                modifier = Modifier.padding(16.dp),
                text = "Switch LED marquee",
                style = MaterialTheme.typography.h5,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Query Status", fontSize = 32.sp, fontWeight = FontWeight.Bold)

        Row {
            StatusButton(
                modifier = Modifier
                    .weight(1.0f)
                    .padding(16.dp),
                onClickListener = readCarStatus,
                status = statusS3,
                text = "ttyS3 Status: ${statusS3.message} "
            )
            StatusButton(
                modifier = Modifier
                    .weight(1.0f)
                    .padding(16.dp),
                onClickListener = readCarStatus,
                status = statusS4,
                text = "ttyS4 Status: ${statusS4.message} "
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("Download Charset", fontSize = 32.sp, fontWeight = FontWeight.Bold)

        LazyRow {
            items(CarLED.Status.values().filter { it.charset.isNotEmpty() }) { status ->
                StatusButton(
                    modifier = Modifier
                        .width(200.dp)
                        .padding(16.dp),
                    onClickListener = {
                        onWriteCharset(status)
                    },
                    status = status,
                    text = status.message
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("Set Status", fontSize = 32.sp, fontWeight = FontWeight.Bold)

        LazyRow {
            items(CarLED.Status.values().filter { it.message.isNotEmpty() }) { status ->
                StatusButton(
                    modifier = Modifier
                        .width(200.dp)
                        .padding(16.dp),
                    onClickListener = { onWriteStatusClick(status) },
                    status = status,
                    text = status.message
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("Update firmware", fontSize = 32.sp, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            StatusButton(
                modifier = Modifier
                    .weight(1.0f)
                    .padding(16.dp),
                onClickListener = onUpdateFirmwareClick,
                status = CarLED.Status.SUBSCRIBE,
                text = "Update firmware"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("Show custom message", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            StatusButton(
                modifier = Modifier
                    .weight(1.0f)
                    .padding(16.dp),
                onClickListener = onCustomMessageClick,
                status = CarLED.Status.SUBSCRIBE,
                text = "Show Custom"
            )
        }

    }
}

@Composable
fun SerialPortWrite(viewModel: SerialPortViewModel = viewModel()) {
    val statusS3 by viewModel.readStatusS3.collectAsState()
    val statusS4 by viewModel.readStatusS4.collectAsState()
    var isMarquee by remember { mutableStateOf(false) }
    val context = LocalContext.current
    SerialPortView(
        isMarquee = isMarquee,
        statusS3 = statusS3,
        statusS4 = statusS4,
        readCarStatus = viewModel::readCarStatus,
        onCheckedChange = { checked ->
            isMarquee = checked
            if (isMarquee) {
                viewModel.startMarquee()
            } else {
                viewModel.stopMarquee()
            }
        },
        onWriteCharset = { status ->
            viewModel.writeCharset(status)
        },
        onWriteStatusClick = { status ->
            viewModel.writeCarStatus(status)
        },
        onUpdateFirmwareClick = {
            viewModel.updateFirmware(context)
        },
        onCustomMessageClick = {
            viewModel.showCustomMessage()
        }
    )

}

@Composable
fun StatusButton(
    modifier: Modifier = Modifier,
    onClickListener: () -> Unit,
    status: CarLED.Status,
    text: String
) {
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

@Preview(
    showSystemUi = true,
    showBackground = true,
)
@Composable
fun SerialPortWritePreview() {
    MaterialTheme {
        SerialPortView(false, CarLED.Status.REST, CarLED.Status.SUBSCRIBE)
    }
}