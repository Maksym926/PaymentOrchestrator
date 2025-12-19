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
        PARTIALLY_REFUNDED,
        REFUNDED,
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
        private FailedTransaction failedTransaction = null;
        private String currency;


        TransactionInfo(String transactionID) {
            this.transactionID = transactionID;
        }
        TransactionInfo(String transactionID, String currency) {
            this.transactionID = transactionID;
            this.currency = currency;
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

        public void setFailedRequest(ActionType failedAction, long amount, String failReason){
            failedTransaction = new FailedTransaction(failedAction, amount, failReason);
        }
        public FailedTransaction getFailedTransaction(){
            return this.failedTransaction;
        }
    }

    static class FailedTransaction{
        public ActionType getFailedAction() {
            return failedAction;
        }

        public long getAmount() {
            return amount;
        }

        public String getFailReason() {
            return failReason;
        }

        private ActionType failedAction;
        private long amount;
        private String failReason;


        public FailedTransaction(ActionType action, long amount, String failReason) {
            this.failedAction = action;
            this.amount = amount;
            this.failReason = failReason;
        }
    }

    // requestID --> Result (APPROVED/DECLINED)
    HashMap<String, Result> result = new HashMap<>();

    //transactionID --> TransactionInfo
    HashMap<String, TransactionInfo> transactions = new HashMap<>();



    List<String> log = new ArrayList<>();

    private List<String> requestProcessing(List<String> requests){

        for(int i = 0; i<requests.size(); i++){
            String request = requests.get(i);
            String[] splitRequest = request.split(",");
            ActionType action = ActionType.valueOf(splitRequest[0].trim().toUpperCase());
            String requestId = splitRequest[1];
            String transactionId = splitRequest[2];
            long amount = Long.parseLong(splitRequest[3]);
            String currency = splitRequest[4];
            RequestInfo requestInfo = new RequestInfo(action,requestId , transactionId, amount, currency);




        }


        // action Authorize
        // check if we do not have the same requestID in result (if the same requestID return REPLAY, requestID )
        // get transactionID from request
        // check if transaction is already recorded
        // if not create new transaction instance and add to transaction
        // get transaction instance from the transactions
        // check the transaction state
        // if it's new we change class params like state = AUTHORIZED  authorizedAmount = newAmount --> update result  APPROVED, requestID
        //if state is already AUTHORIZED the result will be --> AUTHORIZED, requestID
        //if the state is CAPTURED CANCELED PARTIALLY_CAPTURED result is DECLINED, requestID




        // action Capture
        // check if we do not have the same requestID in result (if the same requestID return REPLAY, requestID )
        // get transactionID from request
        // check if transaction is already recorded
        //if not exists resul Decline, requestID
        // if transaction exists check if the state is PARTIALLY_CAPTURED or AUTHORIZED if not decline
        // if yes we check if( capturedAmount + amount < authorizedAmount) let's omit manipulations with currency at the moment
        // if condition above is true Approve transaction and update transaction info  captureAmount = captureAmount + amount;
        // if is false decline transaction

        //action Refund
        // check if we do not have the same requestID in result (if the same requestID return REPLAY, requestID )
        // get transactionID from request
        // check if transaction is already recorded
        //if not exists result --> Decline, requestID
        // if transaction exists check if the state is CAPTURED or PARTIALLY_CAPTURED if not decline
        // if yes check is refund + amount < captured if false decline
        // if true update  and refund = refund + amount


        //Cancel
        // check if we do not have the same requestID in result (if the same requestID return REPLAY, requestID )
        // get transactionID from request
        // check if transaction is already recorded
        //if not exists result --> Decline, requestID
        // if transaction exists check if the state is AUTHORIZED if not decline
        // if yes change the status of transaction on CANCELED





        return log;
    }
    private void processOneRequest(RequestInfo requestInfo){


        // check on idempotency
        if(result.containsKey(requestInfo.requestID)) {
           addResultHelper(requestInfo.requestID, Result.REPLAY);
           return;
        }
        // get transaction(can be null)
        TransactionInfo transaction = transactions.get(requestInfo.transactionID);

        // separate case for retry
        if(requestInfo.action == ActionType.RETRY){
            if(transaction == null){
                addResultHelper(requestInfo.requestID, Result.DECLINED);
                return;
            }
            boolean res = isRequestReattempted(transaction);
            addResultHelper(requestInfo.requestID, res ? Result.APPROVED : Result.DECLINED);
            return;
        }
        // check if transaction has failure
        if(transaction != null && transaction.failedTransaction != null){
            addResultHelper(requestInfo.requestID, Result.DECLINED);
            return;
        }
        // check if transaction is new and request action is AUTHORIZE
        if(transaction == null){

            if(requestInfo.action != ActionType.AUTHORIZE){
                addResultHelper(requestInfo.requestID, Result.DECLINED);
                return;
            }
            transaction = new TransactionInfo(requestInfo.transactionID, requestInfo.currency);
            boolean res = executeAction(requestInfo.action, transaction, requestInfo.amount);
            addResultHelper(requestInfo.requestID, res ? Result.APPROVED : Result.DECLINED);
            return;
        }
        // for rest cases
        boolean res = executeAction(requestInfo.action, transaction, requestInfo.amount);
        addResultHelper(requestInfo.requestID, res ? Result.APPROVED : Result.DECLINED);



    }


    // reattempt request if transaction has failure
    // if failed transaction was reattempted successfully make failedTransaction equals to null
    private boolean isRequestReattempted(TransactionInfo transaction){
        if (transaction.failedTransaction != null) {
            if(executeAction(transaction.failedTransaction.failedAction, transaction, transaction.failedTransaction.amount)){
                transaction.failedTransaction = null;
                return true;
            }
        }
        return false;

    }
    private boolean executeAction(ActionType action, TransactionInfo transaction, long amount){
        if (amount < 0) return false; // check if amount is positive

        switch (action) {
            case AUTHORIZE:
                if (!transaction.state.equals(StateType.NEW)) return false;

                transaction.authorizedAmount = amount;
                transaction.state = StateType.AUTHORIZED;

                transactions.put(transaction.transactionID, transaction);
                return true;


            case CAPTURE:
                switch (transaction.state) {
                    case AUTHORIZED, PARTIALLY_CAPTURED:
                        if (transaction.capturedAmount + amount < transaction.authorizedAmount) {
                            transaction.state = StateType.PARTIALLY_CAPTURED;
                            transaction.capturedAmount += amount;

                            return true;

                        } else if (transaction.capturedAmount + amount == transaction.authorizedAmount) {
                            transaction.state = StateType.CAPTURED;
                            transaction.capturedAmount += amount;
                            return true;
                        } else {
                            return false;
                        }

                    default:
                        return false;
                }

            case REFUND:
                switch (transaction.state) {
                    case CAPTURED, PARTIALLY_CAPTURED, PARTIALLY_REFUNDED:
                        if (transaction.refundedAmount + amount < transaction.capturedAmount) {
                            transaction.state = StateType.PARTIALLY_REFUNDED;
                            transaction.refundedAmount += amount;

                            return true;

                        } else if (transaction.refundedAmount + amount == transaction.capturedAmount) {
                            transaction.state = StateType.REFUNDED;
                            transaction.refundedAmount += amount;
                            return true;
                        } else {
                            return false;
                        }



                    default:
                        return false;

                }



            case CANCEL:
                switch (transaction.state) {
                    case AUTHORIZED:
                        transaction.state = StateType.CANCELED;
                        return true;
                    default:
                        return false;
                }
        }
        return false;
    }
    private void addResultHelper(String requestId, Result res){
        result.put(requestId, res);
        log.add(res + "," + requestId);
    }
}
