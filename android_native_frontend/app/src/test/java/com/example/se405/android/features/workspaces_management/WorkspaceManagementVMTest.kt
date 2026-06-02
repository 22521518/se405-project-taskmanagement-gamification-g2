@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.workspaces_management

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.testing.invoke
import com.example.se405.android.features.workspaces_management.presentation.WorkspaceDetailNav
import com.example.se405.android.features.workspaces_management.presentation.viewmodel.WorkspaceManagementViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalCoroutinesApi::class)
class WorkspaceManagementVMTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: WorkspaceManagementViewModel

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any<String>()) } returns 0
        every { android.util.Log.i(any(), any<String>()) } returns 0
        every { android.util.Log.w(any(), any<String>()) } returns 0
        every { android.util.Log.e(any(), any<String>()) } returns 0
        every { android.util.Log.e(any(), any<String>(), any()) } returns 0
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ─── Stub helpers ─────────────────────────────────────────────────────────
    private fun TestScope.buildAndIdle(workspaceIdForTest: Uuid = Uuid.parse("b9fda29a-79c3-4c33-bd37-3b9839f51f16")) {
        val fakeRoute = WorkspaceDetailNav(workspaceId = workspaceIdForTest)
        val savedStateHandle = SavedStateHandle(route = fakeRoute)
//        viewModel = WorkspaceManagementViewModel(mockk(), savedStateHandle, workspaceUseCase = mockk())
        advanceUntilIdle()
    }
}