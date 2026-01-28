public class DsmccHeader {
    public int tableId;
    public int sectionLength;
    public int transactionId;
    public int messageId;

    @Override
    public String toString() {
        return String.format("DSMCC [tid=0x%02X, len=%d, msgId=0x%04X, transId=0x%08X]",
                tableId, sectionLength, messageId, transactionId);
    }
}
