package com.example.navigationlab.recipes.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed

// Pastel colors matching nav3-recipes theme
private val PastelGreen = Color(0xFFA8E6CF)
private val PastelBlue = Color(0xFFA8D8EA)
private val PastelRed = Color(0xFFFFB3BA)
private val PastelPink = Color(0xFFFFD1DC)
private val PastelPurple = Color(0xFFD5AAFF)
private val PastelMauve = Color(0xFFE0BBE4)

@Composable
private fun ContentBase(
    title: String,
    modifier: Modifier = Modifier,
    onNext: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(48.dp))
    ) {
        Text(
            modifier = Modifier.padding(24.dp),
            fontWeight = FontWeight.Bold,
            text = title,
        )
        if (content != null) content()
        if (onNext != null) {
            Button(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = onNext,
            ) {
                Text("Next")
            }
        }
    }
}

@Composable
fun ContentGreen(
    title: String,
    modifier: Modifier = Modifier,
    onNext: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) = ContentBase(title, modifier.background(PastelGreen), onNext, content)

@Composable
fun ContentBlue(
    title: String,
    modifier: Modifier = Modifier,
    onNext: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) = ContentBase(title, modifier.background(PastelBlue), onNext, content)

@Composable
fun ContentRed(
    title: String,
    modifier: Modifier = Modifier,
    content: (@Composable () -> Unit)? = null,
) = ContentBase(title, modifier.background(PastelRed), content = content)

@Composable
fun ContentPink(
    title: String,
    modifier: Modifier = Modifier,
) = ContentBase(title, modifier.background(PastelPink))

@Composable
fun ContentPurple(
    title: String,
    modifier: Modifier = Modifier,
) = ContentBase(title, modifier.background(PastelPurple))

@Composable
fun ContentMauve(
    title: String,
    modifier: Modifier = Modifier,
    content: (@Composable () -> Unit)? = null,
) = ContentBase(title, modifier.background(PastelMauve), content = content)

// -- Tab screen composables (R09-R12) --

private val PastelYellow = Color(0xFFFFF9C4)
private val PastelOrange = Color(0xFFFFE0B2)
private val PastelTeal = Color(0xFFB2DFDB)

@Composable
fun TabAlphaScreen(onDetail: () -> Unit) {
    ContentGreen("Tab Alpha") {
        Button(onClick = dropUnlessResumed(block = onDetail)) {
            Text("Go to Detail")
        }
    }
}

@Composable
fun TabAlphaDetailScreen(from: String, onEdit: () -> Unit, result: String?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(PastelYellow)
            .clip(RoundedCornerShape(48.dp)),
    ) {
        Text(
            modifier = Modifier.padding(24.dp),
            fontWeight = FontWeight.Bold,
            text = "Alpha Detail (from: $from)",
        )
        if (result != null) {
            Text("Edit result: $result")
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = dropUnlessResumed(block = onEdit)) {
            Text("Edit")
        }
    }
}

@Composable
fun TabAlphaEditScreen(from: String, onDone: (String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(PastelOrange)
            .clip(RoundedCornerShape(48.dp)),
    ) {
        Text(
            modifier = Modifier.padding(24.dp),
            fontWeight = FontWeight.Bold,
            text = "Edit (from: $from)",
        )
        val textState = rememberTextFieldState()
        OutlinedTextField(
            state = textState,
            label = { Text("Enter a value") },
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = dropUnlessResumed { onDone(textState.text.toString()) },
            enabled = textState.text.isNotBlank(),
        ) {
            Text("Done")
        }
    }
}

@Composable
fun TabBetaScreen(onDetail: () -> Unit) {
    ContentBlue("Tab Beta") {
        Button(onClick = dropUnlessResumed(block = onDetail)) {
            Text("Go to Detail")
        }
    }
}

@Composable
fun TabBetaDetailScreen() {
    ContentPurple("Beta Detail (SAME_AS_PARENT)")
}

@Composable
fun TabGammaScreen(onDetail: () -> Unit) {
    ContentMauve("Tab Gamma") {
        Button(onClick = dropUnlessResumed(block = onDetail)) {
            Text("Go to Detail")
        }
    }
}

@Composable
fun TabGammaDetailScreen(result: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(PastelTeal)
            .clip(RoundedCornerShape(48.dp)),
    ) {
        Text(
            modifier = Modifier.padding(24.dp),
            fontWeight = FontWeight.Bold,
            text = "Gamma Detail (ViewModel)",
        )
        Text("ViewModel result: $result")
    }
}

// -- Deep link screen composables (R13) --

@Composable
fun DeepLinkHomeScreen(onNavigate: () -> Unit) {
    ContentGreen("Deep Link Home") {
        Button(onClick = dropUnlessResumed(block = onNavigate)) {
            Text("Navigate to Target")
        }
    }
}

@Composable
fun DeepLinkTargetScreen(param: String) {
    ContentBlue("Deep Link Target") {
        Text("Param: $param")
    }
}

// -- Transition screen composables (R14-R16) --

@Composable
fun TransitionHomeScreen(
    onSlide: () -> Unit,
    onFade: () -> Unit,
    onDialog: () -> Unit,
    onSheet: () -> Unit,
) {
    ContentGreen("Transition Home") {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = dropUnlessResumed(block = onSlide)) {
                Text("Slide Transition")
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = dropUnlessResumed(block = onFade)) {
                Text("Fade Transition")
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = dropUnlessResumed(block = onDialog)) {
                Text("Open Dialog")
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = dropUnlessResumed(block = onSheet)) {
                Text("Open Bottom Sheet")
            }
        }
    }
}

