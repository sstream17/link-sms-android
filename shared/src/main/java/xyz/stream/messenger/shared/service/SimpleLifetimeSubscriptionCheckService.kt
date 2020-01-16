package xyz.stream.messenger.shared.service

import xyz.stream.messenger.shared.util.billing.ProductPurchased

class SimpleLifetimeSubscriptionCheckService : SimpleSubscriptionCheckService() {
    override fun handleBestProduct(best: ProductPurchased) {
        if (best.productId == "lifetime") {
            writeLifetimeSubscriber()
        }
    }
}
