/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.catalog.rest.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.catalog.rest.assembler.CatalogObjectRevisionResourceAssembler;
import org.ow2.proactive.catalog.rest.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.rest.entity.Bucket;
import org.ow2.proactive.catalog.rest.entity.CatalogObject;
import org.ow2.proactive.catalog.rest.entity.CatalogObjectRevision;
import org.ow2.proactive.catalog.rest.service.exception.BucketNotFoundException;
import org.ow2.proactive.catalog.rest.service.exception.CatalogObjectNotFoundException;
import org.ow2.proactive.catalog.rest.service.exception.RevisionNotFoundException;
import org.ow2.proactive.catalog.rest.service.repository.BucketRepository;
import org.ow2.proactive.catalog.rest.service.repository.CatalogObjectRepository;
import org.ow2.proactive.catalog.rest.service.repository.CatalogObjectRevisionRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;


/**
 * @author ActiveEon Team
 */
public class CatalogObjectRevisionServiceTest {

    @InjectMocks
    private CatalogObjectRevisionService catalogObjectRevisionService;

    @Mock
    private CatalogObjectRevisionRepository catalogObjectRevisionRepository;

    @Mock
    private CatalogObjectRepository catalogObjectRepository;

    @Mock
    private BucketRepository bucketRepository;

    private static final Long DUMMY_ID = 0L;

    private static final Long EXISTING_ID = 1L;

    private Bucket mockedBucket;

    private SortedSet<CatalogObjectRevision> revisions;

    private CatalogObject catalogObject2Rev;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        catalogObjectRevisionService = Mockito.spy(catalogObjectRevisionService);

        Mockito.doReturn(new Link("test"))
               .when(catalogObjectRevisionService)
               .createLink(Matchers.any(Long.class),
                           Matchers.any(Long.class),
                           Matchers.any(CatalogObjectRevision.class));

        Long revCnt = EXISTING_ID;
        mockedBucket = newMockedBucket(EXISTING_ID);
        revisions = new TreeSet<>();
        revisions.add(newWorkflowRevision(mockedBucket.getId(), revCnt++, LocalDateTime.now().minusHours(1)));
        revisions.add(newWorkflowRevision(mockedBucket.getId(), revCnt, LocalDateTime.now()));

