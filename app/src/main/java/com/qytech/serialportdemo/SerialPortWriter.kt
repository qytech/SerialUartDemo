package com.qytech.serialportdemo

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qytech.serialportdemo.model.CarLED

@Composable
fun SerialPortWrite(viewModel: SerialPortViewModel = viewModel()) {
    val statusS3 by viewModel.readStatusS3.collectAsState()
    val statusS4 by viewModel.readStatusS4.collectAsState()
    var isMarquee by remember { mutableStateOf(false) }
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
            Switch(checked = isMarquee,
                onCheckedChange = { checked ->
                    isMarquee = checked
                    if (isMarquee) {
                        viewModel.startMarquee()
                    } else {
                        viewModel.stopMarquee()
                    }
                })
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
                onClickListener = { viewModel.readCarStatus() },
                status = statusS3,
                text = "ttyS3 Status: ${statusS3.message} "
            )
            StatusButton(
                modifier = Modifier
                    .weight(1.0f)
                    .padding(16.dp),
                onClickListener = { viewModel.readCarStatus() },
                status = statusS4,
                text = "ttyS4 Status: ${statusS4.message} "
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("Download Charset", fontSize = 32.sp, fontWeight = FontWeight.Bold)

        LazyRow {
            items(CarLED.Status.values().filter { it.message.isNotEmpty() }) { status ->
                StatusButton(
                    modifier = Modifier
                        .width(200.dp)
                        .padding(16.dp),
                    onClickListener = {
                        viewModel.writeCharset(status)
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
                    onClickListener = {
                        viewModel.writeCarStatus(status)
                    },
                    status = status,
                    text = status.message
                )
            }
        }


    }

}

@Composable
fun StatusButton(
    modifier: Modifier,
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

@Preview
@Composable
fun SerialPortWritePreview() {
    SerialPortWrite()
}