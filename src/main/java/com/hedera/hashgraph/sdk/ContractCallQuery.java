package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.ContractCallLocalQuery;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.SmartContractServiceGrpc;
import io.grpc.MethodDescriptor;

public final class ContractCallQuery extends QueryBuilder<ContractFunctionResult, ContractCallQuery> {
    private final ContractCallLocalQuery.Builder builder;

    public ContractCallQuery() {
        builder = ContractCallLocalQuery.newBuilder();
    }

    public ContractCallQuery setContractId(ContractId contractId) {
        builder.setContractID(contractId.toProtobuf());
        return this;
    }

    public ContractCallQuery setGas(long gas) {
        builder.setGas(gas);
        return this;
    }

    public ContractCallQuery setFunctionParameters(ByteString functionParameters) {
        builder.setFunctionParameters(functionParameters);
        return this;
    }

    public ContractCallQuery setFunction(String name) {
        return setFunction(name, new ContractFunctionParams());
    }

    public ContractCallQuery setFunction(String name, ContractFunctionParams params) {
        return setFunctionParameters(params.toBytes(name));
    }

    public ContractCallQuery setMaxResultSize(long size) {
        builder.setMaxResultSize(size);
        return this;
    }

    @Override
    protected void onMakeRequest(Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setContractCallLocal(builder.setHeader(header));
    }

    @Override
    protected ResponseHeader mapResponseHeader(Response response) {
        return response.getContractCallLocal().getHeader();
    }

    @Override
    protected QueryHeader mapRequestHeader(Query request) {
        return request.getContractCallLocal().getHeader();
    }

    @Override
    protected ContractFunctionResult mapResponse(Response response) {
        return new ContractFunctionResult(response.getContractCallLocal().getFunctionResult());
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethodDescriptor() {
        return SmartContractServiceGrpc.getContractCallLocalMethodMethod();
    }
}
