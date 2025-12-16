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

            switch (requestInfo.action){
                case AUTHORIZE:
                    if(!result.containsKey(requestInfo.requestID)){
                        if(transactions.containsKey(requestInfo.transactionID)){
                            addResultHelper(requestInfo.requestID, Result.DECLINED);
                        }else{
                            TransactionInfo newTransaction = new TransactionInfo(requestInfo.transactionID);
                            newTransaction.state = StateType.AUTHORIZED;
                            newTransaction.authorizedAmount = requestInfo.amount;
                            transactions.put(newTransaction.transactionID, newTransaction);
                            addResultHelper(requestInfo.requestID, Result.APPROVED);
                        }


                    }else {
                        addResultHelper(requestInfo.requestID, Result.REPLAY);
                    }
                    break;
                case CAPTURE:
                    if(!result.containsKey(requestInfo.requestID)){
                        if(transactions.containsKey(requestInfo.transactionID)){
                            TransactionInfo existing = transactions.get(requestInfo.transactionID);
                            switch (existing.state){
                                case AUTHORIZED, PARTIALLY_CAPTURED:
                                    if(existing.capturedAmount + requestInfo.amount < existing.authorizedAmount){
                                        existing.state = StateType.PARTIALLY_CAPTURED;
                                        existing.capturedAmount += requestInfo.amount;

                                        addResultHelper(requestInfo.requestID, Result.APPROVED);

                                    }
                                    else if(existing.capturedAmount + requestInfo.amount == existing.authorizedAmount){
                                        existing.state = StateType.CAPTURED;
                                        existing.capturedAmount += requestInfo.amount;
                                        addResultHelper(requestInfo.requestID, Result.APPROVED);
                                    }
                                    else {
                                        addResultHelper(requestInfo.requestID, Result.DECLINED);
                                    }
                                    break;


                                default:
                                    addResultHelper(requestInfo.requestID, Result.DECLINED);

                            }

                        }else {
                            addResultHelper(requestInfo.requestID, Result.DECLINED);
                        }

                    }else {
                        addResultHelper(requestInfo.requestID, Result.REPLAY);
                    }
                    break;

                case REFUND:
                    if(!result.containsKey(requestInfo.requestID)) {
                        if (transactions.containsKey(requestInfo.transactionID)) {
                            TransactionInfo existing = transactions.get(requestInfo.transactionID);
                            switch (existing.state){
                                case CAPTURED, PARTIALLY_CAPTURED, PARTIALLY_REFUNDED:
                                    if(existing.refundedAmount + requestInfo.amount < existing.capturedAmount){
                                        existing.state = StateType.PARTIALLY_REFUNDED;
                                        existing.refundedAmount += requestInfo.amount;

                                        addResultHelper(requestInfo.requestID, Result.APPROVED);

                                    }
                                    else if(existing.capturedAmount + requestInfo.amount == existing.capturedAmount){
                                        existing.state = StateType.REFUNDED;
                                        existing.refundedAmount += requestInfo.amount;
                                        addResultHelper(requestInfo.requestID, Result.APPROVED);
                                    }
                                    else {
                                        addResultHelper(requestInfo.requestID, Result.DECLINED);
                                    }
                                    break;


                                default:
                                    addResultHelper(requestInfo.requestID, Result.DECLINED);

                            }
                        }else {
                            addResultHelper(requestInfo.requestID, Result.DECLINED);
                        }
                    }else {
                        addResultHelper(requestInfo.requestID, Result.REPLAY);
                    }
                    break;

                case CANCEL:
                    if(!result.containsKey(requestInfo.requestID)) {
                        if (transactions.containsKey(requestInfo.transactionID)) {
                            TransactionInfo existing = transactions.get(requestInfo.transactionID);
                            switch (existing.state){
                                case AUTHORIZED :
                                    existing.state = StateType.CANCELED;
                                    addResultHelper(requestInfo.requestID, Result.APPROVED);
                                    break;
                                default:
                                    addResultHelper(requestInfo.requestID, Result.DECLINED);

                            }
                        }else {
                            addResultHelper(requestInfo.requestID, Result.DECLINED);
                        }
                    }else {
                        addResultHelper(requestInfo.requestID, Result.REPLAY);
                    }
                    break;

            }
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
    private void addResultHelper(String requestId, Result res){
        result.put(requestId, res);
        log.add(res + "," + requestId);
    }
}
