package com.smartquiz.app.utils

import android.content.Context
import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

object ErrorHandler {
    
    private const val TAG = "SmartQuizError"
    private const val MAX_LOG_ENTRIES = 100
    
    private val errorLogs = mutableListOf<ErrorLog>()
    
    data class ErrorLog(
        val timestamp: Long,
        val level: LogLevel,
        val message: String,
        val exception: Throwable?,
        val context: String?
    )
    
    enum class LogLevel {
        DEBUG, INFO, WARNING, ERROR, CRITICAL
    }
    
    fun logError(
        message: String,
        exception: Throwable? = null,
        context: String? = null,
        level: LogLevel = LogLevel.ERROR
    ) {
        val errorLog = ErrorLog(
            timestamp = System.currentTimeMillis(),
            level = level,
            message = message,
            exception = exception,
            context = context
        )
        
        // Add to internal log
        synchronized(errorLogs) {
            errorLogs.add(errorLog)
            if (errorLogs.size > MAX_LOG_ENTRIES) {
                errorLogs.removeAt(0)
            }
        }
        
        // Log to Android Log
        val logMessage = buildLogMessage(errorLog)
        when (level) {
            LogLevel.DEBUG -> Log.d(TAG, logMessage, exception)
            LogLevel.INFO -> Log.i(TAG, logMessage, exception)
            LogLevel.WARNING -> Log.w(TAG, logMessage, exception)
            LogLevel.ERROR -> Log.e(TAG, logMessage, exception)
            LogLevel.CRITICAL -> Log.wtf(TAG, logMessage, exception)
        }
    }
    
    fun logDebug(message: String, context: String? = null) {
        logError(message, null, context, LogLevel.DEBUG)
    }
    
    fun logInfo(message: String, context: String? = null) {
        logError(message, null, context, LogLevel.INFO)
    }
    
    fun logWarning(message: String, exception: Throwable? = null, context: String? = null) {
        logError(message, exception, context, LogLevel.WARNING)
    }
    
    fun logCritical(message: String, exception: Throwable? = null, context: String? = null) {
        logError(message, exception, context, LogLevel.CRITICAL)
    }
    
    private fun buildLogMessage(errorLog: ErrorLog): String {
        val sb = StringBuilder()
        
        sb.append("[${errorLog.level}] ")
        
        if (errorLog.context != null) {
            sb.append("[${errorLog.context}] ")
        }
        
        sb.append(errorLog.message)
        
        if (errorLog.exception != null) {
            sb.append("\n")
            sb.append(getStackTraceString(errorLog.exception))
        }
        
        return sb.toString()
    }
    
    private fun getStackTraceString(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        return sw.toString()
    }
    
    fun getErrorLogs(): List<ErrorLog> {
        return synchronized(errorLogs) {
            errorLogs.toList()
        }
    }
    
    fun getErrorLogsFormatted(): String {
        val logs = getErrorLogs()
        val sb = StringBuilder()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        
        logs.forEach { log ->
            sb.append("${dateFormat.format(Date(log.timestamp))} ")
            sb.append("[${log.level}] ")
            if (log.context != null) {
                sb.append("[${log.context}] ")
            }
            sb.append(log.message)
            sb.append("\n")
            
            if (log.exception != null) {
                sb.append(getStackTraceString(log.exception))
                sb.append("\n")
            }
            sb.append("---\n")
        }
        
        return sb.toString()
    }
    
    fun clearLogs() {
        synchronized(errorLogs) {
            errorLogs.clear()
        }
    }
    
    fun handleApiError(exception: Throwable, context: String = "API"): String {
        return when (exception) {
            is java.net.UnknownHostException -> {
                logError("Không có kết nối internet", exception, context)
                "Không có kết nối internet. Vui lòng kiểm tra kết nối và thử lại."
            }
            is java.net.SocketTimeoutException -> {
                logError("Timeout khi kết nối API", exception, context)
                "Kết nối quá chậm. Vui lòng thử lại sau."
            }
            is retrofit2.HttpException -> {
                val code = exception.code()
                val message = when (code) {
                    401 -> "API key không hợp lệ"
                    403 -> "Không có quyền truy cập API"
                    429 -> "Đã vượt quá giới hạn API"
                    500 -> "Lỗi server"
                    else -> "Lỗi HTTP: $code"
                }
                logError(message, exception, context)
                message
            }
            else -> {
                logError("Lỗi không xác định: ${exception.message}", exception, context)
                "Đã xảy ra lỗi. Vui lòng thử lại sau."
            }
        }
    }
    
    fun handleDatabaseError(exception: Throwable, context: String = "Database"): String {
        logError("Lỗi database: ${exception.message}", exception, context)
        return "Lỗi cơ sở dữ liệu. Vui lòng khởi động lại ứng dụng."
    }
    
    fun handleGeneralError(exception: Throwable, context: String = "General"): String {
        logError("Lỗi chung: ${exception.message}", exception, context)
        return "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại."
    }
}
