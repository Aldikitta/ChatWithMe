package com.example.chatwithme.presentation.chat

import android.widget.Toast
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.chatwithme.core.SnackbarController
import com.example.chatwithme.domain.model.User
import com.example.chatwithme.presentation.bottomnavigation.BottomNavItem
import com.example.chatwithme.presentation.chat.chatAppBar.ChatAppBar
import com.example.chatwithme.presentation.chat.chatAppBar.ProfilePictureDialog

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChatScreen(
    chatRoomUUID: String,
    opponentUUID: String,
    registerUUID: String,
    oneSignalUserId: String,
    chatViewModel: ChatScreenViewModel = hiltViewModel(),
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    keyboardController: SoftwareKeyboardController
) {
    val toastMessage = chatViewModel.toastMessage.value
    LaunchedEffect(key1 = toastMessage) {
        if (toastMessage != "") {
            SnackbarController(this).showSnackbar(snackbarHostState, toastMessage, "Close")
        }
    }
    chatViewModel.loadMessagesFromFirebase(chatRoomUUID, opponentUUID, registerUUID)
    ChatScreenContent(
        chatRoomUUID,
        opponentUUID,
        registerUUID,
        oneSignalUserId,
        chatViewModel,
        navController,
        keyboardController
    )

}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChatScreenContent(
    chatRoomUUID: String,
    opponentUUID: String,
    registerUUID: String,
    oneSignalUserId: String,
    chatViewModel: ChatScreenViewModel,
    navController: NavHostController,
    keyboardController: SoftwareKeyboardController
) {
    val messages = chatViewModel.messages

    LaunchedEffect(key1 = Unit) {
        chatViewModel.loadOpponentProfileFromFirebase(opponentUUID)
    }
    var opponentProfileFromFirebase by remember {
        mutableStateOf(User())
    }
    opponentProfileFromFirebase = chatViewModel.opponentProfileFromFirebase.value
    val opponentName = opponentProfileFromFirebase.userName
    val opponentSurname = opponentProfileFromFirebase.userSurName
    val opponentPictureUrl = opponentProfileFromFirebase.userProfilePictureUrl
    val opponentStatus = opponentProfileFromFirebase.status

    var showDialog by remember {
        mutableStateOf(false)
    }
    if (showDialog) {
        ProfilePictureDialog(opponentPictureUrl) {
            showDialog = !showDialog
        }
    }

    val scrollState = rememberLazyListState(initialFirstVisibleItemIndex = messages.size)
    val messagesLoadedFirstTime = chatViewModel.messagesLoadedFirstTime.value
    val messageInserted = chatViewModel.messageInserted.value
    var isChatInputFocus by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(key1 = messagesLoadedFirstTime, messages, messageInserted) {
        if (messages.isNotEmpty()) {
            scrollState.scrollToItem(
                index = messages.size - 1
            )
        }
    }

    val imePaddingValues = PaddingValues()
    val imeBottomPadding = imePaddingValues.calculateBottomPadding().value.toInt()
    LaunchedEffect(key1 = imeBottomPadding) {
        if (messages.isNotEmpty()) {
            scrollState.scrollToItem(
                index = messages.size - 1
            )
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .focusable()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { keyboardController.hide() })
            }
    ) {
        val context = LocalContext.current

        ChatAppBar(
            title = "$opponentName $opponentSurname",
            description = opponentStatus.lowercase(),
            pictureUrl = opponentPictureUrl,
            onUserNameClick = {
                Toast.makeText(context, "User Profile Clicked", Toast.LENGTH_SHORT).show()
            }, onBackArrowClick = {
                navController.popBackStack()
                navController.navigate(BottomNavItem.UserList.fullRoute)
            }, onUserProfilePictureClick = {
                showDialog = true
            },
            onMoreDropDownBlockUserClick = {
                chatViewModel.blockFriendToFirebase(registerUUID)
                navController.navigate(BottomNavItem.UserList.fullRoute)
            }
        )
    }
}