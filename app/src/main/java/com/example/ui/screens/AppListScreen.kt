package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AppInfo
import com.example.service.AppLockAccessibilityService
import com.example.utils.PermissionHelper
import com.example.viewmodel.AppLockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    viewModel: AppLockViewModel,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filteredApps by viewModel.filteredApps.collectAsStateWithLifecycle()
    val isLoadingApps by viewModel.isLoadingApps.collectAsStateWithLifecycle()
    val lockedCount by viewModel.lockedAppsCount.collectAsStateWithLifecycle()
    
    // Monitor permissions state when active
    var isAccessibilityEnabled by remember {
        mutableStateOf(PermissionHelper.isAccessibilityServiceEnabled(context, AppLockAccessibilityService::class.java))
    }
    var isOverlayEnabled by remember {
        mutableStateOf(PermissionHelper.isOverlayPermissionGranted(context))
    }

    LaunchedEffect(Unit) {
        viewModel.loadInstalledApps()
    }
    
    // Sync state dynamically on composition re-entry
    SideEffect {
        isAccessibilityEnabled = PermissionHelper.isAccessibilityServiceEnabled(context, AppLockAccessibilityService::class.java)
        isOverlayEnabled = PermissionHelper.isOverlayPermissionGranted(context)
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var activeUtilId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            if (selectedTab == 0 || (selectedTab == 2 && activeUtilId == null)) {
                LargeTopAppBar(
                    title = {
                        Column {
                            Text(
                                text = if (selectedTab == 0) "Danh sách bảo vệ" else "Tiện ích đa năng",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                text = if (selectedTab == 0) "Đã kích hoạt khóa cho $lockedCount ứng dụng" else "Lịch & Máy tính tích hợp tiện lợi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Cấu hình",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        },
        bottomBar = {
            if (activeUtilId == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Lock, contentDescription = "Bảo vệ") },
                        label = { Text("Bảo vệ") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Chat, contentDescription = "Trợ lý AI") },
                        label = { Text("Trợ lý AI") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.Widgets, contentDescription = "Tiện ích") },
                        label = { Text("Tiện ích") }
                    )
                }
            }
        }
    ) { innerPadding ->
        if (selectedTab == 0) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Tìm kiếm ứng dụng...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Tìm") },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = textFieldColorsFallback(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                // Dynamic filter tabs
                val currentFilter by viewModel.currentFilter.collectAsStateWithLifecycle()
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    FilterChip(
                        selected = currentFilter == AppLockViewModel.AppFilter.ALL,
                        onClick = { viewModel.setFilter(AppLockViewModel.AppFilter.ALL) },
                        label = { Text("Tất cả ứng dụng") }
                    )
                    FilterChip(
                        selected = currentFilter == AppLockViewModel.AppFilter.LOCKED,
                        onClick = { viewModel.setFilter(AppLockViewModel.AppFilter.LOCKED) },
                        label = { Text("Đã bảo vệ") }
                    )
                    FilterChip(
                        selected = currentFilter == AppLockViewModel.AppFilter.UNLOCKED,
                        onClick = { viewModel.setFilter(AppLockViewModel.AppFilter.UNLOCKED) },
                        label = { Text("Chưa bảo vệ") }
                    )
                }

                // Warning panel for permissions setup
                if (!isAccessibilityEnabled || !isOverlayEnabled) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Cảnh báo",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Yêu cầu cấp quyền hoạt động",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Text(
                                "AppLock cần quyền Hỗ trợ tiếp cận (Accessibility) & Vẽ đè màn hình (Overlay) để khóa ứng dụng bảo mật.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                            )

                            // Guide for Android 13+ Restricted Settings
                            if (!isAccessibilityEnabled && Build.VERSION.SDK_INT >= 33) {
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "LƯU Ý QUAN TRỌNG CHO ANDROID 13/14/15/16:",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    "Nếu hệ thống báo 'Ứng dụng đã bị từ chối cấp quyền' (Restricted Settings), hãy bỏ chặn theo 3 bước sau:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                val steps = listOf(
                                    "Bước 1: Mở Cài đặt hệ thống -> Ứng dụng -> Chọn AppLock.",
                                    "Bước 2: Nhấn nút ⋮ (3 chấm) ở góc trên bên phải màn hình thông tin ứng dụng AppLock.",
                                    "Bước 3: Chọn 'Cho phép cài đặt bị hạn chế' (Allow restricted settings), xác nhận mật khẩu điện thoại.",
                                    "Bước 4: Trở lại đây, nhấn nút bên dưới để bật Hỗ trợ tiếp cận AppLock thành công!"
                                )
                                
                                steps.forEach { step ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 3.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            "• ",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = step,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (!isOverlayEnabled) {
                                    Button(
                                        onClick = {
                                            val intent = Intent(
                                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                Uri.parse("package:${context.packageName}")
                                            )
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.onErrorContainer,
                                            contentColor = MaterialTheme.colorScheme.errorContainer
                                        ),
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text("Cấp quyền Vẽ đè")
                                    }
                                }
                                if (!isAccessibilityEnabled) {
                                    Button(
                                        onClick = {
                                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.onErrorContainer,
                                            contentColor = MaterialTheme.colorScheme.errorContainer
                                        )
                                    ) {
                                        Text("Bật Hỗ trợ tiếp cận")
                                    }
                                }
                            }
                        }
                    }
                }

                // Central Items list container
                if (isLoadingApps) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (filteredApps.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = "Trống",
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Không tìm thấy ứng dụng nào",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            AppRowItem(
                                app = app,
                                onToggleLock = { isChecked ->
                                    viewModel.toggleAppLock(app.packageName, app.appName, isChecked)
                                }
                            )
                        }
                    }
                }
            }
        } else if (selectedTab == 1) {
            ChatBotAiView(
                onBack = { selectedTab = 0 },
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            UtilitiesScreen(
                viewModel = viewModel,
                activeUtilId = activeUtilId,
                onActiveUtilIdChange = { activeUtilId = it },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun AppRowItem(
    app: AppInfo,
    onToggleLock: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager
    
    // Load bitmap image offline natively
    val iconBitmap = remember(app.packageName) {
        try {
            val drawable = pm.getApplicationIcon(app.packageName)
            drawable.toBitmap(48, 48).asImageBitmap()
        } catch (e: Exception) {
            pm.defaultActivityIcon.toBitmap(48, 48).asImageBitmap()
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = "Biểu tượng ứng dụng",
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
                
                Spacer(modifier = Modifier.width(14.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = app.isLocked,
                onCheckedChange = { onToggleLock(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun textFieldColorsFallback() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = Color.Transparent
)
