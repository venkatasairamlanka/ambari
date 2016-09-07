/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ambari.logsearch.dao;

import java.util.ArrayList;

import org.apache.ambari.logsearch.conf.SolrAuditLogConfig;
import org.apache.ambari.logsearch.conf.SolrKerberosConfig;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.common.util.NamedList;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

public class AuditSolrDaoTest {

  @TestSubject
  private AuditSolrDao dao = new AuditSolrDao();

  @Mock
  private SolrAuditLogConfig configMock;

  @Mock
  private SolrKerberosConfig kerbConfigMock;

  @Before
  public void setUp() {
    EasyMockSupport.injectMocks(this);
  }

  @Test
  public void testAuditSolrDaoPostConstructor() throws Exception {
    SolrClient mockSolrClient = EasyMock.strictMock(SolrClient.class);
    
    NamedList<Object> response = new NamedList<Object>();
    NamedList<Object> header = new NamedList<Object>();
    header.add("status", 0);
    response.add("responseHeader", header);
    response.add("collections", new ArrayList<String>());

    EasyMock.expect(configMock.getSolrUrl()).andReturn(null);
    EasyMock.expect(configMock.getZkConnectString()).andReturn("dummyHost1:2181,dummyHost2:2181");
    EasyMock.expect(configMock.getConfigName()).andReturn("test_audit_logs_config_name");
    EasyMock.expect(configMock.getCollection()).andReturn("test_audit_logs_collection");
    EasyMock.expect(configMock.getSplitInterval()).andReturn("none");
    EasyMock.expect(configMock.getNumberOfShards()).andReturn(123);
    EasyMock.expect(configMock.getReplicationFactor()).andReturn(456);
    EasyMock.expect(configMock.getAliasNameIn()).andReturn("alias");
    EasyMock.expect(configMock.getRangerCollection()).andReturn("ranger_audit");
    EasyMock.expect(kerbConfigMock.isEnabled()).andReturn(false);
    EasyMock.expect(kerbConfigMock.getJaasFile()).andReturn("jaas_file");
    
    Capture<CollectionAdminRequest.Create> captureCreateRequest = EasyMock.newCapture(CaptureType.LAST);
    
    EasyMock.expect(mockSolrClient.request(EasyMock.anyObject(CollectionAdminRequest.List.class), EasyMock.anyString())).andReturn(response);
    
    mockSolrClient.request(EasyMock.capture(captureCreateRequest), EasyMock.anyString());
    EasyMock.expectLastCall().andReturn(response);
    
    EasyMock.replay(mockSolrClient, configMock, kerbConfigMock);

    dao.solrClient = mockSolrClient;
    dao.isZkConnectString = true;
    
    dao.postConstructor();
    EasyMock.verify(mockSolrClient);
    
    CollectionAdminRequest.Create createRequest = captureCreateRequest.getValue();
    Assert.assertEquals(createRequest.getConfigName(), "test_audit_logs_config_name");
    Assert.assertEquals(createRequest.getNumShards().intValue(), 123);
    Assert.assertEquals(createRequest.getReplicationFactor().intValue(), 456);
    Assert.assertEquals(createRequest.getCollectionName(), "test_audit_logs_collection");
  }
}