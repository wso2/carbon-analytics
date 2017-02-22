package org.wso2.carbon.analytics.indexerservice.impl;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.StreamingResponseCallback;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.wso2.carbon.analytics.indexerservice.utils.IndexerUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * This class represents the Custom SolrClient implementation used for indexing
 */
public class CarbonIndexerClient extends SolrClient {

    private SolrClient solrClient;

    public CarbonIndexerClient(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    public UpdateResponse add(String collection, Collection<SolrInputDocument> docs) throws SolrServerException, IOException {
        return solrClient.add(IndexerUtils.getTableNameWithDomainName(collection), docs);
    }

    public UpdateResponse add(Collection<SolrInputDocument> docs) throws SolrServerException, IOException {
        return solrClient.add(docs);
    }

    public UpdateResponse add(String collection, Collection<SolrInputDocument> docs, int commitWithinMs) throws SolrServerException, IOException {
        return solrClient.add(IndexerUtils.getTableNameWithDomainName(collection), docs, commitWithinMs);
    }

    public UpdateResponse add(Collection<SolrInputDocument> docs, int commitWithinMs) throws SolrServerException, IOException {
        return solrClient.add(docs, commitWithinMs);
    }

    public UpdateResponse add(String collection, SolrInputDocument doc) throws SolrServerException, IOException {
       return solrClient.add(IndexerUtils.getTableNameWithDomainName(collection), doc);
    }

    public UpdateResponse add(SolrInputDocument doc) throws SolrServerException, IOException {
        return solrClient.add(doc);
    }

    public UpdateResponse add(String collection, SolrInputDocument doc, int commitWithinMs) throws SolrServerException, IOException {
        return solrClient.add(IndexerUtils.getTableNameWithDomainName(collection), doc, commitWithinMs);
    }

    public UpdateResponse add(SolrInputDocument doc, int commitWithinMs) throws SolrServerException, IOException {
        return solrClient.add(doc, commitWithinMs);
    }

    public UpdateResponse add(String collection, Iterator<SolrInputDocument> docIterator) throws SolrServerException, IOException {
        return solrClient.add(IndexerUtils.getTableNameWithDomainName(collection), docIterator);
    }

    public UpdateResponse add(Iterator<SolrInputDocument> docIterator) throws SolrServerException, IOException {
        return solrClient.add(docIterator);
    }

    public UpdateResponse addBean(String collection, Object obj) throws IOException, SolrServerException {
        return solrClient.addBean(IndexerUtils.getTableNameWithDomainName(collection), obj);
    }

    public UpdateResponse addBean(Object obj) throws IOException, SolrServerException {
        return solrClient.addBean(obj);
    }

    public UpdateResponse addBean(String collection, Object obj, int commitWithinMs) throws IOException, SolrServerException {
        return solrClient.addBean(IndexerUtils.getTableNameWithDomainName(collection), obj, commitWithinMs);
    }

    public UpdateResponse addBean(Object obj, int commitWithinMs) throws IOException, SolrServerException {
        return solrClient.addBean(obj, commitWithinMs);
    }

    public UpdateResponse addBeans(String collection, Collection<?> beans) throws SolrServerException, IOException {
        return solrClient.addBeans(IndexerUtils.getTableNameWithDomainName(collection), beans);
    }

    public UpdateResponse addBeans(Collection<?> beans) throws SolrServerException, IOException {
        return solrClient.addBeans(beans);
    }

    public UpdateResponse addBeans(String collection, Collection<?> beans, int commitWithinMs) throws SolrServerException, IOException {
        return solrClient.addBeans(IndexerUtils.getTableNameWithDomainName(collection), beans, commitWithinMs);
    }

    public UpdateResponse addBeans(Collection<?> beans, int commitWithinMs) throws SolrServerException, IOException {
        return solrClient.addBeans(beans, commitWithinMs);
    }

    public UpdateResponse addBeans(String collection, final Iterator<?> beanIterator) throws SolrServerException, IOException {
        return solrClient.addBeans(IndexerUtils.getTableNameWithDomainName(collection), beanIterator);
    }

    public UpdateResponse addBeans(Iterator<?> beanIterator) throws SolrServerException, IOException {
        return solrClient.addBeans(beanIterator);
    }

    public UpdateResponse commit(String collection) throws SolrServerException, IOException {
        return solrClient.commit(IndexerUtils.getTableNameWithDomainName(collection));
    }

    public UpdateResponse commit() throws SolrServerException, IOException {
        return solrClient.commit();
    }

    public UpdateResponse commit(String collection, boolean waitFlush, boolean waitSearcher) throws SolrServerException, IOException {
        return solrClient.commit(IndexerUtils.getTableNameWithDomainName(collection), waitFlush, waitSearcher);
    }

    public UpdateResponse commit(boolean waitFlush, boolean waitSearcher) throws SolrServerException, IOException {
        return solrClient.commit(waitFlush, waitSearcher);
    }

    public UpdateResponse commit(String collection, boolean waitFlush, boolean waitSearcher, boolean softCommit) throws SolrServerException, IOException {
        return solrClient.commit(IndexerUtils.getTableNameWithDomainName(collection), waitFlush, waitSearcher, softCommit);
    }

    public UpdateResponse commit(boolean waitFlush, boolean waitSearcher, boolean softCommit) throws SolrServerException, IOException {
        return solrClient.commit(waitFlush, waitSearcher, softCommit);
    }

    public UpdateResponse optimize(String collection) throws SolrServerException, IOException {
        return solrClient.optimize(IndexerUtils.getTableNameWithDomainName(collection));
    }

    public UpdateResponse optimize() throws SolrServerException, IOException {
        return solrClient.optimize();
    }

    public UpdateResponse optimize(String collection, boolean waitFlush, boolean waitSearcher) throws SolrServerException, IOException {
        return solrClient.optimize(IndexerUtils.getTableNameWithDomainName(collection), waitFlush, waitSearcher);
    }

    public UpdateResponse optimize(boolean waitFlush, boolean waitSearcher) throws SolrServerException, IOException {
        return solrClient.optimize(waitFlush, waitSearcher);
    }

    public UpdateResponse optimize(String collection, boolean waitFlush, boolean waitSearcher, int maxSegments) throws SolrServerException, IOException {
        return solrClient.optimize(IndexerUtils.getTableNameWithDomainName(collection), waitFlush, waitSearcher, maxSegments);
    }

    public UpdateResponse optimize(boolean waitFlush, boolean waitSearcher, int maxSegments) throws SolrServerException, IOException {
        return solrClient.optimize(waitFlush, waitSearcher, maxSegments);
    }

    public UpdateResponse rollback(String collection) throws SolrServerException, IOException {
        return solrClient.rollback(IndexerUtils.getTableNameWithDomainName(collection));
    }

    public UpdateResponse rollback() throws SolrServerException, IOException {
        return solrClient.rollback();
    }

    public UpdateResponse deleteById(String collection, String id) throws SolrServerException, IOException {
        return solrClient.deleteById(IndexerUtils.getTableNameWithDomainName(collection), id);
    }

    public UpdateResponse deleteById(String id) throws SolrServerException, IOException {
        return solrClient.deleteById(id);
    }

    public UpdateResponse deleteById(String collection, String id, int commitWithinMs) throws SolrServerException, IOException {
        return solrClient.deleteById(IndexerUtils.getTableNameWithDomainName(collection), id, commitWithinMs);
    }

    public UpdateResponse deleteById(String id, int commitWithinMs) throws SolrServerException, IOException {
        return solrClient.deleteById(id, commitWithinMs);
    }

    public UpdateResponse deleteById(String collection, List<String> ids) throws SolrServerException, IOException {
        return solrClient.deleteById(IndexerUtils.getTableNameWithDomainName(collection), ids);
    }

    public UpdateResponse deleteById(List<String> ids) throws SolrServerException, IOException {
        return solrClient.deleteById(ids);
    }

    public UpdateResponse deleteById(String collection, List<String> ids, int commitWithinMs) throws SolrServerException, IOException {
        return solrClient.deleteById(IndexerUtils.getTableNameWithDomainName(collection), ids, commitWithinMs);
    }

    public UpdateResponse deleteById(List<String> ids, int commitWithinMs) throws SolrServerException, IOException {
        return solrClient.deleteById(ids, commitWithinMs);
    }

    public UpdateResponse deleteByQuery(String collection, String query) throws SolrServerException, IOException {
        return solrClient.deleteByQuery(IndexerUtils.getTableNameWithDomainName(collection), query);
    }

    public UpdateResponse deleteByQuery(String query) throws SolrServerException, IOException {
        return solrClient.deleteByQuery(query);
    }

    public UpdateResponse deleteByQuery(String collection, String query, int commitWithinMs) throws SolrServerException, IOException {
        return solrClient.deleteByQuery(IndexerUtils.getTableNameWithDomainName(collection), query, commitWithinMs);
    }

    public UpdateResponse deleteByQuery(String query, int commitWithinMs) throws SolrServerException, IOException {
        return solrClient.deleteByQuery(query, commitWithinMs);
    }

    public SolrPingResponse ping() throws SolrServerException, IOException {
        return solrClient.ping();
    }

    public QueryResponse query(String collection, SolrParams params) throws SolrServerException, IOException {
        return solrClient.query(IndexerUtils.getTableNameWithDomainName(collection), params);
    }

    public QueryResponse query(SolrParams params) throws SolrServerException, IOException {
        return solrClient.query(params);
    }

    public QueryResponse query(String collection, SolrParams params, SolrRequest.METHOD method) throws SolrServerException, IOException {
        return solrClient.query(IndexerUtils.getTableNameWithDomainName(collection), params, method);
    }

    public QueryResponse query(SolrParams params, SolrRequest.METHOD method) throws SolrServerException, IOException {
        return solrClient.query(params, method);
    }

    public QueryResponse queryAndStreamResponse(String collection, SolrParams params, StreamingResponseCallback callback) throws SolrServerException, IOException {
        return solrClient.queryAndStreamResponse(IndexerUtils.getTableNameWithDomainName(collection), params, callback);
    }

    public QueryResponse queryAndStreamResponse(SolrParams params, StreamingResponseCallback callback) throws SolrServerException, IOException {
        return solrClient.queryAndStreamResponse(params, callback);
    }

    public SolrDocument getById(String collection, String id) throws SolrServerException, IOException {
        return solrClient.getById(IndexerUtils.getTableNameWithDomainName(collection), id);
    }

    public SolrDocument getById(String id) throws SolrServerException, IOException {
        return solrClient.getById(id);
    }

    public SolrDocument getById(String collection, String id, SolrParams params) throws SolrServerException, IOException {
        return solrClient.getById(IndexerUtils.getTableNameWithDomainName(collection), id, params);
    }

    public SolrDocument getById(String id, SolrParams params) throws SolrServerException, IOException {
        return solrClient.getById(id, params);
    }

    public SolrDocumentList getById(String collection, Collection<String> ids) throws SolrServerException, IOException {
        return solrClient.getById(IndexerUtils.getTableNameWithDomainName(collection), ids);
    }

    public SolrDocumentList getById(Collection<String> ids) throws SolrServerException, IOException {
        return solrClient.getById(ids);
    }

    public SolrDocumentList getById(String collection, Collection<String> ids, SolrParams params) throws SolrServerException, IOException {
        return solrClient.getById(IndexerUtils.getTableNameWithDomainName(collection), ids, params);
    }

    public SolrDocumentList getById(Collection<String> ids, SolrParams params) throws SolrServerException, IOException {
        return solrClient.getById(ids, params);
    }
    @Override
    public  NamedList<Object> request(SolrRequest request, String collection) throws SolrServerException, IOException {
        return solrClient.request(request, IndexerUtils.getTableNameWithDomainName(collection));
    }

    public UpdateResponse add(String collection, Collection<SolrInputDocument> docs, String username) throws SolrServerException, IOException {
        return solrClient.add(IndexerUtils.getTableNameWithDomainName(collection), docs);
    }

    public UpdateResponse add(Collection<SolrInputDocument> docs, String username) throws SolrServerException, IOException {
        return solrClient.add(docs);
    }

    public UpdateResponse add(String collection, Collection<SolrInputDocument> docs, int commitWithinMs, String username) throws SolrServerException, IOException {
        return solrClient.add(IndexerUtils.getTableNameWithDomainName(collection), docs, commitWithinMs);
    }

    public UpdateResponse add(Collection<SolrInputDocument> docs, int commitWithinMs, String username) throws SolrServerException, IOException {
        return solrClient.add(docs, commitWithinMs);
    }

    public UpdateResponse add(String collection, SolrInputDocument doc, String username) throws SolrServerException, IOException {
        return solrClient.add(IndexerUtils.getTableNameWithDomainName(collection), doc);
    }

    public UpdateResponse add(SolrInputDocument doc, String username) throws SolrServerException, IOException {
        return solrClient.add(doc);
    }

    public UpdateResponse add(String collection, SolrInputDocument doc, int commitWithinMs, String username) throws SolrServerException, IOException {
        return solrClient.add(IndexerUtils.getTableNameWithDomainName(collection), doc, commitWithinMs);
    }

    public UpdateResponse add(SolrInputDocument doc, int commitWithinMs, String username) throws SolrServerException, IOException {
        return solrClient.add(doc, commitWithinMs);
    }

    public UpdateResponse add(String collection, Iterator<SolrInputDocument> docIterator, String username) throws SolrServerException, IOException {
        return solrClient.add(IndexerUtils.getTableNameWithDomainName(collection), docIterator);
    }

    public UpdateResponse add(Iterator<SolrInputDocument> docIterator, String username) throws SolrServerException, IOException {
        return solrClient.add(docIterator);
    }

    public UpdateResponse addBean(String collection, Object obj, String username) throws IOException, SolrServerException {
        return solrClient.addBean(IndexerUtils.getTableNameWithDomainName(collection), obj);
    }

    public UpdateResponse addBean(Object obj, String username) throws IOException, SolrServerException {
        return solrClient.addBean(obj);
    }

    public UpdateResponse addBean(String collection, Object obj, int commitWithinMs, String username) throws IOException, SolrServerException {
        return solrClient.addBean(IndexerUtils.getTableNameWithDomainName(collection), obj, commitWithinMs);
    }

    public UpdateResponse addBean(Object obj, int commitWithinMs, String username) throws IOException, SolrServerException {
        return solrClient.addBean(obj, commitWithinMs);
    }

    public UpdateResponse addBeans(String collection, Collection<?> beans, String username) throws SolrServerException, IOException {
        return solrClient.addBeans(IndexerUtils.getTableNameWithDomainName(collection), beans);
    }

    public UpdateResponse addBeans(Collection<?> beans, String username) throws SolrServerException, IOException {
        return solrClient.addBeans(beans);
    }

    public UpdateResponse addBeans(String collection, Collection<?> beans, int commitWithinMs, String username) throws SolrServerException, IOException {
        return solrClient.addBeans(IndexerUtils.getTableNameWithDomainName(collection), beans, commitWithinMs);
    }

    public UpdateResponse addBeans(Collection<?> beans, int commitWithinMs, String username) throws SolrServerException, IOException {
        return solrClient.addBeans(beans, commitWithinMs);
    }

    public UpdateResponse addBeans(String collection, final Iterator<?> beanIterator, String username) throws SolrServerException, IOException {
        return solrClient.addBeans(IndexerUtils.getTableNameWithDomainName(collection), beanIterator);
    }

    public UpdateResponse addBeans(Iterator<?> beanIterator, String username) throws SolrServerException, IOException {
        return solrClient.addBeans(beanIterator);
    }

    public UpdateResponse commit(String collection, String username) throws SolrServerException, IOException {
        return solrClient.commit(IndexerUtils.getTableNameWithDomainName(collection));
    }

    public UpdateResponse commit(String collection, boolean waitFlush, boolean waitSearcher, String username) throws SolrServerException, IOException {
        return solrClient.commit(IndexerUtils.getTableNameWithDomainName(collection), waitFlush, waitSearcher);
    }

    public UpdateResponse commit(boolean waitFlush, boolean waitSearcher, String username) throws SolrServerException, IOException {
        return solrClient.commit(waitFlush, waitSearcher);
    }

    public UpdateResponse commit(String collection, boolean waitFlush, boolean waitSearcher, boolean softCommit, String username) throws SolrServerException, IOException {
        return solrClient.commit(IndexerUtils.getTableNameWithDomainName(collection), waitFlush, waitSearcher, softCommit);
    }

    public UpdateResponse commit(boolean waitFlush, boolean waitSearcher, boolean softCommit, String username) throws SolrServerException, IOException {
        return solrClient.commit(waitFlush, waitSearcher, softCommit);
    }

    public UpdateResponse optimize(String collection, String username) throws SolrServerException, IOException {
        return solrClient.optimize(IndexerUtils.getTableNameWithDomainName(collection));
    }

    public UpdateResponse optimizeAuthorizedByUser(String username) throws SolrServerException, IOException {
        return solrClient.optimize();
    }

    public UpdateResponse optimize(String collection, boolean waitFlush, boolean waitSearcher, String username) throws SolrServerException, IOException {
        return solrClient.optimize(IndexerUtils.getTableNameWithDomainName(collection), waitFlush, waitSearcher);
    }

    public UpdateResponse optimize(boolean waitFlush, boolean waitSearcher, String username) throws SolrServerException, IOException {
        return solrClient.optimize(waitFlush, waitSearcher);
    }

    public UpdateResponse optimize(String collection, boolean waitFlush, boolean waitSearcher, int maxSegments, String username) throws SolrServerException, IOException {
        return solrClient.optimize(IndexerUtils.getTableNameWithDomainName(collection), waitFlush, waitSearcher, maxSegments);
    }

    public UpdateResponse optimize(boolean waitFlush, boolean waitSearcher, int maxSegments, String username) throws SolrServerException, IOException {
        return solrClient.optimize(waitFlush, waitSearcher, maxSegments);
    }

    public UpdateResponse rollback(String collection, String username) throws SolrServerException, IOException {
        return solrClient.rollback(IndexerUtils.getTableNameWithDomainName(collection));
    }

    public UpdateResponse rollbackAuthorizedByUser(String username) throws SolrServerException, IOException {
        return solrClient.rollback();
    }

    public UpdateResponse deleteById(String collection, String id, String username) throws SolrServerException, IOException {
        return solrClient.deleteById(IndexerUtils.getTableNameWithDomainName(collection), id);
    }

    public UpdateResponse deleteByIdAuthorizedByUser(String id, String username) throws SolrServerException, IOException {
        return solrClient.deleteById(id);
    }

    public UpdateResponse deleteById(String collection, String id, int commitWithinMs, String username) throws SolrServerException, IOException {
        return solrClient.deleteById(IndexerUtils.getTableNameWithDomainName(collection), id, commitWithinMs);
    }

    public UpdateResponse deleteById(String id, int commitWithinMs, String username) throws SolrServerException, IOException {
        return solrClient.deleteById(id, commitWithinMs);
    }

    public UpdateResponse deleteById(String collection, List<String> ids, String username) throws SolrServerException, IOException {
        return solrClient.deleteById(IndexerUtils.getTableNameWithDomainName(collection), ids);
    }

    public UpdateResponse deleteById(List<String> ids, String username) throws SolrServerException, IOException {
        return solrClient.deleteById(ids);
    }

    public UpdateResponse deleteById(String collection, List<String> ids, int commitWithinMs, String username) throws SolrServerException, IOException {
        return solrClient.deleteById(IndexerUtils.getTableNameWithDomainName(collection), ids, commitWithinMs);
    }

    public UpdateResponse deleteById(List<String> ids, int commitWithinMs, String username) throws SolrServerException, IOException {
        return solrClient.deleteById(ids, commitWithinMs);
    }

    public UpdateResponse deleteByQuery(String collection, String query, String username) throws SolrServerException, IOException {
        return solrClient.deleteByQuery(IndexerUtils.getTableNameWithDomainName(collection), query);
    }

    public UpdateResponse deleteByQueryAuthorizedByUser(String query, String username) throws SolrServerException, IOException {
        return solrClient.deleteByQuery(query);
    }

    public UpdateResponse deleteByQuery(String collection, String query, int commitWithinMs, String username) throws SolrServerException, IOException {
        return solrClient.deleteByQuery(IndexerUtils.getTableNameWithDomainName(collection), query, commitWithinMs);
    }

    public UpdateResponse deleteByQuery(String query, int commitWithinMs, String username) throws SolrServerException, IOException {
        return solrClient.deleteByQuery(query, commitWithinMs);
    }

    public SolrPingResponse ping(String username) throws SolrServerException, IOException {
        return solrClient.ping();
    }

    public QueryResponse query(String collection, SolrParams params, String username) throws SolrServerException, IOException {
        return solrClient.query(IndexerUtils.getTableNameWithDomainName(collection), params);
    }

    public QueryResponse query(SolrParams params, String username) throws SolrServerException, IOException {
        return solrClient.query(params);
    }

    public QueryResponse query(String collection, SolrParams params, SolrRequest.METHOD method, String username) throws SolrServerException, IOException {
        return solrClient.query(IndexerUtils.getTableNameWithDomainName(collection), params, method);
    }

    public QueryResponse query(SolrParams params, SolrRequest.METHOD method, String username) throws SolrServerException, IOException {
        return solrClient.query(params, method);
    }

    public QueryResponse queryAndStreamResponse(String collection, SolrParams params, StreamingResponseCallback callback, String username) throws SolrServerException, IOException {
        return solrClient.queryAndStreamResponse(IndexerUtils.getTableNameWithDomainName(collection), params, callback);
    }

    public QueryResponse queryAndStreamResponse(SolrParams params, StreamingResponseCallback callback, String username) throws SolrServerException, IOException {
        return solrClient.queryAndStreamResponse(params, callback);
    }

    public SolrDocument getById(String collection, String id, String username) throws SolrServerException, IOException {
        return solrClient.getById(IndexerUtils.getTableNameWithDomainName(collection), id);
    }

    public SolrDocument getByIdAuthorizedByUser(String id, String username) throws SolrServerException, IOException {
        return solrClient.getById(id);
    }

    public SolrDocument getById(String collection, String id, SolrParams params, String username) throws SolrServerException, IOException {
        return solrClient.getById(IndexerUtils.getTableNameWithDomainName(collection), id, params);
    }

    public SolrDocument getById(String id, SolrParams params, String username) throws SolrServerException, IOException {
        return solrClient.getById(id, params);
    }

    public SolrDocumentList getById(String collection, Collection<String> ids, String username) throws SolrServerException, IOException {
        return solrClient.getById(IndexerUtils.getTableNameWithDomainName(collection), ids);
    }

    public SolrDocumentList getById(Collection<String> ids, String username) throws SolrServerException, IOException {
        return solrClient.getById(ids);
    }

    public SolrDocumentList getById(String collection, Collection<String> ids, SolrParams params, String username) throws SolrServerException, IOException {
        return solrClient.getById(IndexerUtils.getTableNameWithDomainName(collection), ids, params);
    }

    public SolrDocumentList getById(Collection<String> ids, SolrParams params, String username) throws SolrServerException, IOException {
        return solrClient.getById(ids, params);
    }

    public  NamedList<Object> request(SolrRequest request, String collection, String username) throws SolrServerException, IOException {
        return solrClient.request(request, IndexerUtils.getTableNameWithDomainName(collection));
    }

    public DocumentObjectBinder getBinder() {
        return solrClient.getBinder();
    }

    @Override
    public void close() throws IOException {
        solrClient.close();
    }
}
