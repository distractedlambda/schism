package org.schism.schismatic

sealed class USBTransferType {
    object Control : USBTransferType()

    data class Isochronous(val synchronizationType: SynchronizationType, val usageType: UsageType) : USBTransferType() {
        enum class SynchronizationType {
            None,
            Asynchronous,
            Adaptive,
            Synchronous,
        }

        enum class UsageType {
            Data,
            Feedback,
            ExplicitFeedbackData,
        }
    }

    object Bulk : USBTransferType()

    object Interrupt : USBTransferType()
}
