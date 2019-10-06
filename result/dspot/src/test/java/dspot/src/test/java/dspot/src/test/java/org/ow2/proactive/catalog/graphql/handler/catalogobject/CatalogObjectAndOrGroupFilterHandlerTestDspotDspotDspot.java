package org.ow2.proactive.catalog.graphql.handler.catalogobject;


@org.junit.runner.RunWith(org.mockito.runners.MockitoJUnitRunner.class)
public class CatalogObjectAndOrGroupFilterHandlerTestDspotDspotDspot {
    @org.mockito.InjectMocks
    private org.ow2.proactive.catalog.graphql.handler.catalogobject.CatalogObjectAndOrGroupFilterHandler andFilterHandler;

    @org.mockito.Mock
    private org.ow2.proactive.catalog.graphql.handler.catalogobject.CatalogObjectBucketNameFilterHandler bucketIdHandler;

    @org.mockito.Mock
    private org.ow2.proactive.catalog.graphql.handler.catalogobject.CatalogObjectKindFilterHandler kindHandler;

    @org.mockito.Mock
    private org.ow2.proactive.catalog.graphql.handler.catalogobject.CatalogObjectNameFilterHandler nameHandler;

    @org.mockito.Mock
    private org.ow2.proactive.catalog.graphql.handler.catalogobject.CatalogObjectMetadataFilterHandler metadataHandler;

    private org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs whereArgs;

    @org.junit.Before
    public void setUp() throws java.lang.Exception {
        org.mockito.Mockito.when(bucketIdHandler.handle(org.mockito.Matchers.any(org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs.class))).thenCallRealMethod();
        org.mockito.Mockito.when(nameHandler.handle(org.mockito.Matchers.any(org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs.class))).thenCallRealMethod();
        org.mockito.Mockito.when(kindHandler.handle(org.mockito.Matchers.any(org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs.class))).thenReturn(java.util.Optional.empty());
        org.mockito.Mockito.when(metadataHandler.handle(org.mockito.Matchers.any(org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs.class))).thenReturn(java.util.Optional.empty());
        org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs bucketid = org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs.builder().bucketNameArg(org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectBucketNameWhereArgs.builder().eq("bucket1").build()).build();
        org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs name = org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs.builder().nameArg(org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectNameWhereArgs.builder().eq("name").build()).build();
        org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs bucketid2 = org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs.builder().bucketNameArg(org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectBucketNameWhereArgs.builder().eq("bucket2").build()).build();
        org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs name2 = org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs.builder().nameArg(org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectNameWhereArgs.builder().eq("name2").build()).build();
        org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs name3 = org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs.builder().nameArg(org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectNameWhereArgs.builder().eq("name3").build()).build();
        org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs orwhere3 = org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs.builder().orArg(com.google.common.collect.ImmutableList.of(name2, name3)).build();
        org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs andwhere1 = org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs.builder().andArg(com.google.common.collect.ImmutableList.of(bucketid, name)).build();
        org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs andwhere2 = org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs.builder().andArg(com.google.common.collect.ImmutableList.of(bucketid2, orwhere3)).build();
        whereArgs = org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs.builder().orArg(com.google.common.collect.ImmutableList.of(andwhere1, andwhere2)).build();
        andFilterHandler.init();
    }

    @org.junit.Test(timeout = 10000)
    public void testHandleMethod_literalMutationNumber14_failAssert0() throws java.lang.Exception {
        try {
            java.util.Optional<org.springframework.data.jpa.domain.Specification<org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity>> specification = andFilterHandler.handle(whereArgs);
            org.ow2.proactive.catalog.repository.specification.catalogobject.OrSpecification orSpecification = ((org.ow2.proactive.catalog.repository.specification.catalogobject.OrSpecification) (specification.get()));
            org.ow2.proactive.catalog.repository.specification.catalogobject.AndSpecification leftAnd = ((org.ow2.proactive.catalog.repository.specification.catalogobject.AndSpecification) (orSpecification.getFieldSpecifications().get(-1)));
            org.ow2.proactive.catalog.repository.specification.catalogobject.AndSpecification rightAnd = ((org.ow2.proactive.catalog.repository.specification.catalogobject.AndSpecification) (orSpecification.getFieldSpecifications().get(1)));
            org.ow2.proactive.catalog.repository.specification.catalogobject.OrSpecification rightAndChildOr = ((org.ow2.proactive.catalog.repository.specification.catalogobject.OrSpecification) (rightAnd.getFieldSpecifications().get(1)));
            org.junit.Assert.fail("testHandleMethod_literalMutationNumber14 should have thrown ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            org.junit.Assert.assertEquals("-1", expected.getMessage());
        }
    }
}

