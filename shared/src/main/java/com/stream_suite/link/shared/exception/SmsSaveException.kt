package com.stream_suite.link.shared.exception

class SmsSaveException(exception: Exception) : IllegalStateException(exception.message)