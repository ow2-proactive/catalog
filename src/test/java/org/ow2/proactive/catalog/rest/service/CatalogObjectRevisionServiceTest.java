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
import static org.mockito.Matchers.anyString;
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
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectRevisionRepository;
import org.ow2.proactive.catalog.repository.entity.BucketEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.service.CatalogObjectRevisionService;
import org.ow2.proactive.catalog.service.CatalogObjectService;
import org.ow2.proactive.catalog.service.exception.BucketNotFoundException;
import org.ow2.proactive.catalog.service.exception.RevisionNotFoundException;
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
    private CatalogObjectService catalogObjectService;

    @Mock
    private BucketRepository bucketRepository;

    private static final Long DUMMY_ID = 0L;

    private static final Long EXISTING_ID = 1L;

    private BucketEntity mockedBucket;

    private SortedSet<CatalogObjectRevisionEntity> revisions;

    private CatalogObjectEntity catalogObject2Rev;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        catalogObjectRevisionService = Mockito.spy(catalogObjectRevisionService);

        Mockito.doReturn(new Link("test"))
               .when(catalogObjectRevisionService)
               .createLink(Matchers.any(Long.class),
                           Matchers.any(Long.class),
                           Matchers.any(CatalogObjectRevisionEntity.class));

        Long revCnt = EXISTING_ID;
        mockedBucket = newMockedBucket(EXISTING_ID);
        revisions = new TreeSet<>();
        revisions.add(newWorkflowRevision(mockedBucket.getId(), revCnt++, LocalDateTime.now().minusHours(1)));
        revisions.add(newWorkflowRevision(mockedBucket.getId(), revCnt, LocalDateTime.now()));

        catalogObject2Rev = newMockedCatalogObject(EXISTING_ID, mockedBucket, revisions, 2L, "workflow");
    }

    @Test(expected = BucketNotFoundException.class)
    public void testCreateWorkflowRevisionWithInvalidBucket() throws Exception {
        when(bucketRepository.findOne(Matchers.anyLong())).thenThrow(BucketNotFoundException.class);
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
        when(bucketRepository.findOne(Matchers.anyLong())).thenThrow(BucketNotFoundException.class);
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

    @Test
    public void testListCatalogObjects() throws Exception {
        when(catalogObjectService.getMostRecentRevisions(anyLong(),
                                                         Optional.of(anyString()))).thenReturn(new ArrayList<>());
        when(bucketRepository.findOne(anyLong())).thenReturn(mock(BucketEntity.class));
        when(catalogObjectService.findObjectById(anyLong())).thenReturn(mock(CatalogObjectEntity.class));
        catalogObjectRevisionService.listCatalogObjects(DUMMY_ID, Optional.empty());
        verify(catalogObjectService, times(1)).getMostRecentRevisions(anyLong(), any(Optional.class));
    }

    @Test
    public void testListCatalogObjectRevisions() throws Exception {
        when(catalogObjectRevisionRepository.getRevisions(anyLong())).thenReturn(new ArrayList<>());
        when(bucketRepository.findOne(anyLong())).thenReturn(mock(BucketEntity.class));
        when(catalogObjectService.findObjectById(anyLong())).thenReturn(mock(CatalogObjectEntity.class));
        catalogObjectRevisionService.listCatalogObjectRevisions(DUMMY_ID, DUMMY_ID);
        verify(catalogObjectRevisionRepository, times(1)).getRevisions(anyLong());
    }

    @Test(expected = RevisionNotFoundException.class)
    public void testGetWorkflowWithInvalidRevisionId() throws Exception {
        when(bucketRepository.findOne(anyLong())).thenReturn(mock(BucketEntity.class));
        when(catalogObjectService.findObjectById(anyLong())).thenReturn(mock(CatalogObjectEntity.class));
        when(catalogObjectService.getMostRecentCatalogObjectRevision(anyLong(), anyLong())).thenReturn(null);
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

        CatalogObjectEntity mockedWf = mock(CatalogObjectEntity.class);
        when(mockedWf.getId()).thenReturn(DUMMY_ID);

        CatalogObjectRevisionEntity wfRev = new CatalogObjectRevisionEntity(DUMMY_ID,
                                                                            "workflow",
                                                                            LocalDateTime.now(),
                                                                            "WR-TEST",
                                                                            "commit message",
                                                                            DUMMY_ID,
                                                                            "application/xml",
                                                                            getWorkflowAsByteArray("workflow.xml"));
        wfRev.setCatalogObject(mockedWf);

        when(bucketRepository.findOne(anyLong())).thenReturn(mock(BucketEntity.class));
        when(catalogObjectService.findObjectById(anyLong())).thenReturn(mock(CatalogObjectEntity.class));

        if (revisionId.isPresent()) {
            when(catalogObjectRevisionRepository.getCatalogObjectRevision(anyLong(),
                                                                          anyLong(),
                                                                          anyLong())).thenReturn(wfRev);
        } else {
            when(catalogObjectService.getMostRecentCatalogObjectRevision(anyLong(), anyLong())).thenReturn(wfRev);
        }

        getFunction.apply(revisionId);

        verify(bucketRepository, times(1)).findOne(DUMMY_ID);
        verify(catalogObjectService, times(1)).findObjectById(DUMMY_ID);
    }

    @Test
    public void testGetWorkflowWithoutRevisionINoPayload() throws Exception {
        getCatalogObject(Optional.empty());
        verify(catalogObjectService, times(1)).getMostRecentCatalogObjectRevision(DUMMY_ID, DUMMY_ID);
    }

    @Test
    public void testGetWorkflowWithValidRevisionIdWithPayload() throws Exception {
        getCatalogObject(Optional.of(DUMMY_ID));
        verify(catalogObjectRevisionRepository, times(1)).getCatalogObjectRevision(DUMMY_ID, DUMMY_ID, DUMMY_ID);
    }

    @Test
    public void testGetWorkflowWithoutValidRevisionIdWithPayload() throws Exception {
        getCatalogObject(Optional.empty());
        verify(catalogObjectService, times(1)).getMostRecentCatalogObjectRevision(DUMMY_ID, DUMMY_ID);
    }

    @Test
    public void testGetWorkflowRaw() throws Exception {
        getCatalogObjectRaw(Optional.empty());
        verify(catalogObjectService, times(1)).getMostRecentCatalogObjectRevision(DUMMY_ID, DUMMY_ID);
    }

    @Test
    public void testGetWorkflowRawRevision() throws Exception {
        getCatalogObjectRaw(Optional.of(DUMMY_ID));
        verify(catalogObjectRevisionRepository, times(1)).getCatalogObjectRevision(DUMMY_ID, DUMMY_ID, DUMMY_ID);
    }

    @Test
    public void testDeleteWorkflowWith1Revision() throws Exception {
        CatalogObjectRevisionEntity wfRev = new CatalogObjectRevisionEntity(EXISTING_ID,
                                                                            "workflow",
                                                                            LocalDateTime.now(),
                                                                            "WR-TEST",
                                                                            "commit message",
                                                                            DUMMY_ID,
                                                                            "application/xml",
                                                                            getWorkflowAsByteArray("workflow.xml"));
        CatalogObjectEntity catalogObject1Rev = newMockedCatalogObject(EXISTING_ID,
                                                                       mockedBucket,
                                                                       new TreeSet<CatalogObjectRevisionEntity>() {
                                                                           {
                                                                               add(wfRev);
                                                                           }
                                                                       },
                                                                       EXISTING_ID,
                                                                       "workflow");
        when(catalogObjectService.findObjectById(EXISTING_ID)).thenReturn(catalogObject1Rev);
        when(catalogObjectService.getMostRecentCatalogObjectRevision(mockedBucket.getId(),
                                                                     catalogObject1Rev.getId())).thenReturn(wfRev);
        catalogObjectRevisionService.delete(mockedBucket.getId(), EXISTING_ID, Optional.empty());
        verify(catalogObjectService, times(1)).getMostRecentCatalogObjectRevision(EXISTING_ID, EXISTING_ID);
        verify(catalogObjectService, times(1)).delete(catalogObject1Rev);
    }

    @Test
    public void testDeleteWorkflowWith2RevisionsNoRevisionId() throws Exception {
        when(catalogObjectService.findObjectById(EXISTING_ID)).thenReturn(catalogObject2Rev);
        when(catalogObjectService.getMostRecentCatalogObjectRevision(mockedBucket.getId(),
                                                                     catalogObject2Rev.getId())).thenReturn(revisions.first());
        catalogObjectRevisionService.delete(mockedBucket.getId(), EXISTING_ID, Optional.empty());
        verify(catalogObjectService, times(1)).getMostRecentCatalogObjectRevision(EXISTING_ID, EXISTING_ID);
        verify(catalogObjectService, times(1)).delete(catalogObject2Rev);
    }

    @Test
    public void testDeleteWorkflowWith2RevisionsLastRevision() {
        Long expectedRevisionId = 2L;
        when(catalogObjectService.findObjectById(EXISTING_ID)).thenReturn(catalogObject2Rev);
        when(catalogObjectService.getMostRecentCatalogObjectRevision(mockedBucket.getId(),
                                                                     EXISTING_ID)).thenReturn(revisions.first());
        when(catalogObjectRevisionRepository.getCatalogObjectRevision(mockedBucket.getId(),
                                                                      EXISTING_ID,
                                                                      expectedRevisionId)).thenReturn(revisions.first());
        catalogObjectRevisionService.delete(mockedBucket.getId(), EXISTING_ID, Optional.of(expectedRevisionId));
        verify(catalogObjectRevisionRepository, times(1)).delete(revisions.first());
    }

    @Test
    public void testDeleteWorkflowWith2RevisionsPreviousRevision() {
        when(catalogObjectService.findObjectById(EXISTING_ID)).thenReturn(catalogObject2Rev);
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
        when(catalogObjectService.getMostRecentCatalogObjectRevision(mockedBucket.getId(),
                                                                     0L)).thenReturn(revisions.first());
        when(catalogObjectService.getMostRecentCatalogObjectRevision(mockedBucket.getId(),
                                                                     2L)).thenReturn(revisions.last());
        catalogObjectRevisionService.getCatalogObjectsRevisions(mockedBucket.getId(), idList);
        verify(bucketRepository, times(1)).findOne(mockedBucket.getId());
        verify(catalogObjectService, times(1)).getMostRecentCatalogObjectRevision(mockedBucket.getId(), 0L);
        verify(catalogObjectService, times(1)).getMostRecentCatalogObjectRevision(mockedBucket.getId(), 2L);
    }

    private CatalogObjectRevisionEntity newWorkflowRevision(Long bucketId, Long commitId, LocalDateTime date)
            throws Exception {
        return newCatalogObjectRevision("workflow", bucketId, commitId, date);
    }

    private CatalogObjectRevisionEntity newCatalogObjectRevision(String kind, Long bucketId, Long commitId,
            LocalDateTime date) throws Exception {
        return new CatalogObjectRevisionEntity(commitId,
                                               kind,
                                               date,
                                               "WR-TEST",
                                               "commit message",
                                               bucketId,
                                               "application/xml",
                                               getWorkflowAsByteArray("workflow.xml"));
    }

    private BucketEntity newMockedBucket(Long bucketId) {
        BucketEntity mockedBucket = mock(BucketEntity.class);
        when(mockedBucket.getId()).thenReturn(bucketId);
        return mockedBucket;
    }

    private CatalogObjectEntity newMockedCatalogObject(Long id, BucketEntity bucket,
            SortedSet<CatalogObjectRevisionEntity> revisions, Long lastCommitId, String kind) {
        CatalogObjectEntity catalogObject = mock(CatalogObjectEntity.class);
        when(catalogObject.getId()).thenReturn(id);
        when(catalogObject.getBucket()).thenReturn(bucket);
        when(catalogObject.getRevisions()).thenReturn(revisions);
        when(catalogObject.getLastCommitId()).thenReturn(lastCommitId);
        for (CatalogObjectRevisionEntity catalogObjectRevision : revisions) {
            catalogObjectRevision.setCatalogObject(catalogObject);
        }
        return catalogObject;
    }

    private void createCatalogObject(String name, String kind, String fileName, Optional<Long> wId, String layout)
            throws IOException {
        String layoutStr = "application/xml";
        when(bucketRepository.findOne(anyLong())).thenReturn(mock(BucketEntity.class));
        when(catalogObjectRevisionRepository.save(any(CatalogObjectRevisionEntity.class))).thenReturn(new CatalogObjectRevisionEntity(EXISTING_ID,
                                                                                                                                      kind,
                                                                                                                                      LocalDateTime.now(),
                                                                                                                                      name,
                                                                                                                                      "commit message",
                                                                                                                                      EXISTING_ID,
                                                                                                                                      layoutStr,
                                                                                                                                      Lists.newArrayList(),
                                                                                                                                      getWorkflowAsByteArray(fileName)));

        if (wId.isPresent()) {
            when(catalogObjectService.findObjectById(anyLong())).thenReturn(new CatalogObjectEntity(mock(BucketEntity.class),
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

        verify(catalogObjectRevisionRepository, times(1)).save(any(CatalogObjectRevisionEntity.class));

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
