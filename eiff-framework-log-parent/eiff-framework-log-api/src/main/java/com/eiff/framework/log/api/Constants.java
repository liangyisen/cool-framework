package com.eiff.framework.log.api;

public interface Constants {
	public final static String TRANS_TYPE_HTTP = "URL";
	public final static String TRANS_TYPE_MVC = "MVC";
	// TODO Anders Pigeon是否要换成自定义名称
	public final static String TRANS_TYPE_RPC_CONSUMER = "PigeonCall";
	public final static String TRANS_TYPE_RPC_PROVIDER = "PigeonService";
	public final static String TRANS_TYPE_SERVICE = "SERVICE";
	public final static String TRANS_TYPE_JDBC = "JDBC";
	public final static String TRANS_TYPE_SQL = "SQL";
	public final static String TRANS_TYPE_REDIS = "REDIS";
	public final static String TRANS_TYPE_MQ_SYNC_PRODUCER = "MQSynProducer";
	public final static String TRANS_TYPE_MQ_ASYNC_PRODUCER = "MQAsyProducer";
	public final static String TRANS_TYPE_MQ_ASYNC_PRODUCER_CALLBACK = "MQAsyProducerCallback";
	public final static String TRANS_TYPE_MQ_OW_PRODUCER = "MQOnewayProducer";
	public final static String TRANS_TYPE_MQ_CONSUMER = "MQConsumer";
	public final static String TRANS_TYPE_MQ_PULL_CONSUMER = "MQPullConsumer";
	public final static String TRANS_TYPE_MQ_CONSUMER_FROM = "MQConsumer.from";

	public final static String TRANS_TYPE_SAGAS = "SAGAS";
	public final static String TRANS_TYPE_SAGAS_MANUAL = "SAGAS_MANUAL";
	public final static String TRANS_TYPE_SAGAS_ROLLBACK = "SAGAS_ROLLBACK";
	public final static String TRANS_TYPE_SAGAS_ROLLBACK_FAILED = "SAGAS_ROLLBACK_FAILED";
	public final static String TRANS_TYPE_SAGAS_COMPENSATE = "SAGAS_COMPENSATE";

	public final static String TRANS_STATUS_EXCEPTION = "Exception";

	public final static String EVENT_TYPE_EXCEPTION = "BusinessException";
	public final static String EVENT_TYPE_RPCRETURN = "RPCReturnCode";
	public final static String EVENT_FRONTRESPONSE_RPCRETURN = "FrontReturnCode";
	public final static String EVENT_FRONTRESPONSE_RPCRETURN_SUCCESS = "FrontReturnCode_succ";
	public final static String EVENT_TYPE_SQL_METHOD = "SQL.Method";
	public final static String EVENT_TYPE_SQL_DATABASE = "SQL.Database";
	public final static String EVENT_TYPE_MQ_SYNC_SEND = "MQ.SyncSend";
	public final static String EVENT_TYPE_MQ_UPDATE_OFFSET = "MQ.UPDATE.OFFSET";
	public final static String EVENT_TYPE_MQ_NO_MSG = "MQ.NO.MSG";

	public final static String LOG_FAILED_TO_CREATE_TRANS = "failed to create cat transaction";
	public final static String LOG_FAILED_TO_INVOKE_LOG = "failed to invoke cat log";
	public final static String LOG_FAILED_TO_INVOKE_EVENT = "failed to invoke cat event";
	public final static String LOG_FAILED_TO_SEND_CONTEXT = "failed to send cat context";
	public final static String LOG_FAILED_TO_RECEIVE_CLIENT = "failed to receive cat context";
	public final static String LOG_FAILED_TO_SET_SUCCESS = "failed to set success status for cat";
	public final static String LOG_FAILED_TO_SET_EXCEPTION = "failed to set exception status for cat";
	public final static String LOG_FAILED_TO_SET_COMPLETE = "failed to complete cat transaction";
	public final static String LOG_FAILED_TO_GET_TRACE_ID = "failed to get trace id by cat";
	public final static String LOG_FAILED_TO_RETURN_RPCRESULT = "failed to return RpcResult";
	public final static String LOG_FAILED_TO_GET_CLASS_NAME = "failed to get class name";

	public final static String LOG_HTTP_IN_MSG = "HTTP_IN {}";
	public final static String LOG_HTTP_EX_MSG = "HTTP_EX {}";
	public final static String LOG_HTTP_OUT_MSG = "HTTP_OUT {}";

