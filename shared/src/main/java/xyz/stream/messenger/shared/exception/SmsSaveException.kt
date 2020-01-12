package xyz.stream.messenger.shared.exception

class SmsSaveException(exception: Exception) : IllegalStateException(exception.message)