@Composable
fun TransitionSlideScreen(label: String, onNext: () -> Unit) {
    ContentBlue("Slide: $label") {
        Button(onClick = dropUnlessResumed(block = onNext)) {
            Text("Go to Fade")
        }
    }
}

@Composable
fun TransitionFadeScreen(label: String) {
    ContentPurple("Fade: $label")
}

@Composable
fun DialogContent(message: String, onDismiss: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp),
    ) {
        Text(
            fontWeight = FontWeight.Bold,
            text = "Dialog",
        )
        Spacer(Modifier.height(8.dp))
        Text(message)
        Spacer(Modifier.height(16.dp))
        Button(onClick = dropUnlessResumed(block = onDismiss)) {
            Text("Dismiss")
        }
    }
}

@Composable
fun SheetContent(title: String, onDismiss: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(24.dp)
            .fillMaxSize(),
    ) {
        Text(
            fontWeight = FontWeight.Bold,
            text = "Sheet: $title",
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = dropUnlessResumed(block = onDismiss)) {
            Text("Close Sheet")
        }
    }
}

// -- Adaptive layout screen composables (R17) --

@Composable
fun ItemListScreen(onItemClick: (String) -> Unit) {
    ContentGreen("Item List") {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            listOf("item-1", "item-2", "item-3").forEach { id ->
                Button(onClick = dropUnlessResumed { onItemClick(id) }) {
                    Text("Select $id")
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun ItemDetailScreen(id: String, onExtra: () -> Unit) {
    ContentBlue("Detail: $id") {
        Button(onClick = dropUnlessResumed(block = onExtra)) {
            Text("Show Extra Pane")
        }
    }
}

@Composable
fun ItemExtraScreen(id: String) {
    ContentMauve("Extra: $id")
}

// -- Conditional + deep link screen composables (R18-R19) --

@Composable
fun GateHomeScreen(onProfile: () -> Unit) {
    ContentGreen("Gate Home") {
        Button(onClick = dropUnlessResumed(block = onProfile)) {
            Text("Go to Profile")
        }
    }
}

@Composable
fun GateProfileScreen(onLogout: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    ContentMauve("Profile (logged in)") {
        AnimatedVisibility(visible = visible, enter = fadeIn()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = dropUnlessResumed(block = onLogout)) {
                    Text("Logout")
                }
            }
        }
    }
}

@Composable
fun GateLoginScreen(onLogin: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    ContentRed("Login Required") {
        AnimatedVisibility(visible = visible, enter = fadeIn()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Please log in to continue")
                Spacer(Modifier.height(16.dp))
                Button(onClick = dropUnlessResumed(block = onLogin)) {
                    Text("Log In")
                }
            }
        }
    }
}

@Composable
fun AdvancedDeepHomeScreen(onNavigate: () -> Unit) {
    ContentGreen("Advanced Deep Home") {
        Button(onClick = dropUnlessResumed(block = onNavigate)) {
            Text("Navigate to Target")
        }
    }
}

@Composable
fun AdvancedDeepTargetScreen(name: String, location: String) {
    ContentBlue("Advanced Deep Target") {
        Text("Name: $name")
        Text("Location: $location")
    }
}

// -- Migration screen composables --

@Composable
fun MigScreenA(onSubRouteClick: () -> Unit, onDialogClick: () -> Unit) {
    ContentRed("Route A title") {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = dropUnlessResumed(block = onSubRouteClick)) {
                Text("Go to A1")
            }
            Button(onClick = dropUnlessResumed(block = onDialogClick)) {
                Text("Open dialog D")
            }
        }
    }
}

@Composable
fun MigScreenA1() {
    ContentPink("Route A1 title")
}

@Composable
fun MigScreenB(onDetailClick: (String) -> Unit, onDialogClick: () -> Unit) {
    ContentGreen("Route B title") {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = dropUnlessResumed { onDetailClick("ABC") }) {
                Text("Go to B1")
            }
            Button(onClick = dropUnlessResumed(block = onDialogClick)) {
                Text("Open dialog D")
            }
        }
    }
}

@Composable
fun MigScreenB1(id: String) {
    ContentPurple("Route B1 title. ID: $id")
}

@Composable
fun MigScreenC(onDialogClick: () -> Unit) {
    ContentMauve("Route C title") {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = dropUnlessResumed(block = onDialogClick)) {
                Text("Open dialog D")
            }
        }
    }
}

// -- Results screen composables --

data class Person(val name: String, val favoriteColor: String)

@Composable
fun HomeScreen(
    person: Person?,
    onNext: () -> Unit,
) {
    ContentBlue("Hello ${person?.name ?: "unknown person"}") {
        if (person != null) {
            Text("Your favorite color is ${person.favoriteColor}")
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = dropUnlessResumed(block = onNext)) {
            Text("Tell us about yourself")
        }
    }
}

@Composable
fun PersonDetailsScreen(
    onSubmit: (Person) -> Unit,
) {
    ContentGreen("About you") {
        val nameTextState = rememberTextFieldState()
        OutlinedTextField(
            state = nameTextState,
            label = { Text("Please enter your name") },
        )

        val favoriteColorTextState = rememberTextFieldState()
        OutlinedTextField(
            state = favoriteColorTextState,
            label = { Text("Please enter your favorite color") },
        )

        Button(
            onClick = dropUnlessResumed {
                val person = Person(
                    name = nameTextState.text.toString(),
                    favoriteColor = favoriteColorTextState.text.toString(),
                )
                onSubmit(person)
            },
            enabled = nameTextState.text.isNotBlank() &&
                favoriteColorTextState.text.isNotBlank(),
        ) {
            Text("Submit")
        }
    }
}
