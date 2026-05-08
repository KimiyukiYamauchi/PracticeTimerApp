package com.example.practicetimerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.practicetimerapp.ui.theme.PracticeTimerAppTheme

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource

import androidx.lifecycle.viewmodel.compose.viewModel

import android.media.MediaPlayer
import android.media.RingtoneManager
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.VerticalDivider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PracticeTimerAppTheme {
                Main()
            }
        }
    }
}

@Composable
fun Main() {
    val viewModel: TimerViewModel = viewModel()
    Scaffold(
        bottomBar = { ButtonView(viewModel) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            TimerView(viewModel)
        }
    }
    if (viewModel.finish) {
        FinishDialog(viewModel)
    }
} // Mainの閉じかっこ

@Composable
fun FinishDialog(viewModel: TimerViewModel) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val mediaPlayer = MediaPlayer.create(context, uri).apply {
            isLooping = true
            start()
        }

        onDispose {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    AlertDialog(
        title = { Text("タイマー終了") },
        text = { Text("時間が来ました！") },
        onDismissRequest = { viewModel.applyFinish() },
        confirmButton = {
            TextButton(onClick = { viewModel.applyFinish() }) {
                Text("OK")
            }
        })
} // FinishDialogの閉じかっこ

@Composable
fun TimerView(viewModel: TimerViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val size = minOf(maxWidth, maxHeight)

            Box(
                modifier = Modifier
                    .size(size)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { viewModel.progress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 16.dp
                )
                Text(
                    text = viewModel.timeLeftText,
                    fontSize = 48.sp                )

            }
        }

        OutlinedTextField(
            value = viewModel.totalTimeText,
            onValueChange = {},
            label = { Text(text = "指定の時間") },
            readOnly = true,
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontSize = 24.sp
            )
        )
    }

} // TimerViewの閉じかっこ

@Composable
fun ButtonView(viewModel: TimerViewModel) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .navigationBarsPadding()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        FilledIconButton( // +1分ボタン
            onClick = { viewModel.plus1() },
            enabled = viewModel.canPlus1
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.img_plus_1),
                contentDescription = "+1"
            )
        }
        FilledIconButton( // -1分ボタン
            onClick = { viewModel.minus1() },
            enabled = viewModel.caMinus1
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.img_minus_1),
                contentDescription = "-1"
            )
        }
        VerticalDivider( // 仕切り線
            modifier = Modifier
                .fillMaxHeight()
                .padding(4.dp),
            thickness = 2.dp
        )
        FilledIconButton(
            // スタート
            onClick = { viewModel.startOrPauseTimer() },
        ) {
            val iconId = if (viewModel.isRunning) R.drawable.img_pause else R.drawable.img_play
            Icon(
                imageVector = ImageVector.vectorResource(id = iconId),
                contentDescription = "start/pause"
            )

        }
        FilledIconButton(
            // リセット
            onClick = { viewModel.resetTimer() },
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.img_reset),
                contentDescription = "reset"
            )
        }
        VerticalDivider(
            modifier = Modifier
                .fillMaxHeight()
                .padding(4.dp),
            thickness = 2.dp
        )
        FilledIconButton(
            // 保存
            onClick = {
                viewModel.saveTotalTime(context)
                Toast.makeText(context, "設定を保存しました", Toast.LENGTH_LONG).show()
            },
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.img_save),
                contentDescription = "save"
            )

        }
        FilledIconButton(
            // ロード
            onClick = {
                viewModel.loadTotalTime(context)
                Toast.makeText(context, "設定を復元しました", Toast.LENGTH_LONG).show()
            },
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.img_load),
                contentDescription = "load"
            )
        }
    }
} // BottomViewの閉じかっこ


@Preview(showBackground = true)
@Composable
fun MainPreview() {
    PracticeTimerAppTheme {
        Main()
    }
}