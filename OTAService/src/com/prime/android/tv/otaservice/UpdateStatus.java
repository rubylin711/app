package com.prime.android.tv.otaservice;

public class UpdateStatus {

    //update process status
    public enum status{
        IDLE(0),
        CHECKING_FOR_UPDATE(1),
        UPDATE_AVAILABLE(2),
        DOWNLOADING(3),
        VERIFYING(4),
        FINALIZING(5),
        UPDATED_NEED_REBOOT(6),
        REPORTING_ERROR_EVENT(7),
        ATTEMPTING_ROLLBACK(8),
        DISABLED(9),
        // Broadcast this state when an update aborts because user preferences do not
        // allow updates, e.g. over cellular network.
        NEED_PERMISSION_TO_UPDATE(10);

        private int value;
        private status(int value){
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    };


    //update process error code
    public enum err{
        kSuccess(0),
        kError(1),
        kOmahaRequestError(2),
        kOmahaResponseHandlerError(3),
        kFilesystemCopierError(4),
        kPostinstallRunnerError(5),
        kPayloadMismatchedType(6),
        kInstallDeviceOpenError(7),
        kKernelDeviceOpenError(8),
        kDownloadTransferError(9),
        kPayloadHashMismatchError(10),
        kPayloadSizeMismatchError(11),
        kDownloadPayloadVerificationError(12),
        kDownloadNewPartitionInfoError(13),
        kDownloadWriteError(14),
        kNewRootfsVerificationError(15),
        kNewKernelVerificationError(16),
        kSignedDeltaPayloadExpectedError(17),
        kDownloadPayloadPubKeyVerificationError(18),
        kPostinstallBootedFromFirmwareB(19),
        kDownloadStateInitializationError(20),
        kDownloadInvalidMetadataMagicString(21),
        kDownloadSignatureMissingInManifest(22),
        kDownloadManifestParseError(23),
        kDownloadMetadataSignatureError(24),
        kDownloadMetadataSignatureVerificationError(25),
        kDownloadMetadataSignatureMismatch(26),
        kDownloadOperationHashVerificationError(27),
        kDownloadOperationExecutionError(28),
        kDownloadOperationHashMismatch(29),
        kOmahaRequestEmptyResponseError(30),
        kOmahaRequestXMLParseError(31),
        kDownloadInvalidMetadataSize(32),
        kDownloadInvalidMetadataSignature(33),
        kOmahaResponseInvalid(34),
        kOmahaUpdateIgnoredPerPolicy(35),
        kOmahaUpdateDeferredPerPolicy(36),
        kOmahaErrorInHTTPResponse(37),
        kDownloadOperationHashMissingError(38),
        kDownloadMetadataSignatureMissingError(39),
        kOmahaUpdateDeferredForBackoff(40),
        kPostinstallPowerwashError(41),
        kUpdateCanceledByChannelChange(42),
        kPostinstallFirmwareRONotUpdatable(43),
        kUnsupportedMajorPayloadVersion(44),
        kUnsupportedMinorPayloadVersion(45),
        kOmahaRequestXMLHasEntityDecl(46),
        kFilesystemVerifierError(47),
        kUserCanceled(48),
        kNonCriticalUpdateInOOBE(49),
        kOmahaUpdateIgnoredOverCellular(50),
        kPayloadTimestampError(51),
        kUpdatedButNotActive(52),
        kNoUpdate(53),
        kRollbackNotPossible(54),
        kFirstActiveOmahaPingSentPersistenceError(55),
        kVerityCalculationError(56);

        private int value;
        private err(int value){
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }
	
}
