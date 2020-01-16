package xyz.stream.messenger.shared.util.billing

interface PurchasedItemCallback {
    fun onItemPurchased(productId: String)
    fun onPurchaseError(message: String)
}
