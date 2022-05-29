package org.schism.cousb

public sealed class USBTransferType {
    public object Control : USBTransferType()

    public data class Isochronous(val synchronizationType: SynchronizationType, val usageType: UsageType) : USBTransferType() {
        public enum class SynchronizationType {
            None,
            Asynchronous,
            Adaptive,
            Synchronous,
        }

        public enum class UsageType {
            Data,
            Feedback,
            ExplicitFeedbackData,
        }
    }

    public object Bulk : USBTransferType()

    public object Interrupt : USBTransferType()
}
