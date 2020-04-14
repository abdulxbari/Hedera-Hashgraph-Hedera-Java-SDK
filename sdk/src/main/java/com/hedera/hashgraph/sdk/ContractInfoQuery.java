package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ContractGetInfoQuery;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.SmartContractServiceGrpc;
import io.grpc.MethodDescriptor;

public final class ContractInfoQuery extends QueryBuilder<ContractInfo, ContractInfoQuery> {
    private final ContractGetInfoQuery.Builder builder;

    public ContractInfoQuery() {
        builder = ContractGetInfoQuery.newBuilder();
    }

    public ContractInfoQuery setContractId(ContractId contractId) {
        builder.setContractID(contractId.toProtobuf());

        return this;
    }

    @Override
    protected void onMakeRequest(Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setContractGetInfo(builder.setHeader(header));
    }

    @Override
    protected ResponseHeader mapResponseHeader(Response response) {
        return response.getContractGetInfo().getHeader();
    }

    @Override
    protected QueryHeader mapRequestHeader(Query request) {
        return request.getContractGetInfo().getHeader();
    }

    @Override
    protected ContractInfo mapResponse(Response response) {
        return ContractInfo.fromProtobuf(response.getContractGetInfo().getContractInfo());
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethodDescriptor() {
        return SmartContractServiceGrpc.getGetContractInfoMethod();
    }
}