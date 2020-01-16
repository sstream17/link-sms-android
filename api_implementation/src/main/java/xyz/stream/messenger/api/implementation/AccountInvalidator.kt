package xyz.stream.messenger.api.implementation

interface AccountInvalidator {

    fun onAccountInvalidated(account: Account)

}