	public final static String LOG_MVC_IN_MSG = "MVC_IN {}";
	public final static String LOG_MVC_EX_MSG = "MVC_EX {}";
	public final static String LOG_MVC_REQUEST_MSG = "MVC_REQ {} REQ {}";
	public final static String LOG_MVC_RESPONSE_MSG = "MVC_RSP {} RSP {}";
	public final static String LOG_MVC_OUT_MSG = "MVC_OUT {}";

	public final static String LOG_DUBBO_CONSUMER_IN_MSG = "RPC_CON_IN {} PRO_HOST {}";
	public final static String LOG_DUBBO_CONSUMER_EX_MSG = "RPC_CON_EX= {} PRO_HOST {}";
	public final static String LOG_DUBBO_CONSUMER_REQUEST_MSG = "RPC_CON_REQ {} PRO_HOST {} REQ {}";
	public final static String LOG_DUBBO_CONSUMER_RESPONSE_MSG = "RPC_CON_RSP {} PRO_HOST {} RSP {}";
	public final static String LOG_DUBBO_CONSUMER_OUT_MSG = "RPC_CON_OUT {} PRO_HOST {}";

	public final static String LOG_DUBBO_PROVIDER_IN_MSG = "RPC_PRO_IN {} CON_HOST {}";
	public final static String LOG_DUBBO_PROVIDER_EX_MSG = "RPC_PRO_EX {} CON_HOST {}";
	public final static String LOG_DUBBO_PROVIDER_REQUEST_MSG = "RPC_PRO_REQ {} CON_HOST {} REQ {}";
	public final static String LOG_DUBBO_PROVIDER_RESPONSE_MSG = "RPC_PRO_RSP {} CON_HOST {} RSP {}";
	public final static String LOG_DUBBO_PROVIDER_OUT_MSG = "RPC_PRO_OUT {} CON_ADD {}";

	public final static String LOG_SERVICE_IN_MSG = "SERVICE_IN {}";
	public final static String LOG_SERVICE_EX_MSG = "SERVICE_EX {}";
	public final static String LOG_SERVICE_OUT_MSG = "SERVICE_OUT {}";

	public final static String LOG_JDBC_IN_MSG = "JDBC_IN {} URL {} PARAM {}";
	public final static String LOG_JDBC_EX_MSG = "JDBC_EX {} URL {} PARAM {}";
	public final static String LOG_JDBC_LONG_SQL = "JDBC_LONG {}";
	public final static String LOG_JDBC_OUT_MSG = "JDBC_OUT {} URL {}";

	public final static String LOG_REDIS_IN_MSG = "REDIS_IN {} HOST {}";
	public final static String LOG_REDIS_EX_MSG = "REDIS_EX {} HOST {}";
	public final static String LOG_REDIS_ARGS_MSG = "REDIS_ARG {} HOST {} ARG {}";
	public final static String LOG_REDIS_OUT_MSG = "REDIS_OUT {} HOST {}";

	public final static String LOG_MQ_CONSUMER_IN_MSG = "MQ_CON_IN {} BROKER {} QUEUE {}";
	public final static String LOG_MQ_CONSUMER_EX_MSG = "MQ_CON_EX {} BROKER {} QUEUE {}";
	public final static String LOG_MQ_CONSUMER_MSG_MSG = "MQ_CON_MSG {} MSG {}";
	public final static String LOG_MQ_CONSUMER_SUCCESS_MSG = "MQ_CON_SUCCESS {} BROKER {} QUEUE {}";
	public final static String LOG_MQ_CONSUMER_COMMIT_MSG = "MQ_CON_COMMIT {} BROKER {} QUEUE {}";
	public final static String LOG_MQ_CONSUMER_ROLLBACK_MSG = "MQ_CON_ROLLBACK {} BROKER {} QUEUE {}";
	public final static String LOG_MQ_CONSUMER_SUSPEND_MSG = "MQ_CON_SUSPEND {} BROKER {} QUEUE {}";
	public final static String LOG_MQ_CONSUMER_RECONSUME_MSG = "MQ_CON_RECONSUME {} BROKER {} QUEUE {}";
	public final static String LOG_MQ_CONSUMER_RETURNNULL_MSG = "MQ_CON_NULL {} BROKER {} QUEUE {}";
	public final static String LOG_MQ_CONSUMER_OUT_MSG = "MQ_CON_OUT {} BROKER {} QUEUE {}";
	public final static String LOG_MQ_CONSUMER_NOT_FILTER_REPEAT_MSG = "MQ_CON_NOT_FILTER_REPEAT";

