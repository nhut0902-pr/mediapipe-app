package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AppLock", appName)
  }

  @Test
  fun `update info model properties`() {
    val info = com.example.data.UpdateInfo(
      versionCode = 5,
      versionName = "1.4",
      apkUrl = "https://github.com/nhut0902-pr/mediapipe-app/releases/download/v1.4/app-release.apk",
      forceUpdate = false,
      changelog = "Sửa lỗi và cải thiện hiệu năng"
    )
    assertEquals(5, info.versionCode)
    assertEquals("1.4", info.versionName)
    assertEquals("https://github.com/nhut0902-pr/mediapipe-app/releases/download/v1.4/app-release.apk", info.apkUrl)
    assertEquals(false, info.forceUpdate)
    assertEquals("Sửa lỗi và cải thiện hiệu năng", info.changelog)
  }
}
