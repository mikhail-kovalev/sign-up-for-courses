package ru.oborg.courses.presentation.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.oborg.courses.R
import ru.oborg.courses.core.UiState
import ru.oborg.courses.data.local.UiSettings
import ru.oborg.courses.domain.model.Enrollment
import ru.oborg.courses.domain.model.User
import ru.oborg.courses.presentation.common.AppBackground

@Composable
fun ProfileScreen(
    user: User?,
    enrollmentsState: UiState<List<Enrollment>>,
    formState: ProfileFormState,
    uiSettings: UiSettings,
    onDarkThemeChanged: (Boolean) -> Unit,
    onNotificationsChanged: (Boolean) -> Unit,
    onSupportMessageChanged: (String) -> Unit,
    onSubmitSupportRequest: () -> Unit,
    onCurrentPasswordChanged: (String) -> Unit,
    onNewPasswordChanged: (String) -> Unit,
    onRepeatPasswordChanged: (String) -> Unit,
    onChangePassword: () -> Unit,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enrollments = (enrollmentsState as? UiState.Content)?.value.orEmpty()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        AppBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Header()
                UserCard(
                    user = user,
                    enrollmentsCount = enrollments.size
                )
                SettingsBlock(
                    uiSettings = uiSettings,
                    formState = formState,
                    onDarkThemeChanged = onDarkThemeChanged,
                    onNotificationsChanged = onNotificationsChanged,
                    onCurrentPasswordChanged = onCurrentPasswordChanged,
                    onNewPasswordChanged = onNewPasswordChanged,
                    onRepeatPasswordChanged = onRepeatPasswordChanged,
                    onChangePassword = onChangePassword,
                    onRefresh = onRefresh,
                    onLogout = onLogout
                )
                SocialLinksBlock()
                SupportRequestBlock(
                    userEmail = user?.email.orEmpty(),
                    formState = formState,
                    onMessageChanged = onSupportMessageChanged,
                    onSubmit = onSubmitSupportRequest
                )
                Spacer(modifier = Modifier.height(84.dp))
            }
        }
    }
}