        catalogObject2Rev = newMockedCatalogObject(EXISTING_ID, mockedBucket, revisions, 2L, "workflow");
    }

    @Test(expected = BucketNotFoundException.class)
    public void testCreateWorkflowRevisionWithInvalidBucket() throws Exception {
        when(bucketRepository.findOne(Matchers.anyLong())).thenReturn(null);
        catalogObjectRevisionService.createCatalogObjectRevision(DUMMY_ID,
                                                                 "workflow",
                                                                 "name",
                                                                 "commit message",
                                                                 Optional.empty(),
                                                                 "application/xml",
                                                                 ImmutableList.of(),
                                                                 new byte[0]);
    }

    @Test(expected = BucketNotFoundException.class)
    public void testCreateCatalogObjectRevisionWithInvalidBucket() throws Exception {
        when(bucketRepository.findOne(Matchers.anyLong())).thenReturn(null);
        catalogObjectRevisionService.createCatalogObjectRevision(DUMMY_ID,
                                                                 "oject",
                                                                 "name",
                                                                 "commit message",
                                                                 Optional.empty(),
                                                                 "application/xml",
                                                                 ImmutableList.of(),
                                                                 new byte[0]);
    }

    @Test
    public void testCreateWorkflowWithGenericInfosAndVariables1() throws IOException {
        createCatalogObject("WR-NAME-GI-VARS",
                            "WR-PROJ-NAME-GI-VARS",
                            "workflow.xml",
                            Optional.empty(),
                            "application/xml");
        // assertions are done in the called method
    }

    @Test
    public void testCreateWorkflowWithGenericInfosAndVariables2() throws IOException {
        createCatalogObject("WR-NAME-GI-VARS",
                            "WR-PROJ-NAME-GI-VARS",
                            "workflow.xml",
                            Optional.of(EXISTING_ID),
                            "application/xml");
        // assertions are done in the called method
    }

    @Test
    public void testCreateWorkflowWithoutGenericInfosOrVariables1() throws IOException {
        createCatalogObject("WR-NAME",
                            "WR-PROJ-NAME",
                            "workflow-no-generic-information-no-variable.xml",
                            Optional.empty(),
                            "application/xml");
        // assertions are done in the called method
    }

    @Test
    public void testCreateWorkflowWithoutGenericInfosOrVariables2() throws IOException {
        createCatalogObject("WR-NAME",
                            "WR-PROJ-NAME",
                            "workflow-no-generic-information-no-variable.xml",
                            Optional.of(EXISTING_ID),
                            "application/xml");
        // assertions are done in the called method
    }

    @Test
    public void testCreateWorkflowWithLayout() throws IOException {
        createCatalogObject("WR-NAME-GI-VARS",
                            "WR-PROJ-NAME-GI-VARS",
                            "workflow.xml",
                            Optional.empty(),
                            "application/xml");
        // assertions are done in the called method
    }

    @Test(expected = CatalogObjectNotFoundException.class)
    public void testFindWorkflowInvalidId() throws Exception {
        when(catalogObjectRepository.findOne(anyLong())).thenReturn(null);
        catalogObjectRevisionService.findObjectById(DUMMY_ID);
    }

    @Test
    public void testFindCatalogObject() throws Exception {
        when(catalogObjectRepository.findOne(EXISTING_ID)).thenReturn(mock(CatalogObject.class));
        catalogObjectRevisionService.findObjectById(EXISTING_ID);
        verify(catalogObjectRepository, times(1)).findOne(EXISTING_ID);
    }

    @Test
    public void testFindBucketExisting() {
        Bucket bucket = mock(Bucket.class);
        when(bucketRepository.findOne(EXISTING_ID)).thenReturn(bucket);
        assertEquals(catalogObjectRevisionService.findBucket(EXISTING_ID), bucket);
    }

    @Test(expected = BucketNotFoundException.class)
    public void testFindBucketNonExisting() {
        catalogObjectRevisionService.findBucket(DUMMY_ID);
    }

    @Test
    public void testListWorkflowsWithWorkflowId() throws Exception {
        listWorkflows(Optional.of(DUMMY_ID));
        verify(catalogObjectRevisionRepository, times(1)).getRevisions(anyLong(), any(Pageable.class));
    }

    @Test
    public void testListWorkflowsWithoutWorkflowId() throws Exception {
        listWorkflows(Optional.empty());
        verify(catalogObjectRepository, times(1)).getMostRecentRevisions(anyLong(), any(Pageable.class));
    }

    @Test(expected = RevisionNotFoundException.class)
    public void testGetWorkflowWithInvalidRevisionId() throws Exception {
        when(bucketRepository.findOne(anyLong())).thenReturn(mock(Bucket.class));
        when(catalogObjectRepository.findOne(anyLong())).thenReturn(mock(CatalogObject.class));
        when(catalogObjectRepository.getMostRecentCatalogObjectRevision(anyLong(), anyLong())).thenReturn(null);
        catalogObjectRevisionService.getCatalogObject(DUMMY_ID, DUMMY_ID, Optional.empty());
    }

    @Test
    public void testGetWorkflowWithValidRevisionIdNoPayload() throws Exception {
        getCatalogObject(Optional.of(DUMMY_ID));
        verify(catalogObjectRevisionRepository, times(1)).getCatalogObjectRevision(DUMMY_ID, DUMMY_ID, DUMMY_ID);
    }

    private void getCatalogObject(Optional<Long> revisionId) throws IOException {
        getWorkflowFromFunction(revisionId,
                                revision -> catalogObjectRevisionService.getCatalogObject(DUMMY_ID,
                                                                                          DUMMY_ID,
                                                                                          revision));
    }

    private void getCatalogObjectRaw(Optional<Long> revisionId) throws IOException {
        getWorkflowFromFunction(revisionId,
                                revision -> catalogObjectRevisionService.getCatalogObjectRaw(DUMMY_ID,
                                                                                             DUMMY_ID,
                                                                                             revision));
    }

    private void getWorkflowFromFunction(Optional<Long> revisionId,
            Function<Optional<Long>, ResponseEntity<?>> getFunction) throws IOException {

        CatalogObject mockedWf = mock(CatalogObject.class);
        when(mockedWf.getId()).thenReturn(DUMMY_ID);

        CatalogObjectRevision wfRev = new CatalogObjectRevision(DUMMY_ID,
                                                                "workflow",
                                                                LocalDateTime.now(),
                                                                "WR-TEST",
                                                                "commit message",
                                                                DUMMY_ID,
                                                                "application/xml",
                                                                getWorkflowAsByteArray("workflow.xml"));
        wfRev.setCatalogObject(mockedWf);

        when(bucketRepository.findOne(anyLong())).thenReturn(mock(Bucket.class));
        when(catalogObjectRepository.findOne(anyLong())).thenReturn(mock(CatalogObject.class));

        if (revisionId.isPresent()) {
            when(catalogObjectRevisionRepository.getCatalogObjectRevision(anyLong(),
                                                                          anyLong(),
                                                                          anyLong())).thenReturn(wfRev);
        } else {
            when(catalogObjectRepository.getMostRecentCatalogObjectRevision(anyLong(), anyLong())).thenReturn(wfRev);
        }

        getFunction.apply(revisionId);

        verify(bucketRepository, times(1)).findOne(DUMMY_ID);
        verify(catalogObjectRepository, times(1)).findOne(DUMMY_ID);
    }

    @Test
    public void testGetWorkflowWithoutRevisionINoPayload() throws Exception {
        getCatalogObject(Optional.empty());
        verify(catalogObjectRepository, times(1)).getMostRecentCatalogObjectRevision(DUMMY_ID, DUMMY_ID);
    }

    @Test
    public void testGetWorkflowWithValidRevisionIdWithPayload() throws Exception {
        getCatalogObject(Optional.of(DUMMY_ID));
        verify(catalogObjectRevisionRepository, times(1)).getCatalogObjectRevision(DUMMY_ID, DUMMY_ID, DUMMY_ID);
    }

    @Test
    public void testGetWorkflowWithoutValidRevisionIdWithPayload() throws Exception {
        getCatalogObject(Optional.empty());
        verify(catalogObjectRepository, times(1)).getMostRecentCatalogObjectRevision(DUMMY_ID, DUMMY_ID);
    }

    @Test
    public void testGetWorkflowRaw() throws Exception {
        getCatalogObjectRaw(Optional.empty());
        verify(catalogObjectRepository, times(1)).getMostRecentCatalogObjectRevision(DUMMY_ID, DUMMY_ID);
    }

    @Test
    public void testGetWorkflowRawRevision() throws Exception {
        getCatalogObjectRaw(Optional.of(DUMMY_ID));
        verify(catalogObjectRevisionRepository, times(1)).getCatalogObjectRevision(DUMMY_ID, DUMMY_ID, DUMMY_ID);
    }

    @Test
    public void testDeleteWorkflowWith1Revision() throws Exception {
        CatalogObjectRevision wfRev = new CatalogObjectRevision(EXISTING_ID,
                                                                "workflow",
                                                                LocalDateTime.now(),
                                                                "WR-TEST",
                                                                "commit message",
                                                                DUMMY_ID,
                                                                "application/xml",
                                                                getWorkflowAsByteArray("workflow.xml"));
        CatalogObject catalogObject1Rev = newMockedCatalogObject(EXISTING_ID,
                                                                 mockedBucket,
                                                                 new TreeSet<CatalogObjectRevision>() {
                                                                     {
                                                                         add(wfRev);
                                                                     }
                                                                 },
                                                                 EXISTING_ID,
                                                                 "workflow");
        when(catalogObjectRepository.findOne(EXISTING_ID)).thenReturn(catalogObject1Rev);
        when(catalogObjectRepository.getMostRecentCatalogObjectRevision(mockedBucket.getId(),
                                                                        catalogObject1Rev.getId())).thenReturn(wfRev);
        catalogObjectRevisionService.delete(mockedBucket.getId(), EXISTING_ID, Optional.empty());
        verify(catalogObjectRepository, times(1)).getMostRecentCatalogObjectRevision(EXISTING_ID, EXISTING_ID);
        verify(catalogObjectRepository, times(1)).delete(catalogObject1Rev);
    }

    @Test
    public void testDeleteWorkflowWith2RevisionsNoRevisionId() throws Exception {
        when(catalogObjectRepository.findOne(EXISTING_ID)).thenReturn(catalogObject2Rev);
        when(catalogObjectRepository.getMostRecentCatalogObjectRevision(mockedBucket.getId(),
                                                                        catalogObject2Rev.getId())).thenReturn(revisions.first());
        catalogObjectRevisionService.delete(mockedBucket.getId(), EXISTING_ID, Optional.empty());
        verify(catalogObjectRepository, times(1)).getMostRecentCatalogObjectRevision(EXISTING_ID, EXISTING_ID);
        verify(catalogObjectRepository, times(1)).delete(catalogObject2Rev);
    }

    @Test
    public void testDeleteWorkflowWith2RevisionsLastRevision() {
        Long expectedRevisionId = 2L;
        when(catalogObjectRepository.findOne(EXISTING_ID)).thenReturn(catalogObject2Rev);
        when(catalogObjectRepository.getMostRecentCatalogObjectRevision(mockedBucket.getId(),
                                                                        EXISTING_ID)).thenReturn(revisions.first());
        when(catalogObjectRevisionRepository.getCatalogObjectRevision(mockedBucket.getId(),
                                                                      EXISTING_ID,
                                                                      expectedRevisionId)).thenReturn(revisions.first());
        catalogObjectRevisionService.delete(mockedBucket.getId(), EXISTING_ID, Optional.of(expectedRevisionId));
        verify(catalogObjectRevisionRepository, times(1)).delete(revisions.first());
    }

    @Test
    public void testDeleteWorkflowWith2RevisionsPreviousRevision() {
        when(catalogObjectRepository.findOne(EXISTING_ID)).thenReturn(catalogObject2Rev);
        when(catalogObjectRevisionRepository.getCatalogObjectRevision(mockedBucket.getId(),
                                                                      EXISTING_ID,
                                                                      EXISTING_ID)).thenReturn(revisions.last());
        catalogObjectRevisionService.delete(mockedBucket.getId(), EXISTING_ID, Optional.of(EXISTING_ID));
        verify(catalogObjectRevisionRepository, times(1)).getCatalogObjectRevision(mockedBucket.getId(),
                                                                                   catalogObject2Rev.getId(),
                                                                                   EXISTING_ID);
        verify(catalogObjectRevisionRepository, times(1)).delete(revisions.last());
    }

    @Test
    public void testGetWorkflowsRevisions() {
        List<Long> idList = new ArrayList<>();
        idList.add(0L);
        idList.add(2L);
        when(bucketRepository.findOne(mockedBucket.getId())).thenReturn(mockedBucket);
        when(catalogObjectRepository.getMostRecentCatalogObjectRevision(mockedBucket.getId(),
                                                                        0L)).thenReturn(revisions.first());
        when(catalogObjectRepository.getMostRecentCatalogObjectRevision(mockedBucket.getId(),
                                                                        2L)).thenReturn(revisions.last());
        catalogObjectRevisionService.getCatalogObjectsRevisions(mockedBucket.getId(), idList);
        verify(catalogObjectRevisionService, times(1)).findBucket(mockedBucket.getId());
        verify(catalogObjectRepository, times(1)).getMostRecentCatalogObjectRevision(mockedBucket.getId(), 0L);
        verify(catalogObjectRepository, times(1)).getMostRecentCatalogObjectRevision(mockedBucket.getId(), 2L);
    }

    private CatalogObjectRevision newWorkflowRevision(Long bucketId, Long commitId, LocalDateTime date)
            throws Exception {
        return newCatalogObjectRevision("workflow", bucketId, commitId, date);
    }

    private CatalogObjectRevision newCatalogObjectRevision(String kind, Long bucketId, Long commitId,
            LocalDateTime date) throws Exception {
        return new CatalogObjectRevision(commitId,
                                         kind,
                                         date,
                                         "WR-TEST",
                                         "commit message",
                                         bucketId,
                                         "application/xml",
                                         getWorkflowAsByteArray("workflow.xml"));
    }

    private Bucket newMockedBucket(Long bucketId) {
        Bucket mockedBucket = mock(Bucket.class);
        when(mockedBucket.getId()).thenReturn(bucketId);
        return mockedBucket;
    }

    private CatalogObject newMockedCatalogObject(Long id, Bucket bucket, SortedSet<CatalogObjectRevision> revisions,
            Long lastCommitId, String kind) {
        CatalogObject catalogObject = mock(CatalogObject.class);
        when(catalogObject.getId()).thenReturn(id);
        when(catalogObject.getBucket()).thenReturn(bucket);
        when(catalogObject.getRevisions()).thenReturn(revisions);
        when(catalogObject.getLastCommitId()).thenReturn(lastCommitId);
        for (CatalogObjectRevision catalogObjectRevision : revisions) {
            catalogObjectRevision.setCatalogObject(catalogObject);
        }
        return catalogObject;
    }

    private void listWorkflows(Optional<Long> wId) {
        when(bucketRepository.findOne(anyLong())).thenReturn(mock(Bucket.class));
        when(catalogObjectRepository.findOne(anyLong())).thenReturn(mock(CatalogObject.class));
        PagedResourcesAssembler mockedAssembler = mock(PagedResourcesAssembler.class);
        when(mockedAssembler.toResource(any(PageImpl.class),
                                        any(CatalogObjectRevisionResourceAssembler.class))).thenReturn(null);

        if (wId.isPresent()) {
            when(catalogObjectRevisionRepository.getRevisions(anyLong(),
                                                              any(Pageable.class))).thenReturn(mock(PageImpl.class));
            catalogObjectRevisionService.listCatalogObjectRevisions(DUMMY_ID, wId.get(), null, mockedAssembler);
        } else {
            when(catalogObjectRepository.getMostRecentRevisions(anyLong(),
                                                                any(Pageable.class))).thenReturn(mock(PageImpl.class));
            catalogObjectRevisionService.listCatalogObjects(DUMMY_ID, Optional.empty(), null, mockedAssembler);
        }

    }

    private void createCatalogObject(String name, String kind, String fileName, Optional<Long> wId, String layout)
            throws IOException {
        String layoutStr = "application/xml";
        when(bucketRepository.findOne(anyLong())).thenReturn(mock(Bucket.class));
        when(catalogObjectRevisionRepository.save(any(CatalogObjectRevision.class))).thenReturn(new CatalogObjectRevision(EXISTING_ID,
                                                                                                                          kind,
                                                                                                                          LocalDateTime.now(),
                                                                                                                          name,
                                                                                                                          "commit message",
                                                                                                                          EXISTING_ID,
                                                                                                                          layoutStr,
                                                                                                                          Lists.newArrayList(),
                                                                                                                          getWorkflowAsByteArray(fileName)));

        if (wId.isPresent()) {
            when(catalogObjectRepository.findOne(anyLong())).thenReturn(new CatalogObject(mock(Bucket.class),
                                                                                          Lists.newArrayList()));
        }

        CatalogObjectMetadata actualWFMetadata = catalogObjectRevisionService.createCatalogObjectRevision(DUMMY_ID,
                                                                                                          kind,
                                                                                                          "name",
                                                                                                          "commit message",
                                                                                                          wId,
                                                                                                          layoutStr,
                                                                                                          getWorkflowAsByteArray(fileName)

        );

        verify(catalogObjectRevisionRepository, times(1)).save(any(CatalogObjectRevision.class));

        assertEquals(name, actualWFMetadata.name);
        assertEquals(EXISTING_ID, actualWFMetadata.bucketId);
        assertEquals(EXISTING_ID, actualWFMetadata.commitId);
        assertEquals(kind, actualWFMetadata.kind);
        assertEquals(layout, actualWFMetadata.contentType);
    }

    private static byte[] getWorkflowAsByteArray(String filename) throws IOException {
        return ByteStreams.toByteArray(new FileInputStream(getWorkflowFile(filename)));
    }

    private static File getWorkflowFile(String filename) {
        return new File(CatalogObjectRevisionServiceTest.class.getResource("/workflows/" + filename).getFile());
    }
}
