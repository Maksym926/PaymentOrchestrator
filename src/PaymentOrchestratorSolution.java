import java.util.*;

public class PaymentOrchestratorSolution {
    enum ActionType{ //  enum for tracking actions
        AUTHORIZE,
        CAPTURE,
        REFUND,
        CANCEL,
        RETRY

    }
    enum StateType{ // enum for tracking transaction state
        NEW,
        AUTHORIZED,
        PARTIALLY_CAPTURED,
        CAPTURED,
        CANCELED

    }
    enum Result{ // enum for tracking Result
        APPROVED,
        DECLINED,
        REPLAY
    }
    static class RequestInfo {
        private final ActionType action;
        private final String requestID;
        private final String transactionID;
        private final long amount;
        private final String currency;

        RequestInfo(ActionType action, String requestID, String transactionID, long amount, String currency) {
            this.action = action;
            this.requestID = requestID;
            this.transactionID = transactionID;
            this.amount = amount;
            this.currency = currency;
        }


    }
    static class TransactionInfo{// class contains all information about transaction
        private final String transactionID;
        private StateType state = StateType.NEW;
        private long authorizedAmount = 0;
        private long capturedAmount;
        private long refundedAmount;

        TransactionInfo(String transactionID) {
            this.transactionID = transactionID;
        }


        public long getRefundedAmount() {
            return refundedAmount;
        }

        public void setRefundedAmount(long refundedAmount) {
            this.refundedAmount = refundedAmount;
        }

        public long getCapturedAmount() {
            return capturedAmount;
        }

        public void setCapturedAmount(long capturedAmount) {
            this.capturedAmount = capturedAmount;
        }

        public StateType getState() {
            return state;
        }

        public void setState(StateType state) {
            this.state = state;
        }

        public String getTransactionID() {
            return transactionID;
        }

        public long getAuthorizedAmount() {
            return authorizedAmount;
        }

        public void setAuthorizedAmount(long authorizedAmount) {
            this.authorizedAmount = authorizedAmount;
        }
    }


    private static List<String> requestProcessing(List<String> requests){
        List<String> log = new ArrayList<>();

        // requestID --> Result (APPROVED/DECLINED)
        HashMap<String, Result> hashedRequests = new HashMap<>();

        //transaction --> TransactionID
        HashMap<String, TransactionInfo> transactions = new HashMap<>();

        return log;
    }
}