@Composable
private fun Header() {
    Column {
        Text(
            text = "Профиль",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Настройки, безопасность и связь",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UserCard(
    user: User?,
    enrollmentsCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(
                    modifier = Modifier.size(58.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user?.fullName ?: "Студент",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = user?.email.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatPill(
                    title = "Записи",
                    value = enrollmentsCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    title = "Роль",
                    value = user?.role ?: "student",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun UserAvatar(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(R.drawable.profile_avatar_student),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun StatPill(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SettingsBlock(
    uiSettings: UiSettings,
    formState: ProfileFormState,
    onDarkThemeChanged: (Boolean) -> Unit,
    onNotificationsChanged: (Boolean) -> Unit,
    onCurrentPasswordChanged: (String) -> Unit,
    onNewPasswordChanged: (String) -> Unit,
    onRepeatPasswordChanged: (String) -> Unit,
    onChangePassword: () -> Unit,
    onRefresh: () -> Unit,
    onLogout: () -> Unit
) {
    var passwordExpanded by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            SettingsSwitchRow(
                iconRes = R.drawable.ic_lucide_bell,
                title = "Уведомления",
                subtitle = "Запись и напоминания о занятиях",
                checked = uiSettings.notificationsEnabled,
                onCheckedChange = onNotificationsChanged
            )
            SettingsSwitchRow(
                iconRes = R.drawable.ic_lucide_moon,
                title = "Темная тема",
                subtitle = if (uiSettings.darkTheme) "Включена" else "Выключена",
                checked = uiSettings.darkTheme,
                onCheckedChange = onDarkThemeChanged
            )
            SettingsRow(
                iconRes = R.drawable.ic_lucide_lock_keyhole,
                title = "Смена пароля",
                subtitle = if (passwordExpanded) "Форма открыта" else "Изменить пароль аккаунта",
                onClick = { passwordExpanded = !passwordExpanded }
            )
            if (passwordExpanded) {
                PasswordBlock(
                    formState = formState,
                    onCurrentPasswordChanged = onCurrentPasswordChanged,
                    onNewPasswordChanged = onNewPasswordChanged,
                    onRepeatPasswordChanged = onRepeatPasswordChanged,
                    onSubmit = onChangePassword
                )
            }
            SettingsRow(
                iconRes = R.drawable.ic_lucide_server,
                title = "Сервер",
                subtitle = "Ktor API · обновить данные",
                onClick = onRefresh
            )
            SettingsRow(
                iconRes = R.drawable.ic_lucide_log_out,
                title = "Выйти",
                subtitle = "Завершить текущую сессию",
                onClick = onLogout,
                highlight = true
            )
        }
    }
}

@Composable
private fun SocialLinksBlock() {
    val uriHandler = LocalUriHandler.current
    val openLink: (String) -> Unit = { url ->
        runCatching { uriHandler.openUri(url) }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.ic_lucide_users),
                            contentDescription = null
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Obrorg в соцсетях",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Новости потоков, отзывы студентов и анонсы",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SocialLinkButton(
                    symbol = "VK",
                    title = "ВКонтакте",
                    onClick = { openLink("https://vk.com/obrorg") },
                    modifier = Modifier.weight(1f)
                )
                SocialLinkButton(
                    symbol = "TG",
                    title = "Telegram",
                    onClick = { openLink("https://t.me/obrorg_courses") },
                    modifier = Modifier.weight(1f)
                )
                SocialLinkButton(
                    symbol = "YT",
                    title = "YouTube",
                    onClick = { openLink("https://youtube.com/@obrorg") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SocialLinkButton(
    symbol: String,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = symbol,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SupportRequestBlock(
    userEmail: String,
    formState: ProfileFormState,
    onMessageChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Вопросы и предложения",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Ответ придет на почту аккаунта: ${userEmail.ifBlank { "не указана" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = formState.supportMessage,
                onValueChange = onMessageChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Напишите вопрос или предложение") },
                minLines = 4,
                maxLines = 6,
                shape = MaterialTheme.shapes.medium,
                colors = profileTextFieldColors()
            )
            Button(
                onClick = onSubmit,
                enabled = !formState.isSubmittingSupport,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(if (formState.isSubmittingSupport) "Отправляем" else "Отправить")
            }
            formState.supportStatus?.let { status ->
                StatusText(text = status, successMarker = "отправлено")
            }
        }
    }
}

@Composable
private fun PasswordBlock(
    formState: ProfileFormState,
    onCurrentPasswordChanged: (String) -> Unit,
    onNewPasswordChanged: (String) -> Unit,
    onRepeatPasswordChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Смена пароля",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Введите текущий пароль и новый пароль от 6 символов",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            PasswordField(
                value = formState.currentPassword,
                onValueChange = onCurrentPasswordChanged,
                placeholder = "Текущий пароль"
            )
            PasswordField(
                value = formState.newPassword,
                onValueChange = onNewPasswordChanged,
                placeholder = "Новый пароль"
            )
            PasswordField(
                value = formState.repeatPassword,
                onValueChange = onRepeatPasswordChanged,
                placeholder = "Повторите новый пароль"
            )
            Button(
                onClick = onSubmit,
                enabled = !formState.isChangingPassword,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(if (formState.isChangingPassword) "Сохраняем" else "Изменить пароль")
            }
            formState.passwordStatus?.let { status ->
                StatusText(text = status, successMarker = "изменен")
            }
        }
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        shape = MaterialTheme.shapes.medium,
        colors = profileTextFieldColors()
    )
}

@Composable
private fun StatusText(
    text: String,
    successMarker: String
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = if (successMarker in text.lowercase()) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.error
        }
    )
}

@Composable
private fun profileTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    cursorColor = MaterialTheme.colorScheme.primary
)

@Composable
private fun SettingsSwitchRow(
    iconRes: Int,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        onClick = { onCheckedChange(!checked) },
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    uncheckedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

@Composable
private fun SettingsRow(
    iconRes: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {},
    highlight: Boolean = false
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = MaterialTheme.shapes.medium,
                color = if (highlight) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                contentColor = if (highlight) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.primary
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_lucide_arrow_right),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
