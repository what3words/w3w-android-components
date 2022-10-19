package com.what3words.compose.sample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.what3words.compose.sample.ui.components.LabelCheckBox
import com.what3words.compose.sample.ui.components.MultiLabelTextField
import com.what3words.compose.sample.ui.components.RadioGroup
import com.what3words.compose.sample.ui.components.RadioGroupState
import com.what3words.compose.sample.ui.theme.What3wordscomponentsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            What3wordscomponentsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val radioGroupState = remember {
                        RadioGroupState(
                            items = listOf(
                                "disable voice",
                                "enable voice with inline animation",
                                "enable voice with pop-up animation"
                            ),
                            selectedItemIndex = 0
                        )
                    }
                    RadioGroup(state = radioGroupState)

                }
            }
        }
    }
}


@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    What3wordscomponentsTheme {
        Greeting("Android")
    }
}