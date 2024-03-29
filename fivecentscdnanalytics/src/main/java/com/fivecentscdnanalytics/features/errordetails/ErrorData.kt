package com.fivecentscdnanalytics.features.errordetails

import com.fivecentscdnanalytics.utils.topOfStacktrace


data class ErrorData(val exceptionMessage: String? = null, val exceptionStacktrace: Collection<String>? = null, val additionalData: String? = null) {
    companion object {
        fun fromThrowable(throwable: Throwable, additionalData: String? = null): ErrorData = ErrorData(throwable.message, throwable.topOfStacktrace.toList(), additionalData)
    }
}
