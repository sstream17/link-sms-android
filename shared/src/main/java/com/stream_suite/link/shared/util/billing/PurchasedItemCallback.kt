package com.stream_suite.link.shared.util.billing

interface PurchasedItemCallback {
    fun onItemPurchased(productId: String)
    fun onPurchaseError(message: String)
}