	public final static String LOG_MQ_PRODUCER_SYNC_IN_MSG = "MQ_PRO_SYN_IN {}";
	public final static String LOG_MQ_PRODUCER_SYNC_EX_MSG = "MQ_PRO_SYN_EX {}";
	public final static String LOG_MQ_PRODUCER_SYNC_MSG_MSG = "MQ_PRO_SYN_MSG {}";
	public final static String LOG_MQ_PRODUCER_SYNC_OK_MSG = "MQ_PRO_SYN_OK {} RESULT {}";
	public final static String LOG_MQ_PRODUCER_SYNC_SNA_MSG = "MQ_PRO_SYN_SNA {} RESULT {}";
	public final static String LOG_MQ_PRODUCER_SYNC_FDT_MSG = "MQ_PRO_SYN_FDT {} RESULT {}";
	public final static String LOG_MQ_PRODUCER_SYNC_FST_MSG = "MQ_PRO_SYN_FST {} RESULT {}";
	public final static String LOG_MQ_PRODUCER_SYNC_RETURNNULL_MSG = "MQ_PRO_SYN_NULL {}";
	public final static String LOG_MQ_PRODUCER_SYNC_FINAL_SEND_FAILED_MSG = "MQ_PRO_SYN_FINAL_FAILED {}";
	public final static String LOG_MQ_PRODUCER_SYNC_OUT_MSG = "MQ_PRO_SYN_OUT {}";

	public final static String LOG_MQ_PRODUCER_ASYNC_IN_MSG = "MQ_PRO_ASY_IN {}";
	public final static String LOG_MQ_PRODUCER_ASYNC_EX_MSG = "MQ_PRO_ASY_EX {}";
	public final static String LOG_MQ_PRODUCER_ASYNC_MSG_MSG = "MQ_PRO_ASY_MSG {}";
	public final static String LOG_MQ_PRODUCER_ASYNC_OUT_MSG = "MQ_PRO_ASY_OUT {}";

	public final static String LOG_MQ_PRODUCER_ASYNC_CALLBACK_IN_MSG = "MQ_PRO_ASY_CB_IN {}";
	public final static String LOG_MQ_PRODUCER_ASYNC_CALLBACK_EX_MSG = "MQ_PRO_ASY_CB_EX {}";
	public final static String LOG_MQ_PRODUCER_ASYNC_CALLBACK_OK_MSG = "MQ_PRO_ASY_CB_OK {}";
	public final static String LOG_MQ_PRODUCER_ASYNC_CALLBACK_SNA_MSG = "MQ_PRO_ASY_CB_SNA {}";
	public final static String LOG_MQ_PRODUCER_ASYNC_CALLBACK_FDT_MSG = "MQ_PRO_ASY_CB_FDT {}";
	public final static String LOG_MQ_PRODUCER_ASYNC_CALLBACK_FST_MSG = "MQ_PRO_ASY_CB_FST {}";
	public final static String LOG_MQ_PRODUCER_ASYNC_CALLBACK_RETURNNULL_MSG = "MQ_PRO_ASY_CB_NULL {}";
	public final static String LOG_MQ_PRODUCER_ASYNC_CALLBACK_OUT_MSG = "MQ_PRO_ASY_CB_OUT {}";

	public final static String LOG_MQ_PRODUCER_OW_IN_MSG = "MQ_PRO_ASY_IN {}";
	public final static String LOG_MQ_PRODUCER_OW_EX_MSG = "MQ_PRO_ASY_EX {}";
	public final static String LOG_MQ_PRODUCER_OW_OUT_MSG = "MQ_PRO_ASY_OUT {}";

	public final static String LOG_MQ_CONSUMER_PULL_EX_MSG = "MQ_COS_PULL_EX TOPIC {} EX {}";

	public final static String TRACE_ID_PREFIX = " TRACEID ";
	public final static String PARENT_ID_PREFIX = " PARENT ";
	public final static String CURRENT_ID_PREFIX = " CURRENT ";

	// public final static String RECODER_THIRDPARTY_ROOT_NAME _PAYMENT=
	// "PAYMENT";
	public final static String RECODER_EXTERNALSERVICES_ROOT_TYPE = "EXTERNALSERVICES";

	public final static String RECODER_BUSINESSSERVICES_ROOT_TYPE = "BUSINESSSERVICES";

	// ALERT
	public static final String ALERT_AT_ONCE = "ALERT_RED";
	public static final String ALERT_NOT_AT_ONCE = "ALERT_YELLOW";

	public final static String EMPTY_TRACE_ID = " empty-trace-id ";
	
	public final String TRACE_ROOT = "_traceRootMessageId";

	public final String TRACE_PARENT = "_traceParentMessageId";

	public final String TRACE_CHILD = "_traceChildMessageId";
}
