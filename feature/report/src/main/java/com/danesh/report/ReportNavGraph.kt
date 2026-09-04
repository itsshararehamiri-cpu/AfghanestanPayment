package com.danesh.report

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.danesh.common.GetPasswordScreen
import com.danesh.report.model.ReportMenuType
import com.danesh.report.model.ReportTransactionChipFilter
import com.danesh.settings.ui.ChangePasswordRoute

object ReportRoutes {
    const val PASSWORD = "report_password"
    const val CHANGE_PASSWORD_MANDATORY = "report_change_password_mandatory"
    const val MENU = "report_menu"
    const val LAST_TRANSACTION = "report_last_transaction"
    const val LAST_TEN_SUCCESSFUL = "report_last_ten_successful"
    const val REPRINT_LAST_RECEIPT = "report_reprint_last_receipt"
    const val REPRINT_SPECIFIC_RECEIPT = "report_reprint_specific_receipt"
    const val UNSETTLED_TRANSACTION = "report_unsettled_transaction"
    const val RESULTS = "report_results"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReportNavHost(onFlowComplete: () -> Unit) {
    val navController = rememberNavController()
    val reportsViewModel: ReportsViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = ReportRoutes.PASSWORD,
    ) {
        composable(ReportRoutes.PASSWORD) {
            val passwordViewModel: ReportPasswordViewModel = hiltViewModel()
            val passwordState by passwordViewModel.uiState.collectAsStateWithLifecycle()

            GetPasswordScreen(
                pinValue = passwordState.pinValue,
                title = stringResource(com.danesh.common.R.string.password_title),
                instructionText = stringResource(com.danesh.common.R.string.report_password_hint),
                hintText = passwordState.errorMessage,
                hintColor = Color(0xFFFF8A80),
                showRetryHint = passwordState.errorMessage != null,
                onBackClick = onFlowComplete,
                onPinValueChange = passwordViewModel::onPinChange,
                onPinComplete = {
                    passwordViewModel.submitPassword(
                        onSuccess = {
                            navController.navigate(ReportRoutes.MENU)
                        },
                        onMandatoryPasswordChange = {
                            navController.navigate(ReportRoutes.CHANGE_PASSWORD_MANDATORY)
                        },
                    )
                },
            )
        }

        composable(ReportRoutes.CHANGE_PASSWORD_MANDATORY) {
            ChangePasswordRoute(
                mandatory = true,
                onBackClick = onFlowComplete,
                onCancelClick = onFlowComplete,
                onPasswordChanged = {
                    navController.navigate(ReportRoutes.MENU) {
                        popUpTo(ReportRoutes.CHANGE_PASSWORD_MANDATORY) { inclusive = true }
                    }
                },
            )
        }

        composable(ReportRoutes.MENU) {
            val reportsState by reportsViewModel.uiState.collectAsStateWithLifecycle()
            val flowState by reportsViewModel.flowState.collectAsStateWithLifecycle()
            var showFilters by remember { mutableStateOf(false) }

            ReportsScreen(
                summary = reportsState.summary,
                isLoading = reportsState.isLoading,
                onBackClick = onFlowComplete,
                onLastTransactionClick = {
                    navController.navigate(ReportRoutes.LAST_TRANSACTION)
                },
                onLastTenSuccessfulClick = {
                    navController.navigate(ReportRoutes.LAST_TEN_SUCCESSFUL)
                },
                onAggregateReportClick = {
                    reportsViewModel.setReportType(ReportMenuType.AGGREGATE)
                    showFilters = true
                },
                onTransactionDetailsClick = {
                    reportsViewModel.setReportType(ReportMenuType.TRANSACTION_DETAILS)
                    showFilters = true
                },
                onUnsettledTransactionsClick = {
                    navController.navigate(ReportRoutes.UNSETTLED_TRANSACTION)
                },
                onReprintLastReceiptClick = {
                    navController.navigate(ReportRoutes.REPRINT_LAST_RECEIPT)
                },
                onReprintSpecificReceiptClick = {
                    navController.navigate(ReportRoutes.REPRINT_SPECIFIC_RECEIPT)
                },
            )

            if (showFilters) {
                ReportFiltersBottomSheet(
                    initialFilters = flowState.filters,
                    onDismissRequest = { showFilters = false },
                    onApplyFiltersClick = { filters ->
                        reportsViewModel.setFilters(filters)
                        showFilters = false
                        navController.navigate(ReportRoutes.RESULTS)
                    },
                )
            }
        }

        composable(ReportRoutes.RESULTS) {
            val flowState by reportsViewModel.flowState.collectAsStateWithLifecycle()
            val reportType = flowState.reportType ?: ReportMenuType.TRANSACTION_DETAILS
            val listViewModel: ReportListViewModel = hiltViewModel()
            var showFilters by remember { mutableStateOf(false) }

            ReportListScreen(
                reportType = reportType,
                filters = flowState.filters,
                viewModel = listViewModel,
                onBackClick = { navController.popBackStack() },
                onFilterClick = { showFilters = true }, onHomeClick = {
                    onFlowComplete()
                }
            )

            if (showFilters) {
                ReportFiltersBottomSheet(
                    initialFilters = flowState.filters,
                    onDismissRequest = { showFilters = false },
                    onApplyFiltersClick = { filters ->
                        reportsViewModel.setFilters(filters)
                        showFilters = false
                        listViewModel.load(reportType, filters, ReportTransactionChipFilter.ALL)
                    },
                )
            }
        }

        composable(ReportRoutes.REPRINT_SPECIFIC_RECEIPT) {
            ReprintSpecificReceiptScreen(
                onFinished = { navController.popBackStack() },
            )
        }

        composable(ReportRoutes.REPRINT_LAST_RECEIPT) {
            ReprintLastReceiptScreen(
                onFinished = { navController.popBackStack() },
            )
        }

        composable(ReportRoutes.UNSETTLED_TRANSACTION) {
            UnsettledTransactionScreen(
                onFinished = { navController.popBackStack() },
            )
        }

        composable(ReportRoutes.LAST_TEN_SUCCESSFUL) {
            val viewModel: LastTenSuccessfulTransactionsViewModel = hiltViewModel()

            LastTenSuccessfulTransactionsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(ReportRoutes.LAST_TRANSACTION) {
            val lastTransactionViewModel: LastTransactionViewModel = hiltViewModel()

            LastTransactionScreen(
                viewModel = lastTransactionViewModel,
                onBackClick = { navController.popBackStack() },
                onHomeClick = {
                    onFlowComplete()
                },
            )
        }
    }
}
