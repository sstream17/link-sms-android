package com.stream_suite.link.shared.service

import com.stream_suite.link.shared.util.billing.ProductPurchased

class SimpleLifetimeSubscriptionCheckService : SimpleSubscriptionCheckService() {
    override fun handleBestProduct(best: ProductPurchased) {
        if (best.productId == "lifetime") {
            writeLifetimeSubscriber()
        }
    }
}
