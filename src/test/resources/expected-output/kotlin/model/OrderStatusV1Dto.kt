package io.github.torand.openapi2java.test.model

import org.eclipse.microprofile.openapi.annotations.media.Schema

@Schema(name = "OrderStatusV1", description = "Processing status of an order")
enum class OrderStatusV1Dto {
    Created, Processing, Dispatched
}
